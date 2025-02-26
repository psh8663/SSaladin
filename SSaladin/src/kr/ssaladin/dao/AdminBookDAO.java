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


	public void selectAdminBook() {
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT book_code, book_title, '(' || book_author || ')', CONCAT(book_price, '원') AS book_price, book_status, book_stock FROM BOOKS";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			System.out.println("===전체 도서(판매중지도서 포함)===");
			System.out.printf("%-10s %-30s %-20s %-10s %-10s %-10s%n", "도서코드", "도서명", "저자명", "가격", "상품상태", "재고");
			System.out.println("-".repeat(100));

			if (rs.next()) {
				do {
					int bookCode = rs.getInt(1);
					String bookTitle = rs.getString(2);
					String bookAuthor = rs.getString(3);
					String bookPrice = rs.getString(4);
					int bookStatus = rs.getInt(5);
					int bookStock = rs.getInt(6);
					System.out.printf("%-10d %-30s %-20s %-10s %-10s %-10s%n", bookCode, bookTitle, bookAuthor,
							bookPrice, bookStatus, bookStock);
				} while (rs.next());
			} else {
				System.out.println("등록된 도서가 없습니다.");
			}
			System.out.println("-".repeat(100));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}
	
	public boolean checkCategory(int categoryNum) throws SQLException, ClassNotFoundException {
	    String sql = "SELECT COUNT(*) FROM categories WHERE category_id = ?";
	    try {
	    	conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
	        pstmt.setInt(1, categoryNum);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt(1) > 0; 
	            }
	        }
	    }finally {
	    	DBUtil.executeClose(rs, pstmt, conn);
		}
	    return false;
	}


	public int checkadminBCode(int num) {
		int count = 0;
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT * FROM books WHERE book_code=?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				count = 1; // 레코드가 존재할 때 1 저장
			}
		} catch (Exception e) {
			count = -1; // 오류 발생
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
		return count;
	}

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
			System.out.println("추가실패");
		}
		return 0; // 실패 시 0 반환
	}

	public int insertCategory(String categoryName) {
		String checkSql = "SELECT COUNT(*) FROM categories WHERE category_name = ?";
		String sql = "INSERT INTO categories (category_num, category_name) VALUES (categories_seq.NEXTVAL, ?)";
		int result = 0;

		try {
			conn = DBUtil.getConnection();

			// 중복 체크
			pstmt = conn.prepareStatement(checkSql);
			pstmt.setString(1, categoryName);
			rs = pstmt.executeQuery();
			if (rs.next() && rs.getInt(1) > 0) {
				System.out.println("이미 존재하는 카테고리입니다.");
				return -1; // 중복 카테고리
			}

			// INSERT 실행
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

		try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

	public boolean updateOrderStock(int bookCode, int quantity) throws SQLException, ClassNotFoundException {
		String sql = "UPDATE books SET book_stock = book_stock - ? WHERE book_code = ?";
		try {			
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, quantity);
			pstmt.setInt(2, bookCode);

			return pstmt.executeUpdate() > 0;
		}finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}

	public boolean updateBookStatus(int bookCode) throws SQLException, ClassNotFoundException {
		String sql = "UPDATE books SET book_status = " + "CASE " + "WHEN book_stock = 0 THEN 0 "
				+ "WHEN book_stock > 0 THEN 1 " + "ELSE book_status END " + "WHERE book_code = ?";

		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bookCode);
			return pstmt.executeUpdate() > 0;

		} catch (SQLException e) {
			e.printStackTrace(); 
			return false;
		}finally {
			DBUtil.executeClose(null, pstmt, conn);
		}

	}

	public boolean updateOutOfPrintStatus(int bookCode) throws SQLException, ClassNotFoundException {
		String sql = "UPDATE books SET book_status = 2 WHERE book_code = ?";

		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bookCode);
			return pstmt.executeUpdate() > 0;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}

}
