package net.spy.diggwatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.digg.Comment;

/**
 * Serve recent comments via RSS.
 */
public class CommentRSSServlet extends BaseDiggServlet {

	@Override
	protected void processPath(String user,
		HttpServletRequest req, HttpServletResponse res) throws Exception {
		DiggInterface di=DiggInterface.getInstance();
		List<Comment> comments = new ArrayList<Comment>(
			di.getRelevantComments(user));
		// newest first
		Collections.reverse(comments);
		sendXml(new CommentFeed(user, comments), res);
	}

}
