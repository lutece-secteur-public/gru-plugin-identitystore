package fr.paris.lutece.plugins.identitystore.service.network;

@FunctionalInterface
public interface NetworkSupplier<T> {
    T apply ( ) throws Exception;
}
