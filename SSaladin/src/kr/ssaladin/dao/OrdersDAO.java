package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import kr.util.DBUtil;

public class OrdersDAO {
    private Connection conn = null;
    private PreparedStatement pstmt = null;
    private String sql = null;
    private ResultSet rs = null;
    private boolean flag = false;

    public OrdersDAO(Connection conn) {
        this.conn = conn;
    }

    // 주문 등록
    public boolean insertOrder(String userId, int orderTotal, int orderStatus) throws SQLException {
        sql = "INSERT INTO orders (user_id, order_total, order_status, order_date) VALUES (?, ?, ?, SYSDATE)";
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
        return pstmt.executeQuery();
    }

    // 사용자별 주문 목록 조회
    public ResultSet getUserOrders(String userId) throws SQLException {
        sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, userId);
        return pstmt.executeQuery();
    }
}