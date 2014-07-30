package sanekp.humster.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import sanekp.humster.servlet.ServletContainer;
import sanekp.humster.statistics.Statistics;

/**
 * Created by sanek_000 on 7/25/2014.
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    private ServletContainer servletContainer = new ServletContainer();

    {
        //  Set up the container before anything use it
        servletContainer.getContext().put("statistics", new Statistics());
    }

    private ServerHandler serverHandler = new ServerHandler(servletContainer);  //  stateless

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline p = socketChannel.pipeline();
        p.addLast("shaper", new ChannelTrafficShapingHandler(0));
        p.addLast("codec", new HttpServerCodec());
        p.addLast("handler", serverHandler);
    }
}
