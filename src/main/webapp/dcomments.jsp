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
			Comments on articles from  <c:out value="${domain}"/>
			<a href="/diggwatch/rss/domainComments/<c:out value='${domain}'/>"><img
				src="http://media.west.spy.net/img/rss-icon.png"
				alt="rss"/></a></h1>
		<p class="hint">(The most recent comments appear at the top.) </p>
		<c:forEach var="sc" items="${storyComments}">
			<h2 class="ch">
				Comment by
				<q><a href="<c:out value='${sc.story.diggLink}'/>"><c:out
					value="${sc.story.title}"/></a></q>
				by <a href="<c:out value='../comments/${sc.comment.user}'/>">
					<c:out value='${sc.comment.user}'/>
					<img alt="icon" class="replyicon" width="24" height="24"
						src="<c:out value='${sc.iconUrl}'/>"/></a>
				(+<c:out value="${sc.comment.diggsUp}"/>/-<c:out
					value="${sc.comment.diggsDown}"/>)
			</h2>
			<p class="comment">
				<c:out escapeXml="false" value="${sc.formattedComment}"/>
			</p>
			<p class="commentfoot">
				<a href="<c:out value='${sc.commentLink}'/>">go to comment</a>
				| <a href="<c:out value='${sc.parentLink}'/>">go to parent</a>
			</p>
		</c:forEach>
		<jsp:include page="footer.jsp"/>
	</body>
</html>
