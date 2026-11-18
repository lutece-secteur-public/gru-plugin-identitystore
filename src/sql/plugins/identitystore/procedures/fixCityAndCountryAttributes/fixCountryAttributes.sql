----------------------------------------------------------
-- Correction des codes pays de naissance (birthcountry_code) manquants
-- à partir du libellé (birthcountry) et de la date de naissance (birthdate),
-- via le référentiel geocodes_country.
--
-- Usage (PostgreSQL >= 11, en autocommit : la procédure fait des COMMIT par batch) :
--
--   -- 1. Synthèse globale : nombres d'identités concernées + échantillon de customer_id
--   SELECT * FROM COUNT_TOTAL_MISSING_COUNTRY_CODES(<id birthcountry_code>, <id birthcountry>, <id birthdate>, 20);
--
--   -- 2. Détail par libellé : nombre d'identités + échantillon de customer_id
--   SELECT * FROM COUNT_MISSING_COUNTRY_CODES(<id birthcountry_code>, <id birthcountry>, <id birthdate>, 5);
--
--   -- 3. Correction par batch (+ ligne d'historique par code ajouté)
--   CALL UPDATE_COUNTRY_CODE_BY_LIBELLE(<id birthcountry_code>, <id birthcountry>, <id birthdate>, 1000);
--
--   --    Inclut le cas particulier : libellé FRANCE sans date de naissance -> code 99100
--   --    (désactivable : CALL UPDATE_COUNTRY_CODE_BY_LIBELLE(france_code_without_birthdate => NULL);)
--
-- Voir en fin de fichier la correction inverse : remettre le bon libellé
-- à partir du code existant (UPDATE_COUNTRY_LABEL_BY_CODE).
--
-- Les id_attribute se trouvent avec :
--   SELECT id_attribute, key_name FROM identitystore_ref_attribute
--   WHERE key_name IN ('birthcountry_code', 'birthcountry', 'birthdate');
--
-- Une identité est concernée si :
--   - elle n'est ni supprimée ni fusionnée,
--   - elle a un libellé de pays de naissance renseigné,
--   - son code pays est vide, NULL ou absent (pas de ligne d'attribut).
-- Elle est corrigeable si le libellé correspond à exactement un code geocodes
-- non déprécié (deprecated = 0) valide à sa date de naissance.
----------------------------------------------------------

-- Anciennes signatures (le type de retour et les paramètres ont changé)
DROP FUNCTION IF EXISTS COUNT_MISSING_COUNTRY_CODES(INTEGER, INTEGER);
DROP PROCEDURE IF EXISTS UPDATE_COUNTRY_CODE_BY_LIBELLE(INTEGER, INTEGER, INTEGER);

----------------------------------------------------------
-- Conversion de date sans erreur : NULL si la valeur est invalide
----------------------------------------------------------
CREATE
    OR REPLACE FUNCTION SAFE_TO_DATE(date_value VARCHAR, date_format VARCHAR DEFAULT 'DD/MM/YYYY')
    RETURNS DATE
AS
$$
BEGIN
    IF date_value IS NULL OR date_value !~ '^\d{2}/\d{2}/\d{4}$' THEN
        RETURN NULL;
    END IF;
    RETURN TO_DATE(date_value, date_format);
EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END
$$
    LANGUAGE plpgsql
    IMMUTABLE;

----------------------------------------------------------
-- Normalisation d'un libellé de pays pour la comparaison avec geocodes_country :
-- sans accents, en majuscules, et sans aucun caractère autre que lettre ou chiffre
-- (tirets, apostrophes, espaces, points... ignorés).
--   'Côte-d''Ivoire' / 'COTE D IVOIRE' / 'cote d’ivoire' -> 'COTEDIVOIRE'
-- Indépendant de la locale de la base.
----------------------------------------------------------
CREATE
    OR REPLACE FUNCTION NORMALIZE_COUNTRY_LABEL(val TEXT)
    RETURNS TEXT
AS
$$
SELECT REGEXP_REPLACE(
               UPPER(REPLACE(REPLACE(REPLACE(REPLACE(
                   TRANSLATE(val,
                             'àáâãäåçèéêëìíîïñòóôõöøùúûüýÿÀÁÂÃÄÅÇÈÉÊËÌÍÎÏÑÒÓÔÕÖØÙÚÛÜÝŸ',
                             'aaaaaaceeeeiiiinoooooouuuuyyAAAAAACEEEEIIIINOOOOOOUUUUYY'),
                   'æ', 'ae'), 'Æ', 'AE'), 'œ', 'oe'), 'Œ', 'OE')),
               '[^A-Z0-9]', '', 'g')
$$
    LANGUAGE sql
    IMMUTABLE
    PARALLEL SAFE;

----------------------------------------------------------
-- Liste des identités ayant un libellé de pays de naissance mais pas de code,
-- avec le code geocodes résolu (si unique) et le motif en cas d'échec.
--   resolution_status : RESOLVABLE | NO_BIRTHDATE | UNKNOWN_LABEL | AMBIGUOUS
--   code_row_exists   : TRUE si la ligne d'attribut code existe (vide) -> UPDATE,
--                       FALSE si elle est absente -> INSERT
----------------------------------------------------------
DROP FUNCTION IF EXISTS GET_MISSING_COUNTRY_CODES(INTEGER, INTEGER, INTEGER);
CREATE
    OR REPLACE FUNCTION GET_MISSING_COUNTRY_CODES(attribute_id_birthcountry_code INTEGER default 57,
                                                  attribute_id_birthcountry INTEGER default 7,
                                                  attribute_id_birthdate INTEGER default 5)
    RETURNS TABLE
            (
                id_identity       INTEGER,
                customer_id       VARCHAR,
                birthcountry      VARCHAR,
                birthdate         DATE,
                country_code      VARCHAR,
                resolution_status VARCHAR,
                code_row_exists   BOOLEAN
            )
AS
$$
BEGIN
    RETURN QUERY
        -- Référentiel non déprécié, libellés normalisés une seule fois
        WITH geo_norm AS MATERIALIZED (SELECT g.code,
                                              NORMALIZE_COUNTRY_LABEL(g.value) AS norm_value,
                                              g.date_validity_start,
                                              g.date_validity_end
                                       FROM geocodes_country g
                                       WHERE COALESCE(g.deprecated, 0) = 0),
             -- MATERIALIZED : libellé normalisé et date calculés une seule fois par identité
             -- (sinon le planificateur les recalcule pour chaque ligne du référentiel dans le LATERAL)
             candidates AS MATERIALIZED (SELECT attrcountry.id_identity,
                                   idt.customer_id,
                                   attrcountry.attribute_value                  AS birthcountry,
                                   NORMALIZE_COUNTRY_LABEL(attrcountry.attribute_value) AS norm_birthcountry,
                                   SAFE_TO_DATE(attrdate.attribute_value)       AS birthdate,
                                   (attrcountrycode.id_identity IS NOT NULL)    AS code_row_exists
                            FROM identitystore_identity_attribute attrcountry
                                     JOIN identitystore_identity idt
                                          ON idt.id_identity = attrcountry.id_identity
                                     LEFT JOIN identitystore_identity_attribute attrcountrycode
                                               ON attrcountrycode.id_identity = attrcountry.id_identity
                                                   AND attrcountrycode.id_attribute = attribute_id_birthcountry_code
                                     LEFT JOIN identitystore_identity_attribute attrdate
                                               ON attrdate.id_identity = attrcountry.id_identity
                                                   AND attrdate.id_attribute = attribute_id_birthdate
                            WHERE attrcountry.id_attribute = attribute_id_birthcountry
                              AND attrcountry.attribute_value IS NOT NULL
                              AND attrcountry.attribute_value <> ''
                              AND COALESCE(idt.is_deleted, 0) = 0
                              AND COALESCE(idt.is_merged, 0) = 0
                              AND (attrcountrycode.attribute_value IS NULL OR attrcountrycode.attribute_value = ''))
        SELECT c.id_identity,
               c.customer_id::VARCHAR,
               c.birthcountry::VARCHAR,
               c.birthdate,
               (CASE WHEN geo.nb_codes = 1 THEN geo.code END)::VARCHAR,
               (CASE
                    WHEN c.birthdate IS NULL THEN 'NO_BIRTHDATE'
                    WHEN geo.nb_codes = 0 THEN 'UNKNOWN_LABEL'
                    WHEN geo.nb_codes > 1 THEN 'AMBIGUOUS'
                    ELSE 'RESOLVABLE'
                   END)::VARCHAR,
               c.code_row_exists
        FROM candidates c
                 LEFT JOIN LATERAL (SELECT COUNT(DISTINCT g.code) AS nb_codes, MIN(g.code) AS code
                                    FROM geo_norm g
                                    WHERE g.norm_value = c.norm_birthcountry
                                      AND g.date_validity_start <= c.birthdate
                                      AND g.date_validity_end >= c.birthdate) geo ON TRUE;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Synthèse globale : nombre total d'identités concernées, répartition par
-- statut de résolution, et un échantillon de customer_id (pas la liste complète).
----------------------------------------------------------
DROP FUNCTION IF EXISTS COUNT_TOTAL_MISSING_COUNTRY_CODES(INTEGER, INTEGER, INTEGER, INTEGER);
CREATE
    OR REPLACE FUNCTION COUNT_TOTAL_MISSING_COUNTRY_CODES(attribute_id_birthcountry_code INTEGER default 57,
                                                          attribute_id_birthcountry INTEGER default 7,
                                                          attribute_id_birthdate INTEGER default 5,
                                                          sample_size INTEGER DEFAULT 20)
    RETURNS TABLE
            (
                total_identities   BIGINT,
                resolvable         BIGINT,
                to_update          BIGINT,
                to_insert          BIGINT,
                no_birthdate       BIGINT,
                unknown_label      BIGINT,
                ambiguous          BIGINT,
                sample_customer_ids TEXT
            )
AS
$$
BEGIN
    RETURN QUERY
        WITH missing AS (SELECT *
                         FROM GET_MISSING_COUNTRY_CODES(attribute_id_birthcountry_code,
                                                        attribute_id_birthcountry,
                                                        attribute_id_birthdate))
        SELECT COUNT(*),
               COUNT(*) FILTER (WHERE m.resolution_status = 'RESOLVABLE'),
               COUNT(*) FILTER (WHERE m.resolution_status = 'RESOLVABLE' AND m.code_row_exists),
               COUNT(*) FILTER (WHERE m.resolution_status = 'RESOLVABLE' AND NOT m.code_row_exists),
               COUNT(*) FILTER (WHERE m.resolution_status = 'NO_BIRTHDATE'),
               COUNT(*) FILTER (WHERE m.resolution_status = 'UNKNOWN_LABEL'),
               COUNT(*) FILTER (WHERE m.resolution_status = 'AMBIGUOUS'),
               (SELECT STRING_AGG(s.customer_id, ',' ORDER BY s.id_identity)
                FROM (SELECT ms.id_identity, ms.customer_id
                      FROM missing ms
                      ORDER BY ms.id_identity
                      LIMIT sample_size) s)
        FROM missing m;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Détail par libellé de pays : nombre d'identités concernées, nombre
-- corrigeables, code(s) résolu(s) et un échantillon de customer_id.
----------------------------------------------------------
DROP FUNCTION IF EXISTS COUNT_MISSING_COUNTRY_CODES(INTEGER, INTEGER, INTEGER, INTEGER);
CREATE
    OR REPLACE FUNCTION COUNT_MISSING_COUNTRY_CODES(attribute_id_birthcountry_code INTEGER default 57,
                                                    attribute_id_birthcountry INTEGER default 7,
                                                    attribute_id_birthdate INTEGER default 5,
                                                    sample_size INTEGER DEFAULT 5)
    RETURNS TABLE
            (
                value             VARCHAR,
                country_codes     TEXT,
                count             BIGINT,
                count_resolvable  BIGINT,
                sample_customer_ids TEXT
            )
AS
$$
BEGIN
    RETURN QUERY
        SELECT m.birthcountry,
               STRING_AGG(DISTINCT m.country_code, ','),
               COUNT(*),
               COUNT(*) FILTER (WHERE m.resolution_status = 'RESOLVABLE'),
               ARRAY_TO_STRING((ARRAY_AGG(m.customer_id ORDER BY m.id_identity))[1:sample_size], ',')
        FROM GET_MISSING_COUNTRY_CODES(attribute_id_birthcountry_code,
                                       attribute_id_birthcountry,
                                       attribute_id_birthdate) m
        GROUP BY m.birthcountry
        ORDER BY COUNT(*) DESC;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Renseigne le birthcountry_code à partir du libellé et de la date de naissance,
-- par batch de batch_size identités (un COMMIT par batch).
--   - code vide/NULL existant  -> UPDATE + historique change_type=1 (UPDATE), status 'updated'
--   - ligne de code absente    -> INSERT + historique change_type=0 (CREATE), status 'created'
--     Si le libellé (birthcountry) est certifié, une nouvelle ligne de certification est créée
--     pour le code, copie de celle du libellé (certifier_code, certificate_date,
--     expiration_date) ; sinon id_certification = 0.
-- Seules les identités dont le libellé donne exactement un code geocodes valide
-- à la date de naissance sont traitées.
-- touch_identity : met à jour identitystore_identity.last_update_date (pour la
-- réindexation / synchro des identités modifiées).
-- france_code_without_birthdate : (défaut '99100') traite EN PLUS le cas particulier des
--   identités au libellé FRANCE sans date de naissance (ou date invalide), avec ce code ;
--   NULL pour ne traiter que les codes résolus par libellé + date de naissance.
--
-- À appeler par CALL hors transaction explicite (autocommit), sinon les COMMIT échouent.
----------------------------------------------------------
DROP PROCEDURE IF EXISTS UPDATE_COUNTRY_CODE_BY_LIBELLE(INTEGER, INTEGER, INTEGER, INTEGER, VARCHAR, VARCHAR, BOOLEAN);
CREATE
    OR REPLACE PROCEDURE UPDATE_COUNTRY_CODE_BY_LIBELLE(attribute_id_birthcountry_code INTEGER default 57,
                                                        attribute_id_birthcountry INTEGER default 7,
                                                        attribute_id_birthdate INTEGER default 5,
                                                        batch_size INTEGER DEFAULT 1000,
                                                        history_author_name VARCHAR DEFAULT 'script_insert_missing_country_codes',
                                                        history_client_code VARCHAR DEFAULT 'direct_in_DB',
                                                        touch_identity BOOLEAN DEFAULT FALSE,
                                                        france_code_without_birthdate VARCHAR DEFAULT '99100')
AS
$$
DECLARE
    v_attribute_key VARCHAR;
    v_total         BIGINT;
    v_last_id       INTEGER := 0;
    v_batch_last_id INTEGER;
    v_nb_updated    BIGINT;
    v_nb_inserted   BIGINT;
    v_nb_history    BIGINT;
    v_nb_touched    BIGINT;
    v_sum_updated   BIGINT  := 0;
    v_sum_inserted  BIGINT  := 0;
    v_sum_history   BIGINT  := 0;
    v_batch_num     INTEGER := 0;
    v_cert_seq      TEXT;
    v_nb_certified  BIGINT;
    v_sum_certified BIGINT  := 0;
BEGIN
    IF batch_size IS NULL OR batch_size <= 0 THEN
        RAISE EXCEPTION 'batch_size doit être > 0 (reçu : %)', batch_size;
    END IF;

    -- Séquence des id de certification (colonne SERIAL / IDENTITY)
    v_cert_seq := PG_GET_SERIAL_SEQUENCE('identitystore_identity_attribute_certificate', 'id_attribute_certificate');
    IF v_cert_seq IS NULL THEN
        RAISE EXCEPTION 'Aucune séquence trouvée pour identitystore_identity_attribute_certificate.id_attribute_certificate';
    END IF;

    SELECT key_name
    INTO v_attribute_key
    FROM identitystore_ref_attribute
    WHERE id_attribute = attribute_id_birthcountry_code;

    IF v_attribute_key IS NULL THEN
        RAISE EXCEPTION 'Attribut % introuvable dans identitystore_ref_attribute', attribute_id_birthcountry_code;
    END IF;

    -- Candidats calculés une seule fois, puis traités par tranches d'id_identity
    DROP TABLE IF EXISTS tmp_fix_country_codes;
    CREATE TEMP TABLE tmp_fix_country_codes AS
    SELECT m.id_identity,
           (CASE
                WHEN m.resolution_status = 'RESOLVABLE' THEN m.country_code
                ELSE france_code_without_birthdate END)::VARCHAR AS country_code,
           m.code_row_exists
    FROM GET_MISSING_COUNTRY_CODES(attribute_id_birthcountry_code,
                                   attribute_id_birthcountry,
                                   attribute_id_birthdate) m
    WHERE m.resolution_status = 'RESOLVABLE'
       -- cas particulier : libellé FRANCE (tirets, accents, casse ignorés) sans date de naissance exploitable
       OR (france_code_without_birthdate IS NOT NULL
        AND m.resolution_status = 'NO_BIRTHDATE'
        AND NORMALIZE_COUNTRY_LABEL(m.birthcountry) = 'FRANCE');
    ALTER TABLE tmp_fix_country_codes ADD PRIMARY KEY (id_identity);

    SELECT COUNT(*) INTO v_total FROM tmp_fix_country_codes;
    RAISE NOTICE '% identités à corriger (batch de %)', v_total, batch_size;
    COMMIT;

    LOOP
        WITH batch AS (SELECT t.id_identity, t.country_code, t.code_row_exists
                       FROM tmp_fix_country_codes t
                       WHERE t.id_identity > v_last_id
                       ORDER BY t.id_identity
                       LIMIT batch_size),
             -- Code existant mais vide : mise à jour (re-vérifie qu'il est toujours vide)
             updated AS (
                 UPDATE identitystore_identity_attribute attr
                     SET attribute_value = b.country_code,
                         lastupdate_date = NOW(),
                         lastupdate_client = history_client_code
                     FROM batch b
                     WHERE b.code_row_exists
                         AND attr.id_identity = b.id_identity
                         AND attr.id_attribute = attribute_id_birthcountry_code
                         AND (attr.attribute_value IS NULL OR attr.attribute_value = '')
                     RETURNING attr.id_identity, attr.attribute_value, attr.id_certification),
             -- Lignes de code à créer, avec la certification du libellé à recopier.
             -- Un nouvel id de certification est réservé (nextval) pour chaque libellé certifié :
             -- la CTE est matérialisée une seule fois (fonction volatile), les id sont donc stables.
             to_insert AS (SELECT b.id_identity,
                                  b.country_code,
                                  labelcert.certifier_code,
                                  labelcert.certificate_date,
                                  labelcert.expiration_date,
                                  CASE
                                      WHEN labelcert.id_attribute_certificate IS NOT NULL
                                          THEN NEXTVAL(v_cert_seq)::INTEGER
                                      END AS new_id_certification
                           FROM batch b
                                    JOIN identitystore_identity_attribute attrlabel
                                         ON attrlabel.id_identity = b.id_identity
                                             AND attrlabel.id_attribute = attribute_id_birthcountry
                                    LEFT JOIN identitystore_identity_attribute_certificate labelcert
                                              ON labelcert.id_attribute_certificate = attrlabel.id_certification
                                                  AND attrlabel.id_certification <> 0
                           WHERE NOT b.code_row_exists
                             -- ligne créée entre-temps : ignorée (évite une certification orpheline)
                             AND NOT EXISTS (SELECT 1
                                             FROM identitystore_identity_attribute ex
                                             WHERE ex.id_identity = b.id_identity
                                               AND ex.id_attribute = attribute_id_birthcountry_code)),
             -- Copie de la certification du libellé pour le nouvel attribut code
             certified AS (
                 INSERT INTO identitystore_identity_attribute_certificate (id_attribute_certificate, certifier_code,
                                                                           certificate_date, expiration_date)
                     SELECT ti.new_id_certification, ti.certifier_code, ti.certificate_date,
                            ti.expiration_date
                     FROM to_insert ti
                     WHERE ti.new_id_certification IS NOT NULL
                     RETURNING id_attribute_certificate),
             -- Ligne de code absente : création
             inserted AS (
                 INSERT INTO identitystore_identity_attribute (id_identity, id_attribute, attribute_value,
                                                               id_certification, id_file, lastupdate_date,
                                                               lastupdate_client)
                     SELECT ti.id_identity, attribute_id_birthcountry_code, ti.country_code,
                            COALESCE(ti.new_id_certification, 0), 0, NOW(), history_client_code
                     FROM to_insert ti
                     ON CONFLICT (id_identity, id_attribute) DO NOTHING
                     RETURNING id_identity, attribute_value, id_certification),
             -- Les certifications créées dans cette requête ne sont pas visibles par une jointure
             -- sur la table : on reprend leurs valeurs depuis to_insert.
             changes AS (SELECT 1 AS change_type, 'updated' AS change_status, u.id_identity, u.attribute_value,
                                cert.certifier_code, cert.certificate_date
                         FROM updated u
                                  LEFT JOIN identitystore_identity_attribute_certificate cert
                                            ON cert.id_attribute_certificate = u.id_certification
                         UNION ALL
                         SELECT 0, 'created', i.id_identity, i.attribute_value,
                                ti.certifier_code, ti.certificate_date
                         FROM inserted i
                                  JOIN to_insert ti ON ti.id_identity = i.id_identity),
             -- Une ligne d'historique par code ajouté
             history AS (
                 INSERT INTO identitystore_identity_attribute_history (change_type, change_satus, change_message,
                                                                       author_type, author_name, client_code,
                                                                       id_identity, attribute_key, attribute_value,
                                                                       certification_process, certification_date,
                                                                       modification_date)
                     SELECT c.change_type, c.change_status, NULL, 'application', history_author_name,
                            history_client_code, c.id_identity, v_attribute_key, c.attribute_value,
                            c.certifier_code, c.certificate_date, NOW()
                     FROM changes c
                     RETURNING id_identity),
             touched AS (
                 UPDATE identitystore_identity idt
                     SET last_update_date = NOW()
                     WHERE touch_identity
                         AND idt.id_identity IN (SELECT c.id_identity FROM changes c)
                     RETURNING idt.id_identity)
        SELECT (SELECT MAX(b.id_identity) FROM batch b),
               (SELECT COUNT(*) FROM updated),
               (SELECT COUNT(*) FROM inserted),
               (SELECT COUNT(*) FROM history),
               (SELECT COUNT(*) FROM touched),
               (SELECT COUNT(*) FROM certified)
        INTO v_batch_last_id, v_nb_updated, v_nb_inserted, v_nb_history, v_nb_touched, v_nb_certified;

        EXIT WHEN v_batch_last_id IS NULL;

        v_last_id := v_batch_last_id;
        v_batch_num := v_batch_num + 1;
        v_sum_updated := v_sum_updated + v_nb_updated;
        v_sum_inserted := v_sum_inserted + v_nb_inserted;
        v_sum_history := v_sum_history + v_nb_history;
        v_sum_certified := v_sum_certified + v_nb_certified;

        COMMIT;

        RAISE NOTICE 'Batch % (jusqu''à id_identity %) : % mis à jour, % créés (dont % certifiés), % historisés - cumul % / %',
            v_batch_num, v_last_id, v_nb_updated, v_nb_inserted, v_nb_certified, v_nb_history,
            v_sum_updated + v_sum_inserted, v_total;
    END LOOP;

    DROP TABLE IF EXISTS tmp_fix_country_codes;

    RAISE NOTICE 'Terminé : % mis à jour, % créés (dont % avec certification), % lignes d''historique (% identités candidates)',
        v_sum_updated, v_sum_inserted, v_sum_certified, v_sum_history, v_total;
END
$$
    LANGUAGE plpgsql;



----------------------------------------------------------
----------------------------------------------------------
-- Correction des libellés de pays de naissance (birthcountry) à partir du
-- code existant (birthcountry_code), via le référentiel geocodes_country.
-- (même règle que l'application : le libellé est celui de geocodes pour le code)
--
-- Usage :
--   SELECT * FROM COUNT_TOTAL_WRONG_COUNTRY_LABELS(<id birthcountry_code>, <id birthcountry>, <id birthdate>, 20);
--   SELECT * FROM COUNT_WRONG_COUNTRY_LABELS(<id birthcountry_code>, <id birthcountry>, <id birthdate>, 5);
--   CALL UPDATE_COUNTRY_LABEL_BY_CODE(<id birthcountry_code>, <id birthcountry>, <id birthdate>, 1000);
--
-- Libellés de référence d'un code : les libellés geocodes non dépréciés
-- (deprecated = 0) de ce code valides à la date de naissance ; à défaut (pas de
-- date de naissance, ou aucune période ne la couvre), tous les libellés non
-- dépréciés de ce code. Un code qui n'existe qu'en ligne dépréciée est UNKNOWN_CODE.
-- Une identité est concernée si :
--   - elle n'est ni supprimée ni fusionnée,
--   - son code pays est renseigné,
--   - son libellé est vide, NULL, absent, ou ne fait pas partie des libellés de référence.
--   resolution_status : FC_CERTIFIED (libellé certifié FranceConnect : jamais modifié)
--                       RESOLVABLE   (un seul libellé de référence)
--                       AMBIGUOUS    (plusieurs libellés de référence, aucun ne correspond)
--                       UNKNOWN_CODE (code absent de geocodes_country ou seulement déprécié :
--                                     non corrigeable ici)
-- protected_certifiers : codes certificateurs FranceConnect (certifier_code de
--   identitystore_identity_attribute_certificate) dont le libellé n'est jamais modifié.
--   Défaut : ARRAY['fccertifier', 'fc'] ; à vérifier avec :
--   SELECT DISTINCT certifier_code FROM identitystore_identity_attribute_certificate;
----------------------------------------------------------
----------------------------------------------------------

----------------------------------------------------------
-- Liste des identités dont le libellé de pays ne correspond pas au code
-- pour protéger les identités FC : default ARRAY [ 'fccertifier', 'fc' ]
----------------------------------------------------------
DROP FUNCTION IF EXISTS GET_WRONG_COUNTRY_LABELS(INTEGER, INTEGER, INTEGER);
DROP FUNCTION IF EXISTS GET_WRONG_COUNTRY_LABELS(INTEGER, INTEGER, INTEGER, VARCHAR[]);
CREATE
    OR REPLACE FUNCTION GET_WRONG_COUNTRY_LABELS(attribute_id_birthcountry_code INTEGER default 57,
                                                 attribute_id_birthcountry INTEGER default 7,
                                                 attribute_id_birthdate INTEGER default 5,
                                                 protected_certifiers VARCHAR[] DEFAULT ARRAY ['tototo'])  
    RETURNS TABLE
            (
                id_identity       INTEGER,
                customer_id       VARCHAR,
                country_code      VARCHAR,
                current_label     VARCHAR,
                label_certifier   VARCHAR,
                birthdate         DATE,
                expected_label    VARCHAR,
                resolution_status VARCHAR,
                label_row_exists  BOOLEAN
            )
AS
$$
BEGIN
    RETURN QUERY
        WITH candidates AS (SELECT attrcode.id_identity,
                                   idt.customer_id,
                                   attrcode.attribute_value                 AS country_code,
                                   attrlabel.attribute_value                AS current_label,
                                   labelcert.certifier_code                 AS label_certifier,
                                   SAFE_TO_DATE(attrdate.attribute_value)   AS birthdate,
                                   (attrlabel.id_identity IS NOT NULL)      AS label_row_exists
                            FROM identitystore_identity_attribute attrcode
                                     JOIN identitystore_identity idt
                                          ON idt.id_identity = attrcode.id_identity
                                     LEFT JOIN identitystore_identity_attribute attrlabel
                                               ON attrlabel.id_identity = attrcode.id_identity
                                                   AND attrlabel.id_attribute = attribute_id_birthcountry
                                     LEFT JOIN identitystore_identity_attribute_certificate labelcert
                                               ON labelcert.id_attribute_certificate = attrlabel.id_certification
                                     LEFT JOIN identitystore_identity_attribute attrdate
                                               ON attrdate.id_identity = attrcode.id_identity
                                                   AND attrdate.id_attribute = attribute_id_birthdate
                            WHERE attrcode.id_attribute = attribute_id_birthcountry_code
                              AND attrcode.attribute_value IS NOT NULL
                              AND attrcode.attribute_value <> ''
                              AND COALESCE(idt.is_deleted, 0) = 0
                              AND COALESCE(idt.is_merged, 0) = 0),
             referenced AS (SELECT c.*,
                                   COALESCE(at_date.labels, all_periods.labels, ARRAY []::VARCHAR[]) AS ref_labels
                            FROM candidates c
                                     LEFT JOIN LATERAL (SELECT ARRAY_AGG(DISTINCT g.value::VARCHAR) AS labels
                                                        FROM geocodes_country g
                                                        WHERE g.code = c.country_code
                                                          AND COALESCE(g.deprecated, 0) = 0
                                                          AND g.date_validity_start <= c.birthdate
                                                          AND g.date_validity_end >= c.birthdate) at_date ON TRUE
                                     LEFT JOIN LATERAL (SELECT ARRAY_AGG(DISTINCT g.value::VARCHAR) AS labels
                                                        FROM geocodes_country g
                                                        WHERE g.code = c.country_code
                                                          AND COALESCE(g.deprecated, 0) = 0) all_periods ON TRUE)
        SELECT r.id_identity,
               r.customer_id::VARCHAR,
               r.country_code::VARCHAR,
               r.current_label::VARCHAR,
               r.label_certifier::VARCHAR,
               r.birthdate,
               (CASE WHEN CARDINALITY(r.ref_labels) = 1 THEN r.ref_labels[1] END)::VARCHAR,
               (CASE
                    WHEN r.label_certifier = ANY (protected_certifiers) THEN 'FC_CERTIFIED'
                    WHEN CARDINALITY(r.ref_labels) = 0 THEN 'UNKNOWN_CODE'
                    WHEN CARDINALITY(r.ref_labels) > 1 THEN 'AMBIGUOUS'
                    ELSE 'RESOLVABLE'
                   END)::VARCHAR,
               r.label_row_exists
        FROM referenced r
        WHERE r.current_label IS NULL
           OR r.current_label = ''
           -- comparaison sans accents, casse, tirets, apostrophes, espaces... (NORMALIZE_COUNTRY_LABEL)
           OR NOT EXISTS (SELECT 1
                          FROM UNNEST(r.ref_labels) AS ref(label)
                          WHERE NORMALIZE_COUNTRY_LABEL(ref.label) = NORMALIZE_COUNTRY_LABEL(r.current_label));
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Synthèse globale : nombre total d'identités au libellé incorrect,
-- répartition par statut, et un échantillon de customer_id.
----------------------------------------------------------
DROP FUNCTION IF EXISTS COUNT_TOTAL_WRONG_COUNTRY_LABELS(INTEGER, INTEGER, INTEGER, INTEGER);
DROP FUNCTION IF EXISTS COUNT_TOTAL_WRONG_COUNTRY_LABELS(INTEGER, INTEGER, INTEGER, INTEGER, VARCHAR[]);
CREATE
    OR REPLACE FUNCTION COUNT_TOTAL_WRONG_COUNTRY_LABELS(attribute_id_birthcountry_code INTEGER default 57,
                                                         attribute_id_birthcountry INTEGER default 7,
                                                         attribute_id_birthdate INTEGER default 5,
                                                         sample_size INTEGER DEFAULT 20,
                                                         protected_certifiers VARCHAR[] DEFAULT ARRAY ['fccertifier', 'fc'])
    RETURNS TABLE
            (
                total_identities  BIGINT,
                resolvable        BIGINT,
                to_update         BIGINT,
                to_insert         BIGINT,
                ambiguous         BIGINT,
                unknown_code      BIGINT,
                fc_certified      BIGINT,
                sample_customer_ids TEXT
            )
AS
$$
BEGIN
    RETURN QUERY
        WITH wrong AS (SELECT *
                       FROM GET_WRONG_COUNTRY_LABELS(attribute_id_birthcountry_code,
                                                     attribute_id_birthcountry,
                                                     attribute_id_birthdate,
                                                     protected_certifiers))
        SELECT COUNT(*),
               COUNT(*) FILTER (WHERE w.resolution_status = 'RESOLVABLE'),
               COUNT(*) FILTER (WHERE w.resolution_status = 'RESOLVABLE' AND w.label_row_exists),
               COUNT(*) FILTER (WHERE w.resolution_status = 'RESOLVABLE' AND NOT w.label_row_exists),
               COUNT(*) FILTER (WHERE w.resolution_status = 'AMBIGUOUS'),
               COUNT(*) FILTER (WHERE w.resolution_status = 'UNKNOWN_CODE'),
               COUNT(*) FILTER (WHERE w.resolution_status = 'FC_CERTIFIED'),
               (SELECT STRING_AGG(s.customer_id, ',' ORDER BY s.id_identity)
                FROM (SELECT ws.id_identity, ws.customer_id
                      FROM wrong ws
                      ORDER BY ws.id_identity
                      LIMIT sample_size) s)
        FROM wrong w;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Détail par code pays et libellé actuel : libellé attendu, nombre d'identités,
-- nombre corrigeables et un échantillon de customer_id.
----------------------------------------------------------
DROP FUNCTION IF EXISTS COUNT_WRONG_COUNTRY_LABELS(INTEGER, INTEGER, INTEGER, INTEGER);
DROP FUNCTION IF EXISTS COUNT_WRONG_COUNTRY_LABELS(INTEGER, INTEGER, INTEGER, INTEGER, VARCHAR[]);
CREATE
    OR REPLACE FUNCTION COUNT_WRONG_COUNTRY_LABELS(attribute_id_birthcountry_code INTEGER default 57,
                                                   attribute_id_birthcountry INTEGER default 7,
                                                   attribute_id_birthdate INTEGER default 5,
                                                   sample_size INTEGER DEFAULT 5,
                                                   protected_certifiers VARCHAR[] DEFAULT ARRAY ['fccertifier', 'fc'])
    RETURNS TABLE
            (
                country_code      VARCHAR,
                current_label     VARCHAR,
                expected_labels   TEXT,
                count             BIGINT,
                count_resolvable  BIGINT,
                count_fc_certified BIGINT,
                sample_customer_ids TEXT
            )
AS
$$
BEGIN
    RETURN QUERY
        SELECT w.country_code,
               w.current_label,
               STRING_AGG(DISTINCT w.expected_label, ','),
               COUNT(*),
               COUNT(*) FILTER (WHERE w.resolution_status = 'RESOLVABLE'),
               COUNT(*) FILTER (WHERE w.resolution_status = 'FC_CERTIFIED'),
               ARRAY_TO_STRING((ARRAY_AGG(w.customer_id ORDER BY w.id_identity))[1:sample_size], ',')
        FROM GET_WRONG_COUNTRY_LABELS(attribute_id_birthcountry_code,
                                      attribute_id_birthcountry,
                                      attribute_id_birthdate,
                                      protected_certifiers) w
        GROUP BY w.country_code, w.current_label
        ORDER BY COUNT(*) DESC;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Remet le libellé geocodes correspondant au code pays, par batch de
-- batch_size identités (un COMMIT par batch).
--   - libellé existant (faux, vide ou NULL) -> UPDATE + historique change_type=1, status 'updated'
--     (la certification du libellé est conservée)
--   - ligne de libellé absente              -> INSERT + historique change_type=0, status 'created'
--     (sans certification)
-- Seules les identités au statut RESOLVABLE sont traitées : un libellé certifié
-- par un des protected_certifiers (FranceConnect) n'est jamais modifié, y compris
-- s'il a été certifié FranceConnect pendant l'exécution.
-- À appeler par CALL hors transaction explicite (autocommit).
----------------------------------------------------------
DROP PROCEDURE IF EXISTS UPDATE_COUNTRY_LABEL_BY_CODE(INTEGER, INTEGER, INTEGER, INTEGER, VARCHAR, VARCHAR, BOOLEAN);
DROP PROCEDURE IF EXISTS UPDATE_COUNTRY_LABEL_BY_CODE(INTEGER, INTEGER, INTEGER, INTEGER, VARCHAR, VARCHAR, BOOLEAN, VARCHAR[]);
CREATE
    OR REPLACE PROCEDURE UPDATE_COUNTRY_LABEL_BY_CODE(attribute_id_birthcountry_code INTEGER default 57,
                                                      attribute_id_birthcountry INTEGER default 7,
                                                      attribute_id_birthdate INTEGER default 5,
                                                      batch_size INTEGER DEFAULT 1000,
                                                      history_author_name VARCHAR DEFAULT 'script_fix_country_labels',
                                                      history_client_code VARCHAR DEFAULT 'direct_in_DB',
                                                      touch_identity BOOLEAN DEFAULT FALSE,
                                                      protected_certifiers VARCHAR[] DEFAULT ARRAY ['fccertifier', 'fc'])
AS
$$
DECLARE
    v_attribute_key VARCHAR;
    v_total         BIGINT;
    v_last_id       INTEGER := 0;
    v_batch_last_id INTEGER;
    v_nb_updated    BIGINT;
    v_nb_inserted   BIGINT;
    v_nb_history    BIGINT;
    v_nb_touched    BIGINT;
    v_sum_updated   BIGINT  := 0;
    v_sum_inserted  BIGINT  := 0;
    v_sum_history   BIGINT  := 0;
    v_batch_num     INTEGER := 0;
BEGIN
    IF batch_size IS NULL OR batch_size <= 0 THEN
        RAISE EXCEPTION 'batch_size doit être > 0 (reçu : %)', batch_size;
    END IF;

    SELECT key_name
    INTO v_attribute_key
    FROM identitystore_ref_attribute
    WHERE id_attribute = attribute_id_birthcountry;

    IF v_attribute_key IS NULL THEN
        RAISE EXCEPTION 'Attribut % introuvable dans identitystore_ref_attribute', attribute_id_birthcountry;
    END IF;

    -- Candidats calculés une seule fois, puis traités par tranches d'id_identity
    DROP TABLE IF EXISTS tmp_fix_country_labels;
    CREATE TEMP TABLE tmp_fix_country_labels AS
    SELECT w.id_identity, w.current_label, w.expected_label, w.label_row_exists
    FROM GET_WRONG_COUNTRY_LABELS(attribute_id_birthcountry_code,
                                  attribute_id_birthcountry,
                                  attribute_id_birthdate,
                                  protected_certifiers) w
    WHERE w.resolution_status = 'RESOLVABLE';
    ALTER TABLE tmp_fix_country_labels ADD PRIMARY KEY (id_identity);

    SELECT COUNT(*) INTO v_total FROM tmp_fix_country_labels;
    RAISE NOTICE '% libellés à corriger (batch de %)', v_total, batch_size;
    COMMIT;

    LOOP
        WITH batch AS (SELECT t.id_identity, t.current_label, t.expected_label, t.label_row_exists
                       FROM tmp_fix_country_labels t
                       WHERE t.id_identity > v_last_id
                       ORDER BY t.id_identity
                       LIMIT batch_size),
             -- Libellé existant : mise à jour seulement s'il n'a pas changé entre-temps
             -- et s'il n'est pas (devenu) certifié FranceConnect
             updated AS (
                 UPDATE identitystore_identity_attribute attr
                     SET attribute_value = b.expected_label,
                         lastupdate_date = NOW(),
                         lastupdate_client = history_client_code
                     FROM batch b
                     WHERE b.label_row_exists
                         AND attr.id_identity = b.id_identity
                         AND attr.id_attribute = attribute_id_birthcountry
                         AND attr.attribute_value IS NOT DISTINCT FROM b.current_label
                         AND NOT EXISTS (SELECT 1
                                         FROM identitystore_identity_attribute_certificate labelcert
                                         WHERE labelcert.id_attribute_certificate = attr.id_certification
                                           AND labelcert.certifier_code = ANY (protected_certifiers))
                     RETURNING attr.id_identity, attr.attribute_value, attr.id_certification),
             -- Ligne de libellé absente : création (ignorée si créée entre-temps)
             inserted AS (
                 INSERT INTO identitystore_identity_attribute (id_identity, id_attribute, attribute_value,
                                                               id_certification, id_file, lastupdate_date,
                                                               lastupdate_client)
                     SELECT b.id_identity, attribute_id_birthcountry, b.expected_label, 0, 0, NOW(),
                            history_client_code
                     FROM batch b
                     WHERE NOT b.label_row_exists
                     ON CONFLICT (id_identity, id_attribute) DO NOTHING
                     RETURNING id_identity, attribute_value, id_certification),
             changes AS (SELECT 1 AS change_type, 'updated' AS change_status, u.id_identity, u.attribute_value,
                                u.id_certification
                         FROM updated u
                         UNION ALL
                         SELECT 0, 'created', i.id_identity, i.attribute_value, i.id_certification
                         FROM inserted i),
             -- Une ligne d'historique par libellé corrigé
             history AS (
                 INSERT INTO identitystore_identity_attribute_history (change_type, change_satus, change_message,
                                                                       author_type, author_name, client_code,
                                                                       id_identity, attribute_key, attribute_value,
                                                                       certification_process, certification_date,
                                                                       modification_date)
                     SELECT c.change_type, c.change_status, NULL, 'application', history_author_name,
                            history_client_code, c.id_identity, v_attribute_key, c.attribute_value,
                            cert.certifier_code, cert.certificate_date, NOW()
                     FROM changes c
                              LEFT JOIN identitystore_identity_attribute_certificate cert
                                        ON cert.id_attribute_certificate = c.id_certification
                     RETURNING id_identity),
             touched AS (
                 UPDATE identitystore_identity idt
                     SET last_update_date = NOW()
                     WHERE touch_identity
                         AND idt.id_identity IN (SELECT c.id_identity FROM changes c)
                     RETURNING idt.id_identity)
        SELECT (SELECT MAX(b.id_identity) FROM batch b),
               (SELECT COUNT(*) FROM updated),
               (SELECT COUNT(*) FROM inserted),
               (SELECT COUNT(*) FROM history),
               (SELECT COUNT(*) FROM touched)
        INTO v_batch_last_id, v_nb_updated, v_nb_inserted, v_nb_history, v_nb_touched;

        EXIT WHEN v_batch_last_id IS NULL;

        v_last_id := v_batch_last_id;
        v_batch_num := v_batch_num + 1;
        v_sum_updated := v_sum_updated + v_nb_updated;
        v_sum_inserted := v_sum_inserted + v_nb_inserted;
        v_sum_history := v_sum_history + v_nb_history;

        COMMIT;

        RAISE NOTICE 'Batch % (jusqu''à id_identity %) : % mis à jour, % créés, % historisés - cumul % / %',
            v_batch_num, v_last_id, v_nb_updated, v_nb_inserted, v_nb_history,
            v_sum_updated + v_sum_inserted, v_total;
    END LOOP;

    DROP TABLE IF EXISTS tmp_fix_country_labels;

    RAISE NOTICE 'Terminé : % mis à jour, % créés, % lignes d''historique (% identités candidates)',
        v_sum_updated, v_sum_inserted, v_sum_history, v_total;
END
$$
    LANGUAGE plpgsql;
