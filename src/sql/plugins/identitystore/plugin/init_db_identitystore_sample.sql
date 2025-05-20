

--
-- Data for Name: identitystore_ref_attribute; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO  identitystore_ref_attribute (id_attribute, name, key_name, description, key_type, key_weight, certifiable, pivot, common_search_key, mandatory_for_creation, validation_regex, validation_error_message, validation_error_message_key)  VALUES 
(10, 'Code postal', 'address_postal_code', 'Champ d''adresse : code postal', 0, 0, 1, 0,  null , 0, '^[A-Za-zÀ-Üà-ü\\d\\s''-]+$', 'uniquement caractères alphanumériques, apostrophe, espace et tirets.', 'identitystore.attribute.status.validation.error.only.alphanum.apostrophe.space.dash'),
(11, 'Ville', 'address_city', 'Champ d''adresse : ville', 0, 0, 1, 0,  null , 0, '^[A-Za-zÀ-Üà-ü\\d\\s''-]+$', 'uniquement caractères alphanumériques, apostrophe, espace et tirets.', 'identitystore.attribute.status.validation.error.only.alphanum.apostrophe.space.dash'),
(16, 'Login', 'login', 'Login de connexion (email)', 0, '20', 1, 0, 'common_email', 0, '^[A-Za-zÀ-Üà-ü\\d\\s''-]+$', 'uniquement caractères alphanumériques, apostrophe, espace et tirets.', 'identitystore.attribute.status.validation.error.only.alphanum.apostrophe.space.dash'),
(31, '(FC) Key', 'fc_key', 'Format FranceConnect - Key', 0, 0, 0, 0,  null , 0, '^[A-Za-zÀ-Üà-ü\\d\\s''-]+$', 'uniquement caractères alphanumériques, apostrophe, espace et tirets.', 'identitystore.attribute.status.validation.error.only.alphanum.apostrophe.space.dash'),
(8, 'Adresse', 'address', 'Addresse postale', 0, 0, 1, 0,  null , 0, '^[A-Za-zÀ-Üà-ü\\d\\s''-]+$', 'uniquement caractères alphanumériques, apostrophe, espace et tirets.', 'identitystore.attribute.status.validation.error.only.alphanum.apostrophe.space.dash'),
(9, 'Complément d''adresse', 'address_detail', 'Complément d''adresse', 0, 0, 1, 0,  null , 0, '^[A-Za-zÀ-Üà-ü\\d\\s''-]+$', 'uniquement caractères alphanumériques, apostrophe, espace et tirets.', 'identitystore.attribute.status.validation.error.only.alphanum.apostrophe.space.dash'),
(13, 'Téléphone portable', 'mobile_phone', 'Réservé pour l''envoi de SMS', 0, '15', 1, 0, 'common_phone', 0, '^0(6|7)\\d{8}$', 'uniquement numérique, numéro français sur 10 chiffres commençant par O6 ou 07.', 'identitystore.attribute.status.validation.error.mobile.phone.format'),
(14, 'Téléphone fixe', 'fixed_phone', 'Téléphone fixe', 0, '14', 0, 0, 'common_phone', 0, '^0([1-5]|9)\\d{8}$', 'uniquement numérique, numéro français sur 10 chiffres commençant par 01 à 05 ou 09.', 'identitystore.attribute.status.validation.error.fixed.phone.format'),
(56, 'Code INSEE commune de naissance', 'birthplace_code', 'Code INSEE de la commune de naissance ou vide pour l''&eacute;tranger.&nbsp;(Attribut Pivot)', 0, 5, 1, 1,  null , 0, '^[A-Z\\d]{5}$', 'Alphanumérique sur 5 caractères. Le code doit exister dans notre référentiel géocode', 'identitystore.attribute.status.validation.error.geocodes.format'),
(57, 'Code INSEE pays de naissance', 'birthcountry_code', 'Code INSEE du pays de naissance.&nbsp;(Attribut Pivot)', 0, 3, 1, 1,  null , 0, '^[A-Z\\d]{5}$', 'Alphanumérique sur 5 caractères. Le code doit exister dans notre référentiel géocode', 'identitystore.attribute.status.validation.error.geocodes.format'),
(1, 'Genre', 'gender', '0:Non défini /1:Femme / 2:Homme (Attribut Pivot)', 0, 3, 1, 1,  null , 0, '^[0-2]{1}$', 'uniquement 0, 1 ou 2', 'identitystore.attribute.status.validation.error.gender.format'),
(2, 'Nom d''usage', 'preferred_username', 'Nom d''usage', 0, '15', 0, 0, 'common_lastname', 0, '^[A-Za-zÀ-Üà-ü\\s''-]+$', 'Uniquement caractères aplha, apostrophe, espace et tirets.', 'identitystore.attribute.status.validation.error.only.alpha.apostrophe.space.dash'),
(4, 'Prénoms de naissance', 'first_name', 'Liste des prénoms de naissance séparés par des espaces. (Attribut Pivot).', 0, 10, 1, 1,  null , 1, '^[A-Za-zÀ-Üà-ü\\s''-]+$', 'Uniquement caractères aplha, apostrophe, espace et tirets.', 'identitystore.attribute.status.validation.error.only.alpha.apostrophe.space.dash'),
(3, 'Nom de naissance', 'family_name', 'Nom de naissance. (Attribut Pivot)', 0, '18', 1, 1, 'common_lastname', 1, '^[A-Za-zÀ-Üà-ü\\s''-]+$', 'Uniquement caractères aplha, apostrophe, espace et tirets.', 'identitystore.attribute.status.validation.error.only.alpha.apostrophe.space.dash'),
(12, 'Email de contact', 'email', 'Email de contact', 0, '20', 1, 0, 'common_email', 0, '^[\\w-\;]+@([\\w-]+\;)+[\\w-]{2,4}$', 'format mail du type xxx@yyy.zzz', 'identitystore.attribute.status.validation.error.email.format'),
(5, 'Date de naissance', 'birthdate', 'Date de naissance au format JJ/MM/AAAA (Attribut Pivot)', 0, '17', 1, 1,  null , 0, '^(?:(?:31(\\/)(?:0[13578]|1[02]))\\1|(?:(?:29|30)(\\/)(?:0[13-9]|1[0-2])\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|^(?:29(\\/)02\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0[1-9]|1\\d|2[0-8])(\\/)(?:(?:0[1-9])|(?:1[0-2]))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$', 'Date exprimée sur 10 caractères en jj/mm/aaaa.', 'identitystore.attribute.status.validation.error.date.format'),
(6, 'Libellé commune de naissance', 'birthplace', 'Libellé de la commune de naissance', 0, 5, 1, 0,  null , 0, '^[A-Z\\s]+$', 'uniquement caractères aplha en majuscule non accentué, et espace.', 'identitystore.attribute.status.validation.error.only.uppercase.space'),
(7, 'Libellé pays de naissance', 'birthcountry', 'Libellé du pays de naissance', 0, 3, 1, 0,  null , 0, '^[A-Z\\s]+$', 'uniquement caractères aplha en majuscule non accentué, et espace.', 'identitystore.attribute.status.validation.error.only.uppercase.space')
;

--
-- Data for Name: identitystore_ref_certification_level; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO  identitystore_ref_certification_level (id_ref_certification_level, name, description, level)  VALUES 
(1, 'Déclaratif', 'Des attributs renseignés sans certification ni PJ', 100),
(2, 'PJ non officielle scannée', 'Agent certifie via scan PJ non officielle', 330),
(3, 'PJ non officielle guichet', 'Agent certifie guichet PJ non officielle réelle', 360),
(4, 'PJ officielle scannée', 'Agent certifié avec scan PJ reçu numériquement', 400),
(5, 'PJ officielle guichet', 'Agent certifié en guichet en présence usager et PJ', 500),
(6, 'Validation Mail', 'Email validé par notification mail', 200),
(7, 'Données validées par référentiel de confiance', 'FC, R2P (Tout processus INSEE)', 600),
(9, 'Validation SMS', 'Numéro de portable validé par SMS', 200),
(8, 'Validation Mail Mon Paris', 'Email validé par Mon Paris', 250),
(10, 'Validation SMS Mon Paris', 'Numéro de portable validé par SMS via Mon Paris', 250)
;

INSERT INTO identitystore_identity_search_rule (type) VALUES ('OR');
INSERT INTO identitystore_identity_search_rule_attribute (id_rule, id_attribute) SELECT MAX(r.id_rule), a.id_attribute FROM identitystore_identity_search_rule r, identitystore_ref_attribute a WHERE a.key_name = 'login' GROUP BY a.id_attribute;
INSERT INTO identitystore_identity_search_rule_attribute (id_rule, id_attribute) SELECT MAX(r.id_rule), a.id_attribute FROM identitystore_identity_search_rule r, identitystore_ref_attribute a WHERE a.key_name = 'email' GROUP BY a.id_attribute;

INSERT INTO identitystore_identity_search_rule (type) VALUES ('AND');
INSERT INTO identitystore_identity_search_rule_attribute (id_rule, id_attribute) SELECT MAX(r.id_rule), a.id_attribute FROM identitystore_identity_search_rule r, identitystore_ref_attribute a WHERE a.key_name = 'family_name' GROUP BY a.id_attribute;
INSERT INTO identitystore_identity_search_rule_attribute (id_rule, id_attribute) SELECT MAX(r.id_rule), a.id_attribute FROM identitystore_identity_search_rule r, identitystore_ref_attribute a WHERE a.key_name = 'first_name' GROUP BY a.id_attribute;
INSERT INTO identitystore_identity_search_rule_attribute (id_rule, id_attribute) SELECT MAX(r.id_rule), a.id_attribute FROM identitystore_identity_search_rule r, identitystore_ref_attribute a WHERE a.key_name = 'birthdate' GROUP BY a.id_attribute;


--
-- Data for  identitystore_ref_certification_processus; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO  public.identitystore_ref_certification_processus (id_ref_attribute_certification_processus, label, code)  VALUES 
(6, 'Validation SMS', 'SMS'),
(5, 'Validation Mail', 'MAIL'),
(10, 'Validation Mail Mon Paris', 'emailcertifier'),
(15, 'Validation SMS Mon Paris', 'smscertifier'),
(11, 'PJ non officielle scannée', 'NUM2'),
(13, 'PJ officielle scannée', 'NUM1'),
(12, 'PJ non officielle guichet', 'ORIG2'),
(14, 'PJ officielle guichet', 'ORIG1'),
(9, 'Déclaratif', 'DEC'),
(7, 'R2P', 'R2P'),
(8, 'FranceConnect', 'fccertifier')
;


--
-- Data for Name: identitystore_ref_certification_attribute_level; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO  identitystore_ref_certification_attribute_level (id_attribute, id_ref_certification_level, id_ref_attribute_certification_processus)  VALUES 
(5, 7, 8),
(6, 7, 8),
(7, 7, 8),
(16, 8, 8),
(56, 7, 8),
(57, 7, 8),
(12, 8, 8),
(1, 7, 8),
(2, 7, 8),
(3, 7, 8),
(4, 7, 8),
(13, 2, 6),
(12, 6, 5),
(16, 8, 10),
(12, 8, 10),
(13, 8, 15),
(57, 2, 11),
(1, 2, 11),
(2, 2, 11),
(3, 2, 11),
(4, 2, 11),
(5, 2, 11),
(6, 2, 11),
(7, 2, 11),
(56, 2, 11),
(56, 4, 13),
(57, 4, 13),
(1, 4, 13),
(2, 4, 13),
(3, 4, 13),
(4, 4, 13),
(5, 4, 13),
(6, 4, 13),
(7, 4, 13),
(56, 3, 12),
(57, 3, 12),
(1, 3, 12),
(2, 3, 12),
(3, 3, 12),
(4, 3, 12),
(5, 3, 12),
(6, 3, 12),
(7, 3, 12),
(7, 5, 14),
(56, 5, 14),
(57, 5, 14),
(1, 5, 14),
(2, 5, 14),
(3, 5, 14),
(4, 5, 14),
(5, 5, 14),
(6, 5, 14),
(13, 1, 9),
(12, 1, 9),
(10, 1, 9),
(11, 1, 9),
(1, 1, 9),
(2, 1, 9),
(3, 1, 9),
(4, 1, 9),
(5, 1, 9),
(6, 1, 9),
(7, 1, 9),
(8, 1, 9),
(56, 1, 9),
(9, 1, 9),
(57, 1, 9),
(14, 1, 9),
(5, 7, 7),
(6, 7, 7),
(7, 7, 7),
(56, 7, 7),
(57, 7, 7),
(1, 7, 7),
(2, 7, 7),
(3, 7, 7),
(4, 7, 7)
;


