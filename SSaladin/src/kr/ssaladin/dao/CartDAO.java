package kr.ssaladin.dao;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
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
    private void checkConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            try {
                this.conn = DBUtil.getConnection(); // 연결이 닫혔거나 null이면 다시 연결 시도
            } catch (ClassNotFoundException e) {
                throw new SQLException("데이터베이스 커넥션 실패.", e);
            }
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
    public boolean updateCartQuantity(String userId, int book_code, int cartQuantity) throws SQLException {
        checkConnection();

        // 장바구니의 book_code에 해당하는 cart_num 찾기 
        String selectCartNumSql = "SELECT cart_num FROM cart WHERE user_id = ? AND book_code = ?";
        int cartNum = -1;

        try (PreparedStatement pstmt = conn.prepareStatement(selectCartNumSql)) {
            pstmt.setString(1, userId);
            pstmt.setInt(2, book_code);

            // cart_num 조회
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    cartNum = rs.getInt("cart_num");
                }
            }

            
            if (cartNum == -1) {
                System.out.println("장바구니에 해당 도서가 없습니다.");
                return false;
            }

            // cart_num을 찾았으면, 해당 cart_num에 대해 수량 수정
            String updateSql = "UPDATE cart SET cart_quantity = ? WHERE cart_num = ?";
            try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                updatePstmt.setInt(1, cartQuantity);
                updatePstmt.setInt(2, cartNum);

                return updatePstmt.executeUpdate() > 0;  // 수정 성공 시 true 반환
            }
        }
    }
/*
    // 장바구니 삭제
    public boolean deleteCart(int cartNum) throws SQLException {
    	checkConnection();
        sql = "DELETE FROM cart WHERE cart_num = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartNum);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    */
    
    public boolean deleteCart(int cartNum) throws SQLException {
        checkConnection();
        String sql = "DELETE FROM cart WHERE cart_num = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cartNum);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("삭제할 항목이 존재하지 않습니다. cart_num: " + cartNum);
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("SQL 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;  // 예외 재던지기 (혹은 다른 처리를 할 수 있음)
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