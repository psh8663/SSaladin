package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import kr.util.DBUtil;

public class UserDAO {
	Connection conn = null;
	PreparedStatement pstmt = null;
	String sql = null;
	ResultSet rs = null;
	boolean flag = false;

	public UserDAO(Connection conn) {
		this.conn = conn;
	}

	public boolean registerUser(String userId, String userPw, String userName, String userPhone, String userAddress)
			throws SQLException {
		String sql = "INSERT INTO users (user_id, user_auth, user_point, user_name, user_pw, "
				+ "user_phone, user_address) VALUES (?, 0, 0, ?, ?, ?, ?)";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, userId);
			pstmt.setString(2, userName);
			pstmt.setString(3, userPw);
			pstmt.setString(4, userPhone);
			pstmt.setString(5, userAddress);

			return pstmt.executeUpdate() > 0;
		}
	}

	public boolean loginCheck(String userId, String userPw) throws SQLException {
		String sql = "SELECT user_id FROM users WHERE user_id = ? AND user_pw = ?";

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, userId);
			pstmt.setString(2, userPw);

			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
}