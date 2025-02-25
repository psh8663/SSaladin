package kr.ssaladin.service;	

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kr.ssaladin.dao.CartDAO;
import kr.ssaladin.dao.UserDAO;
import kr.util.DBUtil;

public class CartService {
    private CartDAO cartDAO;
    private UserDAO userDAO;

    public CartService() throws ClassNotFoundException, SQLException {
//        Connection conn = DBUtil.getConnection();  // DB 연결 생성
        this.cartDAO = new CartDAO();  
        this.userDAO = new UserDAO();
    }

    // CartItem 내부 클래스 선언
    public static class CartItem {
        private int cartNum;
        private int book_code;
        private String book_title;
        private int book_price;
        private int cartQuantity;

        public CartItem() {}

        public int getCartNum() { return cartNum; }
        public void setCartNum(int cartNum) { this.cartNum = cartNum; }

        public int getBookCode() { return book_code; }
        public void setBookCode(int book_code) { this.book_code = book_code; }

        public String getBookTitle() { return book_title; }
        public void setBookTitle(String bookTitle) { this.book_title = bookTitle; }

        public int getBookPrice() { return book_price; }
        public void setBookPrice(int bookPrice) { this.book_price = bookPrice; }

        public int getCartQuantity() { return cartQuantity; }
        public void setCartQuantity(int cartQuantity) { this.cartQuantity = cartQuantity; }
    }

    // 로그인 상태 확인
    private boolean checkLoginStatus(String userId, String userPw){
        try {
            return userDAO.checkLogin(userId, userPw);
        } catch (Exception e) {
            System.out.println("로그인 체크 중 오류 발생");
            e.printStackTrace();
            return false;
        }
    }

    // 장바구니에 상품 추가
    public boolean addToCart(String userId, String userPw, int book_code, int quantity) {
        Connection conn = null;
        boolean result = false;
        

        try {
            conn = DBUtil.getConnection();
            result = cartDAO.insertCart(userId, book_code, quantity);

        } catch (Exception e) {
            System.out.println("장바구니 추가 중 오류가 발생했습니다.");
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, null, conn);
        }
        return result;
    }

    // 장바구니 수량 수정 
    public boolean updateQuantity(String userId, int book_code, int quantity) {
        Connection conn = null;
        boolean result = false;

        try {
           
            conn = DBUtil.getConnection();
            cartDAO = new CartDAO(conn);
            result = cartDAO.updateCartQuantity(userId, book_code, quantity);

        } catch (Exception e) {
            System.out.println("수량 수정 중 오류 발생");
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, null, conn);
        }
        return result;
    }

 // 장바구니 항목 삭제
    public boolean removeFromCart(int cartNum) {
        Connection conn = null;
        boolean result = false;

        try {
            conn = DBUtil.getConnection();
            cartDAO = new CartDAO(conn);
            result = cartDAO.deleteCart(cartNum);

            if (!result) {
                System.out.println("장바구니 항목 삭제에 실패했습니다." );
            }
        } catch (Exception e) {
            System.out.println("장바구니 항목 삭제 중 오류 발생" + e.getMessage());
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, null, conn);
        }
        return result;
    }


    
    // 사용자의 장바구니 목록 조회 
    public List<CartItem> getUserCartItems(String userId) {
    	
        Connection conn = null;
        ResultSet rs = null;
        List<CartItem> cartItems = new ArrayList<>();	

        try {
        	// DBUtil 이용해서 connection 수행
            conn = DBUtil.getConnection();
            
            // 장바구니 목록을 다시 조회 (ex. 장바구니 변경or 삭제 후 장바구니를 조회할 때) 시
            //  Connection 을 다시 연결
            if(conn == null || conn.isClosed() ) {
            	conn = DBUtil.getConnection();
            }
            rs = cartDAO.getUserCart(userId);

            while (rs.next()) {
                CartItem item = new CartItem();
                item.setCartNum(rs.getInt("cart_num"));
                item.setBookCode(rs.getInt("book_code"));
                item.setBookTitle(rs.getString("book_title"));
                item.setBookPrice(rs.getInt("book_price"));
                item.setCartQuantity(rs.getInt("cart_quantity"));
                cartItems.add(item);
            }

        } catch (Exception e) {
            System.out.println("장바구니 목록 조회 중 오류 발생");
            e.printStackTrace();
            return cartItems;
        } finally {
            DBUtil.executeClose(rs, null, conn);
        }
        return cartItems;
    }

    
    
    // 장바구니 개별 항목 조회 (로그인 체크 포함)
    public CartItem getCartItem(String userId, String userPw, int cartNum) {
        Connection conn = null;
        ResultSet rs = null;
        CartItem item = null;

        try {
            if (!checkLoginStatus(userId, userPw)) {
                return null;  // 로그인 실패
            }

            conn = DBUtil.getConnection();
            cartDAO = new CartDAO(conn);
            rs = cartDAO.getCartItem(cartNum);
 
            if (rs.next()) {
                item = new CartItem();
                item.setCartNum(rs.getInt("cart_num"));
                item.setBookCode(rs.getInt("book_code"));
                item.setBookTitle(rs.getString("book_title"));
                item.setBookPrice(rs.getInt("book_price"));
                item.setCartQuantity(rs.getInt("cart_quantity"));
            }

        } catch (Exception e) {
            System.out.println("장바구니 항목 조회 중 오류 발생");
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, null, conn);
        }
        return item;
    }

    // 장바구니 총액 계산 (로그인 체크 포함)
    public int calculateTotal(String userId) {
        List<CartItem> items = getUserCartItems(userId);
        if (items == null) return 0;
        
        return items.stream()
            .mapToInt(item -> item.getBookPrice() * item.getCartQuantity())
            .sum();
    }
    
    // 장바구니 상품 구매
    public boolean processPurchase(String userId, List<CartItem> items, int totalAmount) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean success = false;
        
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);  // 트랜잭션 시작

            // 1. orders 테이블에 주문 생성
            String orderSql = "INSERT INTO orders (order_num, user_id, order_total, order_status) " +
                    "VALUES (order_num_seq.NEXTVAL, ?, ?, 1)";
            pstmt = conn.prepareStatement(orderSql);
            pstmt.setString(1, userId);
            pstmt.setInt(2, totalAmount);   
            pstmt.executeUpdate();

            // 2. order_details 테이블에 주문 상세 정보 추가
            String detailSql = "INSERT INTO order_details (detail_num, order_num, book_code, order_quantity, order_price) " +
            		"VALUES (detail_num_seq.NEXTVAL, order_num_seq.CURRVAL, ?, ?, ?)";
            for (CartItem item : items) {
            	pstmt = conn.prepareStatement(detailSql);
            	pstmt.setInt(1, item.getBookCode());
            	pstmt.setInt(2, item.getCartQuantity());
            	pstmt.setInt(3, item.getBookPrice());
            	pstmt.executeUpdate();
            }

            // 3. 사용자 포인트 차감
            String pointSql = "UPDATE users SET user_point = user_point - ? WHERE user_id = ?";
            pstmt = conn.prepareStatement(pointSql);
            pstmt.setInt(1, totalAmount);
            pstmt.setString(2, userId);
            int pointUpdateResult = pstmt.executeUpdate();

            if (pointUpdateResult == 0) {
                throw new SQLException("포인트 차감에 실패했습니다.");
            }

            // 4. 장바구니 비우기
            String cartSql = "DELETE FROM cart WHERE user_id = ?";
            pstmt = conn.prepareStatement(cartSql);
            pstmt.setString(1, userId);
            pstmt.executeUpdate();

            conn.commit();  // 트랜잭션 커밋
            success = true;
            
        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println("주문 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);  // autoCommit 설정 복구
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return success;
    }
}