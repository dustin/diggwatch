package net.spy.diggwatch;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.digg.Comment;

/**
 * Display comments for the given domain.
 */
public class DomainCommentsDisplayServlet extends BaseDiggServlet {

	@Override
	protected void processPath(String path, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		req.setAttribute("domain", path);
		DiggInterface di=DiggInterface.getInstance();
		Collection<Comment> comments=di.getCommentsForDomain(path);
		if(comments.isEmpty()) {
			req.getRequestDispatcher("/nodcomments.jsp").forward(req, res);
		} else {
			req.setAttribute("comments", comments);
			req.getRequestDispatcher("/dcomments.jsp").forward(req, res);
		}
	}

}
