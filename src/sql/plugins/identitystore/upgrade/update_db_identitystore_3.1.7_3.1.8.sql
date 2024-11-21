UPDATE identitystore_service_contract
SET service_type = 'FO non Lutèce'
WHERE service_type = 'FO Métier';

UPDATE identitystore_service_contract
SET service_type = 'BO non Lutèce'
WHERE service_type = 'BO Métier';