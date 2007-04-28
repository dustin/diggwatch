package net.spy.diggwatch;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.digg.DiggException;
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
			user=user.trim();
			try {
				DiggInterface.getInstance().getUserComments(user.trim());
				getLogger().info("Redirecting for %s", user);
				res.sendRedirect("/diggwatch/comments/" + user);
			} catch (DiggException e) {
				if(e.getErrorId() == 404) {
					res.sendRedirect("/diggwatch/?error=No+such+user:+" + user);
				} else {
					res.sendRedirect("/diggwatch/?error="
							+ URLEncoder.encode(e.getMessage(), "UTF-8"));
				}
			} catch(Exception e) {
				res.sendRedirect("/diggwatch/?error=unknown+error");
			}
		}
	}

}
