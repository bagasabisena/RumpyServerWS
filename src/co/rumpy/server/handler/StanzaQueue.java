package co.rumpy.server.handler;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.rumpy.stanza.Stanza;

public class StanzaQueue {
	
	public static ConcurrentHashMap<String, StanzaQueue> queueMap = new ConcurrentHashMap<>();
	final Logger logger;
	
	private String id;
	private LinkedBlockingQueue<Stanza> queue;
	
	public static StanzaQueue getInstance(String signum) {
		
		StanzaQueue queue = queueMap.get(signum);
		if (queue == null) {
			queue = new StanzaQueue(signum);
		} 
		
		return queue;
		
	}
	
	private StanzaQueue(String signum) {
		
		logger = LoggerFactory.getLogger(StanzaQueue.class);
		this.id = signum;
		this.queue = new LinkedBlockingQueue<>();
		queueMap.put(signum, this);
		logger.info("New queue instance created for signum: " + signum);
		
	}
	
	public String getId() {
		return id;
	}
	
	public LinkedBlockingQueue<Stanza> getQueue() {
		return queue;
	}
	
	public boolean queueStanza(Stanza stanza) {
		return queue.add(stanza);
	}
	
	public ArrayList<Stanza> dequeStanza() {
		ArrayList<Stanza> a = new ArrayList<>();
		queue.drainTo(a);
		queueMap.remove(id);
		return a;
	}
	
	
	
	

}
