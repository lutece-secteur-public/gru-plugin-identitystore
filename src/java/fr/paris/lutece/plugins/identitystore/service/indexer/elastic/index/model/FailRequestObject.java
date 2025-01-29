package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

@JsonInclude( JsonInclude.Include.NON_NULL )
public class FailRequestObject
{
    private String errorType;
    private String errorMessage;
    private int errorCode;
    private String concernedRequest;
    private String concernedResponse;
    private Timestamp creationDate;

    public FailRequestObject(String errorType, String errorMessage, int errorCode, String concernedRequest, String concernedResponse, Timestamp creationDate)
    {
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.concernedRequest = concernedRequest;
        this.concernedResponse = concernedResponse;
        this.creationDate = creationDate;
    }

    public FailRequestObject()
    {
    }

    public String getErrorType()
    {
        return errorType;
    }

    public void setErrorType(String errorType)
    {
        this.errorType = errorType;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public int getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(int errorCode)
    {
        this.errorCode = errorCode;
    }

    public String getConcernedRequest()
    {
        return concernedRequest;
    }

    public void setConcernedRequest(String concernedRequest)
    {
        this.concernedRequest = concernedRequest;
    }

    public String getConcernedResponse()
    {
        return concernedResponse;
    }

    public void setConcernedResponse(String concernedResponse)
    {
        this.concernedResponse = concernedResponse;
    }

    public Timestamp getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate)
    {
        this.creationDate = creationDate;
    }
}
