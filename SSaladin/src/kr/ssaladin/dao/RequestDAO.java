package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class RequestDAO {
	Connection conn = null;
	PreparedStatement pstmt = null;
	String sql = null;
	ResultSet rs = null;
	boolean flag = false;
	
	// 게시판 글 보기
	public void selectRequest() {
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT * FROM requst r, users u "
					+ "WHERE r.user_id=u.user_id ORDER BY request_num DESC";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			System.out.println("-".repeat(30));
			
			if (rs.next()) {
				System.out.println("번호\t닉네임\t등록일");
				do {
					System.out.print(rs.getInt("request_num"));
					System.out.print("\t");
					System.out.print(rs.getString("user_id"));
					System.out.print("\t");
					System.out.println(rs.getDate("reg_date"));
				} while (rs.next());
			} else {
				System.out.println("등록된 게시글이 없습니다.");
			} // if
			
			 System.out.println("-".repeat(30));
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		} // try_finally
	}
	
	// 게시판 글 작성
	public void insertRequest(String userId, String requestContent) {
		try {
			conn = DBUtil.getConnection();
			sql = "INSERT INTO request (request_num, user_id, request_content, request_date)"
					+ "VALUES (request_seq.nextval, ?, ?, SYSDATE)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.setString(2, requestContent);
			int count = pstmt.executeUpdate();
			System.out.println(count + "개의 글을 등록했습니다.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(null, pstmt, conn);
		} // try_finally
	}
	
	// 게시글 수정
	public void updateRequest(String userId, int requestNum, String requestContent) {
        if (!checkPermission(userId, requestNum)) {
            System.out.println("권한이 없습니다.");
            return;
        }
		
		try {
			conn =DBUtil.getConnection();
			sql = "UDATE request SET request_content=? WHERE request_num=?";
			pstmt.setString(1, requestContent);
			pstmt.setInt(2, requestNum);
			int count = pstmt.executeUpdate();
			System.out.println(count + "개의 글을 수정했습니다.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
			
		} // try_finally
	}
	
	// 게시글 삭제
	public void deleteRequest(String userId, int requestNum) {
        if (!checkPermission(userId, requestNum)) {
            System.out.println("권한이 없습니다.");
            return;
        }
		
		try {
			conn = DBUtil.getConnection();
			sql = "DELETE FROM reaquest WHERE request_num=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, requestNum);
			int count = pstmt.executeUpdate();
			System.out.println(count + "개의 글을 삭제했습니다.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		} // try_finally
	}
	
	// 작성자 및 관리자 유효성 검사
    private boolean checkPermission(String userId, int requestNum) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT user_id FROM request WHERE request_num=?")) {

            pstmt.setInt(1, requestNum);
            ResultSet rs = pstmt.executeQuery();
            return (rs.next() && (rs.getString("user_id").equals(userId) || "admin".equals(userId))) ? true : false;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}




















