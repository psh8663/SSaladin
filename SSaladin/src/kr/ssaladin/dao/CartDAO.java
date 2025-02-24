package kr.ssaladin.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import kr.util.DBUtil;

public class CartDAO {

	private Connection conn = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;
	private String sql = null;

    
    //생성자 생성

    public CartDAO() {
        try {
            this.conn = DBUtil.getConnection(); // DBUtil에서 Connection 생성
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    
    // connection injection 을 위한 생성자
    public CartDAO(Connection conn) {
        this.conn = conn;	// this. 로 커넥션을 외부에서 받아옴
    }
    
    // connection이 null인지 확인하는 예외처리
    private void checkConnection() throws SQLException{
    	if (conn==null) {
    		throw new SQLException("데이터베이스와의 커넥션이 정상적으로 이뤄지지 않았습니다.");
    	}
    }

    // 장바구니 추가
    public boolean insertCart(String userId, int bookCode, int cartQuantity) throws SQLException {
    	checkConnection();
    	sql = "INSERT INTO cart (cart_num, user_id, book_code, cart_quantity) VALUES (cart_num_seq.NEXTVAL, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setInt(2, bookCode);
            pstmt.setInt(3, cartQuantity);
            return pstmt.executeUpdate() > 0;
        }
    }

    // 장바구니 수량 수정
    public boolean updateCartQuantity(int cartNum, int cartQuantity) throws SQLException {
    	checkConnection();
        sql = "UPDATE cart SET cart_quantity = ? WHERE cart_num = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartQuantity);
            pstmt.setInt(2, cartNum);
            return pstmt.executeUpdate() > 0;
        }
    }

    // 장바구니 삭제
    public boolean deleteCart(int cartNum) throws SQLException {
    	checkConnection();
        sql = "DELETE FROM cart WHERE cart_num = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartNum);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    

    // 사용자의 장바구니 목록 조회
    public ResultSet getUserCart(String userId) throws SQLException {
    	checkConnection();
        sql = "SELECT c.*, b.book_title, b.book_price " +
             "FROM cart c " +
             "JOIN books b ON c.book_code = b.book_code " +
             "WHERE c.user_id = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userId);
        return pstmt.executeQuery();
    }

    // 장바구니 개별 항목 조회
    public ResultSet getCartItem(int cartNum) throws SQLException {
    	checkConnection();
        sql = "SELECT c.*, b.book_title, b.book_price " +
             "FROM cart c " +
             "JOIN books b ON c.book_code = b.book_code " +
             "WHERE c.cart_num = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, cartNum);
        return pstmt.executeQuery();
    }
    
 // 장바구니 상품 구매 후 장바구니 초기화
    public boolean clearCart(String userId) throws SQLException {
    	checkConnection();
        sql = "DELETE FROM cart WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

}