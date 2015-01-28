package co.rumpy.database;

import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

public class ConnectionManager {
	
	private static final String dbName = "rumpy";
	private static final String url = "jdbc:mysql://localhost:3306/" + dbName;
	private static final String user = "root";
	private static final String password = "RootR00t";
	private static final Integer dbPoolMinSize = 5;
	private static final Integer dbPoolMaxSize = 10;
	
	public static DataSource ds = null;
	private static GenericObjectPool<Connection> pool = null;
	
	public ConnectionManager() {
		
		try {
            connectToDB();
        } catch(Exception e) {
            System.err.println( "Failed to construct ConnectionManager");
        }
		
	}

	private void connectToDB() {
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			System.err.println("Cannot load the driver");
			e.printStackTrace();
		}
		
		ConnectionManager.ds = setupDataSource(url, user, password, dbPoolMinSize, dbPoolMaxSize);
		
	}

	private DataSource setupDataSource(String url, String user,
			String password, Integer dbpoolminsize, Integer dbpoolmaxsize) {
		
		//
        // First, we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
		
		GenericObjectPool<Connection> connectionPool = new GenericObjectPool<>();
		
		connectionPool.setMinIdle(dbpoolminsize);
		connectionPool.setMaxActive(dbpoolmaxsize);
		
		ConnectionManager.pool = connectionPool;
		// we keep it for two reasons
	    // #1 We need it for statistics/debugging
	    // #2 PoolingDataSource does not have getPool()
	    // method, for some obscure, weird reason.
		
		//
        // Next, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string from configuration
        //
		
		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, user, password);
		
		//
        // Now we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
		
		new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
		
		PoolingDataSource poolingDataSource = new PoolingDataSource(connectionPool);
		
		return poolingDataSource;
	}

}
