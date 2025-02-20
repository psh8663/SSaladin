package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import kr.util.DBUtil;

public class UserDAO {
	
	private Connection conn;  // Connection 객체를 클래스 변수로 선언

    // Connection을 매개변수로 받는 생성자 추가
    public UserDAO(Connection conn) {
        this.conn = conn;  // 외부에서 주입된 Connection 사용
    }

	
	// 회원가입	
	public boolean JoinCheck(String userId, String userPw, String userName, String userPhone, String userAddress) throws Exception {

		PreparedStatement pstmt = null;
		String sql = null;
		boolean flag = false;

		try {
			// db 연결
			conn = DBUtil.getConnection();
			// 회원가입 쿼리
			sql = "INSERT INTO users (user_id, user_auth, user_point, user_name, user_pw, user_phone, user_address) "
					+ "VALUES (?, 0, 0, ?, ?, ?, ?)";
			// 객체 생성
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.setString(2, userName);
			pstmt.setString(3, userPw);
			pstmt.setString(4, userPhone);
			pstmt.setString(5, userAddress);
			// 데이터 삽입
			int result = pstmt.executeUpdate();
			if (result > 0) {
				flag = true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(null, pstmt, conn);
		}

		return flag;
	}

	// 로그인
	public boolean LoginCheck(String userId, String userPw) throws Exception {
		
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		boolean flag = false;
		
        try {
            // db 연결.
        	conn = DBUtil.getConnection();   
            // 로그인 쿼리
            sql = "SELECT user_id FROM users WHERE user_id = ? AND user_pw = ?";
            // 객체 생성
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setString(2, userPw);

            // 쿼리 실행
            rs = pstmt.executeQuery();
            if (rs.next()) {
                flag = true;
            }

        } catch (SQLException e) {
            System.out.println("SQL 실행 오류 발생");
            e.printStackTrace();
        } finally {
            // 자원 정리
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return flag;
    }
}