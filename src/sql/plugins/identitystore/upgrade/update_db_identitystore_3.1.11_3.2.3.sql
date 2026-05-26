ALTER TABLE identitystore_ref_attribute
    ADD COLUMN alternative_pivot SMALLINT DEFAULT 0;

-- ALTER TABLE identitystore_identity  ADD COLUMN unicity_hash_code VARCHAR(36) NOT NULL UNIQUE DEFAULT gen_random_uuid();

-- version gros volumes, pour mises à jour sans interruption : 

-- ajout colonne
ALTER TABLE identitystore_identity ADD COLUMN unicity_hash_code VARCHAR(36);

-- ajout du défaut  
ALTER TABLE identitystore_identity ALTER COLUMN unicity_hash_code SET DEFAULT gen_random_uuid();
    
    
-- init pour ne pas avoir de NULL
CREATE OR REPLACE PROCEDURE backfill_unicity_hash(batch_size INTEGER DEFAULT 10000)
LANGUAGE plpgsql
AS $$
DECLARE
    rows_updated INTEGER;
    total_updated BIGINT := 0;
BEGIN
    LOOP
        UPDATE identitystore_identity
        SET unicity_hash_code = gen_random_uuid()
        WHERE id_identity IN (
            SELECT id_identity FROM identitystore_identity
            WHERE unicity_hash_code IS NULL
            LIMIT batch_size
        );

        GET DIAGNOSTICS rows_updated = ROW_COUNT;
        total_updated := total_updated + rows_updated;

        COMMIT;  -- commit après chaque batch

        RAISE NOTICE 'Batch: % lignes (total: %)', rows_updated, total_updated;

        EXIT WHEN rows_updated = 0;

        PERFORM pg_sleep(0.1);
    END LOOP;

    RAISE NOTICE 'Terminé. Total: % lignes', total_updated;
END;
$$;

-- init index temp
CREATE INDEX CONCURRENTLY idx_tmp_null_hash
    ON identitystore_identity (id_identity)
    WHERE unicity_hash_code IS NULL;

CALL backfill_unicity_hash(10000);

-- suppr
DROP INDEX idx_tmp_null_hash;






-- création de l'index unique
CREATE UNIQUE INDEX CONCURRENTLY idx_identity_unicity_hash_code
    ON identitystore_identity (unicity_hash_code);
    
    
    
-- ajout de la contrainte not null
-- Crée une contrainte CHECK validée mais NOT VALID (rapide, pas de scan)
ALTER TABLE identitystore_identity
    ADD CONSTRAINT unicity_hash_code_not_null
    CHECK (unicity_hash_code IS NOT NULL) NOT VALID;

-- Valide la contrainte (scan en SHARE UPDATE EXCLUSIVE, n'empêche pas les DML)
ALTER TABLE identitystore_identity
    VALIDATE CONSTRAINT unicity_hash_code_not_null;

-- Pose le NOT NULL sur la colonne : PG voit la CHECK validée et l'accepte sans rescan
ALTER TABLE identitystore_identity
    ALTER COLUMN unicity_hash_code SET NOT NULL;

-- Supprime la CHECK redondante
ALTER TABLE identitystore_identity
    DROP CONSTRAINT unicity_hash_code_not_null;
    

-- ajout "officiel"
ALTER TABLE identitystore_identity
    ADD CONSTRAINT unicity_hash_code_unique
    UNIQUE USING INDEX idx_identity_unicity_hash_code;      
    
  
