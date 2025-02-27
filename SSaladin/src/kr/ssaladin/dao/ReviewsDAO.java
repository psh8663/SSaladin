package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

/**
 * @author jaemoon
 * @date 2025. 2. 26. - 오후 5:02:31
 * @subject
 * @content 
 */
public class ReviewsDAO {

	Connection conn = null;
	PreparedStatement pstmt = null;
	String sql = null;
	ResultSet rs = null;
	boolean flag = false;
	
	// 리뷰 목록 보기
	public void selectReviews() {
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT * FROM reviews r, users u, books b "
					+ "WHERE r.user_id = u.user_id AND r.book_title = b.book_title";
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
			rs = pstmt.executeQuery();
			
			System.out.println("=".repeat(100));

			if (rs.next()) {
				System.out.printf("%-9s %-18s %-19s %-10s%n", "번호", "등록일", "닉네임", "도서명");
				System.out.println("-".repeat(100));
				do {
					System.out.printf("%-10s %-20s %-20s %-10s%n", 
							rs.getInt("review_num"), rs.getDate("reg_date"), rs.getString("user_id"), rs.getString("book_title"), rs.getDate("reg_date"));
				} while (rs.next());
			} else {
				System.out.println("등록된 게시글이 없습니다.");
			} // if

			System.out.println("=".repeat(100));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		} // try_finally
	}

	// user_id에 따른 리뷰 보기
	public void selectReviews_userId(String userId) {

		try {
			conn = DBUtil.getConnection();
			sql = "SELECT * FROM reviews r, users u, books b "
					+ "WHERE r.user_id = u.user_id AND r.book_title = b.book_title AND u.user_id=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.executeUpdate();
			rs = pstmt.executeQuery();

			System.out.println("=".repeat(100));

			if (rs.next()) {
				System.out.printf("%-9s %-18s %-19s %-10s%n", "번호", "등록일", "닉네임", "도서명");
				System.out.println("-".repeat(100));
				do {
//					System.out.print(rs.getInt("review_num"));
//					System.out.print("\t");
//					System.out.print(rs.getString("user_id"));
//					System.out.print("\t");
//					System.out.print(rs.getString("book_title"));
//					System.out.print("\t");
//					System.out.println(rs.getDate("reg_date"));
					System.out.printf("%-10s %-20s %-20s %-10s%n", 
							rs.getInt("review_num"), rs.getDate("reg_date"), rs.getString("user_id"), rs.getString("book_title"));
				} while (rs.next());
			} else {
				System.out.println("등록된 게시글이 없습니다.");
			} // if

			System.out.println("=".repeat(100));


		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	/* 안쓰는 함수
	// bookCode에 따른 리뷰 글 보기
	public void selectReviewsByBookCod(int bookCode) {

		try {
			conn = DBUtil.getConnection();
			sql = "SELECT * FROM reviews r, users u, books b "
					+ "WHERE r.user_id = u.user_id AND r.book_title = b.book_title AND r.book_code=?"
					+ "ORDER BY review_num DESC";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bookCode);
			pstmt.executeUpdate();
			rs = pstmt.executeQuery();

			System.out.println("=".repeat(100));

			if (rs.next()) {
				System.out.printf("%-9s %-18s %-19s %-10s%n", "번호", "등록일", "닉네임", "도서명");
				System.out.println("-".repeat(100));
				do {
//					System.out.print(rs.getInt("review_num"));
//					System.out.print("\t");
//					System.out.print(rs.getString("user_id"));
//					System.out.print("\t");
//					System.out.print(rs.getString("book_title"));
//					System.out.print("\t");
//					System.out.println(rs.getDate("reg_date"));
					System.out.printf("%-10s %-20s %-20s %-10s%n", 
							rs.getInt("review_num"), rs.getDate("reg_date"), rs.getString("user_id"), rs.getString("book_title"));
				} while (rs.next());
			} else {
				System.out.println("등록된 게시글이 없습니다.");
			} // if

			System.out.println("=".repeat(100));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		} // try_finally
	}
	 */
	
	// review_num에 따른 셀렉트 디테일
	public void detailSelectRivews_reviewNum(int reviewNum) {

		try {
			conn = DBUtil.getConnection();	
			sql = "SELECT * FROM reviews r, users u, books b "
					+ "WHERE r.user_id = u.user_id AND r.book_title = b.book_title AND r.review_num=?"
					+ "ORDER BY review_num DESC";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, reviewNum);
			pstmt.executeUpdate();
			rs = pstmt.executeQuery();

			System.out.println("=".repeat(100));

			if (rs.next()) {
				do {
					System.out.println("번호 : " + rs.getInt("review_num"));
					System.out.println("닉네임 : " + rs.getString("user_id"));
					System.out.println("제목 : " + rs.getString("book_title"));
					System.out.println("내용 : " + rs.getString("reviews_content"));
					System.out.println("평점 : " + rs.getInt("rating"));
					System.out.println("등록일 : " + rs.getDate("reg_date"));
					System.out.println("=".repeat(100));
				} while (rs.next());
			}
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		} // try_finally

	}

	// bookCode에 따른 셀렉트 디테일
	public void detailSelectRivews(int bookCode) {

		try {
			conn = DBUtil.getConnection();	
			sql = "SELECT * FROM reviews r, users u, books b "
					+ "WHERE r.user_id = u.user_id AND r.book_title = b.book_title AND r.book_code=?"
					+ "ORDER BY review_num DESC";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bookCode);
			pstmt.executeUpdate();
			rs = pstmt.executeQuery();

			System.out.println("=".repeat(100));

			if (rs.next()) {
				do {
					System.out.println("번호 : " + rs.getInt("review_num"));
					System.out.println("닉네임 : " + rs.getString("user_id"));
					System.out.println("제목 : " + rs.getString("book_title"));
					System.out.println("내용 : " + rs.getString("reviews_content"));
					System.out.println("평점 : " + rs.getInt("rating"));
					System.out.println("등록일 : " + rs.getDate("reg_date"));
					System.out.println("=".repeat(100));
				} while (rs.next());
			} else {
				System.out.println("등록된 게시글이 없습니다.");
			} // if
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		} // try_finally

	}

	// 게시판 글 작성
	public void insertReviews(String userId, String bookTitle, String reviewsContent, int rating, int bookCode) {

		try {
			conn = DBUtil.getConnection();
			sql = "INSERT INTO reviews (review_num, user_id, book_code, book_title, reviews_content, rating, reg_date) "
					+ "SELECT reviews_seq.nextval, ?, b.book_code, b.book_title, ?, ?, SYSDATE "
					+ "FROM books b WHERE b.book_code = ?";

			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.setString(2, reviewsContent);
			pstmt.setInt(3, rating);
			pstmt.setInt(4, bookCode);

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
			System.out.println("리뷰 번호를 확인해 주세요.");
			return;
		}

		try {
			conn =DBUtil.getConnection();
			sql = "UPDATE reviews SET reviews_content=?, rating=? WHERE review_num=?";
			pstmt = conn.prepareStatement(sql);
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
	public void deleteReviews(String userId, int reviewNum) {
		if (!checkPermission(userId, reviewNum)) {
			System.out.println("리뷰 번호를 확인해 주세요.");
			return;
		}

		try {
			conn = DBUtil.getConnection();
			sql = "DELETE FROM reviews WHERE review_num=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, reviewNum);
			int count = pstmt.executeUpdate();
			System.out.println(count + "개의 글을 삭제했습니다.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		} // try_finally
	}
	
	// book_code에 따른 책이름 출력
	public String bookName(int bookCode) {
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT book_title FROM books WHERE book_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bookCode);
			pstmt.executeUpdate();
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getString("book_title");
			} // if
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		} // try_finally
		
		return null;
		
	}

	
	// 작성자 및 관리자 유효성 검사
	public boolean checkPermission(String userId, int reviewNum) {
//		try {
//			conn = DBUtil.getConnection();
//			sql = "SELECT u.user_id FROM reviews r, users u "
//					+ "WHERE r.user_id=u.user_id AND r.review_num=?";
//			pstmt = conn.prepareStatement(sql);
//			pstmt.setInt(1, reviewNum);
//			rs = pstmt.executeQuery();
//			return rs.next() && (rs.getString("user_id").equals(userId) || "admin".equals(userId)) ? true : false;
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			DBUtil.executeClose(rs, pstmt, conn);
//		} // try_finally
//		return false;
//	}
		try (Connection conn = DBUtil.getConnection();
				PreparedStatement pstmt = conn.prepareStatement("SELECT u.user_id FROM reviews r, users u "
						+ "WHERE r.user_id=u.user_id AND r.review_num=?")) {

			pstmt.setInt(1, reviewNum);
			ResultSet rs = pstmt.executeQuery();
			return (rs.next() && (rs.getString("user_id").equals(userId) || "admin".equals(userId))) ? true : false;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
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
	

    // 주문자 id 와 book_code 유효성
    public boolean checkPermissionBookCode(String userId, int bookCode) {
    	  try (Connection conn = DBUtil.getConnection();
    	       PreparedStatement pstmt = conn.prepareStatement("SELECT o.book_code FROM order_details o, reviews r WHERE "
    	       		+ "o.book_code=r.book_code AND r.user_id=?")) {

    	      pstmt.setString(1, userId);
    	      ResultSet rs = pstmt.executeQuery();
    	      return (rs.next() && (rs.getInt("book_code") == bookCode)) ? true : false;

    	  } catch (Exception e) {
    	      e.printStackTrace();
    	  } finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
    	  return false;
    	}
    
    

}


















