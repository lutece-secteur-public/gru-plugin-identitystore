--functions
DROP FUNCTION COUNT_UNTRIMMED_ATTRIBUTES();
DROP FUNCTION COUNT_UNCAPPED_ATTRIBUTE(attribute_id INTEGER);
DROP FUNCTION COUNT_UNINITCAP_ATTRIBUTES(attribute_id INTEGER);
DROP FUNCTION COUNT_MISSING_COUNTRY_CODES(attribute_id_birthcountry_code INTEGER,
    attribute_id_birthcountry INTEGER);
DROP FUNCTION COUNT_MISSING_CITY_CODES(attribute_id_birthplace_code INTEGER, attribute_id_birthplace INTEGER,
    attribute_id_birthcountry_code INTEGER, country_code VARCHAR);
DROP FUNCTION GET_MALFORMED_PHONES_NUMBERS(regexp VARCHAR, attribute_id INTEGER);
DROP FUNCTION GET_MALFORMED_ATTRIBUTES(detectionRegexp VARCHAR, attribute_id INTEGER);
DROP FUNCTION GET_NULL_ATTRIBUTES();

--procedures
DROP PROCEDURE FORMAT_TRIM_ATTRIBUTES();
DROP PROCEDURE FORMAT_DATES(detectionRegexp VARCHAR, detectionPattern VARCHAR, formattingPattern VARCHAR,
    attribute_id INTEGER);
DROP PROCEDURE FORMAT_PLAIN_DATES(formattingPattern VARCHAR, attribute_id INTEGER);
DROP PROCEDURE FORMAT_UPPERCASE_ATTRIBUTES(attribute_id INTEGER);
DROP PROCEDURE FORMAT_INITCAP_ATTRIBUTES(attribute_id INTEGER);
DROP PROCEDURE UPDATE_COUNTRY_CODE_BY_LIBELLE(attribute_id_birthcountry_code INTEGER,
    attribute_id_birthcountry INTEGER,
    attribute_id_birthdate INTEGER);
DROP PROCEDURE UPDATE_CITY_CODE_BY_LIBELLE(attribute_id_birthplace_code INTEGER,
    attribute_id_birthplace INTEGER,
    attribute_id_birthcountry_code INTEGER, country_code VARCHAR,
    attribute_id_birthdate INTEGER);
DROP PROCEDURE UPDATE_PHONE_NUMBER(attribute_id INTEGER);