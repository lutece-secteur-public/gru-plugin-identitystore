/*
 * Copyright (c) 2002-2026, City of Paris
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
package fr.paris.lutece.plugins.identitystore.service.identity.batch;

/**
 * Status object for the UnicityHashBatch task.
 * Holds progress counters, logs, and running state.
 */
public class UnicityHashCodeBatchStatus
{
    private long _lProcessed;
    private long _lTotal;
    private long _lErrors;
    private long _lSkipped;
    private long _lDuplicates;
    private boolean _bRunning;
    private long _lStartTime;
    private long _lEndTime;
    StringBuilder _sbLogs = new StringBuilder( );

    public long getProcessed( )
    {
        return _lProcessed;
    }

    public void setProcessed( long processed )
    {
        _lProcessed = processed;
    }

    public void incrementProcessed( )
    {
        _lProcessed++;
    }

    public long getTotal( )
    {
        return _lTotal;
    }

    public void setTotal( long total )
    {
        _lTotal = total;
    }

    public long getErrors( )
    {
        return _lErrors;
    }

    public void setErrors( long errors )
    {
        _lErrors = errors;
    }

    public void incrementErrors( )
    {
        _lErrors++;
    }

    public long getSkipped( )
    {
        return _lSkipped;
    }

    public void setSkipped( long skipped )
    {
        _lSkipped = skipped;
    }

    public void incrementSkipped( )
    {
        _lSkipped++;
    }

    public long getDuplicates( )
    {
        return _lDuplicates;
    }

    public void setDuplicates( long duplicates )
    {
        _lDuplicates = duplicates;
    }

    public void incrementDuplicates( )
    {
        _lDuplicates++;
    }

    public boolean isRunning( )
    {
        return _bRunning;
    }

    public void setRunning( boolean running )
    {
        _bRunning = running;
    }

    public long getStartTime( )
    {
        return _lStartTime;
    }

    public void setStartTime( long startTime )
    {
        _lStartTime = startTime;
    }

    public long getEndTime( )
    {
        return _lEndTime;
    }

    public void setEndTime( long endTime )
    {
        _lEndTime = endTime;
    }

    /**
     * Returns elapsed time in seconds.
     * If running, calculates from now. If finished, uses the stored end time.
     */
    public long getElapsed( )
    {
        if ( _lStartTime == 0 )
        {
            return 0;
        }
        if ( _lEndTime > 0 )
        {
            return ( _lEndTime - _lStartTime ) / 1000;
        }
        return ( System.currentTimeMillis( ) - _lStartTime ) / 1000;
    }

    public double getProgress( )
    {
        return _lTotal > 0 ? ( ( double ) _lProcessed / _lTotal ) * 100.0 : 0;
    }

    public String getLogs( )
    {
        return _sbLogs.toString( );
    }

    public void log( final String message )
    {
        _sbLogs.append( message ).append( "\n" );
    }
}