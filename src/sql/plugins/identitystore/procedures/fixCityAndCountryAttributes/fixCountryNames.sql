 CREATE
    OR REPLACE PROCEDURE FIX_COUNTRY_NAMES ()
AS
$$
BEGIN
    
 update identitystore_identity_attribute set attribute_value = 'ROYAUME-UNI'  where id_attribute = 7 and upper(attribute_value) ='ANGLETERRE';
 update identitystore_identity_attribute set attribute_value = 'ARGENTINE'  where id_attribute = 7 and upper(attribute_value) ='ARGENTINA';
 update identitystore_identity_attribute set attribute_value = 'AUSTRALIE'  where id_attribute = 7 and upper(attribute_value) ='AUSTRALIA';
 update identitystore_identity_attribute set attribute_value = 'AUTRICHE'  where id_attribute = 7 and upper(attribute_value) ='AUSTRIA';
 update identitystore_identity_attribute set attribute_value = 'BELGIQUE'  where id_attribute = 7 and upper(attribute_value) ='BELGIUM';
 update identitystore_identity_attribute set attribute_value = 'BOLIVIE'  where id_attribute = 7 and upper(attribute_value) ='BOLIVIA';
 update identitystore_identity_attribute set attribute_value = 'BRESIL'  where id_attribute = 7 and upper(attribute_value) ='BRASIL';
 update identitystore_identity_attribute set attribute_value = 'BRESIL'  where id_attribute = 7 and upper(attribute_value) ='BRAZIL';
 update identitystore_identity_attribute set attribute_value = 'BULGARIE'  where id_attribute = 7 and upper(attribute_value) ='BULGARIA';
 update identitystore_identity_attribute set attribute_value = 'CHILI'  where id_attribute = 7 and upper(attribute_value) ='CHILE';
 update identitystore_identity_attribute set attribute_value = 'CHINE'  where id_attribute = 7 and upper(attribute_value) ='CHINA';
 update identitystore_identity_attribute set attribute_value = 'COLOMBIE'  where id_attribute = 7 and upper(attribute_value) ='COLOMBIA';
 update identitystore_identity_attribute set attribute_value = 'COREE (REPUBLIQUE DE)'  where id_attribute = 7 and upper(attribute_value) ='COREE DU SUD';
 update identitystore_identity_attribute set attribute_value = 'DANEMARK'  where id_attribute = 7 and upper(attribute_value) ='DENMARK';
 update identitystore_identity_attribute set attribute_value = 'EQUATEUR'  where id_attribute = 7 and upper(attribute_value) ='ECUADOR';
 update identitystore_identity_attribute set attribute_value = 'EGYPTE'  where id_attribute = 7 and upper(attribute_value) ='EGYPT';
 update identitystore_identity_attribute set attribute_value = 'ROYAUME-UNI'  where id_attribute = 7 and upper(attribute_value) ='ENGLAND';
 update identitystore_identity_attribute set attribute_value = 'ESPAGNE'  where id_attribute = 7 and upper(attribute_value) ='ESPAÑA';
 update identitystore_identity_attribute set attribute_value = 'ETATS-UNIS'  where id_attribute = 7 and upper(attribute_value) ='ETAS UNIS';
 update identitystore_identity_attribute set attribute_value = 'ETATS-UNIS'  where id_attribute = 7 and upper(attribute_value) ='ETATS-UNIS D''AMÉRIQUE';
 update identitystore_identity_attribute set attribute_value = 'FRANCE'  where id_attribute = 7 and upper(attribute_value) ='FANCE';
 update identitystore_identity_attribute set attribute_value = 'FRANCE'  where id_attribute = 7 and upper(attribute_value) ='FEANCE';
 update identitystore_identity_attribute set attribute_value = 'FINLANDE'  where id_attribute = 7 and upper(attribute_value) ='FINLAND';
 update identitystore_identity_attribute set attribute_value = 'FRANCE'  where id_attribute = 7 and upper(attribute_value) ='FR';
 update identitystore_identity_attribute set attribute_value = 'FRANCE'  where id_attribute = 7 and upper(attribute_value) ='FRA';
 update identitystore_identity_attribute set attribute_value = 'FRANCE'  where id_attribute = 7 and upper(attribute_value) ='FRANC';
 update identitystore_identity_attribute set attribute_value = 'FRANCE'  where id_attribute = 7 and upper(attribute_value) ='FRANÇA';
 update identitystore_identity_attribute set attribute_value = 'FRANCE'  where id_attribute = 7 and upper(attribute_value) ='FRANCIA';
 update identitystore_identity_attribute set attribute_value = 'ROYAUME-UNI'  where id_attribute = 7 and upper(attribute_value) ='GRANDE BRETAGNE';
 update identitystore_identity_attribute set attribute_value = 'GRECE'  where id_attribute = 7 and upper(attribute_value) ='GREECE';
 update identitystore_identity_attribute set attribute_value = 'INDE'  where id_attribute = 7 and upper(attribute_value) ='INDIA';
 update identitystore_identity_attribute set attribute_value = 'INDONESIE'  where id_attribute = 7 and upper(attribute_value) ='INDONESIA';
 update identitystore_identity_attribute set attribute_value = 'IRAQ'  where id_attribute = 7 and upper(attribute_value) ='IRAK';
 update identitystore_identity_attribute set attribute_value = 'IRAQ'  where id_attribute = 7 and upper(attribute_value) ='IRAK';
 update identitystore_identity_attribute set attribute_value = 'ITALIE'  where id_attribute = 7 and upper(attribute_value) ='ITALIA';
 update identitystore_identity_attribute set attribute_value = 'ITALIE'  where id_attribute = 7 and upper(attribute_value) ='ITALY';
 update identitystore_identity_attribute set attribute_value = 'JAPON'  where id_attribute = 7 and upper(attribute_value) ='JAPAN';
 update identitystore_identity_attribute set attribute_value = 'LIBAN'  where id_attribute = 7 and upper(attribute_value) ='LEBANON';
 update identitystore_identity_attribute set attribute_value = 'LITUANIE'  where id_attribute = 7 and upper(attribute_value) ='LITHUANIA';
 update identitystore_identity_attribute set attribute_value = 'MALAISIE'  where id_attribute = 7 and upper(attribute_value) ='MALAYSIA';
 update identitystore_identity_attribute set attribute_value = 'MEXIQUE'  where id_attribute = 7 and upper(attribute_value) ='MEXICO';
 update identitystore_identity_attribute set attribute_value = 'MEXIQUE'  where id_attribute = 7 and upper(attribute_value) ='MÉXICO';
 update identitystore_identity_attribute set attribute_value = 'MOLDAVIE'  where id_attribute = 7 and upper(attribute_value) ='MOLDOVA';
 update identitystore_identity_attribute set attribute_value = 'PAYS-BAS'  where id_attribute = 7 and upper(attribute_value) ='NEDERLAND';
 update identitystore_identity_attribute set attribute_value = 'PAYS-BAS'  where id_attribute = 7 and upper(attribute_value) ='NETHERLANDS';
 update identitystore_identity_attribute set attribute_value = 'NOUVELLE-ZELANDE'  where id_attribute = 7 and upper(attribute_value) ='NEW ZEALAND';
 update identitystore_identity_attribute set attribute_value = 'NORVEGE'  where id_attribute = 7 and upper(attribute_value) ='NORWAY';
 update identitystore_identity_attribute set attribute_value = 'PEROU'  where id_attribute = 7 and upper(attribute_value) ='PERU';
 update identitystore_identity_attribute set attribute_value = 'PEROU'  where id_attribute = 7 and upper(attribute_value) ='PERÚ';
 update identitystore_identity_attribute set attribute_value = 'PHILIPPINES'  where id_attribute = 7 and upper(attribute_value) ='PHILIPPINE';
 update identitystore_identity_attribute set attribute_value = 'POLOGNE'  where id_attribute = 7 and upper(attribute_value) ='POLAND';
 update identitystore_identity_attribute set attribute_value = 'POLOGNE'  where id_attribute = 7 and upper(attribute_value) ='POLSKA';
 update identitystore_identity_attribute set attribute_value = 'COREE (REPUBLIQUE DE)'  where id_attribute = 7 and upper(attribute_value) ='REPUBLIC OF KOREA';
 update identitystore_identity_attribute set attribute_value = 'ROUMANIE'  where id_attribute = 7 and upper(attribute_value) ='ROMANIA';
 update identitystore_identity_attribute set attribute_value = 'ROUMANIE'  where id_attribute = 7 and upper(attribute_value) ='ROUMAINE';
 update identitystore_identity_attribute set attribute_value = 'SINGAPOUR'  where id_attribute = 7 and upper(attribute_value) ='SINGAPORE';
 update identitystore_identity_attribute set attribute_value = 'AFRIQUE DU SUD'  where id_attribute = 7 and upper(attribute_value) ='SOUTH AFRICA';
 update identitystore_identity_attribute set attribute_value = 'COREE (REPUBLIQUE DE)'  where id_attribute = 7 and upper(attribute_value) ='SOUTH KOREA';
 update identitystore_identity_attribute set attribute_value = 'ESPAGNE'  where id_attribute = 7 and upper(attribute_value) ='SPAIN';
 update identitystore_identity_attribute set attribute_value = 'SRI LANKA'  where id_attribute = 7 and upper(attribute_value) ='SRILANKA';
 update identitystore_identity_attribute set attribute_value = 'SUEDE'  where id_attribute = 7 and upper(attribute_value) ='SWEDEN';
 update identitystore_identity_attribute set attribute_value = 'SUISSE'  where id_attribute = 7 and upper(attribute_value) ='SWITZERLAND';
 update identitystore_identity_attribute set attribute_value = 'SYRIE'  where id_attribute = 7 and upper(attribute_value) ='SYRIA';
 update identitystore_identity_attribute set attribute_value = 'THAILANDE'  where id_attribute = 7 and upper(attribute_value) ='THAILAND';
 update identitystore_identity_attribute set attribute_value = 'TUNISIE'  where id_attribute = 7 and upper(attribute_value) ='TUNIS';
 update identitystore_identity_attribute set attribute_value = 'TUNISIE'  where id_attribute = 7 and upper(attribute_value) ='TUNISIA';
 update identitystore_identity_attribute set attribute_value = 'TURQUIE'  where id_attribute = 7 and upper(attribute_value) ='TURKEY';
 update identitystore_identity_attribute set attribute_value = 'ROYAUME-UNI'  where id_attribute = 7 and upper(attribute_value) ='UK';
 update identitystore_identity_attribute set attribute_value = 'ETATS-UNIS'  where id_attribute = 7 and upper(attribute_value) ='UNITED STATES';
 update identitystore_identity_attribute set attribute_value = 'ETATS-UNIS'  where id_attribute = 7 and upper(attribute_value) ='UNITED STATES OF AMERICA';
 update identitystore_identity_attribute set attribute_value = 'ETATS-UNIS'  where id_attribute = 7 and upper(attribute_value) ='US';
 update identitystore_identity_attribute set attribute_value = 'ETATS-UNIS'  where id_attribute = 7 and upper(attribute_value) ='USA';
END
$$
    LANGUAGE plpgsql;

