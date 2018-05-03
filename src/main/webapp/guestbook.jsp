<%@ page contentType="application/json;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="edu.umd.clarice.PodioToJSON" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<% PodioToJSON client = new PodioToJSON();%>
<%= client.connect() %>

<%-- //[END all]--%>
