<jsp:useBean id="manageprocessusrefRefCertificationLevel" scope="session" class="fr.paris.lutece.plugins.identitystore.web.RefCertificationLevelJspBean" />
<% String strContent = manageprocessusrefRefCertificationLevel.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
