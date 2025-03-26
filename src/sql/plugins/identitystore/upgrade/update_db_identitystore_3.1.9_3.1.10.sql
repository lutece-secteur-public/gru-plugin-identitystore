ALTER TABLE identitystore_ref_attribute ADD COLUMN creation_date  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE identitystore_ref_attribute ADD COLUMN last_update_date TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE identitystore_ref_attribute ADD COLUMN author_name VARCHAR(255);

ALTER TABLE identitystore_client_application ADD COLUMN creation_date  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE identitystore_client_application ADD COLUMN last_update_date TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE identitystore_client_application ADD COLUMN author_name VARCHAR(255);

ALTER TABLE identitystore_duplicate_rule ADD COLUMN creation_date  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE identitystore_duplicate_rule ADD COLUMN last_update_date TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE identitystore_duplicate_rule ADD COLUMN author_name VARCHAR(255);

ALTER TABLE identitystore_service_contract ADD COLUMN creation_date  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE identitystore_service_contract ADD COLUMN last_update_date TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE identitystore_service_contract ADD COLUMN author_name VARCHAR(255);