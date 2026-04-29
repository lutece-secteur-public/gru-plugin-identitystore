package fr.paris.lutece.plugins.identitystore.utils;

import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Provides static method to compute a hash code based on a map of key/value.
 */
public final class UnicityHasher {

    private static final byte[] SEPARATOR = "|".getBytes(StandardCharsets.UTF_8);
    private static final char[] HEX = "0123456789abcdef".toCharArray();
    public static final String ALGORITHM = "SHA-256";


    /**
     * Compute a hash code, based on a map of key/value.
     * <br>
     * Before computing the 128 bits SHA-256 hash, the map is sorted by keys,
     * and the values are trimmed and lowercased, then computed in a string with a | separator.
     * <br>
     * The computation follows the schema: key=value.trim().toLowerCase()
     *
     * @param values map of key/value to be hashed
     * @return 32 chars hex string (128 bits)
     */
    public static String computeHash( final Map<String, String> values ) throws IdentityStoreException
    {
        try {
            final Map<String, String> sortedValues = new TreeMap<>( values );
            final MessageDigest md = MessageDigest.getInstance( ALGORITHM );
            boolean first = true;

            for ( final Map.Entry<String, String> entry : sortedValues.entrySet( ) )
            {
                if ( !first )
                {
                    md.update( SEPARATOR );
                }
                md.update( entry.getKey( ).getBytes( StandardCharsets.UTF_8 ) );
                md.update( ( byte ) '=' );
                if ( entry.getValue( ) != null )
                {
                    md.update( entry.getValue( ).trim( ).toLowerCase( ).getBytes( StandardCharsets.UTF_8 ) );
                }
                first = false;
            }

            final byte[ ] hash = md.digest( );
            return toString( hash, 16 );
        }
        catch ( final NoSuchAlgorithmException e )
        {
            throw new IdentityStoreException( e.getMessage( ) );
        }
    }

    /**
     * Convert the hash bytes to a hex string compatible with the unicity column
     * that may also contain UUID default values.
     *
     * @param bytes  SHA-256 hash bytes to be converted
     * @param length number of bytes to convert (truncates the SHA-256 result)
     * @return hex string of length x 2 characters
     */
    private static String toString( final byte[] bytes, final int length )
    {
        final char[ ] buf = new char[ length * 2 ];
        for ( int i = 0; i < length; i++ )
        {
            buf[ i * 2 ] = HEX[ ( bytes[ i ] >> 4 ) & 0x0F ];
            buf[ i * 2 + 1 ] = HEX[ bytes[ i ] & 0x0F ];
        }
        return new String( buf );
    }
}
