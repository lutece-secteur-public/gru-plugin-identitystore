-- Create new table identity_account
CREATE TABLE identitystore_identity_account (
  connection_id varchar(100) PRIMARY KEY,
  id_identity   int NOT NULL REFERENCES identitystore_identity(id_identity),
  current       smallint DEFAULT 0 NOT NULL,
  creation_date timestamp(3) default CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX identitystore_identity_account_id_identity ON identitystore_identity_account (id_identity);
CREATE INDEX identitystore_identity_account_connection_id ON identitystore_identity_account (lower(connection_id::text));

-- Insert current connection_id from the identity table, with date = identity's creation date
INSERT INTO identitystore_identity_account (connection_id, id_identity, current, creation_date)
  SELECT connection_id, id_identity, 1, date_create
  FROM identitystore_identity
  WHERE connection_id IS NOT NULL
    AND connection_id != ''
    AND connection_id != 'null'
    AND connection_id != 'NULL';

-- Drop the obsolete connection_id column from the identity table
ALTER TABLE identitystore_identity DROP COLUMN connection_id;

-- Update current connection_id with the correct date, if present in history
WITH last_guid_update_history AS (
    SELECT DISTINCT ON (customer_id)
        customer_id as cuid,
        metadata->>'new_guid' as current_guid,
        modification_date as update_date
    FROM identitystore_identity_history
    WHERE metadata->>'new_guid' IS NOT NULL
      AND metadata->>'new_guid' != ''
      AND metadata->>'new_guid' != 'null'
      AND metadata->>'new_guid' != 'NULL'
    ORDER BY customer_id, modification_date DESC
)
UPDATE identitystore_identity_account
SET creation_date = h.update_date
FROM last_guid_update_history h
WHERE h.current_guid = connection_id;

-- Insert previous connection_id (except first one) with their correct date, if present in history
INSERT INTO identitystore_identity_account (connection_id, id_identity, current, creation_date)
SELECT h.metadata->>'new_guid', i.id_identity, 0, h.modification_date
FROM identitystore_identity_history h
JOIN identitystore_identity i ON i.customer_id = h.customer_id
WHERE metadata->>'new_guid' IS NOT NULL
  AND metadata->>'new_guid' != ''
  AND metadata->>'new_guid' != 'null'
  AND metadata->>'new_guid' != 'NULL'
  AND metadata->>'new_guid' NOT IN (
    SELECT a.connection_id
    FROM identitystore_identity_account a
    WHERE a.current = 1
);

-- Insert first ever connection_id with the identity's creation date, if present in history
WITH first_guid_update_history AS (
    SELECT DISTINCT ON (i.id_identity)
        i.id_identity as id_identity,
        h.metadata->>'old_guid' as first_guid,
        i.date_create as date_create,
        h.modification_date as update_date
    FROM identitystore_identity_history h
    JOIN identitystore_identity i ON i.customer_id = h.customer_id
    WHERE h.metadata->>'old_guid' IS NOT NULL
      AND h.metadata->>'old_guid' != ''
      AND h.metadata->>'old_guid' != 'null'
      AND h.metadata->>'old_guid' != 'NULL'
    ORDER BY i.id_identity, h.modification_date ASC
)
INSERT INTO identitystore_identity_account (connection_id, id_identity, current, creation_date)
  SELECT first_guid, id_identity, 0, date_create
  FROM first_guid_update_history;