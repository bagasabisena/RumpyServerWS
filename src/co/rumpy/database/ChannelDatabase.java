package co.rumpy.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChannelDatabase extends Database {
	
	public static final String CHANNEL_STATUS_CLOSED = "closed";
	public static final String CHANNEL_STATUS_CONNECTED = "connected";
	
	private static final String TABLE_CHANNEL = "channel";
	private static final String COLUMN_USER_ID = "user_id";
	private static final String COLUMN_CHANNEL_ID = "channel_id";
	private static final String COLUMN_CHANNEL_STATUS = "channel_status";
	
	public ChannelDatabase() {
		super();
	}
	
	public void registerChannel(String signum, Integer channelID) {
		
		Connection conn;
		PreparedStatement stmt = null;
		
		conn = getConnection();
		
		String query = "UPDATE " + TABLE_CHANNEL + " SET " + COLUMN_CHANNEL_ID + "=" + channelID + ", " + COLUMN_CHANNEL_STATUS +
				"='" + CHANNEL_STATUS_CONNECTED + "' WHERE " + COLUMN_USER_ID + "='" + signum + "';";
		
		try {
			stmt = conn.prepareStatement(query);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Wrong Query");
			e.printStackTrace();
		}
		
		close(conn, stmt, null);
		
	}
	
	public void unregisterChannel(String signum) {
		
		Connection conn;
		PreparedStatement stmt = null;
		
		conn = super.getConnection();
		
		String query = "UPDATE " + TABLE_CHANNEL + " SET " + COLUMN_CHANNEL_ID + "=" + 0 + ", " + COLUMN_CHANNEL_STATUS +
				"='" + CHANNEL_STATUS_CLOSED + "' WHERE " + COLUMN_USER_ID + "='" + signum + "';";
		
		try {
			stmt = conn.prepareStatement(query);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Wrong Query");
			e.printStackTrace();
		}
		
		close(conn, stmt, null);
		
	}
	
	public void unregisterChannel(Integer channelID) {
		
		Connection conn;
		PreparedStatement stmt = null;
		
		conn = super.getConnection();
		
		String query = "UPDATE " + TABLE_CHANNEL + " SET " + COLUMN_CHANNEL_ID + "=" + 0 + ", " + COLUMN_CHANNEL_STATUS +
				"='" + CHANNEL_STATUS_CLOSED + "' WHERE " + COLUMN_CHANNEL_ID + "='" + channelID + "';";
		
		try {
			stmt = conn.prepareStatement(query);
			stmt.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Wrong Query");
			e.printStackTrace();
		}
		
		close(conn, stmt, null);
	}
	
	public Integer getChannel(String signum) {
		
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs;
		Integer channelID = null;
		
		conn = super.getConnection();
		
		String query = "SELECT " + COLUMN_CHANNEL_ID + " FROM " + TABLE_CHANNEL + " WHERE " + COLUMN_USER_ID + "='" +
				signum + "';";
		
		try {
			stmt = conn.prepareStatement(query);
			rs = stmt.executeQuery();
			
			if (!rs.next()) {
				return null;
			}
			
			if (!rs.isBeforeFirst()) {
				rs.beforeFirst();
			}
			
			rs.next();
			channelID = rs.getInt(1);
			
			
		} catch (SQLException e) {
			System.err.println("Wrong Query");
			e.printStackTrace();
		}
		
		return channelID;
	}

}
