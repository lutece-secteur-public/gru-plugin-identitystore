UPDATE identitystore_ref_attribute
SET validation_regex = '(^[A-Za-zÀ-Üà-ü'']+$)|(^[A-Za-zÀ-Üà-ü'']+((-|\h)[A-Za-zÀ-Üà-ü'']+)+$)'
WHERE id_attribute = 2 OR id_attribute = 3 OR id_attribute = 4;