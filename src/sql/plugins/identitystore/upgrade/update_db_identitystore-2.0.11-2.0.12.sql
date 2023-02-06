--
-- Structure for table identitystore_service_contract
--
DROP TABLE IF EXISTS identitystore_service_contract;
CREATE TABLE identitystore_service_contract (
id_service_contract int AUTO_INCREMENT,
id_client_app int(6) NOT NULL,
name varchar(50) default '' NOT NULL,
organizational_entity varchar(50) default '' NOT NULL,
responsible_name varchar(50) default '' NOT NULL,
contact_name varchar(50) default '' NOT NULL,
service_type varchar(50) default '' NOT NULL,
authorized_read SMALLINT NOT NULL,
authorized_deletion SMALLINT NOT NULL,
authorized_search SMALLINT NOT NULL,
authorized_import SMALLINT NOT NULL,
authorized_export SMALLINT NOT NULL,
PRIMARY KEY (id_service_contract)
);
ALTER TABLE identitystore_service_contract ADD CONSTRAINT fk_service_contract_id_client_app FOREIGN KEY ( id_client_app ) REFERENCES identitystore_client_application ( id_client_app );

--
-- Structure for table identitystore_attribute_requirement
--
DROP TABLE IF EXISTS identitystore_attribute_requirement;
CREATE TABLE identitystore_attribute_requirement (
id_attribute_requirement int AUTO_INCREMENT,
id_service_contract int(6) NOT NULL,
PRIMARY KEY (id_attribute_requirement, id_service_contract)
);
ALTER TABLE identitystore_attribute_requirement ADD CONSTRAINT fk_attribute_requirement_id_service_contract FOREIGN KEY ( id_service_contract ) REFERENCES identitystore_service_contract ( id_service_contract );

--
-- Structure for table identitystore_attribute_certification
--
DROP TABLE IF EXISTS identitystore_attribute_certification;
CREATE TABLE identitystore_attribute_certification (
id_attribute_certification int AUTO_INCREMENT,
id_service_contract int(6) NOT NULL,
PRIMARY KEY (id_attribute_certification, id_service_contract)
);
ALTER TABLE identitystore_attribute_certification ADD CONSTRAINT fk_attribute_certification_id_service_contract FOREIGN KEY ( id_service_contract ) REFERENCES identitystore_service_contract ( id_service_contract );

ALTER TABLE identitystore_attribute_right DROP CONSTRAINT fk_attribute_right_id_client_app;
ALTER TABLE identitystore_attribute_right CHANGE id_client_app id_service_contract int(6) NOT NULL;
ALTER TABLE identitystore_attribute_right ADD CONSTRAINT fk_attribute_right_id_service_contract FOREIGN KEY ( id_service_contract ) REFERENCES identitystore_service_contract ( id_service_contract );
ALTER TABLE identitystore_attribute_right DROP COLUMN readable;
ALTER TABLE identitystore_attribute_right DROP COLUMN writable;
ALTER TABLE identitystore_attribute_right DROP COLUMN certifiable;
ALTER TABLE identitystore_attribute_right DROP COLUMN mandatory;

ALTER TABLE identitystore_attribute ADD COLUMN certifiable SMALLINT;
ALTER TABLE identitystore_attribute ADD COLUMN pivot SMALLINT;