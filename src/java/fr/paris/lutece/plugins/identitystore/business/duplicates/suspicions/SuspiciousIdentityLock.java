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
package fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions;

import java.sql.Timestamp;

public class SuspiciousIdentityLock
{
    private boolean _bIsLocked;
    private Timestamp _dateLockEndDate;
    private String _strAuthorType;
    private String _strAuthorName;

    public boolean isLocked( )
    {
        return _bIsLocked;
    }

    public void setLocked( boolean _bIsLocked )
    {
        this._bIsLocked = _bIsLocked;
    }

    public Timestamp getLockEndDate( )
    {
        return _dateLockEndDate;
    }

    public void setLockEndDate( Timestamp _dateLockEndDate )
    {
        this._dateLockEndDate = _dateLockEndDate;
    }

    public String getAuthorType( )
    {
        return _strAuthorType;
    }

    public void setAuthorType( String _strAuthorType )
    {
        this._strAuthorType = _strAuthorType;
    }

    public String getAuthorName( )
    {
        return _strAuthorName;
    }

    public void setAuthorName( String _strAuthorName )
    {
        this._strAuthorName = _strAuthorName;
    }
}
