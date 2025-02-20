package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import kr.util.DBUtil;

public class UserDAO {

	private Connection conn;

	public UserDAO(Connection conn) {
		this.conn = conn;
	}

	// 회원가입
	public boolean JoinCheck(String userId, String userPw, String userName, String userPhone, String userAddress) {

		PreparedStatement pstmt = null;
		String sql = null;
		boolean flag = false;

		try {

			// 회원가입 쿼리
			sql = "INSERT INTO users (user_id, user_auth, user_point, user_name, user_pw, user_phone, user_address) "
					+ "VALUES (?, 0, 0, ?, ?, ?, ?)";

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

			// 예외 처리
		} catch (SQLException e) {
			System.out.println("오류가 발생 했습니다. 다시 시도해주세요.");
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(null, pstmt, conn);
		}

		return flag;
	}

	// 로그인
	public boolean LoginCheck(String userId, String userPw) {

		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		boolean flag = false;

		try {

			// 로그인 쿼리
			sql = "SELECT user_id FROM users WHERE user_id = ? AND user_pw = ?";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.setString(2, userPw);

			rs = pstmt.executeQuery();
			if (rs.next()) {
				flag = true;
			}

			// 예외 처리
		} catch (SQLException e) {
			System.out.println("오류가 발생 했습니다. 다시 시도해주세요.");
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}

		return flag;
	}
	
	// 권한 체크 
	public int getUserAuth(String userId) {
	    PreparedStatement pstmt = null;
	    String sql = null;
	    ResultSet rs = null;

	    try {
	    	// 권한확인 쿼리
	    	sql = "SELECT user_auth FROM users WHERE user_id = ?";
	    	
	        pstmt = conn.prepareStatement(sql);
	        pstmt.setString(1, userId);
	        rs = pstmt.executeQuery();

	        if (rs.next()) {
	            return rs.getInt("user_auth");
	        }
	    } catch (SQLException e) {
	        System.out.println("오류가 발생 했습니다. 다시 시도해주세요.");
	        e.printStackTrace();
	    } finally {
	        DBUtil.executeClose(rs, pstmt, conn);
	    }

	    return -1;
	}


	// 아이디 중복 체크
	public boolean isUserIdExists(String userId) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT user_id FROM users WHERE user_id = ?";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			System.out.println("오류가 발생 했습니다. 다시 시도해주세요.");
			if (e.getMessage().contains("duplicate")) {
				System.out.println("이미 사용 중인 아이디입니다.");
			} else {
				System.out.println("데이터베이스 연결 오류. 관리자에게 문의 해주세요.");
			}
			return false;
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	// 전화번호 중복 체크
	public boolean isUserPhoneExists(String userPhone) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "SELECT user_phone FROM users WHERE user_phone = ?";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userPhone);
			rs = pstmt.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			System.out.println("오류가 발생 했습니다. 다시 시도해주세요.");
			if (e.getMessage().contains("duplicate")) {
				System.out.println("이미 사용 중인 전화번호입니다.");
			} else {
				System.out.println("데이터베이스 연결 오류. 관리자에게 문의 해주세요.");
			}
			return false;
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}
}
