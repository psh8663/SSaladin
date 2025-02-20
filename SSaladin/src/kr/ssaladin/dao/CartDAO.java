package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import kr.util.DBUtil;

public class CartDAO {
    private Connection conn = null;
    private PreparedStatement pstmt = null;
    private String sql = null;
    private ResultSet rs = null;
    private boolean flag = false;
    
    public CartDAO() {}
    
    
    public CartDAO(Connection conn) {
        this.conn = conn;
    }

    // 장바구니 추가
    public boolean insertCart(String userId, int bookCode, int cartQuantity) throws SQLException {
        sql = "INSERT INTO cart (user_id, book_code, cart_quantity) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setInt(2, bookCode);
            pstmt.setInt(3, cartQuantity);
            return pstmt.executeUpdate() > 0;
        }
    }

    // 장바구니 수량 수정
    public boolean updateCartQuantity(int cartNum, int cartQuantity) throws SQLException {
        sql = "UPDATE cart SET cart_quantity = ? WHERE cart_num = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartQuantity);
            pstmt.setInt(2, cartNum);
            return pstmt.executeUpdate() > 0;
        }
    }

    // 장바구니 삭제
    public boolean deleteCart(int cartNum) throws SQLException {
        sql = "DELETE FROM cart WHERE cart_num = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartNum);
            return pstmt.executeUpdate() > 0;
        }
    }

    // 사용자의 장바구니 목록 조회
    public ResultSet getUserCart(String userId) throws SQLException {
        sql = "SELECT c.*, b.book_name, b.book_price " +
             "FROM cart c " +
             "JOIN books b ON c.book_code = b.book_code " +
             "WHERE c.user_id = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userId);
        return pstmt.executeQuery();
    }

    // 장바구니 개별 항목 조회
    public ResultSet getCartItem(int cartNum) throws SQLException {
        sql = "SELECT c.*, b.book_name, b.book_price " +
             "FROM cart c " +
             "JOIN books b ON c.book_code = b.book_code " +
             "WHERE c.cart_num = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, cartNum);
        return pstmt.executeQuery();
    }
}