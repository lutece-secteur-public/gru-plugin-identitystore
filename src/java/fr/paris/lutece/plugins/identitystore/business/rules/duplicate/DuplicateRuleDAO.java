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

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.KeyType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeTreatmentType;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * This class provides Data Access methods for IdentityAttribute objects
 */
public final class DuplicateRuleDAO implements IDuplicateRuleDAO
{
    // Constants
    private static final String SELECT_ATTRIBUTES = "id_rule, name, code, description, nb_filled_attributes, nb_equal_attributes, nb_missing_attributes, priority, active, daemon, daemon_last_exec_date, detection_limit, creation_date, last_update_date, author_name";
    /** Rule */
    private static final String SQL_QUERY_SELECT_RULE = "SELECT " + SELECT_ATTRIBUTES + " FROM identitystore_duplicate_rule WHERE id_rule = ? ";
    private static final String SQL_QUERY_SELECT_RULE_BY_NAME = "SELECT " + SELECT_ATTRIBUTES + " FROM identitystore_duplicate_rule WHERE name = ? ";
    private static final String SQL_QUERY_SELECT_RULE_BY_CODE = "SELECT " + SELECT_ATTRIBUTES + " FROM identitystore_duplicate_rule WHERE code = ? ";
    private static final String SQL_QUERY_INSERT_RULE = "INSERT INTO identitystore_duplicate_rule ( name, code, description, nb_filled_attributes, nb_equal_attributes, nb_missing_attributes, priority, active, daemon, detection_limit, creation_date, last_update_date, author_name ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE_RULE = "DELETE FROM identitystore_duplicate_rule WHERE id_rule = ?";
    private static final String SQL_QUERY_UPDATE_RULE = "UPDATE identitystore_duplicate_rule SET name = ?,  code = ?, description = ?, nb_filled_attributes = ?, nb_equal_attributes = ?, nb_missing_attributes = ?, priority = ?, active = ?, daemon = ?, daemon_last_exec_date = ?, detection_limit = ?, last_update_date = ?, author_name = ? WHERE id_rule = ? ";
    private static final String SQL_QUERY_UPDATE_RULE_LAST_DATE_AUTHOR = "UPDATE identitystore_duplicate_rule SET last_update_date = ?, author_name = ? WHERE id_rule = ? ";
    private static final String SQL_QUERY_SELECTALL_RULE = "SELECT " + SELECT_ATTRIBUTES + " FROM identitystore_duplicate_rule";

    /** Checked attributes */
    private static final String SQL_QUERY_SELECTALL_CHECKED_ATTRIBUTES_BY_RULE_ID = "SELECT ia.id_attribute, ia.name, ia.description, ia.key_name, ia.key_type, ia.key_weight, ia.certifiable, ia.pivot, ia.common_search_key FROM identitystore_duplicate_rule_checked_attributes idrca JOIN identitystore_ref_attribute ia on idrca.id_attribute = ia.id_attribute WHERE idrca.id_rule = ?";
    private static final String SQL_QUERY_INSERT_CHECKED_ATTRIBUTES = "INSERT INTO identitystore_duplicate_rule_checked_attributes(id_rule, id_attribute) VALUES (?,?)";
    private static final String SQL_QUERY_DELETE_CHECKED_ATTRIBUTES = "DELETE FROM identitystore_duplicate_rule_checked_attributes WHERE id_rule = ?";

    /** Attribute treatments */
    private static final String SQL_QUERY_SELECTALL_ATTRIBUTE_TREATMENTS_BY_RULE_ID = "SELECT idratn.id_attribute_treatment, idrat.type, ia.id_attribute, ia.name, ia.description, ia.key_name, ia.key_type, ia.key_weight, ia.certifiable, ia.pivot, ia.common_search_key "
            + "FROM identitystore_duplicate_rule_attribute_treatment idrat "
            + "    JOIN identitystore_duplicate_rule_attribute_treatment_nuples idratn on idrat.id_attribute_treatment = idratn.id_attribute_treatment "
            + "    JOIN identitystore_ref_attribute ia on idratn.id_attribute = ia.id_attribute " + "WHERE idrat.id_rule = ?";

    private static final String SQL_QUERY_INSERT_ATTRIBUTE_TREATMENTS = "INSERT INTO identitystore_duplicate_rule_attribute_treatment(type, id_rule) VALUES (?,?)";
    private static final String SQL_QUERY_DELETE_ATTRIBUTE_TREATMENTS = "DELETE FROM identitystore_duplicate_rule_attribute_treatment WHERE id_rule = ?";
    private static final String SQL_QUERY_INSERT_ATTRIBUTE_TREATMENT_NUPLES = "INSERT INTO identitystore_duplicate_rule_attribute_treatment_nuples(id_attribute_treatment, id_attribute) VALUES (?,?)";

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( DuplicateRule duplicateRule, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_RULE, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 0;
            daoUtil.setString( ++nIndex, duplicateRule.getName( ) );
            daoUtil.setString( ++nIndex, duplicateRule.getCode( ) );
            daoUtil.setString( ++nIndex, duplicateRule.getDescription( ) );
            daoUtil.setInt( ++nIndex, duplicateRule.getNbFilledAttributes( ) );
            daoUtil.setInt( ++nIndex, duplicateRule.getNbEqualAttributes( ) );
            daoUtil.setInt( ++nIndex, duplicateRule.getNbMissingAttributes( ) );
            daoUtil.setInt( ++nIndex, duplicateRule.getPriority( ) );
            daoUtil.setBoolean( ++nIndex, duplicateRule.isActive( ) );
            daoUtil.setBoolean( ++nIndex, duplicateRule.isDaemon( ) );
            daoUtil.setInt( ++nIndex, duplicateRule.getDetectionLimit( ) );
            daoUtil.setTimestamp( ++nIndex, new Timestamp( new java.util.Date( ).getTime( ) ) );
            daoUtil.setTimestamp( ++nIndex, new Timestamp( new java.util.Date( ).getTime( ) ) );
            daoUtil.setString( ++nIndex,  duplicateRule.getAuthorName( ) );
            daoUtil.executeUpdate( );
            if ( daoUtil.nextGeneratedKey( ) )
            {
                duplicateRule.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
            for ( final AttributeKey checkedAttribute : duplicateRule.getCheckedAttributes( ) )
            {
                this.insertCheckedAttribute( duplicateRule.getId( ), checkedAttribute.getId( ), plugin );
            }

            for ( final DuplicateRuleAttributeTreatment attributeTreatment : duplicateRule.getAttributeTreatments( ) )
            {
                this.insertAttributeTreatments( duplicateRule.getId( ), attributeTreatment, plugin );
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public DuplicateRule select( int nRuleId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_RULE, plugin ) )
        {
            daoUtil.setInt( 1, nRuleId );
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return this.getRule( daoUtil, 0, plugin );
            }
            else
            {
                return null;
            }
        }
    }

    @Override
    public List<DuplicateRule> selectAll( Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_RULE, plugin ) )
        {
            final List<DuplicateRule> rules = new ArrayList<>( );
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                rules.add( this.getRule( daoUtil, 0, plugin ) );
            }
            return rules;
        }
    }

    @Override
    public List<Integer> selectAllIds( Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_RULE, plugin ) )
        {
            final List<Integer> ids = new ArrayList<>( );
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                ids.add( daoUtil.getInt( 0 ) );
            }
            return ids;
        }
    }

    @Override
    public DuplicateRule selectByName( String ruleName, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_RULE_BY_NAME, plugin ) )
        {
            daoUtil.setString( 1, ruleName );
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return this.getRule( daoUtil, 0, plugin );
            }
            else
            {
                return null;
            }
        }
    }

    @Override
    public DuplicateRule selectByCode( String ruleCode, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_RULE_BY_CODE, plugin ) )
        {
            daoUtil.setString( 1, ruleCode );
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return this.getRule( daoUtil, 0, plugin );
            }
            else
            {
                return null;
            }
        }
    }

    @Override
    public List<String> selectAllCodes( Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_RULE, plugin ) )
        {
            final List<String> names = new ArrayList<>( );
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                names.add( daoUtil.getString( 3 ) );
            }
            return names;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void update( DuplicateRule duplicateRule, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_RULE, plugin ) )
        {
            int nIndex = 0;
            daoUtil.setString( ++nIndex, duplicateRule.getName( ) );
            daoUtil.setString( ++nIndex, duplicateRule.getCode( ) );
            daoUtil.setString( ++nIndex, duplicateRule.getDescription( ) );
            daoUtil.setInt( ++nIndex, duplicateRule.getNbFilledAttributes( ) );
            daoUtil.setInt( ++nIndex, duplicateRule.getNbEqualAttributes( ) );
            daoUtil.setInt( ++nIndex, duplicateRule.getNbMissingAttributes( ) );
            daoUtil.setInt( ++nIndex, duplicateRule.getPriority( ) );
            daoUtil.setBoolean( ++nIndex, duplicateRule.isActive( ) );
            daoUtil.setBoolean( ++nIndex, duplicateRule.isDaemon( ) );
            daoUtil.setTimestamp( ++nIndex, duplicateRule.getDaemonLastExecDate( ) );
            daoUtil.setInt( ++nIndex, duplicateRule.getDetectionLimit( ) );
            daoUtil.setTimestamp( ++nIndex, new Timestamp( new java.util.Date( ).getTime( ) ) );
            daoUtil.setString( ++nIndex,  duplicateRule.getAuthorName( ) );
            daoUtil.setInt( ++nIndex, duplicateRule.getId( ) );
            daoUtil.executeUpdate( );
            this.deleteCheckedAttributes( duplicateRule.getId( ), plugin );
            this.deleteAttributeTreatments( duplicateRule.getId( ), plugin );
            for ( final AttributeKey checkedAttribute : duplicateRule.getCheckedAttributes( ) )
            {
                this.insertCheckedAttribute( duplicateRule.getId( ), checkedAttribute.getId( ), plugin );
            }

            for ( final DuplicateRuleAttributeTreatment attributeTreatment : duplicateRule.getAttributeTreatments( ) )
            {
                this.insertAttributeTreatments( duplicateRule.getId( ), attributeTreatment, plugin );
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void updateLastUpdateAuthor( DuplicateRule duplicateRule, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_RULE_LAST_DATE_AUTHOR, plugin ) )
        {
            int nIndex = 0;
            daoUtil.setTimestamp( ++nIndex, new Timestamp( new java.util.Date( ).getTime( ) ) );
            daoUtil.setString( ++nIndex,  duplicateRule.getAuthorName( ) );
        }
    }

    private void deleteAttributeTreatments( int ruleId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_ATTRIBUTE_TREATMENTS, plugin ) )
        {
            daoUtil.setInt( 1, ruleId );
            daoUtil.executeUpdate( );
        }
    }

    private void deleteCheckedAttributes( int ruleId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_CHECKED_ATTRIBUTES, plugin ) )
        {
            daoUtil.setInt( 1, ruleId );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nRuleId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_RULE, plugin ) )
        {
            daoUtil.setInt( 1, nRuleId );
            daoUtil.executeUpdate( );
        }
    }

    private DuplicateRule getRule( final DAOUtil daoUtil, final int offset, Plugin plugin )
    {
        int nIndex = offset;
        final DuplicateRule duplicateRule = new DuplicateRule( );
        duplicateRule.setId( daoUtil.getInt( ++nIndex ) );
        duplicateRule.setName( daoUtil.getString( ++nIndex ) );
        duplicateRule.setCode( daoUtil.getString( ++nIndex ) );
        duplicateRule.setDescription( daoUtil.getString( ++nIndex ) );
        duplicateRule.setNbFilledAttributes( daoUtil.getInt( ++nIndex ) );
        duplicateRule.setNbEqualAttributes( daoUtil.getInt( ++nIndex ) );
        duplicateRule.setNbMissingAttributes( daoUtil.getInt( ++nIndex ) );
        duplicateRule.setPriority( daoUtil.getInt( ++nIndex ) );
        duplicateRule.setActive( daoUtil.getBoolean( ++nIndex ) );
        duplicateRule.setDaemon( daoUtil.getBoolean( ++nIndex ) );
        duplicateRule.setDaemonLastExecDate( daoUtil.getTimestamp( ++nIndex ) );
        duplicateRule.setDetectionLimit( daoUtil.getInt( ++nIndex ) );
        duplicateRule.getCheckedAttributes( ).addAll( this.getCheckedAttributes( duplicateRule.getId( ), plugin ) );
        duplicateRule.getAttributeTreatments( ).addAll( this.getAttributeTreatments( duplicateRule.getId( ), plugin ) );
        duplicateRule.setCreationDate( daoUtil.getDate( ++nIndex ) );
        duplicateRule.setLastUpdateDate( daoUtil.getDate( ++nIndex ) );
        duplicateRule.setAuthorName( daoUtil.getString( ++nIndex ) );
        return duplicateRule;
    }

    private List<AttributeKey> getCheckedAttributes( int ruleId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_CHECKED_ATTRIBUTES_BY_RULE_ID, plugin ) )
        {
            final List<AttributeKey> checkedAttributes = new ArrayList<>( );
            daoUtil.setInt( 1, ruleId );
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                checkedAttributes.add( this.getAttributeKey( daoUtil, 0 ) );
            }
            if ( !checkedAttributes.isEmpty( ) )
            {
                checkedAttributes.sort( Comparator.comparing( AttributeKey::getName ) );
            }
            return checkedAttributes;
        }
    }

    private List<DuplicateRuleAttributeTreatment> getAttributeTreatments( int ruleId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_ATTRIBUTE_TREATMENTS_BY_RULE_ID, plugin ) )
        {
            final List<DuplicateRuleAttributeTreatment> ruleAttributeTreatments = new ArrayList<>( );
            daoUtil.setInt( 1, ruleId );
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                int nIndex = 0;
                final int id = daoUtil.getInt( ++nIndex );
                final AttributeTreatmentType type = AttributeTreatmentType.valueOf( daoUtil.getString( ++nIndex ) );
                DuplicateRuleAttributeTreatment ruleAttributeTreatment;
                final Optional<DuplicateRuleAttributeTreatment> first = ruleAttributeTreatments.stream( ).filter( o -> o.getId( ) == id ).findFirst( );
                if ( first.isPresent( ) )
                {
                    ruleAttributeTreatment = first.get( );
                }
                else
                {
                    ruleAttributeTreatment = new DuplicateRuleAttributeTreatment( );
                    ruleAttributeTreatment.setId( id );
                    ruleAttributeTreatment.setType( type );
                    ruleAttributeTreatments.add( ruleAttributeTreatment );
                }
                ruleAttributeTreatment.getAttributes( ).add( this.getAttributeKey( daoUtil, nIndex ) );
            }
            if ( !ruleAttributeTreatments.isEmpty( ) )
            {
                ruleAttributeTreatments.sort( Comparator.comparing( DuplicateRuleAttributeTreatment::getType ) );
                ruleAttributeTreatments
                        .forEach( attributeTreatment -> attributeTreatment.getAttributes( ).sort( Comparator.comparing( AttributeKey::getName ) ) );
            }
            return ruleAttributeTreatments;
        }
    }

    private AttributeKey getAttributeKey( final DAOUtil daoUtil, final int offset )
    {
        final AttributeKey attributeKey = new AttributeKey( );
        int nIndex = offset;
        attributeKey.setId( daoUtil.getInt( ++nIndex ) );
        attributeKey.setName( daoUtil.getString( ++nIndex ) );
        attributeKey.setDescription( daoUtil.getString( ++nIndex ) );
        attributeKey.setKeyName( daoUtil.getString( ++nIndex ) );
        attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( ++nIndex ) ) );
        attributeKey.setKeyWeight( daoUtil.getInt( ++nIndex ) );
        attributeKey.setCertifiable( daoUtil.getBoolean( ++nIndex ) );
        attributeKey.setPivot( daoUtil.getBoolean( ++nIndex ) );
        attributeKey.setCommonSearchKeyName( daoUtil.getString( ++nIndex ) );
        return attributeKey;
    }

    private void insertCheckedAttribute( final int ruleId, final int attributeId, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_CHECKED_ATTRIBUTES, plugin ) )
        {
            int nIndex = 0;
            daoUtil.setInt( ++nIndex, ruleId );
            daoUtil.setInt( ++nIndex, attributeId );
            daoUtil.executeUpdate( );
        }
    }

    private void insertAttributeTreatments( final int ruleId, final DuplicateRuleAttributeTreatment attributeTreatment, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_ATTRIBUTE_TREATMENTS, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 0;
            daoUtil.setString( ++nIndex, attributeTreatment.getType( ).name( ) );
            daoUtil.setInt( ++nIndex, ruleId );
            daoUtil.executeUpdate( );
            if ( daoUtil.nextGeneratedKey( ) )
            {
                attributeTreatment.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
            for ( final AttributeKey attribute : attributeTreatment.getAttributes( ) )
            {
                this.insertNuple( attributeTreatment.getId( ), attribute.getId( ), plugin );
            }
        }
    }

    private void insertNuple( final int treatmentId, final int attributeId, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_ATTRIBUTE_TREATMENT_NUPLES, plugin ) )
        {
            int nIndex = 0;
            daoUtil.setInt( ++nIndex, treatmentId );
            daoUtil.setInt( ++nIndex, attributeId );
            daoUtil.executeUpdate( );
        }
    }
}
