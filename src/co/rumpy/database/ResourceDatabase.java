package co.rumpy.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import co.rumpy.stanza.Signum;

public class ResourceDatabase extends Database {
	
	public static final String CHANNEL_STATUS_CLOSED = "closed";
	public static final String CHANNEL_STATUS_CONNECTED = "connected";
	
	public static final String TABLE_RESOURCE = "resource";
	public static final String COLUMN_RESOURCE_RESOURCE_ID = "resource_id";
	public static final String COLUMN_RESOURCE_USER_ID = "user_id";
	public static final String COLUMN_RESOURCE_RESOURCE_NAME = "resource_name";
	public static final String COLUMN_RESOURCE_CHANNEL_ID = "channel_id";
	public static final String COLUMN_RESOURCE_CHANNEL_STATUS = "channel_status";
	
	public ResourceDatabase() {
		
	}
	
	public void registerResource(String fullSignum) {
		
		Connection conn;
		PreparedStatement stmt = null;
		conn = super.getConnection();
		Signum s = new Signum(fullSignum);
		String bareSignum = s.getBareSignum();
		String resourcePart = s.getResource();
		
		String query = "INSERT INTO " + TABLE_RESOURCE + " (" + COLUMN_RESOURCE_USER_ID + ", " + COLUMN_RESOURCE_RESOURCE_NAME + ") SELECT " 
						+ UserDatabase.COLUMN_USER_ID + ", '" + resourcePart + "' FROM " + UserDatabase.TABLE_USER 
						+ " WHERE " + UserDatabase.COLUMN_USER_SIGNUM + "='" + bareSignum + "';";
		
		try {
			stmt = conn.prepareStatement(query);
			stmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		close(conn, stmt, null);
		
	}
	
	public boolean registerChannel(String fullSignum, Integer channelID) {
		
		Connection conn;
		PreparedStatement stmt = null;
		conn = super.getConnection();
		boolean isSuccess = false;
		
		Signum s = new Signum(fullSignum);
		String bareSignum = s.getBareSignum();
		String resourcePart = s.getResource();
		
		String query = "UPDATE " + TABLE_RESOURCE + 
				" SET " + COLUMN_RESOURCE_CHANNEL_ID + "=" + channelID + ", " + COLUMN_RESOURCE_CHANNEL_STATUS + "='" + CHANNEL_STATUS_CONNECTED + "'" +
				" WHERE " + COLUMN_RESOURCE_USER_ID + 
				" IN (SELECT " + UserDatabase.COLUMN_USER_ID + " FROM " + UserDatabase.TABLE_USER + 
				" WHERE " + UserDatabase.COLUMN_USER_SIGNUM + "='" + bareSignum + "')" +
				" AND " + COLUMN_RESOURCE_RESOURCE_NAME + "='" + resourcePart + "'";
		
		try {
			stmt = conn.prepareStatement(query);
			int count = stmt.executeUpdate();
			
			if (count == 0) {
				isSuccess = false;
			} else {
				isSuccess = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		close(conn, stmt, null);
		return isSuccess;
		
	}
	
	public boolean unregisterChannel(String fullSignum) {
		
		Connection conn;
		PreparedStatement stmt = null;
		conn = super.getConnection();
		boolean isSuccess = false;
		
		Signum s = new Signum(fullSignum);
		String bareSignum = s.getBareSignum();
		String resourcePart = s.getResource();
		
		String query = "UPDATE " + TABLE_RESOURCE + 
				" SET " + COLUMN_RESOURCE_CHANNEL_ID + "=" + 0 + ", " + COLUMN_RESOURCE_CHANNEL_STATUS +
				"='" + CHANNEL_STATUS_CLOSED + "' WHERE " + COLUMN_RESOURCE_USER_ID + 
				" IN (SELECT " + UserDatabase.COLUMN_USER_ID + " FROM " + UserDatabase.TABLE_USER + 
				" WHERE " + UserDatabase.COLUMN_USER_SIGNUM + "='" + bareSignum + "')" +
				" AND " + COLUMN_RESOURCE_RESOURCE_NAME + "='" + resourcePart + "';";
		
		try {
			stmt = conn.prepareStatement(query);
			int count = stmt.executeUpdate();
			
			if (count == 0) {
				isSuccess = false;
			} else {
				isSuccess = true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		close(conn, stmt, null);
		return isSuccess;
		
	}
	
	public boolean unregisterChannel(Integer channelID) {
		
		Connection conn;
		PreparedStatement stmt = null;
		conn = super.getConnection();
		
		boolean isSuccess = false;
		
		String query = "UPDATE " + TABLE_RESOURCE + " SET " + COLUMN_RESOURCE_CHANNEL_ID + "=" + 0 + ", " + COLUMN_RESOURCE_CHANNEL_STATUS +
				"='" + CHANNEL_STATUS_CLOSED + "' WHERE " + COLUMN_RESOURCE_CHANNEL_ID + "=" + channelID + ";";
		
		try {
			stmt = conn.prepareStatement(query);
			int count = stmt.executeUpdate();
			
			if (count == 0) {
				isSuccess = false;
			} else {
				isSuccess = true;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		close(conn, stmt, null);
		return isSuccess;
	}
	
	public ArrayList<Integer> getActiveChannels(String bareSignum) {
		
		Connection conn;
		PreparedStatement stmt = null;
		conn = super.getConnection();
		ResultSet rs = null;
		ArrayList<Integer> activeChannels = new ArrayList<>();
		
		String query = "SELECT " + COLUMN_RESOURCE_CHANNEL_ID + " FROM " + TABLE_RESOURCE + 
				" WHERE " + COLUMN_RESOURCE_USER_ID +
				" IN (SELECT " + UserDatabase.COLUMN_USER_ID + " FROM " + UserDatabase.TABLE_USER + 
				" WHERE " + UserDatabase.COLUMN_USER_SIGNUM + "='" + bareSignum + "')" +
				" AND channel_status='connected';";
		
		try {
			stmt = conn.prepareStatement(query);
			rs = stmt.executeQuery();
			
			if (!rs.next()) {
				return null;
			}
			
			if (!rs.isBeforeFirst()) {
				rs.beforeFirst();
			}
			
			while (rs.next()) {
				Integer channelID = rs.getInt(1);
				if (channelID != 0) {
					activeChannels.add(rs.getInt(1));
				}
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		close(conn, stmt, rs);
		
		return activeChannels;
		
	}
	
	

}
