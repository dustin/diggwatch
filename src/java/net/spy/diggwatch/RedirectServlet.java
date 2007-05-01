package net.spy.diggwatch;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.digg.DiggException;

/**
 * Redirect based on the path.
 */
public class RedirectServlet extends BaseDiggServlet {

	private String destination = "/diggwatch/comments/";
	private String validName="username";
	private String noSuch="No such user";
	private String dError = "/diggwatch/?derror=";

	@Override
	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);
		destination=conf.getInitParameter("destination");
		validName=conf.getInitParameter("username");
		noSuch=conf.getInitParameter("No such user");
		dError=conf.getInitParameter("dError");
		assert destination != null;
		assert validName != null;
		assert noSuch != null;
		assert dError != null;
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

	@Override
	protected void processPath(String path,
		HttpServletRequest req, HttpServletResponse res) throws Exception {
		if(!validUsername(path)) {
			res.sendRedirect(
				"/diggwatch/?derror=Please+enter+a+valid"
					+ encodeString(validName));
		} else {
			try {
				DiggInterface.getInstance().getUserComments(path.trim());
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
