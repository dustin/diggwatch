package net.spy.diggwatch;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

import net.spy.digg.Event;
import net.spy.jwebkit.xml.XMLOutputServlet;

/**
 * Base REST servlet stuff.
 */
public abstract class BaseDiggServlet extends XMLOutputServlet {

	protected @Inject DiggInterface di;
	protected @Inject @Named("tree.version") String treeVersion;

	private String etagBase=null;

	@Override
	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);

	    ServletContext servletContext = conf.getServletContext();
	    Injector injector = (Injector)
	        servletContext.getAttribute(ContextListener.INJECTOR_NAME);
	    if (injector == null) {
	      throw new UnavailableException("No guice injector found");
	    }
	    injector.injectMembers(this);

        etagBase = treeVersion.split(" ")[0];
	}

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

			@SuppressWarnings("unchecked") // generic enum
			Enumeration<String> etagsEnum = req.getHeaders("If-None-Match");
			Collection<String> eTags=new HashSet<String>();
			// I generally hate reinventing for loops, but I needed the @suppress
			while(etagsEnum.hasMoreElements()) {
				eTags.add(etagsEnum.nextElement());
			}

			try {
				String eTag=getEtag(path);
				if(eTag == null) {
					processPath(path, req, res);
				} else {
					eTag = etagBase + "-" + eTag;
					if(eTags.contains(eTag)) {
						res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					} else {
						res.setHeader("ETag", eTag);
						processPath(path, req, res);
					}
				}
			} catch(Exception e) {
				throw new ServletException("Error processing path " + path, e);
			}
		}
	}

	/**
	 * Compute an etag from the given ordered collection of events.
	 */
	protected String getEtagFromEvents(Collection<? extends Event> events) {
		String rv="0";
		if(!events.isEmpty()) {
			Event event=events.iterator().next();
			rv=event.getTimestamp() / 1000 + "-" + event.getEventId();
		}
		return rv;
	}

	/**
	 * Process the given path.
	 */
	protected abstract void processPath(String path, HttpServletRequest req,
		HttpServletResponse res) throws Exception;

	/**
	 * Get the etag for the given path.
	 */
	protected abstract String getEtag(String path) throws Exception;

}
