/*
 * Copyright (c) 2002-2024, City of Paris
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
package fr.paris.lutece.plugins.identitystore.business.rules.duplicate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.i18n.I18nService;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The aim of this class is to define a rule to be applied when performing duplicate identities search.<br>
 * It must be applied as follows. <br>
 * <br>
 * Eligible identities for the search:
 * <ul>
 * <li><strong>_listCheckedAttributes</strong> is the list of {@link AttributeKey} that are checked by the rule</li>
 * <li><strong>_nNbFilledAttributes</strong> is the minimum number of checked attributes that must be present and valued in the Identities that are selected to
 * execute the rule</li>
 * </ul>
 * The search must return the identities that match:
 * <ul>
 * <li><strong>_nNbEqualAttributes</strong> is the exact number of checked attributes that must be strictly equal in the result</li>
 * <li><strong>_nNbMissingAttributes</strong> is the maximum number of checked attributes that can be absent in the result</li>
 * <li><strong>_listAttributeTreatments</strong> defines a list of conditions (APPROXIMATED or DIFFERENT) on the checked attributes that are not strictly
 * equal.</li>
 * </ul>
 * The logical rules are: <br>
 * <ul>
 * <li>Identities selected for the search:</li>
 *
 * <pre>
 * _listCheckedAttributes && _nNbFilledAttribute
 * </pre>
 *
 * <li>Result of the search:</li>
 *
 * <pre>
 *      _listCheckedAttributes && _nNbEqualAttributes && _nNbMissingAttributes && (_listAttributeTreatments[0] || _listAttributeTreatments[1] || ... || _listAttributeTreatments[n])
 * </pre>
 * </ul>
 * <p>
 * E.g:
 *
 * <pre>
 * | checked | filled | equal | missing | treatments                                                  | result                                                                                                                                                                  |
 * |---------|--------|-------|---------|-------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
 * | 7       | 7      | 7     | 0       | empty                                                       | Every Identity that matches exactly the given attributes                                                                                                                |
 * | 7       | 7      | 6     | 0       | empty                                                       | Every Identity that have 6 matching attributes over the 7 required, the 7th must exist but its value is discarded                                                       |
 * | 7       | 7      | 6     | 1       | empty                                                       | Every Identity that have 6 matching attributes over the 7 required, the 7th can be missing                                                                              |
 * | 7       | 7      | 6     | 0       | attribute_key_a : DIFFERENT, attribute_key_b : APPROXIMATED | Every Identity that have 6 matching attributes over the 7 required, the 7th can be attribute_key_a with a different value OR attribute_key_b with an approximated value |
 *
 * </pre>
 */
public class DuplicateRule implements Serializable
{
    private int _nId;
    private String _strName;
    private String _strCode;
    private String _strDescription;
    private List<AttributeKey> _listCheckedAttributes = new ArrayList<>( );
    private int _nNbFilledAttributes;
    private int _nNbEqualAttributes;
    private int _nNbMissingAttributes;
    private List<DuplicateRuleAttributeTreatment> _listAttributeTreatments = new ArrayList<>( );
    private int _nPriority;
    private boolean _bActive;
    private boolean _bDaemon;
    private Timestamp _dateDaemonLastExecDate;
    private int _nDetectionLimit = -1;
    @JsonIgnore
    private Date creationDate;
    @JsonIgnore
    private Date lastUpdateDate;
    @JsonIgnore
    private String _strAuthorName;

    public void validate( ) throws IdentityStoreException
    {
        int i = this.getNbEqualAttributes( ) + this.getNbMissingAttributes( );
        final int nbCheckedAttributes = this.getCheckedAttributes( ).size( );
        if ( !_listAttributeTreatments.isEmpty( ) )
        {
            for ( final DuplicateRuleAttributeTreatment treatment : _listAttributeTreatments )
            {
                if ( nbCheckedAttributes != i + treatment.getAttributes( ).size( ) )
                {
                    throw new IdentityStoreException( this.getValidationErrorMessage( ) );
                }
            }
        }
        if ( nbCheckedAttributes == i )
        {
            throw new IdentityStoreException( this.getValidationErrorMessage( ) );
        }
    }

    private String getValidationErrorMessage( )
    {
        return I18nService.getLocalizedString( "identitystore.message.error.duplicaterule.validation", Locale.getDefault( ) );
    }

    public int getId( )
    {
        return _nId;
    }

    public void setId( int _nId )
    {
        this._nId = _nId;
    }

    public String getName( )
    {
        return _strName;
    }

    public void setName( String _strName )
    {
        this._strName = _strName;
    }

    public String getCode( )
    {
        return _strCode;
    }

    public void setCode( String _strCode )
    {
        this._strCode = _strCode;
    }

    public String getDescription( )
    {
        return _strDescription;
    }

    public void setDescription( String _strDescription )
    {
        this._strDescription = _strDescription;
    }

    public List<AttributeKey> getCheckedAttributes( )
    {
        return _listCheckedAttributes;
    }

    public void setCheckedAttributes( List<AttributeKey> _listCheckedAttributes )
    {
        this._listCheckedAttributes = _listCheckedAttributes;
    }

    public int getNbFilledAttributes( )
    {
        return _nNbFilledAttributes;
    }

    public void setNbFilledAttributes( int _nNbFilledAttributes )
    {
        this._nNbFilledAttributes = _nNbFilledAttributes;
    }

    public int getNbEqualAttributes( )
    {
        return _nNbEqualAttributes;
    }

    public void setNbEqualAttributes( int _nNbEqualAttributes )
    {
        this._nNbEqualAttributes = _nNbEqualAttributes;
    }

    public int getNbMissingAttributes( )
    {
        return _nNbMissingAttributes;
    }

    public void setNbMissingAttributes( int _nNbMissingAttributes )
    {
        this._nNbMissingAttributes = _nNbMissingAttributes;
    }

    public List<DuplicateRuleAttributeTreatment> getAttributeTreatments( )
    {
        return _listAttributeTreatments;
    }

    public void setAttributeTreatments( List<DuplicateRuleAttributeTreatment> _listAttributeTreatments )
    {
        this._listAttributeTreatments = _listAttributeTreatments;
    }

    public int getPriority( )
    {
        return _nPriority;
    }

    public void setPriority( int _priority )
    {
        this._nPriority = _priority;
    }

    public boolean isActive( )
    {
        return _bActive;
    }

    public void setActive( boolean _bActive )
    {
        this._bActive = _bActive;
    }

    public boolean isDaemon( )
    {
        return _bDaemon;
    }

    public void setDaemon( boolean _bDaemon )
    {
        this._bDaemon = _bDaemon;
    }

    public Timestamp getDaemonLastExecDate( )
    {
        return _dateDaemonLastExecDate;
    }

    public void setDaemonLastExecDate( Timestamp _dateDaemonLastExecDate )
    {
        this._dateDaemonLastExecDate = _dateDaemonLastExecDate;
    }

    /**
     * Default value -1 means unlimited detection.
     */
    public int getDetectionLimit( )
    {
        return _nDetectionLimit;
    }

    public void setDetectionLimit( int _nDetectionLimit )
    {
        this._nDetectionLimit = _nDetectionLimit;
    }

    public String getAuthorName()
    {
        return _strAuthorName;
    }

    public void setAuthorName(String _strAuthorName)
    {
        this._strAuthorName = _strAuthorName;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
