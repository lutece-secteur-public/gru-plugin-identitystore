----------------------------------------------------------
-- Contrôle du format des dates de naissance (birthdate) : format attendu JJ/MM/AAAA (ex : 01/01/2000)
--
-- Usage (PostgreSQL) :
--
--   -- 1. Synthèse par type de problème (+ ligne TOTAL) : nombre de valeurs, nombre corrigeables,
--   --    échantillon "valeur -> correction proposée" et échantillon de customer_id
--   SELECT * FROM COUNT_MALFORMED_BIRTHDATES();            -- attribut 5 par défaut
--   SELECT * FROM COUNT_MALFORMED_BIRTHDATES(5, 10);       -- échantillons de 10
--
--   -- 2. Détail, identité par identité (avec la correction proposée)
--   SELECT * FROM GET_MALFORMED_BIRTHDATES(5) WHERE fixed_value IS NOT NULL;   -- corrigeables
--   SELECT * FROM GET_MALFORMED_BIRTHDATES(5) WHERE issue = 'UNKNOWN_FORMAT';
--
--   -- 3. Test unitaire d'une valeur
--   SELECT * FROM CHECK_BIRTHDATE_FORMAT('1980-02-01');
--
--   -- 4. Correction par batch des valeurs corrigeables (+ ligne d'historique par date corrigée)
--   --    en autocommit (la procédure fait des COMMIT par batch)
--   CALL UPDATE_MALFORMED_BIRTHDATES();                                          -- tous types
--   CALL UPDATE_MALFORMED_BIRTHDATES(5, 1000, issues => ARRAY['ISO_FORMAT']);    -- un seul type
--
-- Seules les identités ni supprimées ni fusionnées sont prises en compte.
--
-- Types de problème (issue) :
--   EMPTY                valeur NULL ou vide                                   -> non corrigeable
--   WHITESPACES          bon format mais espaces avant / après                 -> corrigeable
--   WRONG_SEPARATOR      séparateur autre que '/' : 01-02-1980, 01.02.1980     -> corrigeable
--   MISSING_LEADING_ZERO jour ou mois sur 1 chiffre : 1/2/1980                 -> corrigeable
--   ISO_FORMAT           AAAA-MM-JJ : 1980-02-01                               -> corrigeable
--   WITH_TIME            date suivie d'une heure : 01/02/1980 00:00:00,
--                        1980-02-01T00:00:00Z                                  -> corrigeable
--   NO_SEPARATOR         JJMMAAAA ou AAAAMMJJ : 01021980, 19800201             -> corrigeable si non ambigu
--   TEXT_MONTH           mois en toutes lettres : 1er février 1980             -> corrigeable
--   TWO_DIGIT_YEAR       année sur 2 chiffres : 01/02/80                       -> corrigeable
--                        (année > 26 -> 19xx : 01/02/80 -> 01/02/1980 ; sinon 20xx : 01/02/25 -> 01/02/2025)
--   INVALID_DATE         date inexistante : 31/02/1980, 00/01/1980             -> non corrigeable
--                        (le commentaire signale un éventuel jour/mois inversé : 12/25/1980)
--   OUT_OF_RANGE         bon format mais année < min_year (1900) ou date future -> non corrigeable
--   UNKNOWN_FORMAT       format non reconnu                                    -> non corrigeable
-- Une correction proposée n'est retenue que si la date obtenue existe et est dans les bornes
-- (sinon fixed_value est NULL et comment explique pourquoi).
----------------------------------------------------------

----------------------------------------------------------
-- Construit une date à partir de jour / mois / année, NULL si elle n'existe pas
-- (sans bloc EXCEPTION, pour rester rapide sur un grand volume)
----------------------------------------------------------
CREATE
    OR REPLACE FUNCTION BIRTHDATE_TRY_MAKE(d INTEGER, m INTEGER, y INTEGER)
    RETURNS DATE
AS
$$
BEGIN
    IF d IS NULL OR m IS NULL OR y IS NULL OR y < 1 OR y > 9999 OR m < 1 OR m > 12 OR d < 1 THEN
        RETURN NULL;
    END IF;
    IF d > EXTRACT(DAY FROM (MAKE_DATE(y, m, 1) + INTERVAL '1 month - 1 day')) THEN
        RETURN NULL;
    END IF;
    RETURN MAKE_DATE(y, m, d);
END
$$
    LANGUAGE plpgsql
    IMMUTABLE
    PARALLEL SAFE;

----------------------------------------------------------
-- Analyse une valeur de date de naissance.
--   issue       : NULL si la valeur est correcte, sinon le type de problème (voir en-tête)
--   fixed_value : la valeur corrigée au format JJ/MM/AAAA, NULL si non corrigeable
--   comment     : précision (raison de non correction, correction possible à vérifier...)
----------------------------------------------------------
DROP FUNCTION IF EXISTS CHECK_BIRTHDATE_FORMAT(TEXT, INTEGER);
CREATE
    OR REPLACE FUNCTION CHECK_BIRTHDATE_FORMAT(val TEXT,
                                               min_year INTEGER DEFAULT 1900,
                                               OUT issue VARCHAR,
                                               OUT fixed_value VARCHAR,
                                               OUT comment VARCHAR)
AS
$$
DECLARE
    v_value  TEXT;
    v_parts  TEXT[];
    v_date   DATE;
    v_other  DATE;
    v_month  INTEGER;
    -- Pivot des années sur 2 chiffres : 27..99 -> 1927..1999, 00..26 -> 2000..2026
    v_year_pivot CONSTANT INTEGER := 26;
    v_months TEXT[] := ARRAY ['janvier', 'fevrier', 'mars', 'avril', 'mai', 'juin', 'juillet',
                              'aout', 'septembre', 'octobre', 'novembre', 'decembre'];
BEGIN
    -- Cas nominal (la grande majorité) : JJ/MM/AAAA
    IF val ~ '^\d{2}/\d{2}/\d{4}$' THEN
        v_date := BIRTHDATE_TRY_MAKE(SUBSTR(val, 1, 2)::INTEGER, SUBSTR(val, 4, 2)::INTEGER, SUBSTR(val, 7, 4)::INTEGER);
        IF v_date IS NULL THEN
            issue := 'INVALID_DATE';
            comment := 'date inexistante';
            v_other := BIRTHDATE_TRY_MAKE(SUBSTR(val, 4, 2)::INTEGER, SUBSTR(val, 1, 2)::INTEGER,
                                          SUBSTR(val, 7, 4)::INTEGER);
            IF v_other IS NOT NULL THEN
                comment := 'date inexistante (jour/mois inversés ? ' || TO_CHAR(v_other, 'DD/MM/YYYY') || ')';
            END IF;
        ELSIF EXTRACT(YEAR FROM v_date) < min_year OR v_date > CURRENT_DATE THEN
            issue := 'OUT_OF_RANGE';
            comment := 'année < ' || min_year || ' ou date future';
        END IF;
        RETURN;
    END IF;

    IF val IS NULL OR BTRIM(val) = '' THEN
        issue := 'EMPTY';
        comment := 'valeur vide';
        RETURN;
    END IF;

    v_value := REGEXP_REPLACE(val, '^\s+|\s+$', '', 'g');

    -- Heure en fin de valeur : supprimée, puis analyse de la partie date
    IF v_value ~ '^\S+[ T]\d{1,2}:\d{2}(:\d{2}(\.\d+)?)?\s*(Z|[+-]\d{2}(:?\d{2})?)?$' THEN
        issue := 'WITH_TIME';
        v_value := SUBSTRING(v_value FROM '^(\S+)[ T]');
    END IF;

    IF v_value ~ '^\d{2}/\d{2}/\d{4}$' THEN
        issue := COALESCE(issue, 'WHITESPACES');
        v_date := BIRTHDATE_TRY_MAKE(SUBSTR(v_value, 1, 2)::INTEGER, SUBSTR(v_value, 4, 2)::INTEGER,
                                     SUBSTR(v_value, 7, 4)::INTEGER);

    ELSIF v_value ~ '^\d{1,2}[/. -]\d{1,2}[/. -]\d{4}$' THEN
        v_parts := REGEXP_MATCH(v_value, '^(\d{1,2})[/. -](\d{1,2})[/. -](\d{4})$');
        issue := COALESCE(issue, CASE
                                     WHEN LENGTH(v_parts[1]) = 1 OR LENGTH(v_parts[2]) = 1 THEN 'MISSING_LEADING_ZERO'
                                     ELSE 'WRONG_SEPARATOR' END);
        v_date := BIRTHDATE_TRY_MAKE(v_parts[1]::INTEGER, v_parts[2]::INTEGER, v_parts[3]::INTEGER);

    ELSIF v_value ~ '^\d{4}[/. -]\d{1,2}[/. -]\d{1,2}$' THEN
        v_parts := REGEXP_MATCH(v_value, '^(\d{4})[/. -](\d{1,2})[/. -](\d{1,2})$');
        issue := COALESCE(issue, 'ISO_FORMAT');
        v_date := BIRTHDATE_TRY_MAKE(v_parts[3]::INTEGER, v_parts[2]::INTEGER, v_parts[1]::INTEGER);

    ELSIF v_value ~ '^\d{8}$' THEN
        issue := COALESCE(issue, 'NO_SEPARATOR');
        -- JJMMAAAA ou AAAAMMJJ : retenu seulement si une seule lecture donne une date valide dans les bornes
        v_date := BIRTHDATE_TRY_MAKE(SUBSTR(v_value, 1, 2)::INTEGER, SUBSTR(v_value, 3, 2)::INTEGER,
                                     SUBSTR(v_value, 5, 4)::INTEGER);
        v_other := BIRTHDATE_TRY_MAKE(SUBSTR(v_value, 7, 2)::INTEGER, SUBSTR(v_value, 5, 2)::INTEGER,
                                      SUBSTR(v_value, 1, 4)::INTEGER);
        IF v_date IS NOT NULL AND (EXTRACT(YEAR FROM v_date) < min_year OR v_date > CURRENT_DATE) THEN
            v_date := NULL;
        END IF;
        IF v_other IS NOT NULL AND (EXTRACT(YEAR FROM v_other) < min_year OR v_other > CURRENT_DATE) THEN
            v_other := NULL;
        END IF;
        IF v_date IS NOT NULL AND v_other IS NOT NULL AND v_date <> v_other THEN
            comment := 'ambigu : ' || TO_CHAR(v_date, 'DD/MM/YYYY') || ' ou ' || TO_CHAR(v_other, 'DD/MM/YYYY');
            RETURN;
        END IF;
        v_date := COALESCE(v_date, v_other);

    ELSIF v_value ~ '^\d{1,2}[/. -]\d{1,2}[/. -]\d{2}$' THEN
        -- Année sur 2 chiffres : > v_year_pivot -> 19xx, sinon 20xx
        v_parts := REGEXP_MATCH(v_value, '^(\d{1,2})[/. -](\d{1,2})[/. -](\d{2})$');
        issue := COALESCE(issue, 'TWO_DIGIT_YEAR');
        v_date := BIRTHDATE_TRY_MAKE(v_parts[1]::INTEGER, v_parts[2]::INTEGER,
                                     CASE
                                         WHEN v_parts[3]::INTEGER > v_year_pivot THEN 1900
                                         ELSE 2000 END + v_parts[3]::INTEGER);

    ELSE
        -- Mois en toutes lettres (français, accents et casse ignorés) : "1er février 1980", "01 fevrier 1980"
        v_parts := REGEXP_MATCH(
                TRANSLATE(LOWER(v_value), 'éèêëûüùôöîïâàç', 'eeeeuuuooiiaac'),
                '^(\d{1,2})(er)?\s+([a-z]+)\.?\s+(\d{4})$');
        v_month := ARRAY_POSITION(v_months, v_parts[3]);
        IF v_parts IS NOT NULL AND v_month IS NOT NULL THEN
            issue := COALESCE(issue, 'TEXT_MONTH');
            v_date := BIRTHDATE_TRY_MAKE(v_parts[1]::INTEGER, v_month, v_parts[4]::INTEGER);
        ELSE
            issue := COALESCE(issue, 'UNKNOWN_FORMAT');
            comment := 'format non reconnu';
            RETURN;
        END IF;
    END IF;

    -- Correction proposée : uniquement si la date existe et est dans les bornes
    IF v_date IS NULL THEN
        comment := COALESCE(comment, 'date inexistante');
    ELSIF EXTRACT(YEAR FROM v_date) < min_year OR v_date > CURRENT_DATE THEN
        comment := 'date corrigée hors bornes (année < ' || min_year || ' ou date future) : '
                       || TO_CHAR(v_date, 'DD/MM/YYYY');
    ELSE
        fixed_value := TO_CHAR(v_date, 'DD/MM/YYYY');
    END IF;
END
$$
    LANGUAGE plpgsql
    STABLE
    PARALLEL SAFE;

----------------------------------------------------------
-- Détail des dates de naissance mal formatées, identité par identité
----------------------------------------------------------
DROP FUNCTION IF EXISTS GET_MALFORMED_BIRTHDATES(INTEGER, INTEGER);
CREATE
    OR REPLACE FUNCTION GET_MALFORMED_BIRTHDATES(attribute_id_birthdate INTEGER DEFAULT 5,
                                                 min_year INTEGER DEFAULT 1900)
    RETURNS TABLE
            (
                id_identity     INTEGER,
                customer_id     VARCHAR,
                attribute_value VARCHAR,
                issue           VARCHAR,
                fixed_value     VARCHAR,
                comment         VARCHAR
            )
AS
$$
BEGIN
    RETURN QUERY
        SELECT attr.id_identity,
               idt.customer_id::VARCHAR,
               attr.attribute_value::VARCHAR,
               chk.issue,
               chk.fixed_value,
               chk.comment
        FROM identitystore_identity_attribute attr
                 JOIN identitystore_identity idt
                      ON idt.id_identity = attr.id_identity
                 CROSS JOIN LATERAL CHECK_BIRTHDATE_FORMAT(attr.attribute_value, min_year) chk
        WHERE attr.id_attribute = attribute_id_birthdate
          AND COALESCE(idt.is_deleted, 0) = 0
          AND COALESCE(idt.is_merged, 0) = 0
          AND chk.issue IS NOT NULL;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Synthèse par type de problème, avec une ligne TOTAL :
-- nombre de valeurs, nombre corrigeables / non corrigeables,
-- échantillon "valeur -> correction proposée" (? = non corrigeable) et échantillon de customer_id.
----------------------------------------------------------
DROP FUNCTION IF EXISTS COUNT_MALFORMED_BIRTHDATES(INTEGER, INTEGER, INTEGER);
CREATE
    OR REPLACE FUNCTION COUNT_MALFORMED_BIRTHDATES(attribute_id_birthdate INTEGER DEFAULT 5,
                                                   sample_size INTEGER DEFAULT 5,
                                                   min_year INTEGER DEFAULT 1900)
    RETURNS TABLE
            (
                issue               VARCHAR,
                count               BIGINT,
                count_fixable       BIGINT,
                count_not_fixable   BIGINT,
                sample_values       TEXT,
                sample_customer_ids TEXT
            )
AS
$$
BEGIN
    RETURN QUERY
        SELECT COALESCE(m.issue, 'TOTAL')::VARCHAR,
               COUNT(*),
               COUNT(*) FILTER (WHERE m.fixed_value IS NOT NULL),
               COUNT(*) FILTER (WHERE m.fixed_value IS NULL),
               ARRAY_TO_STRING((ARRAY_AGG(DISTINCT COALESCE(m.attribute_value, 'NULL') || ' -> '
                                                       || COALESCE(m.fixed_value, '?')))[1:sample_size], ' | '),
               ARRAY_TO_STRING((ARRAY_AGG(m.customer_id ORDER BY m.id_identity))[1:sample_size], ',')
        FROM GET_MALFORMED_BIRTHDATES(attribute_id_birthdate, min_year) m
        GROUP BY GROUPING SETS ((m.issue), ())
        ORDER BY GROUPING(m.issue), COUNT(*) DESC;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Correction par batch des dates de naissance mal formatées et corrigeables
-- (fixed_value de CHECK_BIRTHDATE_FORMAT), un COMMIT par batch de batch_size identités.
--   - la valeur n'est modifiée que si elle n'a pas changé depuis l'analyse ;
--   - la certification de l'attribut est conservée (seul le format change) ;
--   - une ligne d'historique par date corrigée (change_type=1, status 'updated'),
--     l'ancienne valeur et le type de problème sont conservés dans change_message ;
--   - touch_identity : met à jour identitystore_identity.last_update_date (réindexation / synchro).
-- issues : types de problème à corriger (NULL = tous les corrigeables), par exemple
--   ARRAY['ISO_FORMAT', 'WRONG_SEPARATOR'] pour ne corriger que ceux-là.
--
-- Usage :
--   CALL UPDATE_MALFORMED_BIRTHDATES();                       -- attribut 5, batch de 1000, tous types
--   CALL UPDATE_MALFORMED_BIRTHDATES(5, 5000);
--   CALL UPDATE_MALFORMED_BIRTHDATES(5, 1000, issues => ARRAY['ISO_FORMAT', 'WITH_TIME']);
--
-- À appeler par CALL hors transaction explicite (autocommit), sinon les COMMIT échouent.
----------------------------------------------------------
DROP PROCEDURE IF EXISTS UPDATE_MALFORMED_BIRTHDATES(INTEGER, INTEGER, VARCHAR, VARCHAR, BOOLEAN, INTEGER, VARCHAR[]);
CREATE
    OR REPLACE PROCEDURE UPDATE_MALFORMED_BIRTHDATES(attribute_id_birthdate INTEGER DEFAULT 5,
                                                     batch_size INTEGER DEFAULT 1000,
                                                     history_author_name VARCHAR DEFAULT 'script_fix_malformed_birthdates',
                                                     history_client_code VARCHAR DEFAULT 'direct_in_DB',
                                                     touch_identity BOOLEAN DEFAULT FALSE,
                                                     min_year INTEGER DEFAULT 1900,
                                                     issues VARCHAR[] DEFAULT NULL)
AS
$$
DECLARE
    v_attribute_key VARCHAR;
    v_total         BIGINT;
    v_last_id       INTEGER := 0;
    v_batch_last_id INTEGER;
    v_nb_updated    BIGINT;
    v_nb_history    BIGINT;
    v_nb_touched    BIGINT;
    v_sum_updated   BIGINT  := 0;
    v_sum_history   BIGINT  := 0;
    v_batch_num     INTEGER := 0;
BEGIN
    IF batch_size IS NULL OR batch_size <= 0 THEN
        RAISE EXCEPTION 'batch_size doit être > 0 (reçu : %)', batch_size;
    END IF;

    SELECT key_name
    INTO v_attribute_key
    FROM identitystore_ref_attribute
    WHERE id_attribute = attribute_id_birthdate;

    IF v_attribute_key IS NULL THEN
        RAISE EXCEPTION 'Attribut % introuvable dans identitystore_ref_attribute', attribute_id_birthdate;
    END IF;

    -- Candidats calculés une seule fois, puis traités par tranches d'id_identity
    DROP TABLE IF EXISTS tmp_fix_birthdates;
    CREATE TEMP TABLE tmp_fix_birthdates AS
    SELECT m.id_identity, m.attribute_value AS current_value, m.fixed_value, m.issue
    FROM GET_MALFORMED_BIRTHDATES(attribute_id_birthdate, min_year) m
    WHERE m.fixed_value IS NOT NULL
      AND (issues IS NULL OR m.issue = ANY (issues));
    ALTER TABLE tmp_fix_birthdates ADD PRIMARY KEY (id_identity);

    SELECT COUNT(*) INTO v_total FROM tmp_fix_birthdates;
    RAISE NOTICE '% dates de naissance à corriger (batch de %)', v_total, batch_size;
    COMMIT;

    LOOP
        WITH batch AS (SELECT t.id_identity, t.current_value, t.fixed_value, t.issue
                       FROM tmp_fix_birthdates t
                       WHERE t.id_identity > v_last_id
                       ORDER BY t.id_identity
                       LIMIT batch_size),
             -- Mise à jour seulement si la valeur n'a pas changé entre-temps
             updated AS (
                 UPDATE identitystore_identity_attribute attr
                     SET attribute_value = b.fixed_value,
                         lastupdate_date = NOW(),
                         lastupdate_client = history_client_code
                     FROM batch b
                     WHERE attr.id_identity = b.id_identity
                         AND attr.id_attribute = attribute_id_birthdate
                         AND attr.attribute_value IS NOT DISTINCT FROM b.current_value
                     RETURNING attr.id_identity, attr.attribute_value, attr.id_certification,
                         b.current_value, b.issue),
             -- Une ligne d'historique par date corrigée (ancienne valeur dans change_message)
             history AS (
                 INSERT INTO identitystore_identity_attribute_history (change_type, change_satus, change_message,
                                                                       author_type, author_name, client_code,
                                                                       id_identity, attribute_key, attribute_value,
                                                                       certification_process, certification_date,
                                                                       modification_date)
                     SELECT 1, 'updated',
                            LEFT('Reformatage ' || u.issue || ' - ancienne valeur : "' || u.current_value || '"', 255),
                            'application', history_author_name, history_client_code, u.id_identity,
                            v_attribute_key, u.attribute_value, cert.certifier_code, cert.certificate_date, NOW()
                     FROM updated u
                              LEFT JOIN identitystore_identity_attribute_certificate cert
                                        ON cert.id_attribute_certificate = u.id_certification
                     RETURNING id_identity),
             touched AS (
                 UPDATE identitystore_identity idt
                     SET last_update_date = NOW()
                     WHERE touch_identity
                         AND idt.id_identity IN (SELECT u.id_identity FROM updated u)
                     RETURNING idt.id_identity)
        SELECT (SELECT MAX(b.id_identity) FROM batch b),
               (SELECT COUNT(*) FROM updated),
               (SELECT COUNT(*) FROM history),
               (SELECT COUNT(*) FROM touched)
        INTO v_batch_last_id, v_nb_updated, v_nb_history, v_nb_touched;

        EXIT WHEN v_batch_last_id IS NULL;

        v_last_id := v_batch_last_id;
        v_batch_num := v_batch_num + 1;
        v_sum_updated := v_sum_updated + v_nb_updated;
        v_sum_history := v_sum_history + v_nb_history;

        COMMIT;

        RAISE NOTICE 'Batch % (jusqu''à id_identity %) : % corrigées, % historisées - cumul % / %',
            v_batch_num, v_last_id, v_nb_updated, v_nb_history, v_sum_updated, v_total;
    END LOOP;

    DROP TABLE IF EXISTS tmp_fix_birthdates;

    RAISE NOTICE 'Terminé : % dates corrigées, % lignes d''historique (% candidates)',
        v_sum_updated, v_sum_history, v_total;
END
$$
    LANGUAGE plpgsql;
