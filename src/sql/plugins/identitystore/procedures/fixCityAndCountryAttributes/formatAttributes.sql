----------------------------------------------------------
-- Detection / formatting of untrimmed, uncapped and uninitcapped attributes (PostgreSQL)
-- Extract of init_procedures.sql : COUNT_UNTRIMMED_ATTRIBUTES, FORMAT_TRIM_ATTRIBUTES,
-- TO_UPPER_UNACCENT, COUNT_UNCAPPED_ATTRIBUTE, FORMAT_UPPERCASE_ATTRIBUTES,
-- TO_INITCAP_UNACCENT, COUNT_UNINITCAP_ATTRIBUTES, FORMAT_INITCAP_ATTRIBUTES
----------------------------------------------------------

-- Remove the previous signatures (the parameters changed, CREATE OR REPLACE would create an overload)
DROP FUNCTION IF EXISTS COUNT_UNTRIMMED_ATTRIBUTES();
DROP PROCEDURE IF EXISTS FORMAT_TRIM_ATTRIBUTES();
DROP FUNCTION IF EXISTS COUNT_UNCAPPED_ATTRIBUTE(INTEGER);
DROP PROCEDURE IF EXISTS FORMAT_UPPERCASE_ATTRIBUTES(INTEGER);
DROP FUNCTION IF EXISTS COUNT_UNINITCAP_ATTRIBUTES(INTEGER);
DROP PROCEDURE IF EXISTS FORMAT_INITCAP_ATTRIBUTES(INTEGER);

----------------------------------------------------------
-- Function that searches, for a given attribute_id, the attributes that start, end or have multiple whitespaces
-- (or contain tabs / line breaks).
-- Returns, for each distinct value, the number of occurrences and a sample of at most sample_size identity ids.
-- Usage : SELECT * FROM COUNT_UNTRIMMED_ATTRIBUTES(1);  SELECT * FROM COUNT_UNTRIMMED_ATTRIBUTES(1, 50);
----------------------------------------------------------
CREATE
    OR REPLACE FUNCTION COUNT_UNTRIMMED_ATTRIBUTES(attribute_id INTEGER, sample_size INTEGER DEFAULT 20)
    RETURNS TABLE
            (
                value      VARCHAR,
                count      BIGINT,
                identities TEXT
            )
AS
$$
BEGIN
    RETURN QUERY
        SELECT attr.attribute_value::VARCHAR,
               COUNT(*) AS count,
               ARRAY_TO_STRING((ARRAY_AGG(attr.id_identity ORDER BY attr.id_identity))[1:sample_size], ',')
        FROM identitystore_identity_attribute attr
        WHERE attr.id_attribute = attribute_id
          AND attr.attribute_value ~ '^\s|\s$|\s\s|[\t\n\r\f\v]'
        GROUP BY attr.attribute_value
        ORDER BY count DESC;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Reformat, for a given attribute_id, the attributes with wrong or too much whitespaces.
-- Processed by batches of batch_size consecutive id_identity, with a COMMIT after each batch
-- (so that it can be run on a large database without a huge transaction / lock).
-- Must be called outside of an explicit transaction block (autocommit), otherwise the COMMIT fails.
-- Usage : CALL FORMAT_TRIM_ATTRIBUTES(1);  CALL FORMAT_TRIM_ATTRIBUTES(1, 50000);
----------------------------------------------------------
CREATE
    OR REPLACE PROCEDURE FORMAT_TRIM_ATTRIBUTES(attribute_id INTEGER, batch_size INTEGER DEFAULT 10000)
AS
$$
DECLARE
    v_min_id  INTEGER;
    v_max_id  INTEGER;
    v_start   INTEGER;
    v_rows    BIGINT;
    v_total   BIGINT := 0;
BEGIN
    SELECT MIN(id_identity), MAX(id_identity)
    INTO v_min_id, v_max_id
    FROM identitystore_identity_attribute
    WHERE id_attribute = attribute_id;

    IF v_min_id IS NULL THEN
        RAISE NOTICE 'FORMAT_TRIM_ATTRIBUTES : no value for id_attribute %', attribute_id;
        RETURN;
    END IF;

    v_start := v_min_id;
    WHILE v_start <= v_max_id
        LOOP
            UPDATE identitystore_identity_attribute
            SET attribute_value = TRIM(REGEXP_REPLACE(attribute_value, '\s+', ' ', 'g'))
            WHERE id_attribute = attribute_id
              AND id_identity >= v_start
              AND id_identity < v_start + batch_size
              AND attribute_value ~ '^\s|\s$|\s\s|[\t\n\r\f\v]';
            GET DIAGNOSTICS v_rows = ROW_COUNT;
            v_total := v_total + v_rows;
            COMMIT;
            RAISE NOTICE 'FORMAT_TRIM_ATTRIBUTES : id_identity [%, %[ -> % rows updated (total %)',
                v_start, v_start + batch_size, v_rows, v_total;
            v_start := v_start + batch_size;
        END LOOP;

    RAISE NOTICE 'FORMAT_TRIM_ATTRIBUTES : done, % rows updated for id_attribute %', v_total, attribute_id;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Uppercase a value without accented capitals (é -> E, ç -> C, œ -> OE, ...).
-- Does not depend on the database locale (UPPER() only handles ASCII with a C locale).
----------------------------------------------------------
CREATE
    OR REPLACE FUNCTION TO_UPPER_UNACCENT(val TEXT)
    RETURNS TEXT
AS
$$
SELECT UPPER(
               REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
                   TRANSLATE(val,
                             'àáâãäåçèéêëìíîïñòóôõöøùúûüýÿÀÁÂÃÄÅÇÈÉÊËÌÍÎÏÑÒÓÔÕÖØÙÚÛÜÝŸ',
                             'aaaaaaceeeeiiiinoooooouuuuyyAAAAAACEEEEIIIINOOOOOOUUUUYY'),
                   'æ', 'ae'), 'Æ', 'AE'), 'œ', 'oe'), 'Œ', 'OE'), 'ß', 'ss'))
$$
    LANGUAGE sql
    IMMUTABLE
    PARALLEL SAFE;

----------------------------------------------------------
-- Search for given attribute_id's attributes that are not fully uppercase without accents.
-- Returns, for each distinct value, the number of occurrences and a sample of at most sample_size identity ids.
-- Usage : SELECT * FROM COUNT_UNCAPPED_ATTRIBUTE(1);  SELECT * FROM COUNT_UNCAPPED_ATTRIBUTE(1, 50);
----------------------------------------------------------
CREATE
    OR REPLACE FUNCTION COUNT_UNCAPPED_ATTRIBUTE(attribute_id INTEGER, sample_size INTEGER DEFAULT 20)
    RETURNS TABLE
            (
                value      VARCHAR,
                count      BIGINT,
                identities TEXT
            )
AS
$$
BEGIN
    RETURN QUERY
        SELECT attr.attribute_value::VARCHAR,
               COUNT(*) AS count,
               ARRAY_TO_STRING((ARRAY_AGG(attr.id_identity ORDER BY attr.id_identity))[1:sample_size], ',')
        FROM identitystore_identity_attribute attr
        WHERE attr.id_attribute = attribute_id
          AND attr.attribute_value <> TO_UPPER_UNACCENT(attr.attribute_value)
        GROUP BY attr.attribute_value
        ORDER BY count DESC;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Capitalize the first letter of each word, the other letters in lowercase, without accented capitals
-- (élodie -> Elodie, HÉLÈNE -> Hélène, jean-pierre -> Jean-Pierre, œuvre -> Oeuvre).
-- Accents on lowercase letters are kept. A word starts after any character that is not a letter or a digit
-- (same rule as INITCAP), but unlike INITCAP it does not depend on the database locale (LC_CTYPE).
----------------------------------------------------------
CREATE
    OR REPLACE FUNCTION TO_INITCAP_UNACCENT(val TEXT)
    RETURNS TEXT
AS
$$
SELECT CASE
           -- fast path : pure ASCII value, INITCAP gives the same result whatever the locale
           WHEN val !~ '[^\x01-\x7F]' THEN INITCAP(val)
           ELSE (WITH low AS (SELECT LOWER(TRANSLATE(val,
                                                    'ÀÁÂÃÄÅÇÈÉÊËÌÍÎÏÑÒÓÔÕÖØÙÚÛÜÝŸÆŒ',
                                                    'àáâãäåçèéêëìíîïñòóôõöøùúûüýÿæœ')) AS v)
                 SELECT COALESCE(STRING_AGG(
                                         CASE
                                             WHEN i > 1 AND SUBSTR(low.v, i - 1, 1) ~ '[0-9a-zàáâãäåçèéêëìíîïñòóôõöøùúûüýÿæœß]'
                                                 THEN SUBSTR(low.v, i, 1)
                                             ELSE INITCAP(TO_UPPER_UNACCENT(SUBSTR(low.v, i, 1)))
                                             END, '' ORDER BY i), val)
                 FROM low,
                      GENERATE_SERIES(1, LENGTH(low.v)) AS i)
           END
$$
    LANGUAGE sql
    IMMUTABLE
    PARALLEL SAFE;

----------------------------------------------------------
-- Search for given attribute_id's attributes without uppercase first letter (or with accented capitals).
-- Returns, for each distinct value, the number of occurrences and a sample of at most sample_size identity ids.
-- Usage : SELECT * FROM COUNT_UNINITCAP_ATTRIBUTES(1);  SELECT * FROM COUNT_UNINITCAP_ATTRIBUTES(1, 50);
----------------------------------------------------------
CREATE
    OR REPLACE FUNCTION COUNT_UNINITCAP_ATTRIBUTES(attribute_id INTEGER, sample_size INTEGER DEFAULT 20)
    RETURNS TABLE
            (
                value      VARCHAR,
                count      BIGINT,
                identities TEXT
            )
AS
$$
BEGIN
    RETURN QUERY
        SELECT attr.attribute_value::VARCHAR,
               COUNT(*) AS count,
               ARRAY_TO_STRING((ARRAY_AGG(attr.id_identity ORDER BY attr.id_identity))[1:sample_size], ',')
        FROM identitystore_identity_attribute attr
        WHERE attr.id_attribute = attribute_id
          AND attr.attribute_value <> TO_INITCAP_UNACCENT(attr.attribute_value)
        GROUP BY attr.attribute_value
        ORDER BY count DESC;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Reformat given attribute_id's attributes in uppercase without accents (see TO_UPPER_UNACCENT).
-- Processed by batches of batch_size consecutive id_identity, with a COMMIT after each batch
-- (so that it can be run on a large database without a huge transaction / lock).
-- Must be called outside of an explicit transaction block (autocommit), otherwise the COMMIT fails.
-- Usage : CALL FORMAT_UPPERCASE_ATTRIBUTES(1);  CALL FORMAT_UPPERCASE_ATTRIBUTES(1, 50000);
----------------------------------------------------------
CREATE
    OR REPLACE PROCEDURE FORMAT_UPPERCASE_ATTRIBUTES(attribute_id INTEGER, batch_size INTEGER DEFAULT 10000)
AS
$$
DECLARE
    v_min_id  INTEGER;
    v_max_id  INTEGER;
    v_start   INTEGER;
    v_rows    BIGINT;
    v_total   BIGINT := 0;
BEGIN
    SELECT MIN(id_identity), MAX(id_identity)
    INTO v_min_id, v_max_id
    FROM identitystore_identity_attribute
    WHERE id_attribute = attribute_id;

    IF v_min_id IS NULL THEN
        RAISE NOTICE 'FORMAT_UPPERCASE_ATTRIBUTES : no value for id_attribute %', attribute_id;
        RETURN;
    END IF;

    v_start := v_min_id;
    WHILE v_start <= v_max_id
        LOOP
            UPDATE identitystore_identity_attribute
            SET attribute_value = TO_UPPER_UNACCENT(attribute_value)
            WHERE id_attribute = attribute_id
              AND id_identity >= v_start
              AND id_identity < v_start + batch_size
              AND attribute_value <> TO_UPPER_UNACCENT(attribute_value);
            GET DIAGNOSTICS v_rows = ROW_COUNT;
            v_total := v_total + v_rows;
            COMMIT;
            RAISE NOTICE 'FORMAT_UPPERCASE_ATTRIBUTES : id_identity [%, %[ -> % rows updated (total %)',
                v_start, v_start + batch_size, v_rows, v_total;
            v_start := v_start + batch_size;
        END LOOP;

    RAISE NOTICE 'FORMAT_UPPERCASE_ATTRIBUTES : done, % rows updated for id_attribute %', v_total, attribute_id;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Reformat given attribute_id's attributes in uppercase without accents (see TO_INITCAP_UNACCENT).
-- Processed by batches of batch_size consecutive id_identity, with a COMMIT after each batch
-- (so that it can be run on a large database without a huge transaction / lock).
-- Must be called outside of an explicit transaction block (autocommit), otherwise the COMMIT fails.
-- Usage : CALL FORMAT_INITCAP_ATTRIBUTES(1);  CALL FORMAT_INITCAP_ATTRIBUTES(1, 50000);
----------------------------------------------------------
CREATE
    OR REPLACE PROCEDURE FORMAT_INITCAP_ATTRIBUTES(attribute_id INTEGER, batch_size INTEGER DEFAULT 10000)
AS
$$
DECLARE
    v_min_id  INTEGER;
    v_max_id  INTEGER;
    v_start   INTEGER;
    v_rows    BIGINT;
    v_total   BIGINT := 0;
BEGIN
    SELECT MIN(id_identity), MAX(id_identity)
    INTO v_min_id, v_max_id
    FROM identitystore_identity_attribute
    WHERE id_attribute = attribute_id;

    IF v_min_id IS NULL THEN
        RAISE NOTICE 'FORMAT_INITCAP_ATTRIBUTES : no value for id_attribute %', attribute_id;
        RETURN;
    END IF;

    v_start := v_min_id;
    WHILE v_start <= v_max_id
        LOOP
            UPDATE identitystore_identity_attribute
            SET attribute_value = TO_INITCAP_UNACCENT(attribute_value)
            WHERE id_attribute = attribute_id
              AND id_identity >= v_start
              AND id_identity < v_start + batch_size
              AND attribute_value <> TO_INITCAP_UNACCENT(attribute_value);
            GET DIAGNOSTICS v_rows = ROW_COUNT;
            v_total := v_total + v_rows;
            COMMIT;
            RAISE NOTICE 'FORMAT_INITCAP_ATTRIBUTES : id_identity [%, %[ -> % rows updated (total %)',
                v_start, v_start + batch_size, v_rows, v_total;
            v_start := v_start + batch_size;
        END LOOP;

    RAISE NOTICE 'FORMAT_INITCAP_ATTRIBUTES : done, % rows updated for id_attribute %', v_total, attribute_id;
END
$$
    LANGUAGE plpgsql;
