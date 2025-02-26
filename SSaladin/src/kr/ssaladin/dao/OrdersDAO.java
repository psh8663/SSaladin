package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import kr.ssaladin.service.CartService;
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
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setInt(2, orderTotal);
            pstmt.setInt(3, orderStatus);
            return pstmt.executeUpdate() > 0;
        } finally {
            DBUtil.executeClose(null, pstmt, null); 
        }
    }
    
    // 주문 상태 수정
    public boolean updateOrderStatus(int orderNum, int orderStatus) throws SQLException {
        sql = "UPDATE orders SET order_status = ? WHERE order_num = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderStatus);
            pstmt.setInt(2, orderNum);
            return pstmt.executeUpdate() > 0;
        } finally {
            DBUtil.executeClose(null, pstmt, null);
    }
    }
    
    // 주문 조회
    public ResultSet getOrder(int orderNum) throws SQLException {
        sql = "SELECT * FROM orders WHERE order_num = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderNum);
            rs = pstmt.executeQuery();
            return rs;

        } catch (SQLException e) {
            DBUtil.executeClose(rs, pstmt, null);
            throw e;
        }
    }
    
    // 사용자별 주문 목록 조회
    public ResultSet getUserOrders(String userId) throws SQLException {
        sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();
            return rs;

        } catch (SQLException e) {
            DBUtil.executeClose(rs, pstmt, null);
            throw e;
        }
    }
    
    // 가장 최근에 생성된 주문 번호 조회
    public int getLastOrderNum() throws SQLException {
        sql = "SELECT MAX(order_num) as last_order_num FROM orders";
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("last_order_num");
            }
            return -1;
        } finally {
            DBUtil.executeClose(rs, pstmt, null);
        }
    }
    
    // 특정 사용자의 가장 최근 주문 번호 조회
    public int getLastOrderNumByUser(String userId) throws SQLException {
        sql = "SELECT MAX(order_num) as last_order_num FROM orders WHERE user_id = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("last_order_num");
            }
            return -1;
        } finally {
            DBUtil.executeClose(rs, pstmt, null);
        }
    }
    


    // 주문 삭제 (관리자용)
    public boolean deleteOrder(int orderNum) throws SQLException {
        sql = "DELETE FROM orders WHERE order_num = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, orderNum);
            return pstmt.executeUpdate() > 0;
        } finally {
            DBUtil.executeClose(null, pstmt, null);
        }
    }
    
    // 주문 총액 업데이트
    public boolean updateOrderTotal(int orderNum, int newTotal) throws SQLException {
        sql = "UPDATE orders SET order_total = ? WHERE order_num = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, newTotal);
            pstmt.setInt(2, orderNum);
            return pstmt.executeUpdate() > 0;
        } finally {
            DBUtil.executeClose(null, pstmt, null);
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
        
        try {
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
        } finally {
            DBUtil.executeClose(rs, pstmt, null);
        }
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
    
    // 사용자 포인트 업데이트 (차감 또는 증가)
    public boolean updateUserPoint(String userId, int pointChange) throws SQLException {
        String sql = "UPDATE users SET user_point = user_point + ? WHERE user_id = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pointChange);
            pstmt.setString(2, userId);
            return pstmt.executeUpdate() > 0;
        } finally {
            DBUtil.executeClose(null, pstmt, null);
        }
    }

    // 주문 생성과 포인트 차감을 한 트랜잭션으로 처리
    public boolean createOrderWithPointUpdate(String userId, int orderTotal, int orderStatus, 
                                              List<CartService.CartItem> items,
                                              AdminBookDAO adminBookDAO) throws SQLException, ClassNotFoundException {
        
        boolean success = false;
        boolean originalAutoCommit = false;
        
        try {
            // 현재 autoCommit 상태 저장
            originalAutoCommit = conn.getAutoCommit();
            
            // 트랜잭션 시작
            conn.setAutoCommit(false);
            
            // 주문 기본 정보 등록
            if (!insertOrder(userId, orderTotal, orderStatus)) {
                conn.rollback();
                return false;
            }
            
            // 방금 생성한 주문번호를 조회
            int orderNum = getLastOrderNumByUser(userId);
            if (orderNum == -1) {
                System.out.println("주문번호 조회에 실패했습니다.");
                conn.rollback();
                return false;
            }
            
            // 주문 상세 정보와 재고 처리
            Order_detailsDAO orderDetailsDAO = new Order_detailsDAO(conn);
            for (CartService.CartItem item : items) {
                // 주문 상세 정보 등록
                if (!orderDetailsDAO.insertOrderDetail(orderNum, item.getBookCode(),
                        item.getCartQuantity(), item.getBookPrice())) {
                    System.out.println("주문 상세 정보 등록에 실패했습니다.");
                    conn.rollback();
                    return false;
                }
                
                // 재고 차감
                if (!adminBookDAO.updateOrderStock(item.getBookCode(), item.getCartQuantity())) {
                    System.out.println("재고 차감에 실패했습니다.");
                    conn.rollback();
                    return false;
                }
                
                // 재고가 0일 경우 품절 상태로 변경
                if (!adminBookDAO.updateBookStatus(item.getBookCode())) {
                    System.out.println("도서 상태 업데이트에 실패했습니다.");
                    conn.rollback();
                    return false;
                }
            }
            
            // 사용자 포인트 차감
            if (!updateUserPoint(userId, -orderTotal)) {
                System.out.println("포인트 차감에 실패했습니다.");
                conn.rollback();
                return false;
            }
            
            // 모든 작업이 성공적으로 완료되면 커밋
            conn.commit();
            success = true;
            System.out.println("주문이 성공적으로 완료되었습니다. 주문번호: " + orderNum);
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("롤백 중 오류 발생: " + ex.getMessage());
                ex.printStackTrace();
            }
            System.out.println("주문 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(originalAutoCommit);
                }
            } catch (SQLException e) {
                System.out.println("AutoCommit 설정 복원 중 오류: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return success;
    }
}