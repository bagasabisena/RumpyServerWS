package co.rumpy.server.handler;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.json.JSONException;
import org.json.JSONObject;

import co.rumpy.stanza.Stanza;
import co.rumpy.stanza.iq.IQ;
import co.rumpy.stanza.message.Message;
import co.rumpy.stanza.presence.Presence;
import co.rumpy.stanza.stream.Stream;

public class JSONDecoder extends SimpleChannelUpstreamHandler {
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		
		String JSON = (String) e.getMessage();
		Stanza stanza = decode(JSON);
		
		Channels.fireMessageReceived(ctx, stanza);
	}
	
	private Stanza decode(String JSON) {
		
		Stanza stanza = null;
		
		try {
			JSONObject jStanza = new JSONObject(JSON);
			if (jStanza.has("stream")) {
				// stream stanza received
				stanza = new Stream();
				stanza = stanza.fromJSON(JSON);
			} else if (jStanza.has("message")) {
				stanza = new Message();
				stanza = stanza.fromJSON(JSON);
			} else if (jStanza.has("iq")) {
				stanza = new IQ();
				stanza = stanza.fromJSON(JSON);
			} else if (jStanza.has("presence")) {
				stanza = new Presence();
				stanza = stanza.fromJSON(JSON);
			} else {
				// ERROR
				System.out.println("ERROR in JSON decoding process");
			}
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return stanza;
	}

}
