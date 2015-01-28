package co.rumpy.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import co.rumpy.stanza.iq.Roster;

public class RosterDatabase extends Database {
	
	public RosterDatabase() {
		super();
	}
	
	public ArrayList<Roster> getRosters (String signum) {
		
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet rsFriends = null;
		ResultSet rsRoster = null;
		
		Statement stmtRoster = null;
		
		conn = getConnection();
		
		ArrayList<String> friends = new ArrayList<>();
		ArrayList<Roster> rosters = new ArrayList<>();
		
		String getFriendsQuery = "SELECT friend_id from user_relationship WHERE me_id = " + "'"+ signum +"'"+ ";";
		
		try {
			stmt = conn.prepareStatement(getFriendsQuery);
			rsFriends = stmt.executeQuery();
			
			if (!rsFriends.next()) {
				return null;
			}
			
			if (!rsFriends.isBeforeFirst()) {
				rsFriends.beforeFirst();
			}
			
			
			while (rsFriends.next()) {
				friends.add(rsFriends.getString("friend_id"));
			}
			
			
			
			for (String s : friends) {
				
				String getRostersQuery = "SELECT * FROM user WHERE user_id = " + "'" + s + "'" + ";";
				stmtRoster = conn.createStatement();
				rsRoster = stmtRoster.executeQuery(getRostersQuery);
				rsRoster.beforeFirst();
				rsRoster.next();
				
				String fullname = rsRoster.getString("fullname");
				String image = rsRoster.getString("image");
				String presence = rsRoster.getString("presence");
				
				rosters.add(new Roster(s, fullname, image, presence));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		close(conn, stmt, rsFriends);
		close(null, stmtRoster, rsRoster);
		
		return rosters;
		
		
	}

}
