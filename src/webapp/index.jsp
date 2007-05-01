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
			This application is used to find, browse and track (via RSS) your digg
			comments and their replies over the last 14 days.
		</p>
		<p>
			<c:if test="${not empty param.error}">
				<p class="error">Internal Error:  <c:out value="${param.error}"/></p>
			</c:if>
		</p>
		<form method="get" action="ur">
			<c:if test="${not empty param.userError}">
				<p class="error">Digg Error:  <c:out value="${param.userError}"/></p>
			</c:if>
			<p>
				Enter your digg username: <input type="text" name="p"/>
			</p>
		</form>
		<hr/>
		<p class="descr">
			You can also track comments for any story that linked to a specific
			domain.
		</p>
		<form method="get" action="dr">
			<c:if test="${not empty param.domainError}">
				<p class="error">Digg Error:  <c:out value="${param.domainError}"/></p>
			</c:if>
			<p>
				Enter a domain name: <input type="text" name="p"/>
			</p>
		</form>
	</body>
</html>
