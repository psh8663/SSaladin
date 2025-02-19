package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

class PointRequestDAO {
	private Connection conn;

	public PointRequestDAO(Connection conn) {
		this.conn = conn;
	}

	public boolean requestPoint(String userId, long amount) throws SQLException {
		String sql = "INSERT INTO point_requests (request_num, user_id, point_amount, "
				+ "request_status, point_used) VALUES (seq_request_num.NEXTVAL, ?, ?, 1, 0)";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, userId);
			pstmt.setLong(2, amount);

			return pstmt.executeUpdate() > 0;
		}
	}
}