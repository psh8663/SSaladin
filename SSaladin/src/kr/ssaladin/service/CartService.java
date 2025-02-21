package kr.ssaladin.service;

import java.sql.Connection;
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
        Connection conn = DBUtil.getConnection();  // DB 연결 생성
        cartDAO = new CartDAO(conn);  // CartDAO에 Connection 전달
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

        public String getBookName() { return book_title; }
        public void setBookName(String bookName) { this.book_title = bookName; }

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

    // 장바구니 추가 (로그인 체크 포함)
    public boolean addToCart(String userId, String userPw, int book_code, int quantity) {
        Connection conn = null;
        boolean result = false;

        try {
            if (!checkLoginStatus(userId, userPw)) {
                return false;  // 로그인 실패
            }

            conn = DBUtil.getConnection();
            cartDAO = new CartDAO(conn);
            result = cartDAO.insertCart(userId, book_code, quantity);

        } catch (Exception e) {
            System.out.println("장바구니 추가 중 오류 발생");
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, null, conn);
        }
        return result;
    }

    // 장바구니 수량 수정 (로그인 체크 포함)
    public boolean updateQuantity(String userId, String userPw, int cartNum, int quantity) {
        Connection conn = null;
        boolean result = false;

        try {
            if (!checkLoginStatus(userId, userPw)) {
                return false;  // 로그인 실패
            }

            conn = DBUtil.getConnection();
            cartDAO = new CartDAO(conn);
            result = cartDAO.updateCartQuantity(cartNum, quantity);

        } catch (Exception e) {
            System.out.println("수량 수정 중 오류 발생");
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, null, conn);
        }
        return result;
    }

    // 장바구니 항목 삭제 (로그인 체크 포함)
    public boolean removeFromCart(String userId, String userPw, int cartNum) {
        Connection conn = null;
        boolean result = false;

        try {
            if (!checkLoginStatus(userId, userPw)) {
                return false;  // 로그인 실패
            }

            conn = DBUtil.getConnection();
            cartDAO = new CartDAO(conn);
            result = cartDAO.deleteCart(cartNum);

        } catch (Exception e) {
            System.out.println("장바구니 항목 삭제 중 오류 발생");
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, null, conn);
        }
        return result;
    }

    // 사용자의 장바구니 목록 조회 (로그인 체크 포함)
    public List<CartItem> getUserCartItems(String userId, String userPw) {
        Connection conn = null;
        ResultSet rs = null;
        List<CartItem> cartItems = new ArrayList<>();

        try {
            if (!checkLoginStatus(userId, userPw)) {
                return null;  // 로그인 실패
            }

            conn = DBUtil.getConnection();
            cartDAO = new CartDAO(conn);
            rs = cartDAO.getUserCart(userId);

            while (rs.next()) {
                CartItem item = new CartItem();
                item.setCartNum(rs.getInt("cart_num"));
                item.setBookCode(rs.getInt("book_code"));
                item.setBookName(rs.getString("book_name"));
                item.setBookPrice(rs.getInt("book_price"));
                item.setCartQuantity(rs.getInt("cart_quantity"));
                cartItems.add(item);
            }

        } catch (Exception e) {
            System.out.println("장바구니 목록 조회 중 오류 발생");
            e.printStackTrace();
            return null;
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
                item.setBookName(rs.getString("book_name"));
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
    public int calculateTotal(String userId, String userPw) {
        List<CartItem> items = getUserCartItems(userId, userPw);
        if (items == null) return 0;
        
        return items.stream()
            .mapToInt(item -> item.getBookPrice() * item.getCartQuantity())
            .sum();
    }
}