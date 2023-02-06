
--
-- Data for table core_admin_right
--
DELETE FROM core_admin_right WHERE id_right = 'IDENTITYSTORE_MANAGEMENT';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES 
('IDENTITYSTORE_MANAGEMENT','identitystore.adminFeature.ManageIdentities.name',1,'jsp/admin/plugins/identitystore/ManageIdentities.jsp','identitystore.adminFeature.ManageIdentities.description',0,'identitystore',NULL,NULL,NULL,4);


--
-- Data for table core_user_right
--
DELETE FROM core_user_right WHERE id_right = 'IDENTITYSTORE_MANAGEMENT';
INSERT INTO core_user_right (id_right,id_user) VALUES ('IDENTITYSTORE_MANAGEMENT',1);


--
-- Data for table core_admin_right
--
DELETE FROM core_admin_right WHERE id_right = 'IDENTITYSTORE_ADMIN_MANAGEMENT';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES 
('IDENTITYSTORE_ADMIN_MANAGEMENT','identitystore.adminFeature.AdminIdentities.name',1,'jsp/admin/plugins/identitystore/ManageClientApplications.jsp','identitystore.adminFeature.AdminIdentities.description',0,'identitystore',NULL,NULL,NULL,4);

--
-- Data for table core_user_right
--
DELETE FROM core_user_right WHERE id_right = 'IDENTITYSTORE_ADMIN_MANAGEMENT';
INSERT INTO core_user_right (id_right,id_user) VALUES ('IDENTITYSTORE_ADMIN_MANAGEMENT',1);

--
-- Data for table core_admin_right
--
DELETE FROM core_admin_right WHERE id_right = 'IDENTITYSTORE_REF_MANAGEMENT';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES
    ('IDENTITYSTORE_REF_MANAGEMENT','identitystore.adminFeature.ManageProcessusRef.name',1,'jsp/admin/plugins/identitystore/ManageRefAttributeCertificationProcessuss.jsp','identitystore.adminFeature.ManageProcessusRef.description',0,'identitystore',NULL,NULL,NULL,4);

--
-- Data for table core_user_right
--
DELETE FROM core_user_right WHERE id_right = 'IDENTITYSTORE_REF_MANAGEMENT';
INSERT INTO core_user_right (id_right,id_user) VALUES ('IDENTITYSTORE_REF_MANAGEMENT',1);


INSERT INTO core_datastore ( entity_key , entity_value ) VALUES ( 'core.plugins.status.identitystore.installed' , 'true' );