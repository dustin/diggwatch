package net.spy.diggwatch;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.digg.DiggException;

/**
 * Redirect to a user's comment page.
 */
public class CommentsRedirectServlet extends BaseDiggServlet {

	private boolean validUsername(String user) {
		boolean rv=true;
        for(char c : user.toCharArray()) {
            if(Character.isWhitespace(c) || Character.isISOControl(c)) {
                rv=false;
            }
        }
        return rv;
	}

	@Override
	protected void processPath(String user,
		HttpServletRequest req, HttpServletResponse res) throws Exception {
		if(!validUsername(user)) {
			res.sendRedirect(
				"/diggwatch/?derror=Please+enter+a+valid+username");
		} else {
			try {
				DiggInterface.getInstance().getUserComments(user.trim());
				getLogger().info("Redirecting for %s", user);
				res.sendRedirect("/diggwatch/comments/" + user);
			} catch (DiggException e) {
				if(e.getErrorId() == 404) {
					res.sendRedirect("/diggwatch/?derror=No+such+user:+" + user);
				} else {
					res.sendRedirect("/diggwatch/?derror="
							+ URLEncoder.encode(e.getMessage(), "UTF-8"));
				}
			} catch(Exception e) {
				getLogger().warn("Unexpected error in diggwatch", e);
				res.sendRedirect("/diggwatch/?error=unknown+error");
			}
		}
	}

}
