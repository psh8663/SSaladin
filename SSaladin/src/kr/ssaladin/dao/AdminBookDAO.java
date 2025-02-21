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

}
