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
    private Connection conn;
    private CartDAO cartDAO;
    private UserDAO userDAO;

    public CartService() {
        try {
            conn = DBUtil.getConnection();
            cartDAO = new CartDAO(conn);
            userDAO = new UserDAO(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 로그인 상태 확인
    private boolean checkLoginStatus(String userId, String userPw) {
        try {
            return userDAO.loginCheck(userId, userPw);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 장바구니 추가 (로그인 체크 포함)
    public boolean addToCart(String userId, String userPw, int bookCode, int quantity) {
        if (!checkLoginStatus(userId, userPw)) {
            return false;  // 로그인 실패
        }
        
        try {
            return cartDAO.insertCart(userId, bookCode, quantity);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 장바구니 수량 수정 (로그인 체크 포함)
    public boolean updateQuantity(String userId, String userPw, int cartNum, int quantity) {
        if (!checkLoginStatus(userId, userPw)) {
            return false;  // 로그인 실패
        }
        
        try {
            return cartDAO.updateCartQuantity(cartNum, quantity);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 장바구니 항목 삭제 (로그인 체크 포함)
    public boolean removeFromCart(String userId, String userPw, int cartNum) {
        if (!checkLoginStatus(userId, userPw)) {
            return false;  // 로그인 실패
        }
        
        try {
            return cartDAO.deleteCart(cartNum);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 사용자의 장바구니 목록 조회 (로그인 체크 포함)
    public List<CartItem> getUserCartItems(String userId, String userPw) {
        if (!checkLoginStatus(userId, userPw)) {
            return null;  // 로그인 실패
        }
        
        List<CartItem> cartItems = new ArrayList<>();
        try {
            try (ResultSet rs = cartDAO.getUserCart(userId)) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setCartNum(rs.getInt("cart_num"));
                    item.setBookCode(rs.getInt("book_code"));
                    item.setBookName(rs.getString("book_name"));
                    item.setBookPrice(rs.getInt("book_price"));
                    item.setCartQuantity(rs.getInt("cart_quantity"));
                    cartItems.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return cartItems;
    }

    // 장바구니 개별 항목 조회 (로그인 체크 포함)
    public CartItem getCartItem(String userId, String userPw, int cartNum) {
        if (!checkLoginStatus(userId, userPw)) {
            return null;  // 로그인 실패
        }
        
        try {
            try (ResultSet rs = cartDAO.getCartItem(cartNum)) {
                if (rs.next()) {
                    CartItem item = new CartItem();
                    item.setCartNum(rs.getInt("cart_num"));
                    item.setBookCode(rs.getInt("book_code"));
                    item.setBookName(rs.getString("book_name"));
                    item.setBookPrice(rs.getInt("book_price"));
                    item.setCartQuantity(rs.getInt("cart_quantity"));
                    return item;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    // 장바구니 총액 계산 (로그인 체크 포함)
    public int calculateTotal(String userId, String userPw) {
        List<CartItem> items = getUserCartItems(userId, userPw);
        if (items == null) return 0;
        
        return items.stream()
            .mapToInt(item -> item.getBookPrice() * item.getCartQuantity())
            .sum();
    }

    // 리소스 해제
    public void close() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}