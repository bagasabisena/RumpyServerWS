package co.rumpy.server.websocket;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import co.rumpy.server.handler.JSONDecoder;
import co.rumpy.server.handler.RumpyHandler;
import co.rumpy.server.handler.WebSocketHandler;

public class PipelineFactory implements ChannelPipelineFactory {

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		
		// Create a default pipeline implementation.
        ChannelPipeline pipeline = Channels.pipeline();
        
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("websocket", new WebSocketHandler());
        
        pipeline.addLast("jsondecoder", new JSONDecoder());
       // pipeline.addLast("jsonencoder", new JSONEncoder());
        
        pipeline.addLast("rumpy", new RumpyHandler());
        
		return pipeline;
	}
	
	

}
