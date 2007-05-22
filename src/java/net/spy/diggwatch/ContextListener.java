package net.spy.diggwatch;

import static com.google.inject.name.Names.named;

import java.util.ResourceBundle;

import javax.servlet.ServletContextEvent;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
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

	/**
	 * Name of the injector.
	 */
	public static final String INJECTOR_NAME = Injector.class.getName();

	private static final String APP_KEY="http://bleu.west.spy.net/diggwatch/";

	private MemcachedClient mc;

	@Override
	protected void ctxInit(ServletContextEvent sce) throws Exception {
		super.ctxInit(sce);
		mc=new MemcachedClient(AddrUtil.getAddresses(
			"red:11211 purple:11211"));

		// Perform all of the injection.
		Injector injector = Guice.createInjector(new ServletModule(), this);
		sce.getServletContext().setAttribute(INJECTOR_NAME, injector);
	}

	@Override
	protected void ctxDestroy(ServletContextEvent sce) throws Exception {
		mc.shutdown();
		sce.getServletContext().removeAttribute(INJECTOR_NAME);
		super.ctxDestroy(sce);
	}

	public void configure(Binder binder) {
		binder.bind(Digg.class).toProvider(new Provider<Digg>(){
			public Digg get() {
				return new Digg(APP_KEY);
			}
		});

        ResourceBundle rb=ResourceBundle.getBundle(
        	"net.spy.diggwatch.diggwatch");

		binder.bind(MemcachedClient.class).toInstance(mc);
		binder.bind(String.class).annotatedWith(named("tree.version"))
			.toInstance(rb.getString("tree.version"));

		binder.requestStaticInjection(CommentFeed.class);
	}
}
