package net.spy.diggwatch;

import javax.servlet.ServletContextEvent;

import net.spy.jwebkit.JWServletContextListener;

/**
 * Initialize context.
 */
public class ContextListener extends JWServletContextListener {

	@Override
	protected void ctxInit(ServletContextEvent sce) throws Exception {
		super.ctxInit(sce);
		DiggInterface.initialize();
	}

	@Override
	protected void ctxDestroy(ServletContextEvent sce) throws Exception {
		DiggInterface.shutdown();
		super.ctxDestroy(sce);
	}
}
