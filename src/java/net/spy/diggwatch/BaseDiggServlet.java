package net.spy.diggwatch;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

import net.spy.jwebkit.xml.XMLOutputServlet;

/**
 * Base REST servlet stuff.
 */
public abstract class BaseDiggServlet extends XMLOutputServlet {

	@Inject
	protected static DiggInterface di;

	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {
		String pi=req.getPathInfo();
		if(pi == null || pi.trim().length() == 0 || pi.equals("/")) {
			getLogger().info("No path info");
			res.sendRedirect("/diggwatch/");
		} else {
			assert pi.startsWith("/");
			String path=pi.substring(1).trim();
			try {
				processPath(path, req, res);
			} catch(Exception e) {
				throw new ServletException("Error processing path " + path, e);
			}
		}
	}

	/**
	 * Process the given path.
	 */
	protected abstract void processPath(String path, HttpServletRequest req,
		HttpServletResponse res) throws Exception;
}
