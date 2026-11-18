--Affiche les identités dont la date de mise à jour est inférieure à la dernière date de modification d'attribut
WITH attribute AS (
    SELECT MAX(attribute.lastupdate_date) AS last_update, id_identity
    FROM identitystore_identity_attribute attribute
    GROUP BY id_identity
)
SELECT DISTINCT identity.last_update_date AS identity_update, attribute.last_update AS attribute_update, count(*)
FROM identitystore_identity identity
    JOIN attribute ON identity.id_identity = attribute.id_identity
WHERE identity.last_update_date < attribute.last_update
GROUP BY last_update_date, attribute.last_update
ORDER BY last_update_date;

--Affiche les identités dont la date de création est inférieure à celle de MonParis
WITH creation AS (
    SELECT MAX(monparis.creation_date) AS creation_date, guid
    FROM identitystore_identity_monparis monparis
    GROUP BY guid
)
SELECT DISTINCT identity.date_create AS identity_update, creation.creation_date AS attribute_update, count(*)
FROM identitystore_identity identity
         JOIN creation ON identity.connection_id = creation.guid
WHERE identity.date_create < creation.creation_date
GROUP BY date_create, creation.creation_date
ORDER BY date_create;

--Met à jour les identités dont la date de mise à jour est inférieure à la dernière date de modification d'attribut
WITH attribute AS (
    SELECT MAX(attribute.lastupdate_date) AS last_update, id_identity
    FROM identitystore_identity_attribute attribute
    GROUP BY id_identity
)
UPDATE identitystore_identity AS identity
SET last_update_date = attribute.last_update
FROM attribute
WHERE identity.id_identity = attribute.id_identity
  AND identity.last_update_date < attribute.last_update;

--Met à jour les identités dont la date de création est inférieure à celle de MonParis
WITH creation AS (
    SELECT MAX(monparis.creation_date) AS creation_date, guid
    FROM identitystore_identity_monparis monparis
    GROUP BY guid
)
UPDATE identitystore_identity AS identity
SET date_create = creation.creation_date
FROM creation
WHERE identity.connection_id = creation.guid
  AND identity.date_create < creation.creation_date;