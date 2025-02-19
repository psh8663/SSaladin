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
    private boolean flag = false;

    public Order_detailsDAO(Connection conn) {
        this.conn = conn;
    }

    // 주문 상세 등록
    public boolean insertOrderDetail(int orderNum, int bookCode, int orderQuantity, int orderPrice) 
            throws SQLException {
        sql = "INSERT INTO order_details (order_num, book_code, order_quantity, order_price) " +
             "VALUES (?, ?, ?, ?)";
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

    // 주문별 상세 목록 조회
    public ResultSet getOrderDetails(int orderNum) throws SQLException {
        sql = "SELECT od.*, b.book_name, b.book_price " +
             "FROM order_details od " +
             "JOIN books b ON od.book_code = b.book_code " +
             "WHERE od.order_num = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, orderNum);
        return pstmt.executeQuery();
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
}