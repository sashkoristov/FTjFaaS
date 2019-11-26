package dps.FTinvoker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SQLLiteDatabase {
	private String url;

	public SQLLiteDatabase(String url) {
		this.url = url;
	}

	public boolean add(String function, Timestamp invokeTime, Timestamp returnTime, String status) {
		String sql = "INSERT INTO invokedFunctions (funcLink,invokeTime,returnTime,execTime,status) VALUES(?,?,?,?,?)";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, function);
			pstmt.setString(2, invokeTime.toString());
			pstmt.setString(3, returnTime.toString());
			pstmt.setLong(4, returnTime.getTime() - invokeTime.getTime());
			pstmt.setString(5, status);
			pstmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		}
	}
}
