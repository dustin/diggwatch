package net.spy.diggwatch;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.digg.Story;

public class DomainDisplayServlet extends BaseDiggServlet {

	@Override
	protected void processPath(String path, HttpServletRequest req,
			HttpServletResponse res) throws Exception {
		req.setAttribute("domain", path);
		Collection<Story> stories =
			DiggInterface.getInstance().getStoriesForDomain(path);
		if(stories.isEmpty()) {
			req.getRequestDispatcher("/nodomain.jsp").forward(req, res);
		} else {
			req.setAttribute("stories", stories);
			req.getRequestDispatcher("/domain.jsp").forward(req, res);
		}
	}

}
