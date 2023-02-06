<jsp:useBean id="manageprocessusrefRefAttributeCertificationProcessus" scope="session" class="fr.paris.lutece.plugins.identitystore.web.RefAttributeCertificationProcessusJspBean" />
<% String strContent = manageprocessusrefRefAttributeCertificationProcessus.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
