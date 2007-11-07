<?xml version="1.0"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<html>
	<head>
		<title>
			Diggwatch - stories from <c:out value="${domain}"/>
		</title>
		<link rel="stylesheet" href="/diggwatch/style.css"/>
		<link rel="alternate" type="application/rss+xml"
		   href="/diggwatch/rss/domain/<c:out value='${domain}'/>"
			 title="RSS feed for <c:out value='${domain}'/> @ digg"/>
		<meta name="robots" content="noindex,nofollow" />
	</head>
	<body>
		<h1>
			Recent articles from <c:out value="${domain}"/> found
			<a href="/diggwatch/rss/domain/<c:out value='${domain}'/>"><img
				src="http://media.west.spy.net/img/rss-icon.png"
				alt="rss"/></a></h1>
		<c:forEach var="s" items="${stories}">
			<h2 class="ch">
				<a href="<c:out value='${s.diggLink}'/>"/><q><c:out
					value="${s.title}"/></q></a>
				by
				<a href="http://digg.com/users/<c:out value='${s.user.name}'/>">
					<c:out value="${s.user.name}"/>
					<img width="24" height="24" src="<c:out value='${s.user.icon}'/>"/>
				</a>
			</h2>
			<p class="story">
				<c:out value="${s.description}"/>
			</p>
			<p class="storySub">
				Status:  <c:out value="${s.status}"/>.
				Topic:  <c:out value="${s.topic.name}"/>.
				<fmt:formatNumber value="${s.comments}"/> comments,
				<fmt:formatNumber value="${s.diggs}"/> diggs.
			</p>
		</c:forEach>
		<jsp:include page="footer.jsp"/>
	</body>
</html>
