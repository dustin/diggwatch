<?xml version="1.0"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<html>
	<head>
		<title>Diggwatch</title>
		<link rel="stylesheet" href="/diggwatch/style.css"/>
	</head>
	<body id="index">
		<p class="descr">
			This application is used to find, browse and track (via RSS) your digg
			comments and their replies over the last 14 days.
		</p>
		<hr/>
		<p>
			<c:if test="${not empty param.error}">
				<p class="error">Internal Error:  <c:out value="${param.error}"/></p>
			</c:if>
		</p>
		<h1>Track Your Threads</h1>
		<form method="get" action="ur">
			<c:if test="${not empty param.userError}">
				<p class="error">Digg Error:  <c:out value="${param.userError}"/></p>
			</c:if>
			<p>
				Enter your digg username: <input type="text" name="p"/>
			</p>
		</form>
		<hr/>
		<h1>Track Your Friends' Comments</h1>
		<p class="descr">
			This will look for any comments posted by any of your friends in the last
			fourteen days.  Due to limitations in the digg API, this will not be very
			fast if you have a lot of friends.  Your RSS reader shouldn't mind.
		</p>
		<form method="get" action="ufr">
			<c:if test="${not empty param.userFError}">
				<p class="error">Digg Error:  <c:out value="${param.userFError}"/></p>
			</c:if>
			<p>
				Enter your digg username: <input type="text" name="p"/>
			</p>
		</form>
		<hr/>
		<h1>Track Stories Posted from a Specific Domain</h1>
		<form method="get" action="dr">
			<c:if test="${not empty param.domainError}">
				<p class="error">Digg Error:  <c:out value="${param.domainError}"/></p>
			</c:if>
			<p>
				Enter a domain name: <input type="text" name="p"/>
			</p>
		</form>
		<hr/>
		<h1>Track Comments to Stories Posted from a Specific Domain</h1>
		<form method="get" action="dcr">
			<c:if test="${not empty param.domainCommentsError}">
				<p class="error">Digg Error:  <c:out value="${param.domainCommentsError}"/></p>
			</c:if>
			<p>
				Enter a domain name: <input type="text" name="p"/>
			</p>
		</form>
		<jsp:include page="footer.jsp"/>
	</body>
</html>
