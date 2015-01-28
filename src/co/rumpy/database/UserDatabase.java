package co.rumpy.database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import co.rumpy.stanza.iq.Roster;

public class UserDatabase extends Database {
	
	
	// user table detail
	public static final String TABLE_USER = "user";
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_USER_SIGNUM = "signum";
	public static final String COLUMN_USER_PASSWORD = "password";
	public static final String COLUMN_USER_ROLE = "role";
	public static final String COLUMN_USER_TOKEN = "token";
	public static final String COLUMN_USER_FULLNAME = "fullname";
	
	public static final String TABLE_USER_RELATION = "user_relation";
	public static final String RELATION_FRIEND = "friend";
	public static final String RELATION_BLOCK = "block";

	
	public UserDatabase() {
		super();
	}
	
	public boolean isUserExist(String bareSignum) {
		
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		conn = super.getConnection();
		boolean isExist = false;
		
		String query = "SELECT * FROM " + TABLE_USER + " WHERE " + COLUMN_USER_SIGNUM + "='" + bareSignum + "';";
		
		try {
			stmt = conn.prepareStatement(query);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				isExist = true;
			} else {
				isExist = false;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		close(conn, stmt, rs);
		
		return isExist;
		
	}
	
	public void register(String bareSignum, String password) {
		
		Connection conn;
		PreparedStatement stmt = null;
		conn = super.getConnection();
		
		String query = "INSERT INTO " + TABLE_USER + "(signum, password, role)"+ " VALUES (?, ?, ?)";
		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, bareSignum);
			stmt.setString(2, sha256(password));
			stmt.setString(3, "user");
			stmt.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		close(conn, stmt, null);
		
	}
	
	public void register(String bareSignum, String password, String fullname) {
		
		Connection conn;
		PreparedStatement stmt = null;
		conn = super.getConnection();
		
		String query = "INSERT INTO " + TABLE_USER + "(signum, password, role, fullname)"+ " VALUES (?, ?, ?, ?)";
		
		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, bareSignum);
			stmt.setString(2, sha256(password));
			stmt.setString(3, "user");
			stmt.setString(4, fullname);
			stmt.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		close(conn, stmt, null);
	}
	
	private String sha256(String password) {
		
		StringBuffer sb = null;
		
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(password.getBytes());
			
			byte[] byteData = md.digest();
			sb = new StringBuffer();
	        for (int i = 0; i < byteData.length; i++) {
	         sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sb.toString();
	}

	public Roster getUser(String bareSignum) {
		
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rsRoster = null;
		Roster roster = null;
		
		conn = super.getConnection();
		
		String query = "SELECT * FROM user WHERE signum = " + "'" + bareSignum + "'" + ";";
		try {
			stmt = conn.prepareStatement(query);
			rsRoster = stmt.executeQuery();
			
			if (!rsRoster.next()) {
				return null;
			}
			
			if (!rsRoster.isBeforeFirst()) {
				rsRoster.beforeFirst();
			}
			
			rsRoster.next();
			
			String fullname = rsRoster.getString("fullname");
			String image = rsRoster.getString("display");
			String presence = rsRoster.getString("presence");
			
			roster = new Roster(bareSignum, fullname, image, presence);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return roster;
		
	}
	
	public boolean setRelation(String mySignum, String remoteSignum, String relation) {
		
		Connection conn;
		PreparedStatement stmt = null;
		boolean isSuccess = false;
		
		conn = super.getConnection();
		
		String query = "INSERT INTO user_relation(me_id, friend_id, relation) " +
				"VALUES ((SELECT user_id FROM user WHERE signum='" + mySignum + "'), " +
						"(SELECT user_id FROM user WHERE signum='" + remoteSignum + "'), " +
						"?)";
		
		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, relation);
			int rowCount = stmt.executeUpdate();
			
			if (rowCount > 0) {
				isSuccess = true;
			} else {
				isSuccess = false;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		close(conn, stmt, null);
		return isSuccess;
		
	}
	
	public boolean updateRelation(String mySignum, String remoteSignum, String relation) {
		
		Connection conn;
		PreparedStatement stmt = null;
		boolean isSuccess = false;
		
		conn = super.getConnection();
		
		String query = "UPDATE user_relation SET relation=? " +
						"WHERE me_id=(SELECT user_id FROM user WHERE signum='" + mySignum + "') " +
						"AND friend_id=(SELECT user_id FROM user WHERE signum='" + remoteSignum + "');";
		
		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, relation);
			int rowCount = stmt.executeUpdate();
			
			if (rowCount > 0) {
				isSuccess = true;
			} else {
				isSuccess = false;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		close(conn, stmt, null);
		return isSuccess;
		
	}

}
