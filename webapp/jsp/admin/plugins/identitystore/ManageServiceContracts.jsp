<jsp:useBean id="manageservicecontractServiceContract" scope="session" class="fr.paris.lutece.plugins.identitystore.web.ServiceContractJspBean" />
<% String strContent = manageservicecontractServiceContract.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
