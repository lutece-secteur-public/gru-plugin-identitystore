package fr.paris.lutece.plugins.identitystore.business.identity;

public class IndicatorsActionsType
{
    private int changeType;
    private String changeStatus;
    private String authorType;
    private String clientCode;
    private int countActions;

    public int getChangeType()
    {
        return changeType;
    }

    public void setChangeType(int changeType)
    {
        this.changeType = changeType;
    }

    public String getChangeStatus()
    {
        return changeStatus;
    }

    public void setChangeStatus(String changeStatus)
    {
        this.changeStatus = changeStatus;
    }

    public String getAuthorType()
    {
        return authorType;
    }

    public void setAuthorType(String authorType)
    {
        this.authorType = authorType;
    }

    public String getClientCode()
    {
        return clientCode;
    }

    public void setClientCode(String clientCode)
    {
        this.clientCode = clientCode;
    }

    public int getCountActions()
    {
        return countActions;
    }

    public void setCountActions(int countActions)
    {
        this.countActions = countActions;
    }
}
