package fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions;

import java.io.Serializable;
import java.util.Date;

public class SuspicionAction implements Serializable {
    private static final long serialVersionUID = 1L;

    private int _nId;
    private String _strCustomerId;
    private String _strActionType;
    private Date _date;

    public int getId() {
        return _nId;
    }

    public void setId(final int nId) {
        _nId = nId;
    }

    public String getCustomerId() {
        return _strCustomerId;
    }

    public void setCustomerId(final String strCustomerId) {
        _strCustomerId = strCustomerId;
    }

    public String getActionType() {
        return _strActionType;
    }

    public void setActionType(final String strActionType) {
        _strActionType = strActionType;
    }

    public Date getDate() {
        return _date;
    }

    public void setDate(final Date date) {
        _date = date;
    }
}
