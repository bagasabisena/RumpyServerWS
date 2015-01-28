package co.rumpy.server.handler;

import java.util.ArrayList;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.rumpy.database.ChannelDatabase;
import co.rumpy.database.ResourceDatabase;
import co.rumpy.database.RosterDatabase;
import co.rumpy.database.UserDatabase;
import co.rumpy.server.util.StringUtil;
import co.rumpy.server.websocket.Routing;
import co.rumpy.stanza.Signum;
import co.rumpy.stanza.Stanza;
import co.rumpy.stanza.iq.IQ;
import co.rumpy.stanza.iq.Roster;
import co.rumpy.stanza.message.Message;
import co.rumpy.stanza.presence.Presence;
import co.rumpy.stanza.stream.Stream;

public class RumpyHandler extends SimpleChannelHandler {
	
	ChannelDatabase channelDB;
	RosterDatabase rosterDB;
	UserDatabase userDB;
	ResourceDatabase resDb;
	Routing routing;
	final Logger logger;
	
	public RumpyHandler() {
		
		channelDB = new ChannelDatabase();
		rosterDB = new RosterDatabase();
		userDB = new UserDatabase();
		resDb = new ResourceDatabase();
		routing = new Routing();
		logger = LoggerFactory.getLogger(RumpyHandler.class);
		
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		
		Stanza inputStanza = (Stanza) e.getMessage();
		String outputJSON = "";
		
		if (inputStanza instanceof Stream) {
			logger.info("Stream received");
			String from = inputStanza.getFrom();
			Channel channel = e.getChannel();
			Integer channelID = channel.getId();
			
			// authentication process is skipped, assumed that user is authentic
			// map user with channelID and store in DB
			//channelDB.registerChannel(inputStanza.getFrom(), e.getChannel().getId());
			resDb.registerChannel(from, channelID);
			logger.info("User " + inputStanza.getFrom() + " registered to server with channelID " + e.getChannel().getId());
			
			// send reply
			Stream outputStream = new Stream();
			outputStream.setTo(inputStanza.getFrom());
			outputStream.setFrom(inputStanza.getTo());
			outputStream.setId(e.getChannel().getId().toString());
			
			sendStanza(e.getChannel(), outputStream);
			
			// check if there are stanza queued bound for the user
			StanzaQueue queue = StanzaQueue.queueMap.get(from);
			if (queue != null) {
				logger.info("There are pending message waiting to be sent to " + from);
				ArrayList<Stanza> pendingStanzas = queue.dequeStanza();
				for (Stanza s : pendingStanzas) {
					sendStanza(channel, s);
				}
			}
			
			//outputJSON = outputStream.toJSON();
			//TextWebSocketFrame frame = new TextWebSocketFrame(outputJSON);
			//e.getChannel().write(frame);
		} else if (inputStanza instanceof IQ) {
			
			IQ iq = (IQ) inputStanza;
			
			String type = iq.getType();
			String content = iq.getContent();
			
			if (type.equals("get") && content.equals("user")) {
				String targetUser = (String) iq.getQuery();
				Roster roster = userDB.getUser(targetUser);
				IQ iqResponse = new IQ();
				iqResponse.setTo(iq.getFrom());
				iqResponse.setFrom(iq.getTo());
				iqResponse.setId(iq.getId());
				iqResponse.setType("result");
				iqResponse.setContent("user");
				iqResponse.setQuery(roster);
				
				sendStanza(e.getChannel(), iqResponse);
				
			} else if (type.equals("set") && content.equals("roster")) {
				String remoteSignum = (String) iq.getQuery();
				String mySignum = iq.getFrom();
				Signum s = new Signum(mySignum);
				String myBareSignum = s.getBareSignum();
				
				//rosterDB.setRelationship(mySignum, remoteSignum, RosterDatabase.RELATION_FRIEND);
				userDB.setRelation(myBareSignum, remoteSignum, UserDatabase.RELATION_FRIEND);
				Roster roster = userDB.getUser(remoteSignum);
				
				ArrayList<Roster> rosters = new ArrayList<>();
				rosters.add(roster);
				
				IQ iqResponse = new IQ();
				iqResponse.setTo(iq.getFrom());
				iqResponse.setFrom(iq.getTo());
				iqResponse.setId(iq.getId());
				iqResponse.setType("result");
				iqResponse.setContent(iq.getContent());
				iqResponse.setQuery(rosters);
				
				sendStanza(e.getChannel(), iqResponse);
			}
			
		} else if (inputStanza instanceof Message) {
			logger.info("message received!");
			Message message = (Message) inputStanza;
			String destination = message.getTo();
			
			/*Channel channel = routing.route(destination);
			
			if (channel != null) {
				sendStanza(channel, message);
			} else {
				logger.info("destination channel null! queue message");
				StanzaQueue queue = StanzaQueue.getInstance(destination);
				queue.queueStanza(message);
			}*/
			
			ArrayList<Channel> channels = routing.route(destination);
			if (channels != null) {
				
				for (Channel channel : channels) {
					sendStanza(channel, message);
				} 
				
			} else {
				
				logger.info("destination channel null! queue message");
				StanzaQueue queue = StanzaQueue.getInstance(destination);
				queue.queueStanza(message);
				
			}
			
			
			
			//TextWebSocketFrame frame = new TextWebSocketFrame(message.toJSON());
			//channel.write(frame);
			
		} else if (inputStanza instanceof Presence) {
			logger.info("Presence received");
			Presence presence = (Presence) inputStanza;
			Roster roster = userDB.getUser(presence.getFrom());
			presence.setRoster(roster);
			String destination = presence.getTo();
			
			/*Channel channel = routing.route(destination);
			
			if (channel !=  null) {
				sendStanza(channel, presence);
			} else {
				logger.info("destination channel null! queue presence");
				StanzaQueue queue = StanzaQueue.getInstance(destination);
				queue.queueStanza(presence);
			}*/
			
			ArrayList<Channel> channels = routing.route(destination);
			if (channels != null) {
				
				for (Channel channel : channels) {
					sendStanza(channel, presence);
				} 
				
			} else {
				
				logger.info("destination channel null! queue message");
				StanzaQueue queue = StanzaQueue.getInstance(destination);
				queue.queueStanza(presence);
				
			}
		}
		
		super.messageReceived(ctx, e);
	}
	
	private String process(Stanza inputStanza) {
		
		String outputJSON = "";
		
		if (inputStanza instanceof Stream) {
			
			Stream outputStream = new Stream();
			outputStream.setTo(inputStanza.getFrom());
			outputStream.setFrom(inputStanza.getTo());
			outputStream.setId(StringUtil.randomStringGenerator(6));
			outputJSON = outputStream.toJSON();
		}
		
		return outputJSON;
	}
	
	private void sendStanza(Channel channel, Stanza stanza) {
		
		final String jsonString = stanza.toJSON();
		final Integer channelID = channel.getId();
		final String remoteAddress = channel.getRemoteAddress().toString();
		
		TextWebSocketFrame frame = new TextWebSocketFrame(jsonString);
		ChannelFuture future = channel.write(frame);
		future.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				
				if (future.isSuccess()) {
					logger.info("OUTGOING raw json data to " + channelID + " - " + remoteAddress);
					logger.info(jsonString);
				}
			}
		});
	}
	

}
