package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import kr.util.DBUtil;

public class AdminBookDAO {
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	String sql = null;

	public int insertBook(int categoryNum, String bookTitle, String bookAuthor, int bookPrice, String bookPublisher,
			String bookDescription, int bookStock, int bookStatus) throws ClassNotFoundException {
		String sql = "INSERT INTO books (book_code, category_num, book_title, book_author, book_price, "
				+ "book_publisher, book_description, book_stock, book_status, book_reg_date) "
				+ "VALUES (books_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, DEFAULT)";

		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, categoryNum);
			pstmt.setString(2, bookTitle);
			pstmt.setString(3, bookAuthor);
			pstmt.setInt(4, bookPrice);
			pstmt.setString(5, bookPublisher);
			pstmt.setString(6, bookDescription);
			pstmt.setInt(7, bookStock);
			pstmt.setInt(8, bookStatus);

			return pstmt.executeUpdate(); // 성공하면 1 반환

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0; // 실패 시 0 반환
	}

	public int insertCategory(String categoryName) {
		String checkSql = "SELECT COUNT(*) FROM categories WHERE category_name = ?";
		String sql = "INSERT INTO categories (category_num, category_name) VALUES (categories_seq.NEXTVAL, ?)";
		int result = 0;

		try {
			conn = DBUtil.getConnection();

			// 1️ 중복 체크
			pstmt = conn.prepareStatement(checkSql);
			pstmt.setString(1, categoryName);
			rs = pstmt.executeQuery();
			if (rs.next() && rs.getInt(1) > 0) {
				System.out.println("이미 존재하는 카테고리입니다.");
				return -1; // 중복 카테고리
			}

			// 2️ INSERT 실행
			pstmt.close(); // 기존 pstmt 닫기
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, categoryName);
			result = pstmt.executeUpdate();

			if (result > 0) {
				System.out.println("새로운 카테고리가 추가되었습니다: " + categoryName);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return -2; // 오류 발생
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
		return result;
	}

	public void updateBookDescription(int book_code, String book_description) {
		try {
			conn = DBUtil.getConnection();
			sql = "UPDATE books SET book_description=? WHERE book_code=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, book_description);
			pstmt.setInt(2, book_code);
			int count = pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 자원정리
			DBUtil.executeClose(null, pstmt, conn);
		}
	}
	
	public void adminUpdateStock(int book_code, int additionalStock) {
	    try {
	        conn = DBUtil.getConnection();
	        sql = "UPDATE books SET book_stock = book_stock + ? WHERE book_code = ?";
	        pstmt = conn.prepareStatement(sql);
	        pstmt.setInt(1, additionalStock); // 기존 재고에 추가할 수량
	        pstmt.setInt(2, book_code);
	        int count = pstmt.executeUpdate();
	        
	        if (count > 0) {
	            System.out.println("재고가 성공적으로 추가되었습니다.");
	        } else {
	            System.out.println("해당 도서가 존재하지 않습니다.");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        // 자원 정리
	        DBUtil.executeClose(null, pstmt, conn);
	    }
	}


	public boolean checkStock(int bookCode, int orderQuantity) throws SQLException, ClassNotFoundException {
		String sql = "SELECT book_stock FROM books WHERE book_code = ?";

		try (Connection conn = DBUtil.getConnection(); 
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, bookCode);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					int currentStock = rs.getInt("book_stock");

					// 재고가 부족하면 false 반환
					if (orderQuantity > currentStock) {
						System.out.println("재고 부족으로 주문할 수 없습니다. (현재 재고: " + currentStock + ")");
						return false;
					}
				} else {
					System.out.println("도서를 찾을 수 없습니다.");
					return false;
				}
			}
		}
		return true; // 재고가 충분한 경우
	}

	public boolean updateStock(int bookCode, int quantity) throws SQLException {
		String sql = "UPDATE books SET book_stock = book_stock - ? WHERE book_code = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, quantity);
			pstmt.setInt(2, bookCode);

			return pstmt.executeUpdate() > 0;
		}
	}
	
	public boolean updateBookStatus(int bookCode) throws SQLException {
	    String sql = "UPDATE books SET book_status = CASE " +
	                 "WHEN book_stock = 0 THEN 0 " +   // 재고가 0일 때 품절 상태
	                 "WHEN book_stock > 0 THEN 1 " +  // 재고가 0보다 크면 판매중 상태
	                 "END WHERE book_code = ?";
	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, bookCode);
	        return pstmt.executeUpdate() > 0;
	    }
	}



}
