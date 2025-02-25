package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import kr.util.DBUtil;

public class OrdersDAO {
    private Connection conn = null;
    private PreparedStatement pstmt = null;
    private String sql = null;
    private ResultSet rs = null;
    
    public OrdersDAO(Connection conn) {
        this.conn = conn;
    }
    
    // 주문 등록
    public boolean insertOrder(String userId, int orderTotal, int orderStatus) throws SQLException {
        sql = "INSERT INTO orders (order_num, user_id, order_total, order_status, order_date) " +
              "VALUES (order_num_seq.NEXTVAL, ?, ?, ?, SYSDATE)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setInt(2, orderTotal);
            pstmt.setInt(3, orderStatus);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    // 주문 상태 수정
    public boolean updateOrderStatus(int orderNum, int orderStatus) throws SQLException {
        sql = "UPDATE orders SET order_status = ? WHERE order_num = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderStatus);
            pstmt.setInt(2, orderNum);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    // 주문 조회
    public ResultSet getOrder(int orderNum) throws SQLException {
        sql = "SELECT * FROM orders WHERE order_num = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, orderNum);
        return pstmt.executeQuery();
    }
    
    // 사용자별 주문 목록 조회
    public ResultSet getUserOrders(String userId) throws SQLException {
        sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userId);
        return pstmt.executeQuery();
    }
    
    // 가장 최근에 생성된 주문 번호 조회
    public int getLastOrderNum() throws SQLException {
        sql = "SELECT MAX(order_num) as last_order_num FROM orders";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("last_order_num");
            }
            return -1;
        }
    }
    
    // 특정 사용자의 가장 최근 주문 번호 조회
    public int getLastOrderNumByUser(String userId) throws SQLException {
        sql = "SELECT MAX(order_num) as last_order_num FROM orders WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("last_order_num");
            }
            return -1;
        }
    }
    
    // 특정 기간 내 주문 목록 조회
    public ResultSet getOrdersByDateRange(Date startDate, Date endDate) throws SQLException {
        sql = "SELECT * FROM orders WHERE order_date BETWEEN ? AND ? ORDER BY order_date DESC";
        pstmt = conn.prepareStatement(sql);
        pstmt.setDate(1, new java.sql.Date(startDate.getTime()));
        pstmt.setDate(2, new java.sql.Date(endDate.getTime()));
        return pstmt.executeQuery();
    }
    
    // 특정 상태의 주문 목록 조회
    public ResultSet getOrdersByStatus(int orderStatus) throws SQLException {
        sql = "SELECT * FROM orders WHERE order_status = ? ORDER BY order_date DESC";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, orderStatus);
        return pstmt.executeQuery();
    }
    
    // 주문 삭제 (관리자용)
    public boolean deleteOrder(int orderNum) throws SQLException {
        sql = "DELETE FROM orders WHERE order_num = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderNum);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    // 주문 총액 업데이트
    public boolean updateOrderTotal(int orderNum, int newTotal) throws SQLException {
        sql = "UPDATE orders SET order_total = ? WHERE order_num = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newTotal);
            pstmt.setInt(2, orderNum);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    // 주문 상세 정보 출력(콘솔)
    public void displayOrderDetails(int orderNum) throws SQLException {
        sql = "SELECT o.order_num, o.user_id, o.order_total, o.order_status, o.order_date, " +
              "od.detail_num, od.book_code, b.book_title, od.order_quantity, od.order_price " +
              "FROM orders o " +
              "JOIN order_details od ON o.order_num = od.order_num " +
              "JOIN books b ON od.book_code = b.book_code " +
              "WHERE o.order_num = ?";
        
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, orderNum);
        rs = pstmt.executeQuery();
        
        boolean isFirst = true;
        System.out.println("=".repeat(80));
        
        while (rs.next()) {
            if (isFirst) {
                System.out.println("주문번호: " + rs.getInt("order_num"));
                System.out.println("주문자ID: " + rs.getString("user_id"));
                System.out.println("주문상태: " + getOrderStatusString(rs.getInt("order_status")));
                System.out.println("주문일자: " + rs.getDate("order_date"));
                System.out.println("주문총액: " + rs.getInt("order_total") + "원");
                System.out.println("-".repeat(80));
                System.out.println("주문 상세 목록:");
                isFirst = false;
            }
            
            System.out.println("\t도서번호: " + rs.getInt("book_code"));
            System.out.println("\t도서명: " + rs.getString("book_title"));
            System.out.println("\t수량: " + rs.getInt("order_quantity"));
            System.out.println("\t가격: " + rs.getInt("order_price") + "원");
            System.out.println("\t-".repeat(70));
        }
        
        if (isFirst) {
            System.out.println("해당 주문 정보가 없습니다.");
        }
        
        System.out.println("=".repeat(80));
    }
    
    // 주문 상태 문자열 반환
    private String getOrderStatusString(int status) {
        switch (status) {
            case 1: return "결제 대기";
            case 2: return "결제 완료";
            case 3: return "배송 중";
            case 4: return "배송 완료";
            case 5: return "주문 취소";
            default: return "알 수 없음";
        }
    }
}