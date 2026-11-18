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
-- Reformat dates that match a given pattern (detectionRegexp && detectionPattern) according to a new pattern (formattingPattern)
----------------------------------------------------------
CREATE
    OR REPLACE PROCEDURE FORMAT_DATES(detectionRegexp VARCHAR, detectionPattern VARCHAR, formattingPattern VARCHAR,
                                      attribute_id INTEGER)
AS
$$
BEGIN
    UPDATE identitystore_identity_attribute
    SET attribute_value = TO_CHAR(TO_DATE(attribute_value, detectionPattern), formattingPattern)
    WHERE id_attribute = attribute_id
      AND attribute_value ~ detectionRegexp;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Reformat dates writen in plain text
----------------------------------------------------------
CREATE
    OR REPLACE PROCEDURE FORMAT_PLAIN_DATES(formattingPattern VARCHAR, attribute_id INTEGER)
AS
$$
BEGIN
    UPDATE identitystore_identity_attribute
    SET attribute_value = TO_CHAR(TO_DATE((CASE
                                               WHEN attribute_value LIKE '%janvier%'
                                                   THEN REPLACE(attribute_value, 'janvier', 'january')
                                               WHEN attribute_value LIKE '%février%'
                                                   THEN REPLACE(attribute_value, 'février', 'february')
                                               WHEN attribute_value LIKE '%mars%'
                                                   THEN REPLACE(attribute_value, 'mars', 'march')
                                               WHEN attribute_value LIKE '%avril%'
                                                   THEN REPLACE(attribute_value, 'avril', 'april')
                                               WHEN attribute_value LIKE '%mai%'
                                                   THEN REPLACE(attribute_value, 'mai', 'may')
                                               WHEN attribute_value LIKE '%juin%'
                                                   THEN REPLACE(attribute_value, 'juin', 'june')
                                               WHEN attribute_value LIKE '%juillet%'
                                                   THEN REPLACE(attribute_value, 'juillet', 'july')
                                               WHEN attribute_value LIKE '%aout%'
                                                   THEN REPLACE(attribute_value, 'aout', 'august')
                                               WHEN attribute_value LIKE '%septembre%'
                                                   THEN REPLACE(attribute_value, 'septembre', 'september')
                                               WHEN attribute_value LIKE '%octobre%'
                                                   THEN REPLACE(attribute_value, 'octobre', 'october')
                                               WHEN attribute_value LIKE '%novembre%'
                                                   THEN REPLACE(attribute_value, 'novembre', 'november')
                                               WHEN attribute_value LIKE '%decembre%'
                                                   THEN REPLACE(attribute_value, 'decembre', 'december')
        END), 'DD Month YYYY'), formattingPattern)
    WHERE id_attribute = attribute_id
      AND attribute_value LIKE '% %';
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

----------------------------------------------------------
-- Count the identities that have a birthcountry set but no birthcountry_code
----------------------------------------------------------
CREATE
    OR REPLACE FUNCTION COUNT_MISSING_COUNTRY_CODES(attribute_id_birthcountry_code INTEGER,
                                                    attribute_id_birthcountry INTEGER)
    RETURNS TABLE
            (
                value VARCHAR,
                count           BIGINT,
                identities      TEXT
            )
AS
$$
BEGIN
    RETURN QUERY
        WITH countrycode AS (SELECT attrcountrycode.id_identity
                             FROM identitystore_identity_attribute attrcountrycode
                             WHERE attrcountrycode.id_attribute = attribute_id_birthcountry_code
                               AND (attrcountrycode.attribute_value IS NULL OR attrcountrycode.attribute_value = ''))
        SELECT attr.attribute_value, COUNT(*), STRING_AGG(attr.id_identity::TEXT, ',')
        FROM identitystore_identity_attribute attr
                 JOIN countrycode ON countrycode.id_identity = attr.id_identity
        WHERE attr.id_attribute = attribute_id_birthcountry
          AND attr.attribute_value IS NOT NULL
          AND countrycode.id_identity IS NOT NULL
        GROUP BY attr.attribute_value;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Update the birthcountry_code if there is a birthcountry and if the birthdate is valid for this geocode
----------------------------------------------------------

CREATE
    OR REPLACE PROCEDURE UPDATE_COUNTRY_CODE_BY_LIBELLE(attribute_id_birthcountry_code INTEGER,
                                                        attribute_id_birthcountry INTEGER,
                                                        attribute_id_birthdate INTEGER)
AS
$$
BEGIN
    WITH country AS (SELECT attrcountry.attribute_value, attrcountry.id_identity
                     FROM identitystore_identity_attribute attrcountry
                     WHERE attrcountry.id_attribute = attribute_id_birthcountry),
         birthdate AS (SELECT attrdate.attribute_value, attrdate.id_identity
                       FROM identitystore_identity_attribute attrdate
                       WHERE attrdate.id_attribute = attribute_id_birthdate)
    UPDATE identitystore_identity_attribute attr
    SET attribute_value =
            (SELECT code
             FROM geocodes_country
                      JOIN birthdate ON attr.id_identity = birthdate.id_identity
             WHERE value = country.attribute_value
               AND date_validity_start <= TO_DATE(birthdate.attribute_value, 'DD/MM/YYYY')
               AND date_validity_end >= TO_DATE(birthdate.attribute_value, 'DD/MM/YYYY'))
    FROM country
    WHERE attr.id_attribute = attribute_id_birthcountry_code
      AND (attr.attribute_value IS NULL
        OR attr.attribute_value = '')
      AND country.id_identity = attr.id_identity
      AND country.attribute_value IS NOT NULL;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Count the identities that have a birthplace set but no birthplace_code
----------------------------------------------------------
CREATE
    OR REPLACE FUNCTION COUNT_MISSING_CITY_CODES(attribute_id_birthplace_code INTEGER, attribute_id_birthplace INTEGER,
                                                 attribute_id_birthcountry_code INTEGER, country_code VARCHAR)
    RETURNS TABLE
            (
                value VARCHAR,
                count           BIGINT,
                identities      TEXT
            )
AS
$$
BEGIN
    RETURN QUERY
        WITH country_codes AS (SELECT attrcountrycode.attribute_value, attrcountrycode.id_identity
                               FROM identitystore_identity_attribute attrcountrycode
                               WHERE attrcountrycode.id_attribute = attribute_id_birthcountry_code
                                 AND attrcountrycode.attribute_value = country_code),
             citiycode AS (SELECT attrcitycode.id_identity
                           FROM identitystore_identity_attribute attrcitycode
                           WHERE attrcitycode.id_attribute = attribute_id_birthplace_code
                             AND (attrcitycode.attribute_value IS NULL OR attrcitycode.attribute_value = ''))

        SELECT attr.attribute_value, COUNT(*), STRING_AGG(attr.id_identity::TEXT, ',')
        FROM identitystore_identity_attribute attr
                 JOIN country_codes ON country_codes.id_identity = attr.id_identity
                 JOIN citiycode ON citiycode.id_identity = attr.id_identity
        WHERE attr.id_attribute = attribute_id_birthplace
          AND attr.attribute_value IS NOT NULL
          AND citiycode.id_identity IS NOT NULL
          AND country_codes.attribute_value IS NOT NULL
        GROUP BY attr.attribute_value;
END
$$
    LANGUAGE plpgsql;


----------------------------------------------------------
-- Update the birthcountry_code if there is a birthcountry and if the birthdate is valid for this geocode
----------------------------------------------------------

CREATE
    OR REPLACE PROCEDURE UPDATE_CITY_CODE_BY_LIBELLE(attribute_id_birthplace_code INTEGER,
                                                     attribute_id_birthplace INTEGER,
                                                     attribute_id_birthcountry_code INTEGER, country_code VARCHAR,
                                                     attribute_id_birthdate INTEGER)
AS
$$
BEGIN
    WITH country_codes AS (SELECT attrcountrycode.attribute_value, attrcountrycode.id_identity
                           FROM identitystore_identity_attribute attrcountrycode
                           WHERE attrcountrycode.id_attribute = attribute_id_birthcountry_code
                             AND attrcountrycode.attribute_value = country_code),
         cities AS (SELECT attrcity.attribute_value, attrcity.id_identity
                    FROM identitystore_identity_attribute attrcity
                    WHERE attrcity.id_attribute = attribute_id_birthplace),
         birthdate AS (SELECT attrdate.attribute_value, attrdate.id_identity
                       FROM identitystore_identity_attribute attrdate
                       WHERE attrdate.id_attribute = attribute_id_birthdate)
    UPDATE identitystore_identity_attribute attr
    SET attribute_value =
            (SELECT code
             FROM geocodes_city
                      JOIN birthdate ON birthdate.id_identity = attr.id_identity
             WHERE (value = cities.attribute_value
                 OR value_min = cities.attribute_value
                 OR value_min_complete = cities.attribute_value)
               AND date_validity_start <= TO_DATE(birthdate.attribute_value, 'DD/MM/YYYY')
               AND date_validity_end >= TO_DATE(birthdate.attribute_value, 'DD/MM/YYYY'))
    FROM cities,
         country_codes
    WHERE attr.id_attribute = attribute_id_birthplace_code
      AND country_codes.id_identity = attr.id_identity
      AND cities.id_identity = attr.id_identity
      AND (attr.attribute_value IS NULL OR attr.attribute_value = '')
      AND cities.attribute_value IS NOT NULL
      AND country_codes.attribute_value IS NOT NULL
      AND cities.id_identity = attr.id_identity;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Count specifically the phones numbers that matches a regex (example : '^\+33.*|(.*\s.*)|(.*\..*)')
----------------------------------------------------------
CREATE
    OR REPLACE FUNCTION GET_MALFORMED_PHONES_NUMBERS(regexp VARCHAR, attribute_id INTEGER)
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
        SELECT attribute_value, COUNT(*), STRING_AGG(id_identity::TEXT, ',')
        FROM identitystore_identity_attribute
        WHERE id_attribute = attribute_id
          AND attribute_value IS NOT NULL
          AND attribute_value ~ regexp
        GROUP BY id_attribute;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Update the phone number if it matches the regex taht scan for dots, whitespaces or start with '+33'
----------------------------------------------------------
CREATE
    OR REPLACE PROCEDURE UPDATE_PHONE_NUMBER(attribute_id INTEGER)
AS
$$
BEGIN
    UPDATE identitystore_identity_attribute
    SET attribute_value = (CASE
                               WHEN attribute_value ~ '(.*\..*)' THEN replace(attribute_value, '.', '')
                               WHEN attribute_value ~ '(.*\s.*)' THEN replace(attribute_value, ' ', '')
                               WHEN attribute_value ~ '^\+33.*' THEN regexp_replace(attribute_value, '\+33', '0')
        END)
    WHERE id_attribute = attribute_id
      AND attribute_value IS NOT NULL
      AND attribute_value ~ '^\+33.*|(.*\s.*)|(.*\..*)';
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Function that searches for attributes (of type identified by attribute_id) that does not respect a given regexp (detectionRegexp)
----------------------------------------------------------
CREATE OR REPLACE FUNCTION GET_MALFORMED_ATTRIBUTES(detectionRegexp VARCHAR, attribute_id INTEGER)
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
        SELECT attribute_value, COUNT(*), STRING_AGG(id_identity::TEXT, ',')
        FROM identitystore_identity_attribute
        WHERE id_attribute = attribute_id
          AND NOT attribute_value ~ detectionRegexp
          AND attribute_value IS NOT NULL
        GROUP BY attribute_value;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Function that searches for attributes that are null or empty
----------------------------------------------------------
CREATE OR REPLACE FUNCTION GET_NULL_ATTRIBUTES()
    RETURNS TABLE
            (
                attribute_id INTEGER,
                count        BIGINT,
                identities   TEXT
            )
AS
$$
BEGIN
    RETURN QUERY
        SELECT id_attribute, COUNT(*), STRING_AGG(id_identity::TEXT, ',')
        FROM identitystore_identity_attribute
        WHERE attribute_value IS NULL
           OR attribute_value = ''
        GROUP BY id_attribute;
END
$$
    LANGUAGE plpgsql;
    
-------------------
-- + grants !
-------------------
-- GRANT ALL ON ALL PROCEDURES IN SCHEMA public TO postgres;
-- GRANT ALL ON ALL FUNCTIONS IN SCHEMA public TO postgres;

