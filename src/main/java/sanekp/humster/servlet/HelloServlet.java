package sanekp.humster.servlet;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

import java.util.concurrent.TimeUnit;

/**
 * Sends "Hello World" after 10 seconds
 */
@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
    @Override
    public void service(HttpRequest request, FullHttpResponse response) {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        response.content().writeBytes("Hello World".getBytes());
    }
}
