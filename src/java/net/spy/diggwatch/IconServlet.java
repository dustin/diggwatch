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
		User user=DiggInterface.getInstance().getUser(u);
		res.sendRedirect(user.getIcon());
	}

}
