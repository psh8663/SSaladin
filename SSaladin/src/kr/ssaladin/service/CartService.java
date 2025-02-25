package kr.ssaladin.service;	

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kr.ssaladin.dao.AdminBookDAO;
import kr.ssaladin.dao.CartDAO;
import kr.ssaladin.dao.UserDAO;
import kr.util.DBUtil;

public class CartService {
    private CartDAO cartDAO;
    private UserDAO userDAO;
    private AdminBookDAO adminBookDAO;
    private BufferedReader br;

    public CartService() throws ClassNotFoundException, SQLException {
//        Connection conn = DBUtil.getConnection();  // DB 연결 생성
        this.cartDAO = new CartDAO();  
        this.userDAO = new UserDAO();
        this.adminBookDAO = new AdminBookDAO();
    }

    // CartItem 내부 클래스 선언
    public static class CartItem {
        private int cartNum;
        private int book_code;
        private String book_title;
        private int book_price;
        private int cartQuantity;

        public CartItem() {}
        public CartItem(int bookCode, int cartQuantity, int bookPrice) {
            this.book_code = bookCode;
            this.cartQuantity = cartQuantity;
            this.book_price = bookPrice;
        }
        
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
            
            //0.재고확인
			for (CartItem item : items) {
	            boolean stockAvailable = adminBookDAO.checkStock(item.getBookCode(), item.getCartQuantity());
	            if (!stockAvailable) {
	                conn.rollback();
	                return false;  // 재고 부족 시 주문 진행 불가
	            }
	        }

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
            	
            	// 재고 차감
				boolean stockUpdated = adminBookDAO.updateOrderStock(item.getBookCode(), item.getCartQuantity());
				if (!stockUpdated) {
					conn.rollback();
					return false;
				}
				//재고가 0일 경우 품절 상태로 변경
			    boolean statusUpdated = adminBookDAO.updateBookStatus(item.getBookCode());
			    if (!statusUpdated) {
			        conn.rollback();
			        return false;
			    } 
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
    
    // ======================== 추가된 메서드들 ========================
    
    // 장바구니 관리 메뉴
    public void manageCart(String userId, int userPoint) throws IOException {
        // 장바구니 관리
        boolean cartMenu = true;
        while (cartMenu) {
            System.out.println("\n==================================== 장바구니 관리 ======================================");
            System.out.println();
            System.out.println("1. 장바구니 목록, 2. 장바구니 상품 수량 변경, 3. 장바구니 상품 삭제, 4. 구매하기, 5. 뒤로가기: ");
            try {
                int no = Integer.parseInt(br.readLine());
                if (no == 1) {
                    // 장바구니 상품 목록 조회
                    showCartItems(userId);
                } else if (no == 2) {
                    // 장바구니에 있는 상품의 수량 변경
                    updateCartItemQuantity(userId);
                } else if (no == 3) {
                    // 장바구니 내의 상품 삭제
                    deleteCartItem();
                } else if (no == 4) {
                    // 장바구니의 상품 구매
                    purchaseCartItem(userId, userPoint);
                } else if (no == 5) {
                    cartMenu = false;
                } else {
                    System.out.println("잘못된 입력입니다.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[ 숫자만 입력 가능합니다. ]");
            }
        }
    }
    
 // 장바구니 목록 보기
    public void showCartItems(String userId) throws IOException {
        if (userId == null || userId.isEmpty()) {
            System.out.println("로그인 후 장바구니 목록을 조회할 수 있습니다.");
            return;
        }

        // 로그인 상태일 때만 장바구니 목록 조회
        try {
            System.out.println("==================================== 내 장바구니 목록 ====================================");
            System.out.println();

            // 장바구니 항목을 가져오기
            List<CartItem> cartItems = getUserCartItems(userId);

            // 장바구니가 비어있는지 확인
            if (cartItems.isEmpty()) {
                System.out.println("장바구니에 담긴 상품이 없습니다.");
            } else {
                // 장바구니 목록 출력
                cartItems.forEach(item -> System.out.println(
                        "주문번호: " + item.getCartNum() + ", 도서코드: " + item.getBookCode() + ", 도서명: " + item.getBookTitle()
                                + ", 수량: " + item.getCartQuantity() + ", 가격: " + item.getBookPrice()));
            }
        } catch (Exception e) {
            System.out.println("장바구니 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    

    // 장바구니 상품 수량 변경
    public void updateCartItemQuantity(String userId) throws IOException {
        // 장바구니에 담긴 상품의 수량 수정
        showCartItems(userId);
        System.out.println();
        System.out.println("-".repeat(86));

        try {
            System.out.print("수량을 수정할 상품의 도서 코드를 입력하세요: ");
            int bookCode = Integer.parseInt(br.readLine());
            System.out.print("새로운 수량을 입력하세요: ");
            int newQuantity = Integer.parseInt(br.readLine());

            // 상품 수량 수정
            boolean success = updateQuantity(userId, bookCode, newQuantity);
            if (success) {
                System.out.println("상품 수량이 성공적으로 수정되었습니다.");
            } else {
                System.out.println("상품 수량 수정에 실패했습니다.");
            }
        } catch (NumberFormatException e) {
            System.out.println("올바른 숫자를 입력해주세요.");
        } catch (Exception e) {
            System.out.println("수량 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // 장바구니 상품 삭제
    public void deleteCartItem() throws IOException {
        // 장바구니 상품 삭제
        System.out.println();
        System.out.println("-".repeat(66));
        System.out.println();
        try {
            System.out.print("장바구니에서 삭제할 주문 번호를 입력하세요: ");
            int productId = Integer.parseInt(br.readLine());

            // 장바구니에서 상품 삭제
            boolean success = removeFromCart(productId);
            if (success) {
                System.out.println("상품이 장바구니에서 삭제되었습니다.");
            } else {
                System.out.println("상품 삭제에 실패했습니다.");
            }
        } catch (NumberFormatException e) {
            System.out.println("올바른 숫자를 입력해주세요.");
        } catch (Exception e) {
            System.out.println("상품 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    public void purchaseCartItem(String userId, int userPoint) throws IOException {
        try {
            // 장바구니 항목 조회
            List<CartItem> cartItems = getUserCartItems(userId);

            if (cartItems == null || cartItems.isEmpty()) {
                System.out.println("장바구니가 비어있습니다.");
                return;
            }

            // 총액 계산
            int totalAmount = calculateTotal(userId);

            // 현재 포인트 확인
            if (totalAmount > userPoint) {
                System.out.println("포인트가 부족합니다. 현재 포인트: " + userPoint + "원, 필요 포인트: " + totalAmount + "원");
                System.out.println("포인트를 충전해주세요.");
                return;
            }

            // 구매 확인
            System.out.println("총 구매 금액: " + totalAmount + "원");
            System.out.println("구매하시겠습니까? (1: 예, 2: 아니오)");
            int confirm = Integer.parseInt(br.readLine());

            if (confirm == 1) {
                boolean success = processPurchase(userId, cartItems, totalAmount);

                if (success) {
                    System.out.println("구매가 완료되었습니다.");
                    System.out.println("잔여 포인트: " + (userPoint - totalAmount) + "원");
                } else {
                    System.out.println("구매 처리 중 오류가 발생했습니다.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("올바른 숫자를 입력해주세요.");
        } catch (Exception e) {
            System.out.println("구매 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
}