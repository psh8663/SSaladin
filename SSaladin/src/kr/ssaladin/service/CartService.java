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

import kr.ssaladin.SSaladinMain;
import kr.ssaladin.dao.AdminBookDAO;
import kr.ssaladin.dao.CartDAO;
import kr.util.DBUtil;

public class CartService {
	private CartDAO cartDAO;
	private AdminBookDAO adminBookDAO;
	private BufferedReader br;
	private PointRequestService pointRequestService;
	private SSaladinMain sSaladinMain; // SSaladinMain 참조 추가

	// 기존 생성자 유지
	public CartService() throws ClassNotFoundException, SQLException {
		this.cartDAO = new CartDAO();  
		this.adminBookDAO = new AdminBookDAO();
		this.br = new BufferedReader(new InputStreamReader(System.in));
		this.sSaladinMain = new SSaladinMain();
	}

	// SSaladinMain 객체를 받는 생성자 추가
	public CartService(SSaladinMain sSaladinMain) throws ClassNotFoundException, SQLException {
		this.cartDAO = new CartDAO();  
		this.adminBookDAO = new AdminBookDAO();
		this.br = new BufferedReader(new InputStreamReader(System.in));
		this.sSaladinMain = sSaladinMain;
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

	// 장바구니 관리 메뉴
	public void manageCart(String userId, int userPoint) throws IOException {
		// 장바구니 관리
		boolean cartMenu = true;
		while (cartMenu) {
			System.out.println("\n――――――――――――――――――――――――――――――――――――――――  장바구니 관리  ――――――――――――――――――――――――――――――――――――――――――");
			System.out.println("      1. 장바구니 목록     2. 장바구니 상품 수량 변경     3. 장바구니 상품 삭제     4. 구매하기      5. 뒤로가기 ");
			System.out.println("――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――");
			try {
				int no = Integer.parseInt(br.readLine());
				switch (no) {
					case 1: showCartItems(userId); break;
					case 2: updateCartItemQuantity(userId); break;
					case 3: deleteCartItem(userId); break;
					case 4: checkOut(userId, userPoint); break;
					case 5: cartMenu = false; break;
					default: System.out.println("잘못된 입력입니다.");
				}
			} catch (NumberFormatException e) {
				System.out.println("[ 숫자만 입력 가능합니다. ]");
			}
		}
	}

	// 장바구니에 상품 추가
	public boolean addToCart(String userId, int book_code, int quantity) {
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
		} finally {
			DBUtil.executeClose(rs, null, conn);
		}
		return cartItems;
	}

	// 장바구니 총액 계산 (로그인 체크 포함)
	public int calculateTotal(List<CartItem> items) {
		if (items == null || items.isEmpty()) return 0;
		
		int total = 0;
		for (CartItem item : items) {
			total += item.getBookPrice() * item.getCartQuantity();
		}
		return total;
	}

	// 장바구니 목록 보기
	public void showCartItems(String userId) throws IOException {
		if (userId == null || userId.isEmpty()) {
			System.out.println("로그인 후 장바구니 목록을 조회할 수 있습니다.");
			return;
		}

		// 로그인 상태일 때만 장바구니 목록 조회
		try {
			System.out.println("――――――――――――――――――――――――――――――――――――――――― 내 장바구니 목록 ―――――――――――――――――――――――――――――――――――――――――");
			System.out.println();

			// 장바구니 항목을 가져오기
			List<CartItem> cartItems = getUserCartItems(userId);

			// 장바구니가 비어있는지 확인
			if (cartItems.isEmpty()) {
				System.out.println("장바구니에 담긴 상품이 없습니다.");
			} else {
				// 장바구니 목록 출력
				for (CartItem item : cartItems) {
					System.out.println("주문번호: " + item.getCartNum() + 
							", 도서코드: " + item.getBookCode() + 
							", 도서명: " + item.getBookTitle() + 
							", 수량: " + item.getCartQuantity() + 
							", 가격: " + item.getBookPrice());
				}
			}
			System.out.println("――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――");
		} catch (Exception e) {
			System.out.println("장바구니 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	// 장바구니 결제
	public void checkOut(String userId, int userPoint) throws IOException {
		try {
			// 장바구니 항목 조회
			List<CartItem> cartItems = getUserCartItems(userId);

			if (cartItems.isEmpty()) {
				System.out.println("장바구니가 비어있습니다.");
				return;
			}

			// 장바구니 목록 출력
			System.out.println("\n―――――――――――――――――――――――――――――――――― 주문 상품 목록 ―――――――――――――――――――――――――――――――");

			// 총액 계산 및 주문 항목 표시
			int totalAmount = displayOrderItems(cartItems);

			// 재고 확인
			if (!checkStock(cartItems)) {
				return;
			}

			// 포인트 확인
			if (!checkPoint(userPoint, totalAmount)) {
				return;
			}

			// 주문 확인 및 처리
			processOrder(userId, cartItems, totalAmount, userPoint);

		} catch (NumberFormatException e) {
			System.out.println("올바른 숫자를 입력해주세요.");
		} catch (Exception e) {
			System.out.println("주문 처리 중 오류가 발생했습니다: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	// 주문 항목 표시 및 총액 계산
	private int displayOrderItems(List<CartItem> cartItems) {
		int totalAmount = 0;
		
		System.out.printf("%-10s %-30s %-10s %-10s %-10s\n", "도서코드", "도서명", "수량", "가격", "소계");
		System.out.println("――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――");

		for (CartItem item : cartItems) {
			int subtotal = item.getBookPrice() * item.getCartQuantity();
			totalAmount += subtotal;
			System.out.printf("%-10d %-30s %-10d %-10d %-10d\n", 
					item.getBookCode(), 
					item.getBookTitle(), 
					item.getCartQuantity(),
					item.getBookPrice(),
					subtotal);
		}
		
		System.out.println("―――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――");
		System.out.printf("%-50s 총 금액: %d원\n", "", totalAmount);
		
		return totalAmount;
	}
	
	// 재고 확인
	private boolean checkStock(List<CartItem> cartItems) {
		for (CartItem item : cartItems) {
			try {
				if (!adminBookDAO.checkStock(item.getBookCode(), item.getCartQuantity())) {
					System.out.println("도서코드 " + item.getBookCode() + "의 재고가 부족합니다.");
					System.out.println("일부 상품의 재고가 부족하여 주문을 진행할 수 없습니다.");
					return false;
				}
			} catch (SQLException | ClassNotFoundException e) {
				System.out.println("재고 확인 중 오류가 발생했습니다: " + e.getMessage());
				return false;
			}
		}
		return true;
	}
	
	// 포인트 확인
	private boolean checkPoint(int userPoint, int totalAmount) throws IOException {
		if (totalAmount > userPoint) {
			System.out.println("포인트가 부족합니다. 현재 포인트: " + userPoint + "원, 필요 포인트: " + totalAmount + "원");
			System.out.print("포인트를 충전하시겠습니까? (Y/N): ");
			String choice = br.readLine().trim().toUpperCase();

			if (choice.equals("Y")) {
				System.out.println("포인트 충전 화면으로 이동합니다.\n");
				SSaladinMain.chargePointFromcart();
				System.out.println("충전 요청이 완료되었습니다. \n");
			}
			return false;
		}
		return true;
	}
	
	// 주문 확인 및 처리
	private void processOrder(String userId, List<CartItem> cartItems, int totalAmount, int userPoint) throws IOException {
		System.out.println("\n―――――――――――――――――――――――――――――――――― 주문 확인 ――――――――――――――――――――――――――――――――――");
		System.out.println("총 구매 금액: " + totalAmount + "원");
		System.out.println("구매 후 잔여 포인트: " + (userPoint - totalAmount) + "원");
		System.out.print("주문을 확정하시겠습니까? (Y/N): ");
		
		String confirm = br.readLine().trim().toUpperCase();
		if (confirm.equals("Y")) {
			try {
				OrderService orderService = new OrderService();
				int orderNum = orderService.createOrderFromCart(userId, cartItems, totalAmount);

				if (orderNum > 0) {
					// 주문 성공 시 장바구니 비움
					clearUserCart(userId);
					System.out.println("\n주문이 성공적으로 완료되었습니다. 주문번호: " + orderNum);

					// 포인트 갱신
					updateUserPoint(userPoint, totalAmount);
				} else {
					System.out.println("주문 처리 중 오류가 발생했습니다.");
				}
			} catch (Exception e) {
				System.out.println("주문 처리 중 오류가 발생했습니다: " + e.getMessage());
				e.printStackTrace();
			}
		} else {
			System.out.println("주문이 취소되었습니다.");
		}
	}
	
	// 사용자 포인트 갱신
	private void updateUserPoint(int userPoint, int totalAmount) {
		int newPoint = userPoint - totalAmount;
		if (sSaladinMain != null) {
			sSaladinMain.setUserPoint(newPoint);
			System.out.println("잔여 포인트: " + newPoint + "원");
		} else {
			System.out.println("잔여 포인트: " + (userPoint - totalAmount) + "원");
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

			// 유효성 검사: 수량은 1 이상이어야 함
			if (newQuantity <= 0) {
				System.out.println("수량은 1 이상이어야 합니다.");
				return;
			}

			// 재고 확인
			int availableStock = getBookStock(bookCode);
			if (availableStock < newQuantity) {
				System.out.println("요청하신 수량이 재고보다 많습니다.");
				System.out.println("현재 재고: " + availableStock + "개");
				System.out.println("요청 수량: " + newQuantity + "개");
				return;
			}

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

	// 도서 코드로 재고 확인하는 메서드
	private int getBookStock(int bookCode) throws SQLException, ClassNotFoundException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			String sql = "SELECT book_stock FROM books WHERE book_code = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, bookCode);
			rs = pstmt.executeQuery();
			return rs.next() ? rs.getInt("book_stock") : 0;
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	// 장바구니 상품 삭제
	public void deleteCartItem(String userId) throws IOException {
		showCartItems(userId);
		System.out.println("\n―――――――――――――――――――――――――――――――― 상품 삭제 ――――――――――――――――――――――――――――――――");
		
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
			System.out.println("―――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――");
		} catch (NumberFormatException e) {
			System.out.println("올바른 숫자를 입력해주세요.");
		} catch (Exception e) {
			System.out.println("상품 삭제 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	// 사용자 장바구니 비우기
	private boolean clearUserCart(String userId) throws ClassNotFoundException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = DBUtil.getConnection();
			String sql = "DELETE FROM cart WHERE user_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("장바구니 비우기 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}
}