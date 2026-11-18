-- décompte total avant : 
\timing on
select * from COUNT_TOTAL_MISSING_COUNTRY_CODES();


-- 1/ birthdates : 
-- test
select * from COUNT_MALFORMED_BIRTHDATES();

-- update  : 
call UPDATE_MALFORMED_BIRTHDATES();


-- 2/ formatages des libellés de pays saisis
-- test
select * from COUNT_UNTRIMMED_ATTRIBUTES(7);
select * from COUNT_UNCAPPED_ATTRIBUTE(7);


-- update
call FORMAT_TRIM_ATTRIBUTES(7);
call FORMAT_UPPERCASE_ATTRIBUTES(7);
\i fixCountryNames.sql ;

-- 3/ ajout codes pays manquants
-- test 
select * from COUNT_TOTAL_MISSING_COUNTRY_CODES();
select * from GET_MISSING_COUNTRY_CODES();

-- update
call UPDATE_COUNTRY_CODE_BY_LIBELLE();

select * from COUNT_TOTAL_MISSING_COUNTRY_CODES();

-- ajout cas particulier France (inclu ci-dessus finalement)


-- 4/ ajout codes commune manquants
-- test
select * from COUNT_TOTAL_MISSING_CITY_CODES();
select * from GET_MISSING_CITY_CODES();
select * from COUNT_UNTRIMMED_ATTRIBUTES(6);

-- update
call FORMAT_TRIM_ATTRIBUTES(6);
call UPDATE_CITY_CODE_BY_LIBELLE();

-- 5/ suppression libellés non resolvables
SELECT * FROM LIST_UNRESOLVED_COUNTRY_LABELS();   -- libellés de pays sans code, non résolus
CALL DELETE_UNRESOLVED_COUNTRY_LABELS();          -- suppression + historique

-- 6/ suppression communes non resolvables
SELECT * FROM LIST_UNRESOLVED_CITY_LABELS();      -- libellés de communes (nés en France) sans code, non résolus
CALL DELETE_UNRESOLVED_CITY_LABELS();


-- 7/ controle des identités avec code commune sans code pays france...
 select count(*) from identitystore_identity_attribute a where a.id_attribute = 56 and not exists ( select * from identitystore_identity_attribute a2 where a2.id_identity = a.id_identity and a2.id_attribute = 57 and a2.attribute_value ='99100') limit 10;


-- pour plus tard : get wrong country labels, init cap des birthplace label, 



\timing off


