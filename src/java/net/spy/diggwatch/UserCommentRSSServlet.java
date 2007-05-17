package net.spy.diggwatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.digg.Comment;

/**
 * Serve recent user comments via RSS.
 */
public class UserCommentRSSServlet extends BaseDiggServlet {

	@Override
	protected void processPath(String path,
		HttpServletRequest req, HttpServletResponse res) throws Exception {
		@SuppressWarnings("unchecked") // generic enum
		Enumeration<String> etagsEnum = req.getHeaders("If-None-Match");
		Collection<String> eTags=new HashSet<String>();
		// I generally hate reinventing for loops, but I needed the @suppress
		while(etagsEnum.hasMoreElements()) {
			eTags.add(etagsEnum.nextElement());
		}
		List<Comment> comments = new ArrayList<Comment>(
			di.getRelevantComments(path));

		String eTag=computeEtag(comments);
		if(eTags.contains(eTag)) {
			res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		} else {
			res.setHeader("ETag", eTag);
			sendXml(new CommentFeed(path, path
					+ "'s comments (and replies) on digg", comments), res);
		}
	}

	private String computeEtag(List<Comment> comments) {
		String rv="0";
		if(!comments.isEmpty()) {
			Comment c=comments.get(0);
			rv=(c.getTimestamp() / 1000) + "-" + c.getEventId();
		}
		return rv;
	}

}
