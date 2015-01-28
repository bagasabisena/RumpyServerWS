package co.rumpy.server.websocket;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;

import co.rumpy.database.ChannelDatabase;
import co.rumpy.database.ResourceDatabase;
import co.rumpy.stanza.Stanza;

public class Routing {
	
	
	public static ChannelGroup connectedChannels = new DefaultChannelGroup("connected");
	public static ConcurrentHashMap<String, LinkedBlockingQueue<Stanza>> stanzaQueues = new ConcurrentHashMap<>();
	private ChannelDatabase channelDB;
	private ResourceDatabase resDb;
	
	public Routing() {
		
		channelDB = new ChannelDatabase();
		resDb = new ResourceDatabase();
		
	}
	
	/*public Channel route(String destinationSignum) {
		
		Channel channel = getChannel(destinationSignum);
		
		if (channel == null) {
			addQueue(destinationSignum);
			return null;
		}
		
		return channel;
	}*/
	
	public ArrayList<Channel> route(String destBareSignum) {
		
		ArrayList<Channel> channels = getActiveChannels(destBareSignum);
		
		if (channels == null || channels.isEmpty()) {
			addQueue(destBareSignum);
			return null;
		}
		
		return channels;
	}
	
	private void addQueue(String destinationSignum) {
		
		Boolean isQueueAvailable = stanzaQueues.containsKey(destinationSignum);
		
		if (!isQueueAvailable) {
			stanzaQueues.put(destinationSignum, new LinkedBlockingQueue<Stanza>());
		}
		
	}

	private Channel getChannel(String destinationSignum) {
		
		Channel channel = null;
		Integer channelID = channelDB.getChannel(destinationSignum);
		
		if (channelID == null) {
			return null;
		}
		
		channel = connectedChannels.find(channelID);
		return channel;
	}
	
	private ArrayList<Channel> getActiveChannels(String destinationBareSignum) {
		
		ArrayList<Channel> activeChannels = new ArrayList<>();
		ArrayList<Integer> activeChannelIds = resDb.getActiveChannels(destinationBareSignum);
		
		if (activeChannelIds == null) {
			return null;
		} else {
			
			if (activeChannelIds.size() == 0) {
				return null;
			} else {
				// there is at least on active channel for the signum
				for (Integer channelId : activeChannelIds) {
					Channel channel = connectedChannels.find(channelId);
					activeChannels.add(channel);
				}
				
				return activeChannels;
			}
		}
		
		/*// there is at least on active channel for the signum
		for (Integer channelId : activeChannelIds) {
			Channel channel = connectedChannels.find(channelId);
			activeChannels.add(channel);
		}
		
		return activeChannels;*/
		
	}

}
