package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import kr.util.DBUtil;

public class BookListDAO {

	// DB 연결과 자원 해제를 위한 공통 변수
	private Connection conn = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;
	private String sql = null;

	// 전체 도서 목록 보기
	public void selectBook() {
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT book_code, book_title, '(' || book_author || ')', CONCAT(book_price, '원') AS book_price FROM BOOKS "
					+ "WHERE book_status IN (0, 1)";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			System.out.println("-".repeat(50));
			System.out.printf("%-10s %-30s %-20s %-10s%n", "도서코드", "도서명", "저자명", "가격");
			System.out.println("-".repeat(50));
			if (rs.next()) {
				do {
					int bookCode = rs.getInt(1);
					String bookTitle = rs.getString(2);
					String bookAuthor = rs.getString(3);
					String bookPrice = rs.getString(4);

					System.out.printf("%-10d %-30s %-20s %-10s%n", bookCode, bookTitle, bookAuthor, bookPrice);
				} while (rs.next());
			} else {
				System.out.println("등록된 도서가 없습니다.");
			}
			System.out.println("-".repeat(50));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	// 조회하는 도서코드가 유효한지 여부 체크
	public int checkBCode(int num) {
		int count = 0;
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT * FROM books WHERE book_code=? and book_status IN (0, 1)";
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

	// 도서 상세보기
	public void selectDetailBook(int num) {
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT b.*, c.category_name, "
					+ "(SELECT AVG(rating) FROM reviews r WHERE r.book_code = b.book_code) AS avg_rating "
					+ "FROM books b " + "JOIN categories c ON b.category_num = c.category_num "
					+ "WHERE b.book_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				System.out.println("도서코드 : " + rs.getInt("book_code"));
				System.out.println("카테고리명 : " + rs.getString("category_name"));
				System.out.println("도서명 : " + rs.getString("book_title"));
				System.out.println("저자명 : " + rs.getString("book_author"));
				System.out.println("가격 : " + rs.getInt("book_price"));
				System.out.println("출판사 : " + rs.getString("book_publisher"));
				System.out.println("설명 : " + rs.getString("book_description"));
				System.out.println("상품상태(0:품절, 1:판매중, 2:판매중지): " + rs.getInt("book_status"));
				System.out.println("재고 :"+ rs.getInt("book_status"));
				Float avgRating = rs.getObject("avg_rating", Float.class);
				System.out.println("평균평점 : " + (avgRating != null ? avgRating : "평점 없음"));
				System.out.println("등록일 : " + rs.getDate("book_reg_date"));
			} else {
				System.out.println("검색된 정보가 없습니다.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	// 도서검색(제목)
	public void selectBookByTitle(String title) {
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT * FROM books b JOIN categories c ON b.category_num = c.category_num "
					+ "WHERE b.book_title LIKE ? and b.book_status IN (0, 1)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "%" + title + "%"); // 부분 검색
			rs = pstmt.executeQuery();

			boolean found = false;
			while (rs.next()) {
				found = true;
				System.out.println("도서코드 : " + rs.getInt("book_code"));
				System.out.println("카테고리명 : " + rs.getString("category_name"));
				System.out.println("도서명 : " + rs.getString("book_title"));
				System.out.println("저자명 : " + rs.getString("book_author"));
				System.out.println("가격 : " + rs.getInt("book_price"));
				System.out.println("-".repeat(50));
			}

			if (!found) {
				System.out.println("검색된 도서가 없습니다.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	// 카테고리 조회
	public void selectCategories() {
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT category_num, category_name FROM categories";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			System.out.println("=== 카테고리 목록 ===");
			while (rs.next()) {
				System.out.println(rs.getInt("category_num") + ". " + rs.getString("category_name"));
			}
			System.out.println("-".repeat(50));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	// 카테고리내 도서 조회
	public void selectBooksByCategory(int categoryNum) {
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT c.category_name, b.book_code, b.book_title, b.book_author, b.book_price "
					+ "FROM books b JOIN categories c ON b.category_num = c.category_num " 
					+ "WHERE b.category_num = ? and b.book_status IN (0, 1)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, categoryNum);
			rs = pstmt.executeQuery();

			boolean found = false;
			String categoryName = null;

			while (rs.next()) {
				if (categoryName == null) { // 첫 번째 결과에서 카테고리 이름을 가져옴
					categoryName = rs.getString("category_name");
					System.out.println("=== " + categoryName + " 카테고리의 도서 목록 ===");
				}
				found = true;
				System.out.println("도서코드: " + rs.getInt("book_code"));
				System.out.println("도서명: " + rs.getString("book_title"));
				System.out.println("저자명: " + rs.getString("book_author"));
				System.out.println("가격: " + rs.getInt("book_price"));
				System.out.println("-".repeat(50));
			}

			if (!found) {
				System.out.println("해당 카테고리에 등록된 도서가 없습니다.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	// 베스트셀러 도서
	public void selectBestSeller() throws SQLException, ClassNotFoundException {
		try {
			String sql = "SELECT * FROM ("
			           + "    SELECT ROWNUM AS ranking, t.*"
			           + "    FROM ("
			           + "        SELECT b.book_code, b.book_title, b.book_author, b.book_price, SUM(od.order_quantity) AS total_quantity"
			           + "        FROM order_details od"
			           + "        JOIN books b ON od.book_code = b.book_code"
			           + "        WHERE b.book_status IN (0,1)" 
			           + "        GROUP BY b.book_code, b.book_title, b.book_author, b.book_price"
			           + "        ORDER BY total_quantity DESC"
			           + "    ) t"
			           + "    WHERE ROWNUM <= 5"
			           + ")";

			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			System.out.println("-".repeat(50));
			System.out.println("=== 베스트셀러 도서 ===");
			while (rs.next()) {
			    System.out.println("순위: " + rs.getInt("ranking"));
			    System.out.println("도서 코드: " + rs.getInt("book_code"));
			    System.out.println("도서 제목: " + rs.getString("book_title"));
			    System.out.println("저자: " + rs.getString("book_author"));
			    System.out.println("가격: " + rs.getInt("book_price"));
			    System.out.println("총 판매량: " + rs.getInt("total_quantity"));
			    System.out.println("-".repeat(50));
			}
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}
}
