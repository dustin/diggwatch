package net.spy.diggwatch;

import javax.servlet.ServletContextEvent;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;

import net.spy.digg.Digg;
import net.spy.jwebkit.JWServletContextListener;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

/**
 * Initialize context.
 */
public class ContextListener extends JWServletContextListener
	implements Module {

	private static final String APP_KEY="http://bleu.west.spy.net/diggwatch/";

	private MemcachedClient mc=null;

	@Override
	protected void ctxInit(ServletContextEvent sce) throws Exception {
		super.ctxInit(sce);
		mc=new MemcachedClient(AddrUtil.getAddresses(
			"red:11211 purple:11211"));

		// Perform all of the injection.
		Guice.createInjector(new ServletModule(), this);
	}

	@Override
	protected void ctxDestroy(ServletContextEvent sce) throws Exception {
		mc.shutdown();
		super.ctxDestroy(sce);
	}

	public void configure(Binder binder) {
		binder.bind(Digg.class).toInstance(new Digg(APP_KEY));
		binder.bind(MemcachedClient.class).toInstance(mc);

		binder.requestStaticInjection(DiggInterface.class);
		binder.requestStaticInjection(BaseDiggServlet.class);
		binder.requestStaticInjection(RedirectServlet.class);
	}
}
