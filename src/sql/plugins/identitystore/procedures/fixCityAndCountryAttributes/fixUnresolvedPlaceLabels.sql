----------------------------------------------------------
----------------------------------------------------------
-- Libellés de pays / de communes de naissance pour lesquels aucun code n'a pu être trouvé
-- par les procédures de fixCountryAttributes.sql et fixCityAttributes.sql :
--   - listes regroupées par libellé,
--   - suppression de ces attributs libellé, avec une ligne d'historique par suppression.
--
-- PRÉREQUIS : fixCountryAttributes.sql et fixCityAttributes.sql chargés
--   (GET_MISSING_COUNTRY_CODES, GET_MISSING_CITY_CODES, NORMALIZE_COUNTRY_LABEL, NORMALIZE_CITY_LABEL).
--   Lancer d'abord les procédures de correction des codes (UPDATE_COUNTRY_CODE_BY_LIBELLE,
--   UPDATE_CITY_CODE_BY_LIBELLE) : ce qui reste ensuite est ce qui n'a pas pu être résolu.
--
-- Usage (procédures en autocommit : elles font des COMMIT par batch) :
--
--   -- 1. Libellés de pays sans code, non résolus, regroupés
--   SELECT * FROM LIST_UNRESOLVED_COUNTRY_LABELS();
--   -- 2. Libellés de communes (nés en France) sans code, non résolus, regroupés
--   SELECT * FROM LIST_UNRESOLVED_CITY_LABELS();
--
--   -- 3. Suppression des libellés de pays non résolus (+ historique)
--   CALL DELETE_UNRESOLVED_COUNTRY_LABELS();
--   CALL DELETE_UNRESOLVED_COUNTRY_LABELS(keep_known_labels => TRUE);   -- garde les libellés connus
--   -- 4. Suppression des libellés de communes non résolus (+ historique)
--   CALL DELETE_UNRESOLVED_CITY_LABELS();
--   CALL DELETE_UNRESOLVED_CITY_LABELS(keep_known_labels => TRUE);      -- garde les libellés connus
--
-- Statuts (resolution_status, repris des fonctions GET_MISSING_xxx_CODES) :
--   UNKNOWN_LABEL : aucun code ne correspond au libellé à la date de naissance
--   AMBIGUOUS     : plusieurs codes possibles
--   NO_BIRTHDATE  : pas de date de naissance exploitable (validité du code non vérifiable)
-- label_in_referential : TRUE si le libellé existe dans le référentiel geocodes, à n'importe
--   quelle date, parmi les entrées non dépréciées (deprecated = 0). Il s'agit alors d'un libellé
--   correct dont le code n'a pas pu être déduit (date manquante, homonymes...) : par défaut ces
--   libellés SONT supprimés aussi ; passer keep_known_labels => TRUE pour les conserver.
--
-- Suppression (même règle que l'application quand un attribut est vidé) :
--   - DELETE de la ligne identitystore_identity_attribute (si la valeur n'a pas changé entre-temps),
--   - historique : change_type = 1, change_satus = 'removed', attribute_value = valeur supprimée,
--   - la ligne de certification éventuelle n'est pas supprimée (comme l'application).
----------------------------------------------------------
----------------------------------------------------------


----------------------------------------------------------
-- 1. Libellés de pays de naissance sans code et non résolus, regroupés par libellé et statut
----------------------------------------------------------
DROP FUNCTION IF EXISTS LIST_UNRESOLVED_COUNTRY_LABELS(INTEGER, INTEGER, INTEGER, INTEGER);
CREATE
    OR REPLACE FUNCTION LIST_UNRESOLVED_COUNTRY_LABELS(attribute_id_birthcountry_code INTEGER default 57,
                                                       attribute_id_birthcountry INTEGER default 7,
                                                       attribute_id_birthdate INTEGER default 5,
                                                       sample_size INTEGER DEFAULT 5)
    RETURNS TABLE
            (
                label                VARCHAR,
                resolution_status    VARCHAR,
                label_in_referential BOOLEAN,
                count                BIGINT,
                sample_customer_ids  TEXT
            )
AS
$$
BEGIN
    RETURN QUERY
        WITH unresolved AS (SELECT m.birthcountry,
                                   m.resolution_status,
                                   COUNT(*)                                                                   AS nb,
                                   ARRAY_TO_STRING((ARRAY_AGG(m.customer_id ORDER BY m.id_identity))[1:sample_size],
                                                   ',')                                                       AS sample
                            FROM GET_MISSING_COUNTRY_CODES(attribute_id_birthcountry_code,
                                                           attribute_id_birthcountry,
                                                           attribute_id_birthdate) m
                            WHERE m.resolution_status <> 'RESOLVABLE'
                            GROUP BY m.birthcountry, m.resolution_status),
         -- Formes normalisées des libellés du référentiel, calculées une seule fois
         -- (au lieu d'un parcours complet de geocodes_country par libellé)
         ref_keys AS MATERIALIZED (SELECT DISTINCT NORMALIZE_COUNTRY_LABEL(g.value) AS country_key
                                   FROM geocodes_country g
                                   WHERE COALESCE(g.deprecated, 0) = 0
                                     AND NORMALIZE_COUNTRY_LABEL(g.value) <> '')
        SELECT u.birthcountry::VARCHAR,
               u.resolution_status::VARCHAR,
               (rk.country_key IS NOT NULL),
               u.nb,
               u.sample
        FROM unresolved u
                 LEFT JOIN ref_keys rk ON rk.country_key = NORMALIZE_COUNTRY_LABEL(u.birthcountry)
        ORDER BY u.nb DESC, u.birthcountry;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- 2. Libellés de communes de naissance (identités nées en France) sans code et non résolus,
-- regroupés par libellé et statut
----------------------------------------------------------
DROP FUNCTION IF EXISTS LIST_UNRESOLVED_CITY_LABELS(INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, VARCHAR);
CREATE
    OR REPLACE FUNCTION LIST_UNRESOLVED_CITY_LABELS(attribute_id_birthplace_code INTEGER default 56,
                                                    attribute_id_birthplace INTEGER default 6,
                                                    attribute_id_birthcountry_code INTEGER default 57,
                                                    attribute_id_birthdate INTEGER default 5,
                                                    sample_size INTEGER DEFAULT 5,
                                                    france_country_code VARCHAR DEFAULT '99100')
    RETURNS TABLE
            (
                label                VARCHAR,
                resolution_status    VARCHAR,
                label_in_referential BOOLEAN,
                count                BIGINT,
                sample_customer_ids  TEXT
            )
AS
$$
BEGIN
    RETURN QUERY
        WITH unresolved AS (SELECT m.birthplace,
                                   m.resolution_status,
                                   COUNT(*)                                                                   AS nb,
                                   ARRAY_TO_STRING((ARRAY_AGG(m.customer_id ORDER BY m.id_identity))[1:sample_size],
                                                   ',')                                                       AS sample
                            FROM GET_MISSING_CITY_CODES(attribute_id_birthplace_code,
                                                        attribute_id_birthplace,
                                                        attribute_id_birthcountry_code,
                                                        attribute_id_birthdate,
                                                        france_country_code) m
                            WHERE m.resolution_status <> 'RESOLVABLE'
                            GROUP BY m.birthplace, m.resolution_status),
         -- Formes normalisées des libellés du référentiel, calculées une seule fois
         -- (au lieu d'un parcours complet de geocodes_city par libellé)
         ref_keys AS MATERIALIZED (SELECT DISTINCT k.city_key
                                   FROM geocodes_city g
                                            CROSS JOIN LATERAL (VALUES (NORMALIZE_CITY_LABEL(g.value)),
                                                                       (NORMALIZE_CITY_LABEL(g.value_min)),
                                                                       (NORMALIZE_CITY_LABEL(g.value_min_complete))) k(city_key)
                                   WHERE g.code_country = france_country_code
                                     AND COALESCE(g.deprecated, 0) = 0
                                     AND k.city_key IS NOT NULL)
        SELECT u.birthplace::VARCHAR,
               u.resolution_status::VARCHAR,
               (rk.city_key IS NOT NULL),
               u.nb,
               u.sample
        FROM unresolved u
                 LEFT JOIN ref_keys rk ON rk.city_key = NORMALIZE_CITY_LABEL(u.birthplace)
        ORDER BY u.nb DESC, u.birthplace;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Suppression par batch des attributs listés dans la table temporaire tmp_delete_labels
-- (id_identity, attribute_value), pour l'attribut attribute_id, avec une ligne d'historique
-- par suppression. Procédure interne, appelée par DELETE_UNRESOLVED_COUNTRY_LABELS et
-- DELETE_UNRESOLVED_CITY_LABELS.
----------------------------------------------------------
DROP PROCEDURE IF EXISTS DELETE_ATTRIBUTES_FROM_TMP(INTEGER, INTEGER, VARCHAR, VARCHAR, VARCHAR, BOOLEAN);
CREATE
    OR REPLACE PROCEDURE DELETE_ATTRIBUTES_FROM_TMP(attribute_id INTEGER,
                                                    batch_size INTEGER,
                                                    history_author_name VARCHAR,
                                                    history_client_code VARCHAR,
                                                    history_message VARCHAR,
                                                    touch_identity BOOLEAN)
AS
$$
DECLARE
    v_attribute_key VARCHAR;
    v_total         BIGINT;
    v_last_id       INTEGER := 0;
    v_batch_last_id INTEGER;
    v_nb_deleted    BIGINT;
    v_nb_history    BIGINT;
    v_nb_touched    BIGINT;
    v_sum_deleted   BIGINT  := 0;
    v_sum_history   BIGINT  := 0;
    v_batch_num     INTEGER := 0;
BEGIN
    IF batch_size IS NULL OR batch_size <= 0 THEN
        RAISE EXCEPTION 'batch_size doit être > 0 (reçu : %)', batch_size;
    END IF;

    SELECT key_name
    INTO v_attribute_key
    FROM identitystore_ref_attribute
    WHERE id_attribute = attribute_id;

    IF v_attribute_key IS NULL THEN
        RAISE EXCEPTION 'Attribut % introuvable dans identitystore_ref_attribute', attribute_id;
    END IF;

    SELECT COUNT(*) INTO v_total FROM tmp_delete_labels;
    RAISE NOTICE '% attributs % à supprimer (batch de %)', v_total, v_attribute_key, batch_size;
    COMMIT;

    LOOP
        WITH batch AS (SELECT t.id_identity, t.attribute_value
                       FROM tmp_delete_labels t
                       WHERE t.id_identity > v_last_id
                       ORDER BY t.id_identity
                       LIMIT batch_size),
             -- Suppression seulement si la valeur n'a pas changé depuis l'analyse
             deleted AS (
                 DELETE FROM identitystore_identity_attribute attr
                     USING batch b
                     WHERE attr.id_identity = b.id_identity
                         AND attr.id_attribute = attribute_id
                         AND attr.attribute_value IS NOT DISTINCT FROM b.attribute_value
                     RETURNING attr.id_identity, attr.attribute_value, attr.id_certification),
             -- Une ligne d'historique par suppression (statut 'removed', comme l'application)
             history AS (
                 INSERT INTO identitystore_identity_attribute_history (change_type, change_satus, change_message,
                                                                       author_type, author_name, client_code,
                                                                       id_identity, attribute_key, attribute_value,
                                                                       certification_process, certification_date,
                                                                       modification_date)
                     SELECT 1, 'removed', LEFT(history_message, 255), 'application', history_author_name,
                            history_client_code, d.id_identity, v_attribute_key, d.attribute_value,
                            cert.certifier_code, cert.certificate_date, NOW()
                     FROM deleted d
                              LEFT JOIN identitystore_identity_attribute_certificate cert
                                        ON cert.id_attribute_certificate = d.id_certification
                     RETURNING id_identity),
             touched AS (
                 UPDATE identitystore_identity idt
                     SET last_update_date = NOW()
                     WHERE touch_identity
                         AND idt.id_identity IN (SELECT d.id_identity FROM deleted d)
                     RETURNING idt.id_identity)
        SELECT (SELECT MAX(b.id_identity) FROM batch b),
               (SELECT COUNT(*) FROM deleted),
               (SELECT COUNT(*) FROM history),
               (SELECT COUNT(*) FROM touched)
        INTO v_batch_last_id, v_nb_deleted, v_nb_history, v_nb_touched;

        EXIT WHEN v_batch_last_id IS NULL;

        v_last_id := v_batch_last_id;
        v_batch_num := v_batch_num + 1;
        v_sum_deleted := v_sum_deleted + v_nb_deleted;
        v_sum_history := v_sum_history + v_nb_history;

        COMMIT;

        RAISE NOTICE 'Batch % (jusqu''à id_identity %) : % supprimés, % historisés - cumul % / %',
            v_batch_num, v_last_id, v_nb_deleted, v_nb_history, v_sum_deleted, v_total;
    END LOOP;

    DROP TABLE IF EXISTS tmp_delete_labels;

    RAISE NOTICE 'Terminé : % attributs % supprimés, % lignes d''historique (% candidats)',
        v_sum_deleted, v_attribute_key, v_sum_history, v_total;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- 3. Supprime les libellés de pays de naissance (birthcountry) des identités sans code pays
-- pour lesquelles aucun code n'a pu être trouvé, avec une ligne d'historique par suppression.
--   statuses          : statuts à supprimer (défaut : tous les non résolus)
--   keep_known_labels : FALSE (défaut) = supprime aussi les libellés qui existent dans geocodes_country
--                       (non dépréciés) ; TRUE = les conserve (ex : ESPAGNE sans date de naissance)
--
-- Usage :
--   CALL DELETE_UNRESOLVED_COUNTRY_LABELS();
--   CALL DELETE_UNRESOLVED_COUNTRY_LABELS(keep_known_labels => TRUE);
--   CALL DELETE_UNRESOLVED_COUNTRY_LABELS(statuses => ARRAY['UNKNOWN_LABEL']);
--
-- À appeler par CALL hors transaction explicite (autocommit), sinon les COMMIT échouent.
----------------------------------------------------------
DROP PROCEDURE IF EXISTS DELETE_UNRESOLVED_COUNTRY_LABELS(INTEGER, INTEGER, INTEGER, INTEGER, VARCHAR[], BOOLEAN, VARCHAR, VARCHAR, BOOLEAN);
CREATE
    OR REPLACE PROCEDURE DELETE_UNRESOLVED_COUNTRY_LABELS(attribute_id_birthcountry_code INTEGER default 57,
                                                          attribute_id_birthcountry INTEGER default 7,
                                                          attribute_id_birthdate INTEGER default 5,
                                                          batch_size INTEGER DEFAULT 1000,
                                                          statuses VARCHAR[] DEFAULT ARRAY ['UNKNOWN_LABEL', 'AMBIGUOUS', 'NO_BIRTHDATE'],
                                                          keep_known_labels BOOLEAN DEFAULT FALSE,
                                                          history_author_name VARCHAR DEFAULT 'script_delete_unresolved_country_labels',
                                                          history_client_code VARCHAR DEFAULT 'direct_in_DB',
                                                          touch_identity BOOLEAN DEFAULT FALSE)
AS
$$
BEGIN
    DROP TABLE IF EXISTS tmp_delete_labels;
    CREATE TEMP TABLE tmp_delete_labels AS
    WITH unresolved AS (SELECT m.id_identity, m.birthcountry
                        FROM GET_MISSING_COUNTRY_CODES(attribute_id_birthcountry_code,
                                                       attribute_id_birthcountry,
                                                       attribute_id_birthdate) m
                        WHERE m.resolution_status = ANY (statuses)),
         -- Formes normalisées des libellés du référentiel, calculées une seule fois
         -- (au lieu d'un parcours complet de geocodes_country par libellé)
         ref_keys AS MATERIALIZED (SELECT DISTINCT NORMALIZE_COUNTRY_LABEL(g.value) AS country_key
                                   FROM geocodes_country g
                                   WHERE COALESCE(g.deprecated, 0) = 0
                                     AND NORMALIZE_COUNTRY_LABEL(g.value) <> ''),
         -- libellés présents dans le référentiel (jointure hash par libellé distinct)
         known AS (SELECT l.birthcountry
                   FROM (SELECT DISTINCT u.birthcountry FROM unresolved u) l
                            JOIN ref_keys rk ON rk.country_key = NORMALIZE_COUNTRY_LABEL(l.birthcountry))
    SELECT u.id_identity, u.birthcountry::TEXT AS attribute_value
    FROM unresolved u
    WHERE NOT keep_known_labels
       OR NOT EXISTS (SELECT 1 FROM known k WHERE k.birthcountry = u.birthcountry);
    ALTER TABLE tmp_delete_labels ADD PRIMARY KEY (id_identity);

    CALL DELETE_ATTRIBUTES_FROM_TMP(attribute_id_birthcountry, batch_size, history_author_name, history_client_code,
                                    'Suppression du libellé de pays sans code correspondant', touch_identity);
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- 4. Supprime les libellés de communes de naissance (birthplace) des identités nées en France
-- sans code commune, pour lesquelles aucun code n'a pu être trouvé, avec une ligne d'historique
-- par suppression.
--   statuses          : statuts à supprimer (défaut : tous les non résolus)
--   keep_known_labels : FALSE (défaut) = supprime aussi les libellés qui existent dans geocodes_city
--                       (communes non dépréciées du pays france_country_code) ; TRUE = les conserve
--
-- Usage :
--   CALL DELETE_UNRESOLVED_CITY_LABELS();
--   CALL DELETE_UNRESOLVED_CITY_LABELS(keep_known_labels => TRUE);
--   CALL DELETE_UNRESOLVED_CITY_LABELS(statuses => ARRAY['UNKNOWN_LABEL']);
--
-- À appeler par CALL hors transaction explicite (autocommit), sinon les COMMIT échouent.
----------------------------------------------------------
DROP PROCEDURE IF EXISTS DELETE_UNRESOLVED_CITY_LABELS(INTEGER, INTEGER, INTEGER, INTEGER, INTEGER, VARCHAR[], BOOLEAN, VARCHAR, VARCHAR, BOOLEAN, VARCHAR);
CREATE
    OR REPLACE PROCEDURE DELETE_UNRESOLVED_CITY_LABELS(attribute_id_birthplace_code INTEGER default 56,
                                                       attribute_id_birthplace INTEGER default 6,
                                                       attribute_id_birthcountry_code INTEGER default 57,
                                                       attribute_id_birthdate INTEGER default 5,
                                                       batch_size INTEGER DEFAULT 1000,
                                                       statuses VARCHAR[] DEFAULT ARRAY ['UNKNOWN_LABEL', 'AMBIGUOUS', 'NO_BIRTHDATE'],
                                                       keep_known_labels BOOLEAN DEFAULT FALSE,
                                                       history_author_name VARCHAR DEFAULT 'script_delete_unresolved_city_labels',
                                                       history_client_code VARCHAR DEFAULT 'direct_in_DB',
                                                       touch_identity BOOLEAN DEFAULT FALSE,
                                                       france_country_code VARCHAR DEFAULT '99100')
AS
$$
BEGIN
    DROP TABLE IF EXISTS tmp_delete_labels;
    CREATE TEMP TABLE tmp_delete_labels AS
    WITH unresolved AS (SELECT m.id_identity, m.birthplace
                        FROM GET_MISSING_CITY_CODES(attribute_id_birthplace_code,
                                                    attribute_id_birthplace,
                                                    attribute_id_birthcountry_code,
                                                    attribute_id_birthdate,
                                                    france_country_code) m
                        WHERE m.resolution_status = ANY (statuses)),
         -- Formes normalisées des libellés du référentiel, calculées une seule fois
         -- (au lieu d'un parcours complet de geocodes_city par libellé)
         ref_keys AS MATERIALIZED (SELECT DISTINCT k.city_key
                                   FROM geocodes_city g
                                            CROSS JOIN LATERAL (VALUES (NORMALIZE_CITY_LABEL(g.value)),
                                                                       (NORMALIZE_CITY_LABEL(g.value_min)),
                                                                       (NORMALIZE_CITY_LABEL(g.value_min_complete))) k(city_key)
                                   WHERE g.code_country = france_country_code
                                     AND COALESCE(g.deprecated, 0) = 0
                                     AND k.city_key IS NOT NULL),
         -- libellés présents dans le référentiel (jointure hash par libellé distinct)
         known AS (SELECT l.birthplace
                   FROM (SELECT DISTINCT u.birthplace FROM unresolved u) l
                            JOIN ref_keys rk ON rk.city_key = NORMALIZE_CITY_LABEL(l.birthplace))
    SELECT u.id_identity, u.birthplace::TEXT AS attribute_value
    FROM unresolved u
    WHERE NOT keep_known_labels
       OR NOT EXISTS (SELECT 1 FROM known k WHERE k.birthplace = u.birthplace);
    ALTER TABLE tmp_delete_labels ADD PRIMARY KEY (id_identity);

    CALL DELETE_ATTRIBUTES_FROM_TMP(attribute_id_birthplace, batch_size, history_author_name, history_client_code,
                                    'Suppression du libellé de commune sans code correspondant', touch_identity);
END
$$
    LANGUAGE plpgsql;
