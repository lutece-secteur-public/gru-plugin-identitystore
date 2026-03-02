ALTER TABLE identitystore_ref_attribute
    ADD COLUMN alternative_pivot SMALLINT DEFAULT 0;

ALTER TABLE identitystore_identity
    ADD COLUMN unicity_hash_code VARCHAR(255) NOT NULL UNIQUE DEFAULT gen_random_uuid();