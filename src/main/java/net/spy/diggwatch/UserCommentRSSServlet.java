package net.spy.diggwatch;

import java.util.ArrayList;
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
		List<Comment> comments = new ArrayList<Comment>(
			di.getRelevantComments(path));

		sendXml(new CommentFeed(path, path
			+ "'s comments (and replies) on digg", comments), res);
	}

	@Override
	protected String getEtag(String path) throws Exception {
		return getEtagFromEvents(di.getRelevantComments(path));
	}

}
