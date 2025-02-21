package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import kr.util.DBUtil;

public class UserDAO {

	// 회원가입
	public boolean joinUser(String userId, String userPw, String userName, String userPhone, String userAddress) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean flag = false;

		String sql = "INSERT INTO users (user_id, user_auth, user_point, user_name, user_pw, user_phone, user_address) "
				+ "VALUES (?, 0, 0, ?, ?, ?, ?)";

		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.setString(2, userName);
			pstmt.setString(3, userPw);
			pstmt.setString(4, userPhone);
			pstmt.setString(5, userAddress);

			int result = pstmt.executeUpdate();
			if (result > 0) {
				flag = true;
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
		return flag;
	}

	// 로그인
	public boolean checkLogin(String userId, String userPw) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean flag = false;

		String sql = "SELECT user_id FROM users WHERE user_id = ? AND user_pw = ?";

		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.setString(2, userPw);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				flag = true;
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
		return flag;
	}

	// 포인트 확인
	public int getUserPoint(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int userPoint = 0;

		String sql = "SELECT user_point FROM users WHERE user_id = ?";

		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery(); 
			if (rs.next()) {
				userPoint = rs.getInt("user_point");
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
		return userPoint;
	}

	// 권한 체크
	public int getUserAuth(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int userAuth = -1;

		String sql = "SELECT user_auth FROM users WHERE user_id = ?";

		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				userAuth = rs.getInt("user_auth");
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
		return userAuth;
	}

	// 아이디 존재 여부 확인
	public boolean checkUserId(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sql = "SELECT user_id FROM users WHERE user_id = ?";

		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();
			return rs.next();
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	// 전화번호 존재 여부 확인
	public boolean checkUserPhone(String userPhone) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sql = "SELECT user_phone FROM users WHERE user_phone = ?";

		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userPhone);
			rs = pstmt.executeQuery();
			return rs.next();
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}
}