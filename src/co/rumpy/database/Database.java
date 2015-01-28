package co.rumpy.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
	
	protected Connection getConnection() {
		
		Connection connection = null;
		
		try {
			connection = ConnectionManager.ds.getConnection();
		} catch (SQLException e) {
			System.err.println("Error in getting SQL connection");
			e.printStackTrace();
		}
		
		return connection;
	}
	
	protected void close(Connection conn, PreparedStatement stmt, ResultSet rs) {
		
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				System.err.println("Error in closing SQL");
				e.printStackTrace();
			}
		}
		
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				System.err.println("Error in closing SQL");
				e.printStackTrace();
			}
		}
		
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				System.err.println("Error in closing SQL");
				e.printStackTrace();
			}
		}
		
	}
	
	protected void close(Connection conn, Statement stmt, ResultSet rs) {
		
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				System.err.println("Error in closing SQL");
				e.printStackTrace();
			}
		}
		
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				System.err.println("Error in closing SQL");
				e.printStackTrace();
			}
		}
		
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				System.err.println("Error in closing SQL");
				e.printStackTrace();
			}
		}
		
	}

}
