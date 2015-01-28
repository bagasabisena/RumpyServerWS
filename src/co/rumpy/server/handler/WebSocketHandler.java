package co.rumpy.server.handler;

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpMethod.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.rumpy.database.ChannelDatabase;
import co.rumpy.database.ResourceDatabase;
import co.rumpy.server.websocket.Routing;

public class WebSocketHandler extends SimpleChannelHandler {
	
	private WebSocketServerHandshaker handshaker;
	ChannelDatabase channelDB;
	ResourceDatabase resDb;
	final Logger logger;
	
	public WebSocketHandler() {
		
		channelDB = new ChannelDatabase();
		resDb = new ResourceDatabase();
		logger = LoggerFactory.getLogger(WebSocketHandler.class);
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		
		Object msg = e.getMessage();
		
		if (msg instanceof HttpRequest) {
			handleHttpRequest(ctx, (HttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
		//super.messageReceived(ctx, e);
	}
	
	private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) {
		
		// Allow only GET methods.
		if (req.getMethod() != GET) {
			sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
		}
		
		// Handshake
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, false);
		handshaker = wsFactory.newHandshaker(req);
		
		if (handshaker == null) {
			wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
		} else {
			ChannelFuture future = handshaker.handshake(ctx.getChannel(), req);
			future.addListener(WebSocketServerHandshaker.HANDSHAKE_LISTENER);
		}
		
	}

	private String getWebSocketLocation(HttpRequest req) {
		return "ws://" + req.getHeader(HttpHeaders.Names.HOST);
	}

	private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
		
		// Generate an error page if response status code is not OK (200).
        if (res.getStatus().getCode() != 200) {
            res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
            setContentLength(res, res.getContent().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.getChannel().write(res);
        if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
		
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
		
		Channel channel = ctx.getChannel();
		final Integer channelID = channel.getId();
		final String remoteAddress = channel.getRemoteAddress().toString();
		
		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) frame);
		} else if (frame instanceof PingWebSocketFrame) {
			
			logger.info("Ping Received from " + channelID + " - " + remoteAddress);
			PongWebSocketFrame pongFrame = new PongWebSocketFrame(frame.isFinalFragment(), frame.getRsv(), frame.getBinaryData());
			ChannelFuture f = ctx.getChannel().write(pongFrame);
			f.addListener(new ChannelFutureListener() {
				
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					
					if (future.isSuccess()) {
						logger.info("Successfully replied the ping from " + channelID + " - " + remoteAddress);
					}
					
				}
			});
			
		} else if (frame instanceof TextWebSocketFrame) {
			// this is the XML/JSON encoded Rumpy stanza
			String jsonString = ((TextWebSocketFrame) frame).getText();
			logger.info("INCOMING raw json data from " + channelID + " - " + remoteAddress);
			logger.info(jsonString);
			//logger.info(jsonString);
			Channels.fireMessageReceived(ctx, (String) jsonString);
			//TextWebSocketFrame textFrame = new TextWebSocketFrame(frame.isFinalFragment(), frame.getRsv(), jsonString);
			//ctx.getChannel().write(textFrame);
			
		} else if (frame instanceof BinaryWebSocketFrame) {
			
			// Binary data not yet handled, just echo
			
			BinaryWebSocketFrame binaryFrame = new BinaryWebSocketFrame(frame.isFinalFragment(), frame.getRsv(), frame.getBinaryData());
			ctx.getChannel().write(binaryFrame);
		} else if (frame instanceof ContinuationWebSocketFrame) {
            ctx.getChannel().write(
                    new ContinuationWebSocketFrame(frame.isFinalFragment(), frame.getRsv(), frame.getBinaryData()));
        } else if (frame instanceof PongWebSocketFrame) {
            // Ignore
        } else {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		e.getCause().printStackTrace();
		super.exceptionCaught(ctx, e);
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		
		
		
		Channel channel = e.getChannel();
		Routing.connectedChannels.add(channel);
		
		Integer channelID = channel.getId();
		String remoteAddress = channel.getRemoteAddress().toString();
		
		logger.info("Connected " + channelID + " - " + remoteAddress);
		//String local = e.getChannel().getRemoteAddress().toString();
		//System.out.println("Connected" + local + "--" + channel.getId());
		//super.channelConnected(ctx, e);
	}
	
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		
		Channel channel = e.getChannel();
		Integer channelID = channel.getId();
		String remoteAddress = channel.getRemoteAddress().toString();
		
		logger.info("Disconnected " + channelID + " - " + remoteAddress);
		//String local = channel.getLocalAddress().toString();
		//System.out.println("Disconnected" + local + "--" + channel.getId());
		
		ChannelFuture future = channel.close();
		future.addListener(closeListener);
		
		//super.channelDisconnected(ctx, e);
	}
	
	private ChannelFutureListener closeListener = new ChannelFutureListener() {
		
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			
			Channel channel = future.getChannel();
			Integer channelID = channel.getId();
			
			//channelDB.unregisterChannel(channelID);
			resDb.unregisterChannel(channelID);
			
		}
	};
	
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		
		logger.info("Closing " + e.getChannel().getId() + " - " + e.getChannel().getRemoteAddress().toString());
		//System.out.println("Closing" + e.getChannel().getRemoteAddress().toString());
		//super.channelClosed(ctx, e);
	}
	
	/*@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		
		String JSON = (String) e.getMessage();
		TextWebSocketFrame textFrame = new TextWebSocketFrame(JSON);
		ctx.getChannel().write(textFrame);
		
	}*/

	

}
