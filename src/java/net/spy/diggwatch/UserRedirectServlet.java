package net.spy.diggwatch;


/**
 * RedirectServlet that validates the user.
 */
public class UserRedirectServlet extends RedirectServlet {

	@Override
	protected void validatePath(String path) throws Exception {
		di.getUserComments(path);
	}

}
