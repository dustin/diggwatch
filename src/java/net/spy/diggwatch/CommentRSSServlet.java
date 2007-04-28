package net.spy.diggwatch;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
			// some sort of index thing
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
		sendXml(new CommentFeed(user, new CommentFetcher().getComments(user)),
				res);
	}

}
