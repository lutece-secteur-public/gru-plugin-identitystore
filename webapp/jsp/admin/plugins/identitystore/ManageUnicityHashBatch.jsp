<jsp:useBean id="unicityHashBatch" scope="session" class="fr.paris.lutece.plugins.identitystore.web.UnicityHashBatchJspBean" />
<% String strContent = unicityHashBatch.processController( request, response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
