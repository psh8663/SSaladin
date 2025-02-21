package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class AdminBookDAO {
	public int insertCategory(String categoryName) {
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    String checkSql = "SELECT COUNT(*) FROM categories WHERE category_name = ?";
	    String insertSql = "INSERT INTO categories (category_num, category_name) VALUES (categories_seq.NEXTVAL, ?)";
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
	        pstmt = conn.prepareStatement(insertSql);
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
