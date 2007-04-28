package net.spy.diggwatch;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.jwebkit.JWHttpServlet;

/**
 * Redirect to a user's comment page.
 */
public class CommentsRedirectServlet extends JWHttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {
		String user=req.getParameter("user");
		if(user == null || user.trim().length() == 0) {
			getLogger().info("No username");
			res.sendRedirect("/diggwatch/");
		} else {
			getLogger().info("Redirecting for %s", user.trim());
			res.sendRedirect("/diggwatch/comments/" + user.trim());
		}
	}

}
