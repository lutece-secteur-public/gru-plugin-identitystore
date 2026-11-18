CREATE TABLE tmp_identity_without_history AS (SELECT identity.customer_id, identity.date_create
                                              FROM identitystore_identity identity
                                                       LEFT JOIN identitystore_identity_history history
                                                                 ON identity.customer_id = history.customer_id AND history.change_type = 0
                                              WHERE history.customer_id IS NULL
                                                AND identity.date_create < '2024-01-30 00:00:01.000');


INSERT INTO identitystore_identity_history(change_type, change_status, change_message, author_type, author_name, customer_id, modification_date)
SELECT 11, 'SUCCESS', 'SUCCESS', 'application',
       'import_migration', t1.customer_id, '2024-01-30 00:00:01.000'
FROM tmp_identity_without_history AS t1;

INSERT INTO identitystore_identity_history(change_type, change_status, change_message, author_type, author_name, customer_id, modification_date)
SELECT 0, 'SUCCESS', 'SUCCESS', 'application',
       'import_migration', t1.customer_id, t1.date_create
FROM tmp_identity_without_history AS t1;

DROP TABLE tmp_identity_without_history;

INSERT INTO identitystore_identity_attribute_history (change_type, change_satus, change_message, author_type, author_name,
                                                      id_identity, attribute_key, attribute_value, certification_process,
                                                      certification_date, modification_date)
SELECT 0, 'SUCCESS', 'SUCCESS', 'application',
       'import_migration',i.id_identity, ra.key_name, a.attribute_value,
       c.certifier_code, c.certificate_date, i.date_create
FROM identitystore_identity i
         JOIN identitystore_identity_attribute a ON a.id_identity=i.id_identity
         JOIN identitystore_identity_attribute_certificate c ON a.id_certification = c.id_attribute_certificate
         JOIN identitystore_ref_attribute ra ON ra.id_attribute=a.id_attribute
         LEFT OUTER JOIN identitystore_identity_attribute_history ah ON ( ah.id_identity=i.id_identity AND ah.attribute_key=ra.key_name AND ah.change_type = 0)
WHERE ah.id_identity IS NULL
  AND  a.lastupdate_date < '2024-01-30 00:00:01.000';