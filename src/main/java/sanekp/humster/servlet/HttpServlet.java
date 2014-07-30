package sanekp.humster.servlet;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

/**
 * All servlets have to extend this
 */
public abstract class HttpServlet {
    private ServletContainer servletContainer;

    /**
     * Should be invoked from overridden
     */
    public void init(ServletContainer servletContainer) {
        this.servletContainer = servletContainer;
    }

    public abstract void service(HttpRequest request, FullHttpResponse response);

    public ServletContainer getContainer() {
        return servletContainer;
    }
}
