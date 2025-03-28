-- Function that searches for attributes that start,end or have multiple whitespaces
CREATE
    OR REPLACE FUNCTION COUNT_UNTRIMMED_ATTRIBUTES()
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
        SELECT attribute_value, COUNT(*) AS count, STRING_AGG(id_identity::TEXT, ',')
        FROM identitystore_identity_attribute
        WHERE attribute_value ~ '(^\s+.*)|(.*\s\s.*)|(.*\s+$)'
        GROUP BY attribute_value;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Reformat attributes with wrong or too much witespaces
----------------------------------------------------------
CREATE
    OR REPLACE PROCEDURE FORMAT_TRIM_ATTRIBUTES()
AS
$$
BEGIN
    UPDATE identitystore_identity_attribute
    SET attribute_value = trim(regexp_replace(attribute_value, '\s+', ' ', 'g'))
    WHERE attribute_value ~ '(^\s+.*)|(.*\s\s.*)|(.*\s+$)';
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
-- Search for given attribute_id's attributes with lowercase
----------------------------------------------------------
CREATE
    OR REPLACE FUNCTION COUNT_UNCAPPED_ATTRIBUTE(attribute_id INTEGER)
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
        SELECT attribute_value, COUNT(*) AS count, STRING_AGG(id_identity::TEXT, ',')
        FROM identitystore_identity_attribute
        WHERE id_attribute = attribute_id
          AND attribute_value != UPPER(attribute_value)
        GROUP BY attribute_value;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Search for given attribute_id's attributes without uppercase first letter
----------------------------------------------------------
CREATE
    OR REPLACE FUNCTION COUNT_UNINITCAP_ATTRIBUTES(attribute_id INTEGER)
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
        SELECT attribute_value, COUNT(*) AS count, STRING_AGG(id_identity::TEXT, ',')
        FROM identitystore_identity_attribute
        WHERE id_attribute = attribute_id
          AND attribute_value != INITCAP(attribute_value)
        GROUP BY attribute_value;
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Reformat given attribute_id's attributes lowercase
----------------------------------------------------------
CREATE
    OR REPLACE PROCEDURE FORMAT_UPPERCASE_ATTRIBUTES(attribute_id INTEGER)
AS
$$
BEGIN
    UPDATE identitystore_identity_attribute
    SET attribute_value = UPPER(attribute_value)
    WHERE id_attribute = attribute_id
      AND attribute_value != UPPER(attribute_value);
END
$$
    LANGUAGE plpgsql;

----------------------------------------------------------
-- Reformat given attribute_id's attributes without uppercase first letter
----------------------------------------------------------
CREATE
    OR REPLACE PROCEDURE FORMAT_INITCAP_ATTRIBUTES(attribute_id INTEGER)
AS
$$
BEGIN
    UPDATE identitystore_identity_attribute
    SET attribute_value = INITCAP(attribute_value)
    WHERE id_attribute = attribute_id
      AND attribute_value != INITCAP(attribute_value);
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

