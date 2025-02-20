package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class ReviewsDAO {
	
	Connection conn = null;
	PreparedStatement pstmt = null;
	String sql = null;
	ResultSet rs = null;
	boolean flag = false;
	
	// 게시판 글 보기
	public void selectReviews() {
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT * FROM reviews ORDER BY review_num DESC";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			System.out.println("-".repeat(30));
			
			if (rs.next()) {
				System.out.println("번호\t닉네임\t도서명\t등록일");
				do {
					System.out.print(rs.getInt("review_num"));
					System.out.print("\t");
					System.out.print(rs.getString("user_id"));
					System.out.print("\t");
					System.out.print(rs.getString("book_title"));
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
	public void insertReviews(String userId, String bookTitle, String reviewsContent, int rating) {
		try {
			conn = DBUtil.getConnection();
			sql = "INSERT INTO reviews (reiview_num, book_title, reviews_content, rating, reg_date)"
					+ "VALUES (reivews_seq.nextval, ?, ?, ?, SYSDATE)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, bookTitle);
			pstmt.setString(1, reviewsContent);
			pstmt.setInt(3, rating);
			int count = pstmt.executeUpdate();
			System.out.println(count + "개의 글을 등록했습니다.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(null, pstmt, conn);
		} // try_finally
	}
	
	// 게시글 수정
	public void updateReviews(String userId, int reviewNum, String reviewsContent, int rating) {
        if (!checkPermission(userId, reviewNum)) {
            System.out.println("권한이 없습니다.");
            return;
        }
		
		try {
			conn =DBUtil.getConnection();
			sql = "UDATE reivews SET reviews_content=?, rating=? WHERE review_num=?";
			pstmt.setString(1, reviewsContent);
			pstmt.setInt(2, rating);
			pstmt.setInt(3, reviewNum);
			int count = pstmt.executeUpdate();
			System.out.println(count + "개의 글을 수정했습니다.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
			
		} // try_finally
	}
	
	// 게시글 삭제
	public void deleteReviews(String userId, int reiviewNum) {
        if (!checkPermission(userId, reiviewNum)) {
            System.out.println("권한이 없습니다.");
            return;
        }
		
		try {
			conn = DBUtil.getConnection();
			sql = "DELETE FROM reivews WHERE review_num=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, reiviewNum);
			int count = pstmt.executeUpdate();
			System.out.println(count + "개의 글을 삭제했습니다.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		} // try_finally
	}
	
	// 작성자 및 관리자 유효성 검사
    private boolean checkPermission(String userId, int reviewNum) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT user_id FROM reviews WHERE review_num=?")) {

            pstmt.setInt(1, reviewNum);
            ResultSet rs = pstmt.executeQuery();
            return (rs.next() && (rs.getString("user_id").equals(userId) || "admin".equals(userId))) ? true : false;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 조회하는 리뷰 글이 존재하는지 여부
    public int checkReviews(int reviewNum) {
    	
    	int count = 0;
    	
    	try {
			conn = DBUtil.getConnection();
			sql = "SELECT * FROM reviews WHERE review_num=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, reviewNum);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count = 1;
			} // if
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		} // try_finally
    	
    	return count;
    }

}


















