----------------------------------------------------------
----------------------------------------------------------
-- Correction des communes de naissance (birthplace / birthplace_code)
-- des identités nées en France, via le référentiel geocodes_city.
--
-- Deux corrections indépendantes :
--   A. code commune manquant  -> retrouvé depuis le libellé (UPDATE_CITY_CODE_BY_LIBELLE)
--   B. libellé commune faux   -> remis depuis le code       (UPDATE_CITY_LABEL_BY_CODE)
--
-- Usage (PostgreSQL >= 11, procédures en autocommit : elles font des COMMIT par batch) :
--
--   -- A. Code commune manquant
--   SELECT * FROM COUNT_TOTAL_MISSING_CITY_CODES(<id birthplace_code>, <id birthplace>, <id birthcountry_code>, <id birthdate>, 20);
--   SELECT * FROM COUNT_MISSING_CITY_CODES(<id birthplace_code>, <id birthplace>, <id birthcountry_code>, <id birthdate>, 5);
--   CALL UPDATE_CITY_CODE_BY_LIBELLE(<id birthplace_code>, <id birthplace>, <id birthcountry_code>, <id birthdate>, 1000);
--
--   -- B. Libellé commune faux
--   SELECT * FROM COUNT_TOTAL_WRONG_CITY_LABELS(<id birthplace_code>, <id birthplace>, <id birthcountry_code>, <id birthdate>, 20);
--   SELECT * FROM COUNT_WRONG_CITY_LABELS(<id birthplace_code>, <id birthplace>, <id birthcountry_code>, <id birthdate>, 5);
--   CALL UPDATE_CITY_LABEL_BY_CODE(<id birthplace_code>, <id birthplace>, <id birthcountry_code>, <id birthdate>, 1000);
--
-- Les id_attribute se trouvent avec :
--   SELECT id_attribute, key_name FROM identitystore_ref_attribute
--   WHERE key_name IN ('birthplace_code', 'birthplace', 'birthcountry_code', 'birthdate');
--
-- Règles reprises de l'application (IdentityAttributeGeocodesAdjustmentService + plugin geocodes) :
--   - seules les identités nées en France (birthcountry_code = '99100', paramètre
--     france_country_code) sont traitées : l'application ne gère ni code ni
--     ajustement de libellé pour une commune étrangère ;
--   - la date de référence est la date de naissance, ramenée au 01/01/1943 si
--     elle est antérieure (début du référentiel) ;
--   - une commune est valide à la date d si date_validity_start <= d < date_validity_end ;
--   - le libellé de référence d'un code est value_min_complete.
-- Les identités supprimées ou fusionnées sont ignorées.
--
-- Le script est indépendant de fixCountriesIdentities.sql ; les deux définissent
-- la même fonction SAFE_TO_DATE et peuvent être chargés dans n'importe quel ordre.
----------------------------------------------------------
----------------------------------------------------------

-- Anciennes signatures du plugin identitystore (le type de retour et les paramètres ont changé)
DROP FUNCTION IF EXISTS COUNT_MISSING_CITY_CODES(INTEGER, INTEGER, INTEGER, VARCHAR);
DROP PROCEDURE IF EXISTS UPDATE_CITY_CODE_BY_LIBELLE(INTEGER, INTEGER, INTEGER, VARCHAR, INTEGER);

----------------------------------------------------------
-- Conversion de date sans erreur : NULL si la valeur est invalide
-- (identique à celle de fixCountriesIdentities.sql)
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
-- Normalisation d'un libellé de commune pour la comparaison :
-- minuscules, sans accents, tout caractère autre que [a-z0-9] (tirets, apostrophes,
-- points, espaces...) remplacé par un espace, espaces multiples réduits.
-- Les accents sont retirés avant LOWER, en minuscules comme en majuscules :
-- le résultat ne dépend pas de la locale de la base (en locale C, LOWER('É') = 'É').
--   'L''Abergement-Clémenciat' -> 'l abergement clemenciat'
--   'L’ABERGEMENT-CLÉMENCIAT'  -> 'l abergement clemenciat'
--   'Évry–Courcouronnes'       -> 'evry courcouronnes'
----------------------------------------------------------
CREATE
    OR REPLACE FUNCTION NORMALIZE_CITY_LABEL(label VARCHAR)
    RETURNS VARCHAR
AS
$$
SELECT NULLIF(TRIM(REGEXP_REPLACE(
        LOWER(REPLACE(REPLACE(REPLACE(REPLACE(
            TRANSLATE(label,
                      'àáâãäåçèéêëìíîïñòóôõöøùúûüýÿÀÁÂÃÄÅÇÈÉÊËÌÍÎÏÑÒÓÔÕÖØÙÚÛÜÝŸ',
                      'aaaaaaceeeeiiiinoooooouuuuyyAAAAAACEEEEIIIINOOOOOOUUUUYY'),
            'æ', 'ae'), 'Æ', 'AE'), 'œ', 'oe'), 'Œ', 'OE')),
        '[^a-z0-9]+', ' ', 'g')), '')
$$
    LANGUAGE sql
    IMMUTABLE
    PARALLEL SAFE;


----------------------------------------------------------
----------------------------------------------------------
-- A. CODE COMMUNE MANQUANT
--
-- Une identité est concernée si :
--   - elle n'est ni supprimée ni fusionnée et est née en France,
--   - son libellé de commune (birthplace) est renseigné,
--   - son code commune est vide, NULL ou absent.
-- Le libellé est comparé, après normalisation, à value, value_min et
-- value_min_complete des communes non dépréciées valides à la date de référence.
-- (l'application ne compare qu'à value ; comparer aussi les deux autres colonnes
-- retrouve les libellés saisis sous la forme 'L''Abergement-Clémenciat')
--   resolution_status : RESOLVABLE    (exactement un code)
--                       NO_BIRTHDATE  (pas de date de naissance ou date invalide)
--                       UNKNOWN_LABEL (aucune commune ne correspond à cette date)
--                       AMBIGUOUS     (plusieurs codes possibles)
----------------------------------------------------------
----------------------------------------------------------

DROP FUNCTION IF EXISTS GET_MISSING_CITY_CODES(INTEGER, INTEGER, INTEGER, INTEGER, VARCHAR);
CREATE
    OR REPLACE FUNCTION GET_MISSING_CITY_CODES(attribute_id_birthplace_code INTEGER default 56,
                                               attribute_id_birthplace INTEGER default 6,
                                               attribute_id_birthcountry_code INTEGER default 57,
                                               attribute_id_birthdate INTEGER default 5,
                                               france_country_code VARCHAR DEFAULT '99100')
    RETURNS TABLE
            (
                id_identity       INTEGER,
                customer_id       VARCHAR,
                birthplace        VARCHAR,
                birthdate         DATE,
                city_code         VARCHAR,
                resolution_status VARCHAR,
                code_row_exists   BOOLEAN
            )
AS
$$
BEGIN
    RETURN QUERY
        WITH candidates AS (SELECT attrcity.id_identity,
                                   idt.customer_id,
                                   attrcity.attribute_value                     AS birthplace,
                                   NORMALIZE_CITY_LABEL(attrcity.attribute_value) AS birthplace_key,
                                   SAFE_TO_DATE(attrdate.attribute_value)       AS birthdate,
                                   (attrcitycode.id_identity IS NOT NULL)       AS code_row_exists
                            FROM identitystore_identity_attribute attrcity
                                     JOIN identitystore_identity idt
                                          ON idt.id_identity = attrcity.id_identity
                                     JOIN identitystore_identity_attribute attrcountrycode
                                          ON attrcountrycode.id_identity = attrcity.id_identity
                                              AND attrcountrycode.id_attribute = attribute_id_birthcountry_code
                                              AND attrcountrycode.attribute_value = france_country_code
                                     LEFT JOIN identitystore_identity_attribute attrcitycode
                                               ON attrcitycode.id_identity = attrcity.id_identity
                                                   AND attrcitycode.id_attribute = attribute_id_birthplace_code
                                     LEFT JOIN identitystore_identity_attribute attrdate
                                               ON attrdate.id_identity = attrcity.id_identity
                                                   AND attrdate.id_attribute = attribute_id_birthdate
                            WHERE attrcity.id_attribute = attribute_id_birthplace
                              AND attrcity.attribute_value IS NOT NULL
                              AND attrcity.attribute_value <> ''
                              -- (écrit sans COALESCE pour que le planificateur estime bien le volume)
                              AND (idt.is_deleted = 0 OR idt.is_deleted IS NULL)
                              AND (idt.is_merged = 0 OR idt.is_merged IS NULL)
                              AND (attrcitycode.attribute_value IS NULL OR attrcitycode.attribute_value = '')),
             -- Une ligne par forme de libellé de chaque commune non dépréciée
             city_keys AS (SELECT DISTINCT g.code, g.date_validity_start, g.date_validity_end, k.city_key
                           FROM geocodes_city g
                                    CROSS JOIN LATERAL (VALUES (NORMALIZE_CITY_LABEL(g.value)),
                                                               (NORMALIZE_CITY_LABEL(g.value_min)),
                                                               (NORMALIZE_CITY_LABEL(g.value_min_complete))) k(city_key)
                           WHERE COALESCE(g.deprecated, 0) = 0
                             AND g.code_country = france_country_code
                             AND k.city_key IS NOT NULL),
             -- Jointure par égalité sur le libellé normalisé (jointure hash), filtre de date,
             -- puis agrégation par identité
             matches AS (SELECT c.id_identity,
                                c.customer_id,
                                c.birthplace,
                                c.birthdate,
                                c.code_row_exists,
                                COUNT(DISTINCT ck.code) AS nb_codes,
                                MIN(ck.code)            AS code
                         FROM candidates c
                                  LEFT JOIN city_keys ck
                                            ON ck.city_key = c.birthplace_key
                                                -- obligatoire : GREATEST ignore les NULL
                                                AND c.birthdate IS NOT NULL
                                                AND ck.date_validity_start <= GREATEST(c.birthdate, DATE '1943-01-01')
                                                AND ck.date_validity_end > GREATEST(c.birthdate, DATE '1943-01-01')
                         GROUP BY c.id_identity, c.customer_id, c.birthplace, c.birthdate, c.code_row_exists)
        SELECT m.id_identity,
               m.customer_id::VARCHAR,
               m.birthplace::VARCHAR,
               m.birthdate,
               (CASE WHEN m.nb_codes = 1 THEN m.code END)::VARCHAR,
               (CASE
                    WHEN m.birthdate IS NULL THEN 'NO_BIRTHDATE'
                    WHEN m.nb_codes = 0 THEN 'UNKNOWN_LABEL'
                    WHEN m.nb_codes > 1 THEN 'AMBIGUOUS'
                    ELSE 'RESOLVABLE'
                   END)::VARCHAR,
               m.code_row_exists
        FROM matches m;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- A. Synthèse globale : nombre total d'identités concernées, répartition par
-- statut de résolution, et un échantillon de customer_id.
----------------------------------------------------------
DROP FUNCTION IF EXISTS COUNT_TOTAL_MISSING_CITY_CODES(INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, VARCHAR);
CREATE
    OR REPLACE FUNCTION COUNT_TOTAL_MISSING_CITY_CODES(attribute_id_birthplace_code INTEGER default 56 ,
                                                       attribute_id_birthplace INTEGER default 6,
                                                       attribute_id_birthcountry_code INTEGER default 57,
                                                       attribute_id_birthdate INTEGER default 5,
                                                       sample_size INTEGER DEFAULT 20,
                                                       france_country_code VARCHAR DEFAULT '99100')
    RETURNS TABLE
            (
                total_identities  BIGINT,
                resolvable        BIGINT,
                to_update         BIGINT,
                to_insert         BIGINT,
                no_birthdate      BIGINT,
                unknown_label     BIGINT,
                ambiguous         BIGINT,
                sample_customer_ids TEXT
            )
AS
$$
BEGIN
    RETURN QUERY
        WITH missing AS (SELECT *
                         FROM GET_MISSING_CITY_CODES(attribute_id_birthplace_code,
                                                     attribute_id_birthplace,
                                                     attribute_id_birthcountry_code,
                                                     attribute_id_birthdate,
                                                     france_country_code))
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
-- A. Détail par libellé de commune : code(s) trouvé(s), nombre d'identités,
-- nombre corrigeables et un échantillon de customer_id.
----------------------------------------------------------
DROP FUNCTION IF EXISTS COUNT_MISSING_CITY_CODES(INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, VARCHAR);
CREATE
    OR REPLACE FUNCTION COUNT_MISSING_CITY_CODES(attribute_id_birthplace_code INTEGER default 56,
                                                 attribute_id_birthplace INTEGER default 6,
                                                 attribute_id_birthcountry_code INTEGER default 57,
                                                 attribute_id_birthdate INTEGER default 5,
                                                 sample_size INTEGER DEFAULT 5,
                                                 france_country_code VARCHAR DEFAULT '99100')
    RETURNS TABLE
            (
                value             VARCHAR,
                city_codes        TEXT,
                count             BIGINT,
                count_resolvable  BIGINT,
                sample_customer_ids TEXT
            )
AS
$$
BEGIN
    RETURN QUERY
        SELECT m.birthplace,
               STRING_AGG(DISTINCT m.city_code, ','),
               COUNT(*),
               COUNT(*) FILTER (WHERE m.resolution_status = 'RESOLVABLE'),
               ARRAY_TO_STRING((ARRAY_AGG(m.customer_id ORDER BY m.id_identity))[1:sample_size], ',')
        FROM GET_MISSING_CITY_CODES(attribute_id_birthplace_code,
                                    attribute_id_birthplace,
                                    attribute_id_birthcountry_code,
                                    attribute_id_birthdate,
                                    france_country_code) m
        GROUP BY m.birthplace
        ORDER BY COUNT(*) DESC;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- A. Renseigne le birthplace_code à partir du libellé et de la date de naissance,
-- par batch de batch_size identités (un COMMIT par batch).
--   - code vide/NULL existant  -> UPDATE + historique change_type=1 (UPDATE), status 'updated'
--   - ligne de code absente    -> INSERT + historique change_type=0 (CREATE), status 'created'
--     Si le libellé (birthplace) est certifié, une nouvelle ligne de certification est créée
--     pour le code, copie de celle du libellé (certifier_code, certificate_date,
--     expiration_date) ; sinon id_certification = 0.
-- Seules les identités au statut RESOLVABLE sont traitées.
-- touch_identity : met à jour identitystore_identity.last_update_date (réindexation).
-- À appeler par CALL hors transaction explicite (autocommit).
----------------------------------------------------------
DROP PROCEDURE IF EXISTS UPDATE_CITY_CODE_BY_LIBELLE(INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, VARCHAR, VARCHAR, BOOLEAN, VARCHAR);
CREATE
    OR REPLACE PROCEDURE UPDATE_CITY_CODE_BY_LIBELLE(attribute_id_birthplace_code INTEGER default 56,
                                                     attribute_id_birthplace INTEGER default 6,
                                                     attribute_id_birthcountry_code INTEGER default 57,
                                                     attribute_id_birthdate INTEGER default 5,
                                                     batch_size INTEGER DEFAULT 1000,
                                                     history_author_name VARCHAR DEFAULT 'script_fix_city_codes',
                                                     history_client_code VARCHAR DEFAULT 'direct_in_DB',
                                                     touch_identity BOOLEAN DEFAULT FALSE,
                                                     france_country_code VARCHAR DEFAULT '99100')
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
    WHERE id_attribute = attribute_id_birthplace_code;

    IF v_attribute_key IS NULL THEN
        RAISE EXCEPTION 'Attribut % introuvable dans identitystore_ref_attribute', attribute_id_birthplace_code;
    END IF;

    -- Candidats calculés une seule fois, puis traités par tranches d'id_identity
    DROP TABLE IF EXISTS tmp_fix_city_codes;
    CREATE TEMP TABLE tmp_fix_city_codes AS
    SELECT m.id_identity, m.city_code, m.code_row_exists
    FROM GET_MISSING_CITY_CODES(attribute_id_birthplace_code,
                                attribute_id_birthplace,
                                attribute_id_birthcountry_code,
                                attribute_id_birthdate,
                                france_country_code) m
    WHERE m.resolution_status = 'RESOLVABLE';
    ALTER TABLE tmp_fix_city_codes ADD PRIMARY KEY (id_identity);

    SELECT COUNT(*) INTO v_total FROM tmp_fix_city_codes;
    RAISE NOTICE '% codes commune à renseigner (batch de %)', v_total, batch_size;
    COMMIT;

    LOOP
        WITH batch AS (SELECT t.id_identity, t.city_code, t.code_row_exists
                       FROM tmp_fix_city_codes t
                       WHERE t.id_identity > v_last_id
                       ORDER BY t.id_identity
                       LIMIT batch_size),
             -- Code existant mais vide : mise à jour (re-vérifie qu'il est toujours vide)
             updated AS (
                 UPDATE identitystore_identity_attribute attr
                     SET attribute_value = b.city_code,
                         lastupdate_date = NOW(),
                         lastupdate_client = history_client_code
                     FROM batch b
                     WHERE b.code_row_exists
                         AND attr.id_identity = b.id_identity
                         AND attr.id_attribute = attribute_id_birthplace_code
                         AND (attr.attribute_value IS NULL OR attr.attribute_value = '')
                     RETURNING attr.id_identity, attr.attribute_value, attr.id_certification),
             -- Lignes de code à créer, avec la certification du libellé à recopier.
             -- Un nouvel id de certification est réservé (nextval) pour chaque libellé certifié :
             -- la CTE est matérialisée une seule fois (fonction volatile), les id sont donc stables.
             to_insert AS (SELECT b.id_identity,
                                  b.city_code,
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
                                             AND attrlabel.id_attribute = attribute_id_birthplace
                                    LEFT JOIN identitystore_identity_attribute_certificate labelcert
                                              ON labelcert.id_attribute_certificate = attrlabel.id_certification
                                                  AND attrlabel.id_certification <> 0
                           WHERE NOT b.code_row_exists
                             -- ligne créée entre-temps : ignorée (évite une certification orpheline)
                             AND NOT EXISTS (SELECT 1
                                             FROM identitystore_identity_attribute ex
                                             WHERE ex.id_identity = b.id_identity
                                               AND ex.id_attribute = attribute_id_birthplace_code)),
             -- Copie de la certification du libellé pour le nouvel attribut code
             certified AS (
                 INSERT INTO identitystore_identity_attribute_certificate (id_attribute_certificate, certifier_code,
                                                                           certificate_date, expiration_date)
                     SELECT ti.new_id_certification, ti.certifier_code, ti.certificate_date, ti.expiration_date
                     FROM to_insert ti
                     WHERE ti.new_id_certification IS NOT NULL
                     RETURNING id_attribute_certificate),
             -- Ligne de code absente : création
             inserted AS (
                 INSERT INTO identitystore_identity_attribute (id_identity, id_attribute, attribute_value,
                                                               id_certification, id_file, lastupdate_date,
                                                               lastupdate_client)
                     SELECT ti.id_identity, attribute_id_birthplace_code, ti.city_code,
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

    DROP TABLE IF EXISTS tmp_fix_city_codes;

    RAISE NOTICE 'Terminé : % mis à jour, % créés (dont % avec certification), % lignes d''historique (% identités candidates)',
        v_sum_updated, v_sum_inserted, v_sum_certified, v_sum_history, v_total;
END
$$
    LANGUAGE plpgsql;


----------------------------------------------------------
----------------------------------------------------------
-- B. LIBELLÉ COMMUNE FAUX
--
-- Communes de référence d'un code : les lignes geocodes_city non dépréciées
-- (deprecated = 0) de ce code valides à la date de référence ; à défaut (pas de
-- date de naissance, ou aucune période ne la couvre), toutes les lignes non
-- dépréciées de ce code. Un code qui n'existe qu'en ligne dépréciée est UNKNOWN_CODE.
-- Le libellé attendu est value_min_complete (règle de l'application).
--
-- Une identité est concernée si :
--   - elle n'est ni supprimée ni fusionnée et est née en France,
--   - son code commune est renseigné,
--   - son libellé est vide, NULL, absent, ou ne correspond pas aux communes de référence.
-- strict_label :
--   FALSE (défaut) : un libellé est correct s'il est égal, après normalisation, à
--                    value, value_min ou value_min_complete d'une commune de référence
--                    ('PARIS', 'Paris' et 'paris' sont acceptés) ;
--   TRUE           : un libellé est correct seulement s'il est exactement égal à
--                    value_min_complete (comme l'application à l'écriture) ; tous les
--                    libellés au format 'ABERGEMENT CLEMENCIAT' sont alors réécrits.
-- protected_certifiers : codes certificateurs FranceConnect ; un libellé certifié
--   par l'un d'eux n'est jamais modifié. Défaut : ARRAY['fccertifier', 'fc'] ; à vérifier avec :
--   SELECT DISTINCT certifier_code FROM identitystore_identity_attribute_certificate;
--   resolution_status : FC_CERTIFIED (libellé certifié FranceConnect : jamais modifié)
--                       RESOLVABLE   (un seul libellé attendu)
--                       AMBIGUOUS    (plusieurs libellés attendus possibles)
--                       UNKNOWN_CODE (code absent de geocodes_city ou seulement déprécié :
--                                     non corrigeable ici)
----------------------------------------------------------
----------------------------------------------------------

DROP FUNCTION IF EXISTS GET_WRONG_CITY_LABELS(INTEGER, INTEGER, INTEGER, INTEGER, BOOLEAN, VARCHAR[], VARCHAR);
CREATE
    OR REPLACE FUNCTION GET_WRONG_CITY_LABELS(attribute_id_birthplace_code INTEGER default 56,
                                              attribute_id_birthplace INTEGER default 6,
                                              attribute_id_birthcountry_code INTEGER default 57,
                                              attribute_id_birthdate INTEGER default 5,
                                              strict_label BOOLEAN DEFAULT FALSE,
                                              protected_certifiers VARCHAR[] DEFAULT ARRAY ['fccertifier', 'fc'],
                                              france_country_code VARCHAR DEFAULT '99100')
    RETURNS TABLE
            (
                id_identity       INTEGER,
                customer_id       VARCHAR,
                city_code         VARCHAR,
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
        WITH candidates AS (SELECT attrcitycode.id_identity,
                                   idt.customer_id,
                                   attrcitycode.attribute_value                    AS city_code,
                                   attrcity.attribute_value                        AS current_label,
                                   NORMALIZE_CITY_LABEL(attrcity.attribute_value)  AS current_key,
                                   labelcert.certifier_code                        AS label_certifier,
                                   SAFE_TO_DATE(attrdate.attribute_value)          AS birthdate,
                                   -- date de référence : naissance ramenée au 01/01/1943 ; NULL si pas de date
                                   -- (GREATEST ignore les NULL, d'où le CASE)
                                   CASE
                                       WHEN SAFE_TO_DATE(attrdate.attribute_value) IS NOT NULL
                                           THEN GREATEST(SAFE_TO_DATE(attrdate.attribute_value), DATE '1943-01-01')
                                       END                                         AS ref_date,
                                   (attrcity.id_identity IS NOT NULL)              AS label_row_exists
                            FROM identitystore_identity_attribute attrcitycode
                                     JOIN identitystore_identity idt
                                          ON idt.id_identity = attrcitycode.id_identity
                                     JOIN identitystore_identity_attribute attrcountrycode
                                          ON attrcountrycode.id_identity = attrcitycode.id_identity
                                              AND attrcountrycode.id_attribute = attribute_id_birthcountry_code
                                              AND attrcountrycode.attribute_value = france_country_code
                                     LEFT JOIN identitystore_identity_attribute attrcity
                                               ON attrcity.id_identity = attrcitycode.id_identity
                                                   AND attrcity.id_attribute = attribute_id_birthplace
                                     LEFT JOIN identitystore_identity_attribute_certificate labelcert
                                               ON labelcert.id_attribute_certificate = attrcity.id_certification
                                     LEFT JOIN identitystore_identity_attribute attrdate
                                               ON attrdate.id_identity = attrcitycode.id_identity
                                                   AND attrdate.id_attribute = attribute_id_birthdate
                            WHERE attrcitycode.id_attribute = attribute_id_birthplace_code
                              AND attrcitycode.attribute_value IS NOT NULL
                              AND attrcitycode.attribute_value <> ''
                              -- (écrit sans COALESCE pour que le planificateur estime bien le volume)
                              AND (idt.is_deleted = 0 OR idt.is_deleted IS NULL)
                              AND (idt.is_merged = 0 OR idt.is_merged IS NULL)),
             -- Jointure par égalité sur le code (jointure hash), agrégée par identité :
             -- libellés attendus et correspondance du libellé actuel, à la date et toutes périodes
             referenced AS (SELECT c.id_identity,
                                   c.customer_id,
                                   c.city_code,
                                   c.current_label,
                                   c.label_certifier,
                                   c.birthdate,
                                   c.label_row_exists,
                                   ARRAY_AGG(DISTINCT g.value_min_complete::VARCHAR)
                                   FILTER (WHERE g.date_validity_start <= c.ref_date
                                       AND g.date_validity_end > c.ref_date)          AS labels_at_date,
                                   BOOL_OR(CASE
                                               WHEN strict_label THEN c.current_label = g.value_min_complete
                                               ELSE c.current_key IN (NORMALIZE_CITY_LABEL(g.value),
                                                                      NORMALIZE_CITY_LABEL(g.value_min),
                                                                      NORMALIZE_CITY_LABEL(g.value_min_complete))
                                       END)
                                   FILTER (WHERE g.date_validity_start <= c.ref_date
                                       AND g.date_validity_end > c.ref_date)          AS match_at_date,
                                   ARRAY_AGG(DISTINCT g.value_min_complete::VARCHAR)
                                   FILTER (WHERE g.code IS NOT NULL)                  AS labels_all,
                                   BOOL_OR(CASE
                                               WHEN strict_label THEN c.current_label = g.value_min_complete
                                               ELSE c.current_key IN (NORMALIZE_CITY_LABEL(g.value),
                                                                      NORMALIZE_CITY_LABEL(g.value_min),
                                                                      NORMALIZE_CITY_LABEL(g.value_min_complete))
                                       END)
                                   FILTER (WHERE g.code IS NOT NULL)                  AS match_all
                            FROM candidates c
                                     LEFT JOIN geocodes_city g
                                               ON g.code = c.city_code
                                                   AND g.code_country = france_country_code
                                                   -- seules les communes non dépréciées font référence
                                                   AND COALESCE(g.deprecated, 0) = 0
                            GROUP BY c.id_identity, c.customer_id, c.city_code, c.current_label, c.label_certifier,
                                     c.birthdate, c.label_row_exists),
             resolved AS (SELECT r.*,
                                 (CASE
                                      WHEN r.labels_at_date IS NOT NULL THEN r.labels_at_date
                                      ELSE r.labels_all END)                          AS ref_labels,
                                 (CASE
                                      WHEN r.labels_at_date IS NOT NULL THEN COALESCE(r.match_at_date, FALSE)
                                      ELSE COALESCE(r.match_all, FALSE) END)          AS label_ok
                          FROM referenced r)
        SELECT r.id_identity,
               r.customer_id::VARCHAR,
               r.city_code::VARCHAR,
               r.current_label::VARCHAR,
               r.label_certifier::VARCHAR,
               r.birthdate,
               (CASE WHEN CARDINALITY(r.ref_labels) = 1 THEN r.ref_labels[1] END)::VARCHAR,
               (CASE
                    WHEN r.label_certifier = ANY (protected_certifiers) THEN 'FC_CERTIFIED'
                    WHEN r.ref_labels IS NULL THEN 'UNKNOWN_CODE'
                    WHEN CARDINALITY(r.ref_labels) > 1 THEN 'AMBIGUOUS'
                    ELSE 'RESOLVABLE'
                   END)::VARCHAR,
               r.label_row_exists
        FROM resolved r
        WHERE r.current_label IS NULL
           OR r.current_label = ''
           OR NOT r.label_ok;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- B. Synthèse globale : nombre total d'identités au libellé de commune incorrect,
-- répartition par statut, et un échantillon de customer_id.
----------------------------------------------------------
DROP FUNCTION IF EXISTS COUNT_TOTAL_WRONG_CITY_LABELS(INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, BOOLEAN, VARCHAR[], VARCHAR);
CREATE
    OR REPLACE FUNCTION COUNT_TOTAL_WRONG_CITY_LABELS(attribute_id_birthplace_code INTEGER default 56,
                                                      attribute_id_birthplace INTEGER default 6,
                                                      attribute_id_birthcountry_code INTEGER default 57,
                                                      attribute_id_birthdate INTEGER default 5,
                                                      sample_size INTEGER DEFAULT 20,
                                                      strict_label BOOLEAN DEFAULT FALSE,
                                                      protected_certifiers VARCHAR[] DEFAULT ARRAY ['fccertifier', 'fc'],
                                                      france_country_code VARCHAR DEFAULT '99100')
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
                       FROM GET_WRONG_CITY_LABELS(attribute_id_birthplace_code,
                                                  attribute_id_birthplace,
                                                  attribute_id_birthcountry_code,
                                                  attribute_id_birthdate,
                                                  strict_label,
                                                  protected_certifiers,
                                                  france_country_code))
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
-- B. Détail par code commune et libellé actuel : libellé attendu, nombre
-- d'identités, nombre corrigeables, nombre certifiés FC et un échantillon de customer_id.
----------------------------------------------------------
DROP FUNCTION IF EXISTS COUNT_WRONG_CITY_LABELS(INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, BOOLEAN, VARCHAR[], VARCHAR);
CREATE
    OR REPLACE FUNCTION COUNT_WRONG_CITY_LABELS(attribute_id_birthplace_code INTEGER default 56,
                                                attribute_id_birthplace INTEGER default 6,
                                                attribute_id_birthcountry_code INTEGER default 57,
                                                attribute_id_birthdate INTEGER default 5,
                                                sample_size INTEGER DEFAULT 5,
                                                strict_label BOOLEAN DEFAULT FALSE,
                                                protected_certifiers VARCHAR[] DEFAULT ARRAY ['fccertifier', 'fc'],
                                                france_country_code VARCHAR DEFAULT '99100')
    RETURNS TABLE
            (
                city_code          VARCHAR,
                current_label      VARCHAR,
                expected_labels    TEXT,
                count              BIGINT,
                count_resolvable   BIGINT,
                count_fc_certified BIGINT,
                sample_customer_ids TEXT
            )
AS
$$
BEGIN
    RETURN QUERY
        SELECT w.city_code,
               w.current_label,
               STRING_AGG(DISTINCT w.expected_label, ','),
               COUNT(*),
               COUNT(*) FILTER (WHERE w.resolution_status = 'RESOLVABLE'),
               COUNT(*) FILTER (WHERE w.resolution_status = 'FC_CERTIFIED'),
               ARRAY_TO_STRING((ARRAY_AGG(w.customer_id ORDER BY w.id_identity))[1:sample_size], ',')
        FROM GET_WRONG_CITY_LABELS(attribute_id_birthplace_code,
                                   attribute_id_birthplace,
                                   attribute_id_birthcountry_code,
                                   attribute_id_birthdate,
                                   strict_label,
                                   protected_certifiers,
                                   france_country_code) w
        GROUP BY w.city_code, w.current_label
        ORDER BY COUNT(*) DESC;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- B. Remet le libellé geocodes (value_min_complete) correspondant au code commune,
-- par batch de batch_size identités (un COMMIT par batch).
--   - libellé existant (faux, vide ou NULL) -> UPDATE + historique change_type=1, status 'updated'
--     (la certification du libellé est conservée)
--   - ligne de libellé absente              -> INSERT + historique change_type=0, status 'created'
--     (sans certification)
-- Seules les identités au statut RESOLVABLE sont traitées : un libellé certifié
-- par un des protected_certifiers (FranceConnect) n'est jamais modifié, y compris
-- s'il a été certifié FranceConnect pendant l'exécution.
-- À appeler par CALL hors transaction explicite (autocommit).
----------------------------------------------------------
DROP PROCEDURE IF EXISTS UPDATE_CITY_LABEL_BY_CODE(INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, VARCHAR, VARCHAR, BOOLEAN, BOOLEAN, VARCHAR[], VARCHAR);
CREATE
    OR REPLACE PROCEDURE UPDATE_CITY_LABEL_BY_CODE(attribute_id_birthplace_code INTEGER default 56,
                                                   attribute_id_birthplace INTEGER default 6,
                                                   attribute_id_birthcountry_code INTEGER default 57,
                                                   attribute_id_birthdate INTEGER default 5,
                                                   batch_size INTEGER DEFAULT 1000,
                                                   history_author_name VARCHAR DEFAULT 'script_fix_city_labels',
                                                   history_client_code VARCHAR DEFAULT 'direct_in_DB',
                                                   touch_identity BOOLEAN DEFAULT FALSE,
                                                   strict_label BOOLEAN DEFAULT FALSE,
                                                   protected_certifiers VARCHAR[] DEFAULT ARRAY ['fccertifier', 'fc'],
                                                   france_country_code VARCHAR DEFAULT '99100')
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
    WHERE id_attribute = attribute_id_birthplace;

    IF v_attribute_key IS NULL THEN
        RAISE EXCEPTION 'Attribut % introuvable dans identitystore_ref_attribute', attribute_id_birthplace;
    END IF;

    -- Candidats calculés une seule fois, puis traités par tranches d'id_identity
    DROP TABLE IF EXISTS tmp_fix_city_labels;
    CREATE TEMP TABLE tmp_fix_city_labels AS
    SELECT w.id_identity, w.current_label, w.expected_label, w.label_row_exists
    FROM GET_WRONG_CITY_LABELS(attribute_id_birthplace_code,
                               attribute_id_birthplace,
                               attribute_id_birthcountry_code,
                               attribute_id_birthdate,
                               strict_label,
                               protected_certifiers,
                               france_country_code) w
    WHERE w.resolution_status = 'RESOLVABLE';
    ALTER TABLE tmp_fix_city_labels ADD PRIMARY KEY (id_identity);

    SELECT COUNT(*) INTO v_total FROM tmp_fix_city_labels;
    RAISE NOTICE '% libellés de commune à corriger (batch de %)', v_total, batch_size;
    COMMIT;

    LOOP
        WITH batch AS (SELECT t.id_identity, t.current_label, t.expected_label, t.label_row_exists
                       FROM tmp_fix_city_labels t
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
                         AND attr.id_attribute = attribute_id_birthplace
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
                     SELECT b.id_identity, attribute_id_birthplace, b.expected_label, 0, 0, NOW(),
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

    DROP TABLE IF EXISTS tmp_fix_city_labels;

    RAISE NOTICE 'Terminé : % mis à jour, % créés, % lignes d''historique (% identités candidates)',
        v_sum_updated, v_sum_inserted, v_sum_history, v_total;
END
$$
    LANGUAGE plpgsql;
