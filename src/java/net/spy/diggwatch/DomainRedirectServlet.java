package net.spy.diggwatch;

/**
 * Redirect servlet that checks domain paths.
 */
public class DomainRedirectServlet extends RedirectServlet {

	@Override
	protected void validatePath(String path) throws Exception {
		DiggInterface.getInstance().getStoriesForDomain(path);
	}

}
