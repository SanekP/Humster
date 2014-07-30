package sanekp.humster.servlet;

import io.netty.handler.codec.http.*;
import sanekp.humster.statistics.Statistics;

import java.util.List;

/**
 * Redirects to address from URL parameter <em>url</em>
 * e.g. <em>redirect?url=http://google.com</em> redirects to google.com
 */
@WebServlet("/redirect")
public class RedirectServlet extends HttpServlet {
    private Statistics statistics;  //  cache

    @Override
    public void init(ServletContainer servletContainer) {
        super.init(servletContainer);
        statistics = (Statistics) servletContainer.getContext().get("statistics");
    }

    @Override
    public void service(HttpRequest request, FullHttpResponse response) {
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        List<String> urls = decoder.parameters().get("url");
        if (urls != null && urls.size() != 0) {
            response.setStatus(HttpResponseStatus.FOUND);
            response.headers().set(HttpHeaders.Names.LOCATION, urls);
            statistics.addRedirect(urls.get(0));
        } else {
            response.setStatus(HttpResponseStatus.BAD_REQUEST);
        }
    }
}
