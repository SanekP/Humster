package sanekp.humster.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import io.netty.util.AttributeKey;
import sanekp.humster.servlet.HttpServlet;
import sanekp.humster.servlet.ServletContainer;
import sanekp.humster.statistics.ConnectionInfo;
import sanekp.humster.statistics.Statistics;

import java.net.InetSocketAddress;
import java.sql.Timestamp;

/**
 * Handle HTTP requests
 */
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<HttpRequest> {
    /**
     * Log of URIs
     */
    public static final AttributeKey<StringBuilder> URIS_KEY = AttributeKey.valueOf("uris");
    /**
     * Time when connection accepted
     */
    public static final AttributeKey<Long> TIME = AttributeKey.valueOf("time");
    private ServletContainer servletContainer;
    private Statistics statistics;

    public ServerHandler(ServletContainer servletContainer) {
        this.servletContainer = servletContainer;
        statistics = (Statistics) servletContainer.getContext().get("statistics");  //  cache
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        statistics.incrementActiveConnections();
        ctx.attr(URIS_KEY).set(new StringBuilder());
        ctx.attr(TIME).set(System.nanoTime());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest request) throws Exception {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        statistics.addQuery(inetSocketAddress);

        FullHttpResponse response = new DefaultFullHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK);
        response.headers().set(HttpHeaders.Names.SERVER, "humster");
        boolean keepAlive = HttpHeaders.isKeepAlive(request);
        if (!request.getDecoderResult().isSuccess()) {
            response.setStatus(HttpResponseStatus.BAD_REQUEST);
            keepAlive = false;
        } else if (request.getMethod() != HttpMethod.GET) {
            response.setStatus(HttpResponseStatus.METHOD_NOT_ALLOWED);
            keepAlive = false;
        } else {
            StringBuilder uris = ctx.attr(URIS_KEY).get();
            if (uris.length() != 0) {
                uris.append("\n");
            }
            uris.append(request.getUri());

            QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
            HttpServlet servlet = servletContainer.getMap().get(decoder.path());
            if (servlet != null) {
                servlet.service(request, response);
            } else {
                response.setStatus(HttpResponseStatus.NOT_FOUND);
            }
        }
        if (keepAlive) {
            response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }
        ctx.write(response, ctx.voidPromise()); //  Reduces object creation
        if (!keepAlive) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        statistics.decrementActiveConnections();
        ChannelTrafficShapingHandler channelTrafficShapingHandler = (ChannelTrafficShapingHandler) ctx.pipeline().get("shaper");
        TrafficCounter trafficCounter = channelTrafficShapingHandler.trafficCounter();
        long received = trafficCounter.cumulativeReadBytes();
        long sent = trafficCounter.cumulativeWrittenBytes();
        ConnectionInfo newConnectionInfo = new ConnectionInfo();
        newConnectionInfo.setIp(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress());
        newConnectionInfo.setUri(ctx.attr(URIS_KEY).get().toString());
        newConnectionInfo.setTimestamp(new Timestamp(System.currentTimeMillis()));
        newConnectionInfo.setSent(sent);
        newConnectionInfo.setReceived(received);
//        double elapsed = (System.currentTimeMillis() - trafficCounter.lastCumulativeTime()) / 1000.;  //  not long enough
        double elapsed = (System.nanoTime() - ctx.attr(TIME).get()) / 1_000_000_000.;
        double speed = (received + sent) / elapsed;
        newConnectionInfo.setSpeed(speed);
        statistics.addConnection(newConnectionInfo);
    }
}
