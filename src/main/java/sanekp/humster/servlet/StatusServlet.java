package sanekp.humster.servlet;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import sanekp.humster.statistics.ClientInfo;
import sanekp.humster.statistics.ConnectionInfo;
import sanekp.humster.statistics.Statistics;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Gives statistic information
 */
@WebServlet("/status")
public class StatusServlet extends HttpServlet {
    private static final String PATH = "pages/status.html";
    /**
     * Template of generated page
     */
    private String template;
    private Statistics statistics;  //  cache
    private DecimalFormat decimalFormat = new DecimalFormat("###,###,##0.0##");

    public StatusServlet() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PATH);
        try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8")) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.setLength(0);
            char[] chars = new char[1024];
            int read;
            for (; ; ) {
                read = inputStreamReader.read(chars);
                if (read == -1) {
                    break;
                }
                stringBuilder.append(chars, 0, read);
            }
            template = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(ServletContainer servletContainer) {
        super.init(servletContainer);
        statistics = (Statistics) servletContainer.getContext().get("statistics");
    }

    @Override
    public void service(HttpRequest request, FullHttpResponse response) {
        Map<String, ClientInfo> uniqueQueries = statistics.getUniqueQueries();
        StringBuilder stringBuilder = new StringBuilder();  //  micro-optimization
        for (ClientInfo clientInfo : uniqueQueries.values()) {
            stringBuilder.append("<tr><td>");
            stringBuilder.append(clientInfo.getInetSocketAddress().getAddress().getHostAddress());
            stringBuilder.append("</td><td>");
            stringBuilder.append(clientInfo.getQueries());
            stringBuilder.append("</td><td>");
            stringBuilder.append(clientInfo.getLast());
            stringBuilder.append("</td></tr>\n");
        }
        String uniqueQueriesTable = stringBuilder.toString();
        stringBuilder.setLength(0);
        for (Map.Entry<String, AtomicLong> redirect : statistics.getRedirects().entrySet()) {
            stringBuilder.append("<tr><td>");
            stringBuilder.append(redirect.getKey());
            stringBuilder.append("</td><td>");
            stringBuilder.append(redirect.getValue().get());
            stringBuilder.append("</td></tr>\n");
        }
        String redirectsTable = stringBuilder.toString();
        stringBuilder.setLength(0);
        for (ConnectionInfo connectionInfo : statistics.getConnectionsInfo().values()) {
            stringBuilder.append("<tr><td>");
            stringBuilder.append(connectionInfo.getIp());
            stringBuilder.append("</td><td><pre>");
            stringBuilder.append(connectionInfo.getUri());
            stringBuilder.append("</pre></td><td>");
            stringBuilder.append(connectionInfo.getTimestamp());
            stringBuilder.append("</td><td>");
            stringBuilder.append(connectionInfo.getSent());
            stringBuilder.append("</td><td>");
            stringBuilder.append(connectionInfo.getReceived());
            stringBuilder.append("</td><td>");
            stringBuilder.append(decimalFormat.format(connectionInfo.getSpeed()));
            stringBuilder.append("</td></tr>\n");
        }
        String connectionsTable = stringBuilder.toString();
        String page = template
                .replace("${totalQueries}", Long.toString(statistics.getTotalQueries()))
                .replace("${uniqueQueries.size}", Integer.toString(uniqueQueries.size()))
                .replace("${uniqueQueries}", uniqueQueriesTable)
                .replace("${redirects}", redirectsTable)
                .replace("${activeConnections}", Long.toString(statistics.getActiveConnections()))
                .replace("${connections}", connectionsTable);
        try {
            response.content().writeBytes(page.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
