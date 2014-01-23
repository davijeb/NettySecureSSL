package netty;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

import static org.jboss.netty.channel.Channels.*;
import static org.jboss.netty.channel.Channels.pipeline;

/**
 * Creates a newly configured {@link ChannelPipeline} for a new channel.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 *
 * @version $Rev: 2080 $, $Date: 2010-01-26 18:04:19 +0900 (Tue, 26 Jan 2010) $
 *
 */
public class SecureChatClientPipelineFactory implements
        ChannelPipelineFactory {

    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = pipeline();

        // Add SSL handler first to encrypt and decrypt everything.
        // In this example, we use a bogus certificate in the server side
        // and accept any invalid certificates in the client side.
        // You will need something more complicated to identify both
        // and server in the real world.

        SSLEngine engine =
                SecureChatSslContextFactory.getClientContext().createSSLEngine();
        engine.setUseClientMode(true);

        pipeline.addLast("ssl", new SslHandler(engine));

        // On top of the SSL handler, add the text line codec.
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(
                8192, Delimiters.lineDelimiter()));
        pipeline.addLast("decoder", new StringDecoder());
        pipeline.addLast("encoder", new StringEncoder());

        // and then business logic.
        pipeline.addLast("handler", new SecureChatClientHandler());

        return pipeline;
    }
}
