<?xml version="1.0"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<html>
	<head>
		<title>
			Diggwatch - comments on articles from <c:out value="${domain}"/>
		</title>
		<link rel="stylesheet" href="/diggwatch/style.css"/>
		<link rel="alternate" type="application/rss+xml"
		   href="/diggwatch/rss/domainComments/<c:out value='${domain}'/>"
			 title="RSS feed for <c:out value='${domain}'/> @ digg"/>
		<meta name="robots" content="noindex,nofollow" />
	</head>
	<body>
		<h1>
			No recent comments for articles from <c:out value="${domain}"/> found
			<a href="/diggwatch/rss/domainComments/<c:out value='${domain}'/>"><img
				src="http://media.west.spy.net/img/rss-icon.png"
				alt="rss"/></a></h1>
		<jsp:include page="footer.jsp"/>
	</body>
</html>
