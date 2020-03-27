package dps.FTinvoker.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dps.FTinvoker.function.Function;

/**
 * SQLLite Database url = url to SQLite Database file.
 */
public class SQLLiteDatabase {
	private String url;

	public SQLLiteDatabase(String url) {
		this.url = url;
	}

	/**
	 * returns the availability of a passed Region returns 1 if no other
	 * availability is set for this Region in the DB
	 */
	public double getRegionAvailability(String region) {
		// Will return 1 if no other value set in DB for this Region!
		Statement stmt = null;
		String sql = "select availability from Regions where region == '" + region + "'";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				return rs.getDouble("availability");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 1; // Standard
	}

	/**
	 * Calculates average execution time of a given function in miliseconds
	 * returns 0 if no data about function in DB
	 */
	public double getAverageExecutionTime(String funcURL) {
		Statement stmt = null;
		String sql = "select avg(execTime) from Invokations where funclink == '" + funcURL + "' and execTime > 0";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				return rs.getDouble("avg(execTime)");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	/**
	 * Returns List of alternativeFunctions with their successRate set List will
	 * be sorted starting with highest successRate
	 */
	public List<Function> getFunctionAlternatives(Function function) {
		Statement stmt = null;
		String sql = "SELECT DISTINCT funcLink,type,region FROM Invokations where TYPE = '" + function.getType()
				+ "' and funcLink != '" + function.getUrl() + "'";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			ArrayList<Function> AlternativeList = new ArrayList<Function>();
			while (rs.next()) {
				Function alternativeFunc = new Function(rs.getString("funcLink"), function.getType(),
						function.getFunctionInputs());
				alternativeFunc.setRegion(rs.getString("region"));
				AlternativeList.add(alternativeFunc);
			}
			for (Function each : AlternativeList) {
				each.setSuccessRate(getSuccessRate(each.getUrl()) * getRegionAvailability(each.getRegion()));
				// Multiply availability set for this function with availability
				// of its region
			}
			Collections.sort(AlternativeList, new FunctionSuccessRateComperator());
			Collections.reverse(AlternativeList);// So it will be starting with
													// highest SuccessRate
			return AlternativeList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns successRate of a Function returns 1 if no data about function in
	 * Database (we have to assume it will work if we have no data)
	 */
	public double getSuccessRate(String funcURL) {
		// Will return 1 if function not found! (We will have to assume
		// availability = 1 until we have data)
		Statement stmt = null;
		String ok = "select count(*) from Invokations where funclink == '" + funcURL + "' and status == 'OK'";
		// String sql2 = "select count(*) from Invokations where funclink ==
		// '"+funcURL+"' and status != 'OK'"; version 1
		String all = "select count(*) from Invokations where funclink == '" + funcURL + "'";
		Connection conn = null;
		try {
			double successRate;
			double okCount = 0;
			double allCount = 0;
			conn = DriverManager.getConnection(url);
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(ok);
			while (rs.next()) {
				okCount = rs.getDouble("count(*)");
			}
			ResultSet rs2 = stmt.executeQuery(all);
			while (rs2.next()) {
				allCount = rs2.getDouble("count(*)");
			}
			if (okCount == 0 && allCount == 0) {
				return 1; // Function not found
			}

			if (okCount == 0) {
				successRate = 0;
			} else {
				successRate = (okCount / allCount);
			}
			return successRate;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public int getCount() {
		String count = "select count(*) from Invokations";
		Connection conn = null;
		try {
			double successRate;
			int okCount = 0;
			double allCount = 0;
			conn = DriverManager.getConnection(url);
			Statement stmt = null;
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(count);
			while (rs.next()) {
				okCount = rs.getInt("count(*)");
				return okCount;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	/**
	 * Adds an Invocation to the Invokations table of Database
	 */
	public boolean add(String function, String type, String provider, String region, Timestamp invokeTime,
			Timestamp returnTime, String status, String errorMessage) {
		String sql = "INSERT INTO Invokations (funcLink,type,provider,region,invokeTime,returnTime,execTime,status,errorMessage) VALUES(?,?,?,?,?,?,?,?,?)";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, function);
			pstmt.setString(2, type);
			pstmt.setString(3, provider);
			pstmt.setString(4, region);
			if (invokeTime != null) {
				pstmt.setString(5, invokeTime.toString());
			} else {
				pstmt.setString(5, null);
			}
			if (returnTime != null) {
				pstmt.setString(6, returnTime.toString());
			} else {
				pstmt.setString(6, null);
			}
			if (invokeTime != null && returnTime != null) {
				pstmt.setLong(7, returnTime.getTime() - invokeTime.getTime());
			} else {
				pstmt.setLong(7, 0);
			}
			pstmt.setString(8, status);
			pstmt.setString(9, errorMessage);
			pstmt.executeUpdate();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
