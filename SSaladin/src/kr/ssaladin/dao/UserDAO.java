package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kr.ssaladin.model.User;
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

	// 모든 유저 조회
	public List<User> getAllUsersInfo() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<User> userList = new ArrayList<>();

		String sql = "SELECT user_id, user_auth, user_point, user_name, user_pw, user_phone, user_address, user_date FROM users";

		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			// 결과를 한 명씩 User 객체에 담아서 리스트에 추가
			while (rs.next()) {
				User user = new User();
				user.setUserId(rs.getString("user_id"));
				user.setUserAuth(rs.getInt("user_auth"));
				user.setUserPoint(rs.getInt("user_point"));
				user.setUserName(rs.getString("user_name"));
				user.setUserPw(rs.getString("user_pw"));
				user.setUserPhone(rs.getString("user_phone"));
				user.setUserAddress(rs.getString("user_address"));
				user.setUser_date(rs.getDate("user_date"));

				userList.add(user);
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}

		return userList;
	}

	// 유저 조회 (회원정보 수정용)
	public User getUserInfo(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		User user = null;

		String sql = "SELECT user_pw, user_phone, user_address FROM users WHERE user_id = ?";

		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				user = new User();
				user.setUserId(userId);
				user.setUserPw(rs.getString("user_pw"));
				user.setUserPhone(rs.getString("user_phone"));
				user.setUserAddress(rs.getString("user_address"));
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
		return user;
	}

	// 회원 정보 수정

	public boolean updateUserInfo(String userId, String userPw, String userPhone, String userAddress) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean flag = false;

		String sql = "UPDATE users SET user_pw = ?, user_phone = ?, user_address = ? WHERE user_id = ?";

		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userPw);
			pstmt.setString(2, userPhone);
			pstmt.setString(3, userAddress);
			pstmt.setString(4, userId);

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
	
	// 회원 탈퇴
	public boolean deleteUser(String userId) {
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    boolean flag = false;

	    String sql = "DELETE FROM users WHERE user_id = ?";

	    try {
	        conn = DBUtil.getConnection();
	        pstmt = conn.prepareStatement(sql);
	        pstmt.setString(1, userId);

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

	// VIP 등업

	public boolean updateVIPStatus(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean flag = false;

		String sql = "UPDATE users SET user_auth = 1 WHERE user_id = ? AND "
				+ "(SELECT SUM(point_used) FROM point_uses WHERE user_id = ?) >= 50000";

		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.setString(2, userId);

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
	public boolean checkUserPhone(String userPhone, String userId) {
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    String sql;

	    if (userId != null) {
	        // 회원정보 수정 시 
	        sql = "SELECT user_phone FROM users WHERE user_phone = ? AND user_id != ?";
	    } else {
	        // 회원가입 시 
	        sql = "SELECT user_phone FROM users WHERE user_phone = ?";
	    }

	    try {
	        conn = DBUtil.getConnection();
	        pstmt = conn.prepareStatement(sql);
	        pstmt.setString(1, userPhone);
	        
	        if (userId != null) {
	            pstmt.setString(2, userId);
	        }
	        
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