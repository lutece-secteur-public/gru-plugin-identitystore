package fr.paris.lutece.plugins.identitystore.utils;

public interface LoggingTask {
    void debug( final String log );
    void info( final String log );
    void error( final String log );
}
