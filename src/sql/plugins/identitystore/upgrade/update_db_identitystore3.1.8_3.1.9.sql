ALTER TABLE identitystore_service_contract
    ALTER COLUMN name TYPE VARCHAR(255) USING name::VARCHAR(255);