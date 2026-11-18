----------------------------------------------------------
----------------------------------------------------------
-- Ajout du pays de naissance FRANCE (birthcountry = 'FRANCE', birthcountry_code = '99100')
-- aux identités qui ont un code commune de naissance (birthplace_code) mais pas de code pays.
--
-- PRÉREQUIS : fixCountryAttributes.sql chargé (NORMALIZE_COUNTRY_LABEL).
--
-- Usage (PostgreSQL >= 12, en autocommit : la procédure fait des COMMIT par batch) :
--
--   -- 1. Synthèse par statut : nombre d'identités + échantillon de customer_id
--   SELECT * FROM COUNT_MISSING_COUNTRIES();
--
--   -- 2. Détail par identité
--   SELECT * FROM GET_MISSING_COUNTRIES() WHERE status = 'LABEL_CONFLICT';
--
--   -- 3. Correction par batch (+ ligne d'historique par attribut ajouté ou complété)
--   CALL FIX_MISSING_COUNTRIES();
--
-- Une identité est concernée si :
--   - elle n'est ni supprimée ni fusionnée,
--   - son code commune (birthplace_code) est renseigné,
--   - son code pays (birthcountry_code) est vide, NULL ou absent (pas de ligne d'attribut).
--   status : TO_FIX            (corrigée par FIX_MISSING_COUNTRIES)
--            UNKNOWN_CITY_CODE (le code commune n'existe pas dans geocodes_city pour la France,
--                               ex : un code pays 99xxx saisi comme code commune : non corrigée)
--            LABEL_CONFLICT    (un libellé de pays autre que FRANCE est déjà renseigné : non corrigée)
--
-- Correction (identités TO_FIX) :
--   - birthcountry_code : créé, ou complété s'il existe vide, avec france_country_code ;
--   - birthcountry      : créé, ou complété s'il existe vide, avec france_country_label ;
--                         s'il vaut déjà FRANCE sous une autre forme ('France', 'france ', 'FRANCE.'...),
--                         il est réécrit systématiquement en 'FRANCE' (majuscules) ;
--   - certification : chaque attribut créé ou complété reçoit une copie (nouvelle ligne) de la
--     certification du code commune (certifier_code, certificate_date, expiration_date) ; sans
--     certification du code commune, id_certification = 0. Un libellé seulement remis en
--     majuscules garde sa propre certification ;
--   - historique : une ligne par attribut écrit (change_type 0 'created' ou 1 'updated'),
--     avec le processus et la date de certification.
----------------------------------------------------------
----------------------------------------------------------


----------------------------------------------------------
-- 1. Identités avec un code commune mais sans code pays
----------------------------------------------------------
DROP FUNCTION IF EXISTS GET_MISSING_COUNTRIES(INTEGER, INTEGER, INTEGER, VARCHAR, VARCHAR);
CREATE
    OR REPLACE FUNCTION GET_MISSING_COUNTRIES(attribute_id_birthplace_code INTEGER default 56,
                                              attribute_id_birthcountry INTEGER default 7,
                                              attribute_id_birthcountry_code INTEGER default 57,
                                              france_country_code VARCHAR DEFAULT '99100',
                                              france_country_label VARCHAR DEFAULT 'FRANCE')
    RETURNS TABLE
            (
                id_identity             INTEGER,
                customer_id             VARCHAR,
                birthplace_code         VARCHAR,
                birthcountry            VARCHAR,
                status                  VARCHAR,
                code_row_exists         BOOLEAN,
                label_row_exists        BOOLEAN,
                label_to_write          BOOLEAN,
                label_empty             BOOLEAN,
                id_certification_source INTEGER
            )
AS
$$
BEGIN
    RETURN QUERY
        -- Codes communes français connus (toutes dates, dépréciés compris), calculés une seule fois
        WITH city_codes AS MATERIALIZED (SELECT DISTINCT g.code
                                         FROM geocodes_city g
                                         WHERE g.code_country = france_country_code),
             candidates AS (SELECT attrcitycode.id_identity,
                                   idt.customer_id,
                                   TRIM(attrcitycode.attribute_value)         AS birthplace_code,
                                   attrcountry.attribute_value                AS birthcountry,
                                   (attrcountrycode.id_identity IS NOT NULL)  AS code_row_exists,
                                   (attrcountry.id_identity IS NOT NULL)      AS label_row_exists,
                                   (COALESCE(TRIM(attrcountry.attribute_value), '') = '') AS label_empty,
                                   attrcitycode.id_certification              AS id_certification_source
                            FROM identitystore_identity_attribute attrcitycode
                                     JOIN identitystore_identity idt
                                          ON idt.id_identity = attrcitycode.id_identity
                                     LEFT JOIN identitystore_identity_attribute attrcountrycode
                                               ON attrcountrycode.id_identity = attrcitycode.id_identity
                                                   AND attrcountrycode.id_attribute = attribute_id_birthcountry_code
                                     LEFT JOIN identitystore_identity_attribute attrcountry
                                               ON attrcountry.id_identity = attrcitycode.id_identity
                                                   AND attrcountry.id_attribute = attribute_id_birthcountry
                            WHERE attrcitycode.id_attribute = attribute_id_birthplace_code
                              AND TRIM(attrcitycode.attribute_value) <> ''
                              -- (écrit sans COALESCE pour que le planificateur estime bien le volume)
                              AND (idt.is_deleted = 0 OR idt.is_deleted IS NULL)
                              AND (idt.is_merged = 0 OR idt.is_merged IS NULL)
                              AND COALESCE(TRIM(attrcountrycode.attribute_value), '') = '')
        SELECT c.id_identity,
               c.customer_id::VARCHAR,
               c.birthplace_code::VARCHAR,
               c.birthcountry::VARCHAR,
               (CASE
                    WHEN cc.code IS NULL THEN 'UNKNOWN_CITY_CODE'
                    WHEN NOT c.label_empty
                        AND NORMALIZE_COUNTRY_LABEL(c.birthcountry) <> NORMALIZE_COUNTRY_LABEL(france_country_label)
                        THEN 'LABEL_CONFLICT'
                    ELSE 'TO_FIX'
                   END)::VARCHAR,
               c.code_row_exists,
               c.label_row_exists,
               -- libellé à écrire : absent, vide, ou FRANCE écrit autrement que france_country_label
               (c.label_empty OR c.birthcountry IS DISTINCT FROM france_country_label),
               c.label_empty,
               c.id_certification_source
        FROM candidates c
                 LEFT JOIN city_codes cc ON cc.code = c.birthplace_code;
END
$$
    LANGUAGE plpgsql;


----------------------------------------------------------
-- 2. Synthèse par statut
----------------------------------------------------------
DROP FUNCTION IF EXISTS COUNT_MISSING_COUNTRIES(INTEGER, INTEGER, INTEGER, VARCHAR, VARCHAR, INTEGER);
CREATE
    OR REPLACE FUNCTION COUNT_MISSING_COUNTRIES(attribute_id_birthplace_code INTEGER default 56,
                                                attribute_id_birthcountry INTEGER default 7,
                                                attribute_id_birthcountry_code INTEGER default 57,
                                                france_country_code VARCHAR DEFAULT '99100',
                                                france_country_label VARCHAR DEFAULT 'FRANCE',
                                                sample_size INTEGER DEFAULT 20)
    RETURNS TABLE
            (
                status              VARCHAR,
                count               BIGINT,
                count_certified     BIGINT,
                sample_customer_ids TEXT
            )
AS
$$
BEGIN
    RETURN QUERY
        SELECT m.status,
               COUNT(*),
               COUNT(*) FILTER (WHERE COALESCE(m.id_certification_source, 0) <> 0),
               ARRAY_TO_STRING((ARRAY_AGG(m.customer_id ORDER BY m.id_identity))[1:sample_size], ',')
        FROM GET_MISSING_COUNTRIES(attribute_id_birthplace_code,
                                   attribute_id_birthcountry,
                                   attribute_id_birthcountry_code,
                                   france_country_code,
                                   france_country_label) m
        GROUP BY m.status
        ORDER BY m.status;
END
$$
    LANGUAGE plpgsql;


----------------------------------------------------------
-- 3. Ajout de birthcountry_code = 99100 et birthcountry = FRANCE (identités TO_FIX)
----------------------------------------------------------
DROP PROCEDURE IF EXISTS FIX_MISSING_COUNTRIES(INTEGER, INTEGER, INTEGER, INTEGER, VARCHAR, VARCHAR, BOOLEAN, VARCHAR, VARCHAR);
CREATE
    OR REPLACE PROCEDURE FIX_MISSING_COUNTRIES(attribute_id_birthplace_code INTEGER default 56,
                                               attribute_id_birthcountry INTEGER default 7,
                                               attribute_id_birthcountry_code INTEGER default 57,
                                               batch_size INTEGER DEFAULT 1000,
                                               history_author_name VARCHAR DEFAULT 'script_fix_missing_countries',
                                               history_client_code VARCHAR DEFAULT 'direct_in_DB',
                                               touch_identity BOOLEAN DEFAULT FALSE,
                                               france_country_code VARCHAR DEFAULT '99100',
                                               france_country_label VARCHAR DEFAULT 'FRANCE')
AS
$$
DECLARE
    v_total         BIGINT;
    v_last_id       INTEGER := 0;
    v_batch_last_id INTEGER;
    v_nb_updated    BIGINT;
    v_nb_inserted   BIGINT;
    v_nb_history    BIGINT;
    v_nb_touched    BIGINT;
    v_nb_certified  BIGINT;
    v_sum_updated   BIGINT  := 0;
    v_sum_inserted  BIGINT  := 0;
    v_sum_history   BIGINT  := 0;
    v_sum_certified BIGINT  := 0;
    v_batch_num     INTEGER := 0;
    v_cert_seq      TEXT;
BEGIN
    IF batch_size IS NULL OR batch_size <= 0 THEN
        RAISE EXCEPTION 'batch_size doit être > 0 (reçu : %)', batch_size;
    END IF;

    -- Séquence des id de certification (colonne SERIAL / IDENTITY)
    v_cert_seq := PG_GET_SERIAL_SEQUENCE('identitystore_identity_attribute_certificate', 'id_attribute_certificate');
    IF v_cert_seq IS NULL THEN
        RAISE EXCEPTION 'Aucune séquence trouvée pour identitystore_identity_attribute_certificate.id_attribute_certificate';
    END IF;

    IF (SELECT COUNT(*)
        FROM identitystore_ref_attribute
        WHERE id_attribute IN (attribute_id_birthcountry, attribute_id_birthcountry_code)) <> 2 THEN
        RAISE EXCEPTION 'Attributs % / % introuvables dans identitystore_ref_attribute',
            attribute_id_birthcountry, attribute_id_birthcountry_code;
    END IF;

    -- Candidats calculés une seule fois, puis traités par tranches d'id_identity
    DROP TABLE IF EXISTS tmp_fix_missing_countries;
    CREATE TEMP TABLE tmp_fix_missing_countries AS
    SELECT m.id_identity,
           m.code_row_exists,
           m.label_row_exists,
           m.label_to_write,
           m.label_empty,
           m.id_certification_source
    FROM GET_MISSING_COUNTRIES(attribute_id_birthplace_code,
                               attribute_id_birthcountry,
                               attribute_id_birthcountry_code,
                               france_country_code,
                               france_country_label) m
    WHERE m.status = 'TO_FIX';
    ALTER TABLE tmp_fix_missing_countries ADD PRIMARY KEY (id_identity);

    SELECT COUNT(*) INTO v_total FROM tmp_fix_missing_countries;
    RAISE NOTICE '% identités à corriger (batch de %)', v_total, batch_size;
    COMMIT;

    LOOP
        WITH batch AS (SELECT t.*
                       FROM tmp_fix_missing_countries t
                       WHERE t.id_identity > v_last_id
                       ORDER BY t.id_identity
                       LIMIT batch_size),
             -- Attributs à écrire : toujours le code pays ; le libellé s'il est absent, vide,
             -- ou à remettre en majuscules (fix_case : la certification existante est conservée)
             targets AS (SELECT b.id_identity,
                                attribute_id_birthcountry_code AS id_attribute,
                                france_country_code            AS attribute_value,
                                b.code_row_exists              AS row_exists,
                                FALSE                          AS fix_case,
                                b.id_certification_source
                         FROM batch b
                         UNION ALL
                         SELECT b.id_identity,
                                attribute_id_birthcountry,
                                france_country_label,
                                b.label_row_exists,
                                b.label_row_exists AND NOT b.label_empty,
                                b.id_certification_source
                         FROM batch b
                         WHERE b.label_to_write),
             -- Re-vérifie l'état de chaque attribut (modifié entre-temps : ignoré, évite une
             -- certification orpheline) et réserve un id de certification par attribut écrit
             -- quand le code commune est certifié. MATERIALIZED : nextval évalué une seule fois.
             to_write AS MATERIALIZED (SELECT t.id_identity,
                                              t.id_attribute,
                                              t.attribute_value,
                                              t.row_exists,
                                              t.fix_case,
                                              src.certifier_code,
                                              src.certificate_date,
                                              src.expiration_date,
                                              CASE
                                                  WHEN src.id_attribute_certificate IS NOT NULL
                                                      THEN NEXTVAL(v_cert_seq)::INTEGER
                                                  END AS new_id_certification
                                       FROM targets t
                                                LEFT JOIN identitystore_identity_attribute_certificate src
                                                          ON src.id_attribute_certificate = t.id_certification_source
                                                              AND t.id_certification_source <> 0
                                                              AND NOT t.fix_case
                                       WHERE CASE
                                                 WHEN t.fix_case THEN EXISTS (SELECT 1
                                                                              FROM identitystore_identity_attribute ex
                                                                              WHERE ex.id_identity = t.id_identity
                                                                                AND ex.id_attribute = t.id_attribute
                                                                                AND ex.attribute_value <> t.attribute_value
                                                                                AND NORMALIZE_COUNTRY_LABEL(ex.attribute_value)
                                                                                    = NORMALIZE_COUNTRY_LABEL(t.attribute_value))
                                                 WHEN t.row_exists THEN EXISTS (SELECT 1
                                                                                FROM identitystore_identity_attribute ex
                                                                                WHERE ex.id_identity = t.id_identity
                                                                                  AND ex.id_attribute = t.id_attribute
                                                                                  AND COALESCE(TRIM(ex.attribute_value), '') = '')
                                                 ELSE NOT EXISTS (SELECT 1
                                                                  FROM identitystore_identity_attribute ex
                                                                  WHERE ex.id_identity = t.id_identity
                                                                    AND ex.id_attribute = t.id_attribute)
                                                 END),
             -- Copie de la certification du code commune pour chaque attribut écrit
             certified AS (
                 INSERT INTO identitystore_identity_attribute_certificate (id_attribute_certificate, certifier_code,
                                                                           certificate_date, expiration_date)
                     SELECT w.new_id_certification, w.certifier_code, w.certificate_date, w.expiration_date
                     FROM to_write w
                     WHERE w.new_id_certification IS NOT NULL
                     RETURNING id_attribute_certificate),
             -- Attribut existant vide : complété ; libellé FRANCE mal écrit : remis en majuscules
             updated AS (
                 UPDATE identitystore_identity_attribute attr
                     SET attribute_value = w.attribute_value,
                         id_certification = CASE
                                                WHEN w.fix_case THEN attr.id_certification
                                                ELSE COALESCE(w.new_id_certification, 0) END,
                         lastupdate_date = NOW(),
                         lastupdate_client = history_client_code
                     FROM to_write w
                     WHERE w.row_exists
                         AND attr.id_identity = w.id_identity
                         AND attr.id_attribute = w.id_attribute
                         AND (CASE
                                  WHEN w.fix_case THEN attr.attribute_value <> w.attribute_value
                                      AND NORMALIZE_COUNTRY_LABEL(attr.attribute_value)
                                          = NORMALIZE_COUNTRY_LABEL(w.attribute_value)
                                  ELSE COALESCE(TRIM(attr.attribute_value), '') = '' END)
                     RETURNING attr.id_identity, attr.id_attribute, attr.attribute_value, attr.id_certification),
             -- Attribut absent : créé
             inserted AS (
                 INSERT INTO identitystore_identity_attribute (id_identity, id_attribute, attribute_value,
                                                               id_certification, id_file, lastupdate_date,
                                                               lastupdate_client)
                     SELECT w.id_identity, w.id_attribute, w.attribute_value,
                            COALESCE(w.new_id_certification, 0), 0, NOW(), history_client_code
                     FROM to_write w
                     WHERE NOT w.row_exists
                     ON CONFLICT (id_identity, id_attribute) DO NOTHING
                     RETURNING id_identity, id_attribute, attribute_value),
             -- Les certifications créées dans cette requête ne sont pas visibles par une jointure
             -- sur la table : on reprend leurs valeurs depuis to_write.
             -- (libellé remis en majuscules : certification existante, lue dans la table)
             changes AS (SELECT 1 AS change_type, 'updated' AS change_status, u.id_identity, u.id_attribute,
                                u.attribute_value,
                                COALESCE(w.certifier_code, oldcert.certifier_code)     AS certifier_code,
                                COALESCE(w.certificate_date, oldcert.certificate_date) AS certificate_date
                         FROM updated u
                                  JOIN to_write w ON w.id_identity = u.id_identity AND w.id_attribute = u.id_attribute
                                  LEFT JOIN identitystore_identity_attribute_certificate oldcert
                                            ON w.fix_case
                                                AND oldcert.id_attribute_certificate = u.id_certification
                         UNION ALL
                         SELECT 0, 'created', i.id_identity, i.id_attribute,
                                i.attribute_value, w.certifier_code, w.certificate_date
                         FROM inserted i
                                  JOIN to_write w ON w.id_identity = i.id_identity AND w.id_attribute = i.id_attribute),
             -- Une ligne d'historique par attribut écrit
             history AS (
                 INSERT INTO identitystore_identity_attribute_history (change_type, change_satus, change_message,
                                                                       author_type, author_name, client_code,
                                                                       id_identity, attribute_key, attribute_value,
                                                                       certification_process, certification_date,
                                                                       modification_date)
                     SELECT c.change_type, c.change_status, NULL, 'application', history_author_name,
                            history_client_code, c.id_identity, ra.key_name, c.attribute_value,
                            c.certifier_code, c.certificate_date, NOW()
                     FROM changes c
                              JOIN identitystore_ref_attribute ra ON ra.id_attribute = c.id_attribute
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

        RAISE NOTICE 'Batch % (jusqu''à id_identity %) : % attributs complétés, % créés, % certifications, % historisés',
            v_batch_num, v_last_id, v_nb_updated, v_nb_inserted, v_nb_certified, v_nb_history;
    END LOOP;

    DROP TABLE IF EXISTS tmp_fix_missing_countries;

    RAISE NOTICE 'Terminé : % attributs complétés, % créés (% certifications copiées), % lignes d''historique (% identités candidates)',
        v_sum_updated, v_sum_inserted, v_sum_certified, v_sum_history, v_total;
END
$$
    LANGUAGE plpgsql;
