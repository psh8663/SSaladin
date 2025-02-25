package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import kr.util.DBUtil;

public class Order_detailsDAO {
    private Connection conn = null;
    private PreparedStatement pstmt = null;
    private String sql = null;
    private ResultSet rs = null;
    
    public Order_detailsDAO(Connection conn) {
        this.conn = conn;
    }
    
    // 주문 상세 등록
    public boolean insertOrderDetail(int orderNum, int bookCode, int orderQuantity, int orderPrice) 
            throws SQLException {
        sql = "INSERT INTO order_details (detail_num, order_num, book_code, order_quantity, order_price) " +
             "VALUES (detail_num_seq.NEXTVAL, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderNum);
            pstmt.setInt(2, bookCode);
            pstmt.setInt(3, orderQuantity);
            pstmt.setInt(4, orderPrice);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    // 주문 상세 조회
    public ResultSet getOrderDetail(int detailNum) throws SQLException {
        sql = "SELECT * FROM order_details WHERE detail_num = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, detailNum);
        return pstmt.executeQuery();
    }
    
    // 주문별 상세 목록 조회 (수정: o -> od로 변경)
    public ResultSet getOrderDetails(int orderNum) throws SQLException {
        sql = "SELECT od.*, b.book_title " +
             "FROM order_details od " +
             "JOIN books b ON od.book_code = b.book_code " +
             "WHERE od.order_num = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, orderNum);
        return pstmt.executeQuery();
    }
   
    // 사용자의 id별로 주문 목록 조회
    public void getOrderDetailsByUserId(String userId) throws SQLException {
        sql = "SELECT DISTINCT od.book_code, b.book_title, b.book_price "
        		+ "FROM order_details od "
        		+ "JOIN books b ON od.book_code = b.book_code "
        		+ "JOIN orders o ON od.order_num = o.order_num "
        		+ "WHERE o.user_id = ? "
        		+ "ORDER BY od.book_code";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userId);
        rs = pstmt.executeQuery();
        
        System.out.println("-".repeat(50));
        if (rs.next()) {
            do {
                System.out.println("도서 번호 : " + rs.getInt("book_code"));
                System.out.println("제목 : " + rs.getString("book_title"));
                System.out.println("-".repeat(50));
            } while (rs.next());
        } else {
            System.out.println("구매한 도서가 없습니다.");
        }
    }
    
    // 주문 상세 수정
    public boolean updateOrderDetail(int detailNum, int orderQuantity, int orderPrice) 
            throws SQLException {
        sql = "UPDATE order_details SET order_quantity = ?, order_price = ? WHERE detail_num = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderQuantity);
            pstmt.setInt(2, orderPrice);
            pstmt.setInt(3, detailNum);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    // 주문에 속한 도서의 총 개수 조회
    public int getTotalItemCount(int orderNum) throws SQLException {
        sql = "SELECT SUM(order_quantity) as total FROM order_details WHERE order_num = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderNum);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        }
    }
    
    // 주문 상세 삭제
    public boolean deleteOrderDetail(int detailNum) throws SQLException {
        sql = "DELETE FROM order_details WHERE detail_num = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, detailNum);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    // 주문에 속한 모든 상세 항목 삭제
    public boolean deleteAllOrderDetails(int orderNum) throws SQLException {
        sql = "DELETE FROM order_details WHERE order_num = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderNum);
            return pstmt.executeUpdate() > 0;
        }
    }
}