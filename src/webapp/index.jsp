<?xml version="1.0"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<html>
	<head>
		<title>Diggwatcher</title>
		<link rel="stylesheet" href="/diggwatch/style.css"/>
	</head>
	<body>
		<p class="descr">
			This application is used to find, browse and track your digg comments
			over the last 14 days.
		</p>
		<form method="get" action="cr">
			<c:if test="${not empty param.derror}">
				<p class="error">Digg Error:  <c:out value="${param.derror}"/></p>
			</c:if>
			<c:if test="${not empty param.error}">
				<p class="error">Internal Error:  <c:out value="${param.error}"/></p>
			</c:if>
			<p>
				Enter your digg username: <input type="text" name="user"/>
			</p>
		</form>
	</body>
</html>
