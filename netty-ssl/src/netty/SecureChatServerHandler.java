package netty;

import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.ssl.SslHandler;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles a server-side channel.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2121 $, $Date: 2010-02-02 09:38:07 +0900 (Tue, 02 Feb 2010) $
 */
public class SecureChatServerHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = Logger.getLogger(
            SecureChatServerHandler.class.getName());

    static final ChannelGroup channels = new DefaultChannelGroup();

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

        // Get the SslHandler in the current pipeline.
        // We added it in SecureChatPipelineFactory.
        final SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);

        // Get notified when SSL handshake is done.
        ChannelFuture handshakeFuture = sslHandler.handshake();
        handshakeFuture.addListener(new Greeter(sslHandler));
    }

    @Override
    public void channelDisconnected(
            ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // Unregister the channel from the global channel list
        // so the channel does not receive messages anymore.
        channels.remove(e.getChannel());
    }

    @Override
    public void messageReceived(
            ChannelHandlerContext ctx, MessageEvent e) {

        // Convert to a String first.
        String request = (String) e.getMessage();

        // Send the received message to all channels but the current one.
        for (Channel c: channels) {
            if (c != e.getChannel()) {
                c.write("[" + e.getChannel().getRemoteAddress() + "] " +
                        request + '\n');
            } else {
                c.write("[you] " + request + '\n');
            }
        }

        // Close the connection if the client has sent 'bye'.
        if (request.toLowerCase().equals("bye")) {
            e.getChannel().close();
        }
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

    /**
     * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
     * @author <a href="http://gleamynode.net/">Trustin Lee</a>
     * @version $Rev: 2121 $, $Date: 2010-02-02 09:38:07 +0900 (Tue, 02 Feb 2010) $
     */
    private static final class Greeter implements ChannelFutureListener {

        private final SslHandler sslHandler;

        Greeter(SslHandler sslHandler) {
            this.sslHandler = sslHandler;
        }

        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
                // Once session is secured, send a greeting.
                future.getChannel().write(
                        "Welcome to " + InetAddress.getLocalHost().getHostName() +
                                " secure chat service!\n");
                future.getChannel().write(
                        "Your session is protected by " +
                                sslHandler.getEngine().getSession().getCipherSuite() +
                                " cipher suite.\n");

                // Register the channel to the global channel list
                // so the channel received the messages from others.
                channels.add(future.getChannel());
            } else {
                future.getChannel().close();
            }
        }
    }
}
