-- Enhance queries
CREATE INDEX identitystore_identity_master_id ON identitystore_identity (id_master_identity);
CREATE INDEX identitystore_quality_suspicious_identity_cuid ON identitystore_quality_suspicious_identity (customer_id);
CREATE INDEX identitystore_identity_expiration_date ON identitystore_identity (expiration_date);

-- Remove unused daemon
DELETE FROM core_datastore WHERE entity_key IN ('core.daemon.identityDuplicatesPurgeDaemon.interval', 'core.daemon.identityDuplicatesPurgeDaemon.onStartUp');