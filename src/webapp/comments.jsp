<?xml version="1.0"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<html>
	<head>
		<title>Diggwatcher - comments for <c:out value="${username}"/></title>
		<link rel="stylesheet" href="/diggwatch/style.css"/>
		<link rel="alternate" type="application/rss+xml"
		   href="/diggwatch/rss/comments/<c:out value='${username}'/>"
			 title="RSS feed for <c:out value='${username}'/> @ digg"/>
	</head>
	<body>
		<h1>Comments for <c:out value="${username}"/></h1>
		<p class="hint">(The most recent comments appear at the top.) </p>
		<c:forEach var="sc" items="${storyComments}">
			<h2 class="ch">
				<c:choose>
					<c:when test="${username == sc.comment.user}">
						Your Comment on <q><a
							href="<c:out value='${sc.story.diggLink}'/>"><c:out
							value="${sc.story.title}"/></a></q>
					</c:when>
					<c:otherwise>
						Possible reply on
						<q><a href="<c:out value='${sc.story.diggLink}'/>"><c:out
							value="${sc.story.title}"/></a></q>
						by <a href="<c:out value='${sc.comment.user}'/>">
							<c:out value='${sc.comment.user}'/></a>
					</c:otherwise>
				</c:choose>
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
	</body>
</html>
