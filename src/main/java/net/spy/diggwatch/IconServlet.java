package net.spy.diggwatch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.digg.User;

/**
 * Serve up icons.
 */
public class IconServlet extends BaseDiggServlet {

	@Override
	protected void processPath(String u,
		HttpServletRequest req, HttpServletResponse res) throws Exception {
		User user=di.getUser(u);
		res.sendRedirect(user.getIcon());
	}

	@Override
	protected String getEtag(String path) throws Exception {
		return null;
	}

}
