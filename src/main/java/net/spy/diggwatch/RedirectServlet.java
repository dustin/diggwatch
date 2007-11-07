package net.spy.diggwatch;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Injector;

import net.spy.digg.DiggException;
import net.spy.jwebkit.JWHttpServlet;

/**
 * Redirect based on the path.
 */
public class RedirectServlet extends JWHttpServlet {

	private String destination = "/diggwatch/comments/";
	private String validName="username";
	private String noSuch="No such user";
	private String dError = "/diggwatch/?derror=";

	@Inject
	protected DiggInterface di;

	@Override
	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);
		destination=conf.getInitParameter("destination");
		validName=conf.getInitParameter("validName");
		noSuch=conf.getInitParameter("noSuch");
		dError=conf.getInitParameter("dError");
		assert destination != null;
		assert validName != null;
		assert noSuch != null;
		assert dError != null;

	    ServletContext servletContext = conf.getServletContext();
	    Injector injector = (Injector)
	        servletContext.getAttribute(ContextListener.INJECTOR_NAME);
	    if (injector == null) {
	      throw new UnavailableException("No guice injector found");
	    }
	    injector.injectMembers(this);
	}

	private boolean validUsername(String user) {
		boolean rv=true;
        for(char c : user.toCharArray()) {
            if(Character.isWhitespace(c) || Character.isISOControl(c)
            	|| c == ',' || c == '/') {
                rv=false;
            }
        }
        return rv;
	}

	private String encodeString(String s) throws IOException {
		return URLEncoder.encode(s, "UTF-8");
	}

	protected void validatePath(String path) throws Exception {
		// nothing
	}

	@Override
	protected void doGet(
		HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {
		String path=req.getParameter("p").trim();
		if(!validUsername(path)) {
			res.sendRedirect(dError + "Please+enter+a+valid+"
					+ encodeString(validName));
		} else {
			try {
				validatePath(path);
				getLogger().info("Redirecting for %s", path);
				res.sendRedirect(destination + path);
			} catch (DiggException e) {
				if(e.getErrorId() == 404) {
					res.sendRedirect(dError + encodeString(noSuch)
						+ ":+" + path);
				} else {
					res.sendRedirect(dError + encodeString(e.getMessage()));
				}
			} catch(Exception e) {
				getLogger().warn("Unexpected error in diggwatch", e);
				res.sendRedirect("/diggwatch/?error=unknown+error");
			}
		}
	}

}
