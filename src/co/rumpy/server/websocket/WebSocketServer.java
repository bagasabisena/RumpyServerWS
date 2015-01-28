package co.rumpy.server.websocket;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.rumpy.database.ConnectionManager;

public class WebSocketServer implements Daemon {
	
	private final int port;
	final Logger logger;
	
	Executor bossPool;
	Executor workerPool;
	ChannelFactory factory;
	ServerBootstrap bootstrap;

    public WebSocketServer(int port) {
        this.port = port;
        logger = LoggerFactory.getLogger(WebSocketServer.class);
    }
    
    public void run() {
    	
    	// Configure the server.
		bossPool = Executors.newCachedThreadPool();
		workerPool = Executors.newCachedThreadPool();
		
		factory = new NioServerSocketChannelFactory(bossPool, workerPool);
		
		bootstrap = new ServerBootstrap(factory);
		
		PipelineFactory pipelineFactory = new PipelineFactory();
		bootstrap.setPipelineFactory(pipelineFactory);
		
		// Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(port));
        logger.info("Web Socket Server started at port " + port);
        //System.out.println("Web Socket Server started at port " + port);
        
        new ConnectionManager();
        
    }
    
    public static void main(String[] args) {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 9000;
        }
        final WebSocketServer server = new WebSocketServer(port);
        server.run();
        
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				server.shutdown();
				
			}
		}));
    }
    
    private void shutdown() {
    	
    	ChannelGroupFuture groupFuture = Routing.connectedChannels.close();
    	groupFuture.awaitUninterruptibly();
    	factory.releaseExternalResources();
    	//System.out.println("BOOM!");
    	logger.info("Server Shutdown");
    	
    }

	@Override
	public void init(DaemonContext context) throws DaemonInitException,
			Exception {
		System.out.println("initializing ...");
	}

	@Override
	public void start() throws Exception {
		System.out.println("starting ...");
        main(null);
	}

	@Override
	public void stop() throws Exception {
		System.out.println("stopping ...");
		shutdown();
	}

	@Override
	public void destroy() {
		System.out.println("done.");
	}

}
