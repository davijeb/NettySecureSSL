package netty;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.ssl.SslHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles a client-side channel.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2121 $, $Date: 2010-02-02 09:38:07 +0900 (Tue, 02 Feb 2010) $
 */
public class SecureChatClientHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = Logger.getLogger(
            SecureChatClientHandler.class.getName());

    @Override
    public void handleUpstream(
            ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            logger.info(e.toString());
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void channelConnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // Get the SslHandler from the pipeline
        // which were added in SecureChatPipelineFactory.
        SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);

        // Begin handshake.
        sslHandler.handshake();
    }

    @Override
    public void messageReceived(
            ChannelHandlerContext ctx, MessageEvent e) {
        System.err.println(e.getMessage());
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.log(
                Level.WARNING,
                "Unexpected exception from downstream.",
                e.getCause());
        e.getChannel().close();
    }
}

