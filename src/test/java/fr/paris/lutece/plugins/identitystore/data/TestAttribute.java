/*
 * Copyright (c) 2002-2023, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.identitystore.data;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.Objects;

@JsonInclude( JsonInclude.Include.NON_NULL )
public class TestAttribute
{
    /**
     * Clef de l'attribut en base
     */
    protected String key;

    /**
     * Valeur de l'attribut
     */
    protected String value;

    /**
     * Type de l'attribut
     */
    protected String type;

    /**
     * Niveau du processus de certification
     */
    protected Integer certificationLevel;

    /**
     * Code de l'entité certifiante
     */
    protected String certifier;

    /**
     * Date de certification
     */
    protected Date certificationDate;

    public String getKey( )
    {
        return key;
    }

    public void setKey( String key )
    {
        this.key = key;
    }

    public String getValue( )
    {
        return value;
    }

    public void setValue( String value )
    {
        this.value = value;
    }

    public String getType( )
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public Integer getCertificationLevel( )
    {
        return certificationLevel;
    }

    public void setCertificationLevel( Integer certificationLevel )
    {
        this.certificationLevel = certificationLevel;
    }

    public String getCertifier( )
    {
        return certifier;
    }

    public void setCertifier( String certifier )
    {
        this.certifier = certifier;
    }

    public Date getCertificationDate( )
    {
        return certificationDate;
    }

    public void setCertificationDate( Date certificationDate )
    {
        this.certificationDate = certificationDate;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
            return true;
        if ( o == null || getClass( ) != o.getClass( ) )
            return false;
        final TestAttribute that = (TestAttribute) o;
        return Objects.equals( key, that.key ) && Objects.equals( value, that.value ) && Objects.equals( type, that.type )
                && Objects.equals( certificationLevel, that.certificationLevel ) && Objects.equals( certifier, that.certifier )
                && Objects.equals( certificationDate, that.certificationDate );
    }

    @Override
    public int hashCode( )
    {
        return Objects.hash( key, value, type, certificationLevel, certifier, certificationDate );
    }
}
