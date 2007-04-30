package net.spy.diggwatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.digg.Comment;
import net.spy.jwebkit.xml.XMLOutputServlet;

/**
 * Serve recent comments via RSS.
 */
public class CommentRSSServlet extends XMLOutputServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
		String pi=req.getPathInfo();
		if(pi == null || pi.length() == 0 || pi.equals("/")) {
			// some sort of index thing?
			throw new ServletException("No username.");
		} else {
			assert pi.startsWith("/");
			String u=pi.substring(1);
			try {
				processUser(u, res);
			} catch(Exception e) {
				throw new ServletException("Error processing user " + u, e);
			}
		}
	}

	private void processUser(String user, HttpServletResponse res)
		throws Exception {
		DiggInterface di=DiggInterface.getInstance();
		List<Comment> comments = new ArrayList<Comment>(
			di.getRelevantComments(user));
		// newest first
		Collections.reverse(comments);
		// Make sure these have been fetched so they have a chance to arrive
		// in bulk
		di.getStoriesForComments(comments);
		sendXml(new CommentFeed(user, comments), res);
	}

}
