package fr.paris.lutece.plugins.identitystore.utils;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import junit.framework.TestCase;

import java.util.Map;

public class UnicityHasherTest extends TestCase {
    private static final Map<String, String> identity_fr_1 = Map.of(
            Constants.PARAM_GENDER, "0",
            Constants.PARAM_FIRST_NAME, "Jean Charles",
            Constants.PARAM_FAMILY_NAME, "Dupont",
            Constants.PARAM_BIRTH_DATE, "10/10/2000",
            Constants.PARAM_BIRTH_COUNTRY_CODE, "99100",
            Constants.PARAM_BIRTH_PLACE_CODE, "95412"
    );

    private static final Map<String, String> identity_fr_2 = Map.of(
            Constants.PARAM_GENDER, "0",
            Constants.PARAM_FIRST_NAME, "Jean Charles",
            Constants.PARAM_FAMILY_NAME, "Dupont",
            Constants.PARAM_BIRTH_DATE, "10/10/2000",
            Constants.PARAM_BIRTH_COUNTRY_CODE, "99100",
            Constants.PARAM_BIRTH_PLACE_CODE, "95412"
    );

    private static final Map<String, String> identity_ext_1 = Map.of(
            Constants.PARAM_GENDER, "0",
            Constants.PARAM_FIRST_NAME, "Jean Charles",
            Constants.PARAM_FAMILY_NAME, "Dupont",
            Constants.PARAM_BIRTH_DATE, "10/10/2000",
            Constants.PARAM_BIRTH_COUNTRY_CODE, "99300"
    );

    private static final Map<String, String> identity_ext_2 = Map.of(
            Constants.PARAM_GENDER, "0",
            Constants.PARAM_FIRST_NAME, "Jean Charles",
            Constants.PARAM_FAMILY_NAME, "Dupont",
            Constants.PARAM_BIRTH_DATE, "10/10/2000",
            Constants.PARAM_BIRTH_COUNTRY_CODE, "99300"
    );

    public void testFranceBorn() throws IdentityStoreException {
        assertEquals(UnicityHasher.computeHash(identity_fr_1), UnicityHasher.computeHash(identity_fr_2));
    }

    public void testForeignBorn() throws IdentityStoreException {
        assertEquals(UnicityHasher.computeHash(identity_ext_1), UnicityHasher.computeHash(identity_ext_2));
    }

    public void testNotEqual() throws IdentityStoreException {
        assertNotSame(UnicityHasher.computeHash(identity_ext_1), UnicityHasher.computeHash(identity_fr_2));
    }

}
