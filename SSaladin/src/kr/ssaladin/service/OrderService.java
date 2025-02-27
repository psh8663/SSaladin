package kr.ssaladin.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.ssaladin.dao.OrdersDAO;
import kr.ssaladin.dao.AdminBookDAO;
import kr.ssaladin.dao.Order_detailsDAO;
import kr.ssaladin.dao.UserDAO;
import kr.ssaladin.service.CartService.CartItem;
import kr.util.DBUtil;

public class OrderService {
	private Connection conn;
	private OrdersDAO ordersDAO;
	private Order_detailsDAO orderDetailsDAO;
	private AdminBookDAO adminBookDAO;
	private UserDAO userDAO;
	private BufferedReader br;

	public OrderService() throws Exception {
		try {
			conn = DBUtil.getConnection();
			ordersDAO = new OrdersDAO(conn);
			orderDetailsDAO = new Order_detailsDAO(conn);
			adminBookDAO = new AdminBookDAO();
			userDAO = new UserDAO();
			br = new BufferedReader(new InputStreamReader(System.in));
		} catch (SQLException e) {
			System.out.println("OrderService 초기화 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}


	// 장바구니 상품 구매. 주문 생성 (CartService에서 이동)
	// OrderService 클래스의 메서드
	public int createOrderFromCart(String userId, List<CartItem> items, int totalAmount) throws ClassNotFoundException {
	    try {
	        conn.setAutoCommit(false); // 트랜잭션 시작

	        // 재고확인
	        for (CartItem item : items) {
	            boolean stockAvailable = adminBookDAO.checkStock(item.getBookCode(), item.getCartQuantity());
	            if (!stockAvailable) {
	                System.out.println("도서코드 " + item.getBookCode() + "의 재고가 부족합니다.");
	                conn.rollback();
	                return -1; // 오류 코드
	            }
	        }

	        // 주문 기본 정보 등록
	        boolean orderInserted = ordersDAO.insertOrder(userId, totalAmount, OrderStatus.PROCESSING);
	        if (!orderInserted) {
	            System.out.println("주문 정보 등록에 실패했습니다.");
	            conn.rollback();
	            return -1;
	        }

	        // 방금 생성한 주문번호를 조회
	        int orderNum = ordersDAO.getLastOrderNumByUser(userId);
	        if (orderNum == -1) {
	            System.out.println("주문번호 조회에 실패했습니다.");
	            conn.rollback();
	            return -1;
	        }

	        // 주문 상세 정보 등록
	        for (CartItem item : items) {
	            boolean detailInserted = orderDetailsDAO.insertOrderDetail(orderNum, item.getBookCode(),
	                    item.getCartQuantity(), item.getBookPrice());
	            if (!detailInserted) {
	                System.out.println("주문 상세 정보 등록에 실패했습니다.");
	                conn.rollback();
	                return -1;
	            }

	            // 재고 차감
	            boolean stockUpdated = adminBookDAO.updateOrderStock(item.getBookCode(), item.getCartQuantity());
	            if (!stockUpdated) {
	                System.out.println("재고 차감에 실패했습니다.");
	                conn.rollback();
	                return -1;
	            }

	            // 재고가 0일 경우 품절 상태로 변경
	            boolean statusUpdated = adminBookDAO.updateBookStatus(item.getBookCode());
	            if (!statusUpdated) {
	                System.out.println("도서 상태 업데이트에 실패했습니다.");
	                conn.rollback();
	                return -1;
	            }
	        }

	        // 사용자 포인트 차감
	        boolean pointUpdated = updateUserPoint(userId, -totalAmount);
	        if (!pointUpdated) {
	            System.out.println("포인트 차감에 실패했습니다.");
	            conn.rollback();
	            return -1;
	        }

	        conn.commit();
	        System.out.println("주문이 성공적으로 완료되었습니다. 주문번호: " + orderNum);
	        return orderNum; // 성공 시 주문번호 반환

	    } catch (SQLException e) {
	        try {
	            conn.rollback();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	        System.out.println("주문 처리 중 오류 발생: " + e.getMessage());
	        e.printStackTrace();
	        return -1;
	    } finally {
	        try {
	            conn.setAutoCommit(true);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}

	// 주문 생성 (트랜잭션 처리)
	public boolean createOrder(String userId, int orderTotal, int orderStatus, List<OrderItem> orderItems) throws ClassNotFoundException {
		try {
			conn.setAutoCommit(false); // 트랜잭션 시작

			// 재고확인
			for (OrderItem item : orderItems) {
				boolean stockAvailable = adminBookDAO.checkStock(item.getBookCode(), item.getQuantity());
				if (!stockAvailable) {
					System.out.println("도서코드 " + item.getBookCode() + "의 재고가 부족합니다.");
					conn.rollback();
					return false;
				}
			}

			// 주문 기본 정보 등록
			boolean orderInserted = ordersDAO.insertOrder(userId, orderTotal, orderStatus);
			if (!orderInserted) {
				System.out.println("주문 정보 등록에 실패했습니다.");
				conn.rollback();
				return false;
			}

			// 방금 생성한 주문번호를 조회
			int orderNum = ordersDAO.getLastOrderNumByUser(userId);
			if (orderNum == -1) {
				System.out.println("주문번호 조회에 실패했습니다.");
				conn.rollback();
				return false;
			}

			// 주문 상세 정보 등록
			for (OrderItem item : orderItems) {
				boolean detailInserted = orderDetailsDAO.insertOrderDetail(orderNum, item.getBookCode(),
						item.getQuantity(), item.getPrice());
				if (!detailInserted) {
					System.out.println("주문 상세 정보 등록에 실패했습니다.");
					conn.rollback();
					return false;
				}

				// 재고 차감
				boolean stockUpdated = adminBookDAO.updateOrderStock(item.getBookCode(), item.getQuantity());
				if (!stockUpdated) {
					System.out.println("재고 차감에 실패했습니다.");
					conn.rollback();
					return false;
				}

				// 재고가 0일 경우 품절 상태로 변경
				boolean statusUpdated = adminBookDAO.updateBookStatus(item.getBookCode());
				if (!statusUpdated) {
					System.out.println("도서 상태 업데이트에 실패했습니다.");
					conn.rollback();
					return false;
				}
			}

			// 사용자 포인트 차감
			boolean pointUpdated = updateUserPoint(userId, -orderTotal);
			if (!pointUpdated) {
				System.out.println("포인트 차감에 실패했습니다.");
				conn.rollback();
				return false;
			}

			conn.commit();
			System.out.println("주문이 성공적으로 완료되었습니다. 주문번호: " + orderNum);
			return true;

		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			System.out.println("주문 처리 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// 사용자 포인트 업데이트 (차감 또는 증가)
	private boolean updateUserPoint(String userId, int pointChange) throws SQLException {
		String sql = "UPDATE users SET user_point = user_point + ? WHERE user_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, pointChange);
			pstmt.setString(2, userId);
			return pstmt.executeUpdate() > 0;
		}
	}

	// 주문 상태 변경
	public boolean updateOrderStatus(int orderNum, int newStatus) {
		try {
			return ordersDAO.updateOrderStatus(orderNum, newStatus);
		} catch (SQLException e) {
			System.out.println("주문 상태 변경 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			return false;
			
		}
	}

	// 주문 정보 조회
	public OrderInfo getOrderInfo(int orderNum) {
		OrderInfo orderInfo = new OrderInfo();
		try {
			// 주문 기본 정보 조회
			try (ResultSet orderRs = ordersDAO.getOrder(orderNum)) {
				if (orderRs.next()) {
					orderInfo.setOrderNum(orderRs.getInt("order_num"));
					orderInfo.setUserId(orderRs.getString("user_id"));
					orderInfo.setOrderTotal(orderRs.getInt("order_total"));
					orderInfo.setOrderStatus(orderRs.getInt("order_status"));
					orderInfo.setOrderDate(orderRs.getDate("order_date"));
				} else {
					return null; // 주문 정보가 없음
				}
			}

			// 주문 상세 정보 조회
			List<OrderDetailInfo> details = new ArrayList<>();
			try (ResultSet detailRs = orderDetailsDAO.getOrderDetails(orderNum)) {
				while (detailRs.next()) {
					OrderDetailInfo detail = new OrderDetailInfo();
					detail.setDetailNum(detailRs.getInt("detail_num"));
					detail.setBookCode(detailRs.getInt("book_code"));
					detail.setBookName(detailRs.getString("book_title"));
					detail.setOrderQuantity(detailRs.getInt("order_quantity"));
					detail.setOrderPrice(detailRs.getInt("order_price"));
					details.add(detail);
				}
			}
			orderInfo.setOrderDetails(details);

		} catch (SQLException e) {
			System.out.println("주문 정보 조회 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		return orderInfo;
	}

	// 사용자의 주문 목록 조회
	public List<OrderInfo> getUserOrders(String userId) {
		List<OrderInfo> orderList = new ArrayList<>();
		try {
			try (ResultSet rs = ordersDAO.getUserOrders(userId)) {
				while (rs.next()) {
					OrderInfo order = new OrderInfo();
					order.setOrderNum(rs.getInt("order_num"));
					order.setUserId(rs.getString("user_id"));
					order.setOrderTotal(rs.getInt("order_total"));
					order.setOrderStatus(rs.getInt("order_status"));
					order.setOrderDate(rs.getDate("order_date"));

					// 주문 상세 정보도 함께 조회
					order.setOrderDetails(getOrderDetailsByOrderNum(order.getOrderNum()));

					orderList.add(order);
				}
			}
		} catch (SQLException e) {
			System.out.println("사용자 주문 목록 조회 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		return orderList;
	}

	// 주문번호로 상세 정보 조회
	private List<OrderDetailInfo> getOrderDetailsByOrderNum(int orderNum) {
		List<OrderDetailInfo> details = new ArrayList<>();
		try {
			try (ResultSet detailRs = orderDetailsDAO.getOrderDetails(orderNum)) {
				while (detailRs.next()) {
					OrderDetailInfo detail = new OrderDetailInfo();
					detail.setDetailNum(detailRs.getInt("detail_num"));
					detail.setBookCode(detailRs.getInt("book_code"));
					detail.setBookName(detailRs.getString("book_title"));
					detail.setOrderQuantity(detailRs.getInt("order_quantity"));
					detail.setOrderPrice(detailRs.getInt("order_price"));
					details.add(detail);
				}
			}
		} catch (SQLException e) {
			System.out.println("주문 상세 정보 조회 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
		return details;
	}

	// 주문 취소 기능 (트랜잭션 처리)
	public boolean cancelOrder(int orderNum) {
		try {
			conn.setAutoCommit(false); // 트랜잭션 시작

			// 주문 정보 확인
			OrderInfo orderInfo = getOrderInfo(orderNum);
			if (orderInfo == null) {
				System.out.println("해당 주문 정보가 없습니다.");
				return false;
			}

			// 이미 취소된 주문인지 확인
			if (orderInfo.getOrderStatus() == OrderStatus.CANCELLED) {
				System.out.println("이미 취소된 주문입니다.");
				return false;
			}

			// 배송 완료된 주문은 취소 불가
			if (orderInfo.getOrderStatus() == OrderStatus.DELIVERY_COM) {
				System.out.println("배송 완료된 주문은 취소할 수 없습니다.");
				return false;
			}

			// 주문 상태를 취소로 변경
			boolean statusUpdated = ordersDAO.updateOrderStatus(orderNum, OrderStatus.CANCELLED);
			if (!statusUpdated) {
				System.out.println("주문 상태 변경에 실패했습니다.");
				conn.rollback();
				return false;
			}

			// 주문에 포함된 도서 재고 복구
			for (OrderDetailInfo detail : orderInfo.getOrderDetails()) {
				// 재고 증가 - AdminBookDAO 메서드 추가 필요
				boolean stockUpdated = restoreStock(detail.getBookCode(), detail.getOrderQuantity());
				if (!stockUpdated) {
					System.out.println("재고 복구에 실패했습니다.");
					conn.rollback();
					return false;
				}

				// 도서 상태 업데이트 (품절 -> 판매중) - AdminBookDAO 메서드 추가 필요
				boolean statusUpdated2 = updateBookStatusAfterCancel(detail.getBookCode());
				if (!statusUpdated2) {
					System.out.println("도서 상태 업데이트에 실패했습니다.");
					conn.rollback();
					return false;
				}
			}

			// 사용자 포인트 환불
			boolean pointUpdated = updateUserPoint(orderInfo.getUserId(), orderInfo.getOrderTotal());
			if (!pointUpdated) {
				System.out.println("포인트 환불에 실패했습니다.");
				conn.rollback();
				return false;
			}

			conn.commit();
			System.out.println("주문이 성공적으로 취소되었습니다.");
			return true;

		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			System.out.println("주문 취소 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// 재고 복구 메서드
	private boolean restoreStock(int bookCode, int quantity) throws SQLException {
		String sql = "UPDATE books SET book_stock = book_stock + ? WHERE book_code = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, quantity);
			pstmt.setInt(2, bookCode);
			return pstmt.executeUpdate() > 0;
		}
	}

	// 도서 상태 업데이트 (품절 -> 판매중)
	private boolean updateBookStatusAfterCancel(int bookCode) throws SQLException {
		String sql = "UPDATE books SET book_status = 1 WHERE book_code = ? AND book_stock > 0";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, bookCode);
			return pstmt.executeUpdate() > 0;
		}
	}

	// 관리자용: 모든 주문 목록 조회
	public List<OrderInfo> getAllOrders() {
		List<OrderInfo> orderList = new ArrayList<>();
		try {
			String sql = "SELECT * FROM orders ORDER BY order_date DESC";
			try (PreparedStatement pstmt = conn.prepareStatement(sql);
					ResultSet rs = pstmt.executeQuery()) {

				while (rs.next()) {
					OrderInfo order = new OrderInfo();
					order.setOrderNum(rs.getInt("order_num"));
					order.setUserId(rs.getString("user_id"));
					order.setOrderTotal(rs.getInt("order_total"));
					order.setOrderStatus(rs.getInt("order_status"));
					order.setOrderDate(rs.getDate("order_date"));
					orderList.add(order);
				}
			}
		} catch (SQLException e) {
			System.out.println("전체 주문 목록 조회 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		return orderList;
	}

	// 주문 처리 단계 변경 (배송 중, 배송 완료 등)
	public boolean processOrder(int orderNum, int newStatus) {
		try {
			OrderInfo orderInfo = getOrderInfo(orderNum);
			if (orderInfo == null) {
				System.out.println("해당 주문 정보가 없습니다.");
				return false;
			}

			// 이미 취소된 주문은 상태 변경 불가
			if (orderInfo.getOrderStatus() == OrderStatus.CANCELLED) {
				System.out.println("취소된 주문의 상태는 변경할 수 없습니다.");
				return false;
			}

			return ordersDAO.updateOrderStatus(orderNum, newStatus);
		} catch (SQLException e) {
			System.out.println("주문 처리 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	// 주문 내역 출력
	public void displayUserOrderHistory(String userId) {
		try {
			List<OrderInfo> orders = getUserOrders(userId);

			if (orders == null || orders.isEmpty()) {
				System.out.println("주문 내역이 없습니다.");
				return;
			}

			System.out.println("\n================= 주문 내역 =================");
			for (OrderInfo order : orders) {
				System.out.println("주문번호: " + order.getOrderNum());
				System.out.println("주문일시: " + order.getOrderDate());
				System.out.println("주문상태: " + getOrderStatusString(order.getOrderStatus()));
				System.out.println("주문금액: " + order.getOrderTotal() + "원");
				System.out.println("\n주문 상세:");

				List<OrderDetailInfo> details = order.getOrderDetails();
				if (details != null && !details.isEmpty()) {
					for (OrderDetailInfo detail : details) {
						System.out.println("\t- " + detail.getBookName() + 
								" (수량: " + detail.getOrderQuantity() + 
								", 가격: " + detail.getOrderPrice() + "원)");
					}
				} else {
					System.out.println("\t상세 정보가 없습니다.");
				}
				System.out.println("-".repeat(45));
			}
			System.out.println("=".repeat(45));
		} catch (Exception e) {
			System.out.println("주문 내역 조회 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// 관리자용: 모든 주문 내역 출력
	public void displayAllOrders() {
		try {
			List<OrderInfo> orders = getAllOrders();

			if (orders == null || orders.isEmpty()) {
				System.out.println("주문 내역이 없습니다.");
				return;
			}

			System.out.println("\n================= 전체 주문 내역 =================");
			for (OrderInfo order : orders) {
				System.out.println("주문번호: " + order.getOrderNum());
				System.out.println("주문자ID: " + order.getUserId());
				System.out.println("주문일시: " + order.getOrderDate());
				System.out.println("주문상태: " + getOrderStatusString(order.getOrderStatus()));
				System.out.println("주문금액: " + order.getOrderTotal() + "원");
				System.out.println("-".repeat(50));
			}
			System.out.println("=".repeat(50));
		} catch (Exception e) {
			System.out.println("전체 주문 내역 조회 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// 사용자 주문 내역 관리 메뉴
	public void manageUserOrders(String userId) throws IOException {
		boolean running = true;
		while (running) {
			System.out.println("\n====== 주문 내역 관리 ======");
			System.out.println("1. 주문 내역 조회");
			System.out.println("2. 주문 상세 정보 조회");
			System.out.println("3. 주문 취소");
			System.out.println("4. 뒤로 가기");
			System.out.print("메뉴 선택: ");

			try {
				int choice = Integer.parseInt(br.readLine());

				switch (choice) {
				case 1:
					displayUserOrderHistory(userId);
					break;
				case 2:
					System.out.print("조회할 주문번호 입력: ");
					int orderNum = Integer.parseInt(br.readLine());
					OrderInfo orderInfo = getOrderInfo(orderNum);

					if (orderInfo != null && orderInfo.getUserId().equals(userId)) {
						displayOrderDetails(orderInfo);
					} else {
						System.out.println("해당 주문 정보가 없거나 접근 권한이 없습니다.");
					}
					break;
				case 3:
					System.out.print("취소할 주문번호 입력: ");
					int cancelOrderNum = Integer.parseInt(br.readLine());
					OrderInfo cancelOrderInfo = getOrderInfo(cancelOrderNum);

					if (cancelOrderInfo != null && cancelOrderInfo.getUserId().equals(userId)) {
						System.out.print("정말 취소하시겠습니까? (Y/N): ");
						String confirm = br.readLine().trim().toUpperCase();

						if (confirm.equals("Y")) {
							if (cancelOrder(cancelOrderNum)) {
								System.out.println("주문이 취소되었습니다.");
							}
						}
					} else {
						System.out.println("해당 주문 정보가 없거나 접근 권한이 없습니다.");
					}
					break;
				case 4:
					running = false;
					break;
				default:
					System.out.println("잘못된 메뉴 선택입니다.");
				}
			} catch (NumberFormatException e) {
				System.out.println("숫자만 입력해주세요.");
			}
		}
	}

	// 관리자용: 주문 관리 메뉴
	public void manageAllOrders() throws IOException {
		boolean running = true;
		while (running) {
			System.out.println("\n====== 관리자 주문 관리 ======");
			System.out.println("1. 전체 주문 목록");
			System.out.println("2. 주문 상세 정보 조회");
			System.out.println("3. 주문 상태 변경");
			System.out.println("4. 주문 취소");
			System.out.println("5. 뒤로 가기");
			System.out.print("메뉴 선택: ");

			try {
				int choice = Integer.parseInt(br.readLine());

				switch (choice) {
				case 1:
					displayAllOrders();
					break;
				case 2:
					System.out.print("조회할 주문번호 입력: ");
					int orderNum = Integer.parseInt(br.readLine());
					OrderInfo orderInfo = getOrderInfo(orderNum);

					if (orderInfo != null) {
						displayOrderDetails(orderInfo);
					} else {
						System.out.println("해당 주문 정보가 없습니다.");
					}
					break;
				case 3:
					System.out.print("상태 변경할 주문번호 입력: ");
					int updateOrderNum = Integer.parseInt(br.readLine());
					OrderInfo updateOrderInfo = getOrderInfo(updateOrderNum);

					if (updateOrderInfo != null) {
						System.out.println("현재 상태: " + getOrderStatusString(updateOrderInfo.getOrderStatus()));
						System.out.println("1. 결제대기, 2. 결제완료, 3. 배송중, 4. 배송완료, 5. 주문취소");
						System.out.print("변경할 상태 선택: ");
						int newStatus = Integer.parseInt(br.readLine());

						if (newStatus >= 1 && newStatus <= 5) {
							if (processOrder(updateOrderNum, newStatus)) {
								System.out.println("주문 상태가 변경되었습니다.");
							}
						} else {
							System.out.println("잘못된 상태 값입니다.");
						}
					} else {
						System.out.println("해당 주문 정보가 없습니다.");
					}
					break;
				case 4:
					System.out.print("취소할 주문번호 입력: ");
					int cancelOrderNum = Integer.parseInt(br.readLine());
					OrderInfo cancelOrderInfo = getOrderInfo(cancelOrderNum);

					if (cancelOrderInfo != null) {
						System.out.print("정말 취소하시겠습니까? (Y/N): ");
						String confirm = br.readLine().trim().toUpperCase();

						if (confirm.equals("Y")) {
							if (cancelOrder(cancelOrderNum)) {
								System.out.println("주문이 취소되었습니다.");
							}
						}
					} else {
						System.out.println("해당 주문 정보가 없습니다.");
					}
					break;
				case 5:
					running = false;
					break;
				default:
					System.out.println("잘못된 메뉴 선택입니다.");
				}
			} catch (NumberFormatException e) {
				System.out.println("숫자만 입력해주세요.");
			}
		}
	}

	// 주문 상세 정보 화면 출력
	private void displayOrderDetails(OrderInfo orderInfo) {
		System.out.println("\n================= 주문 상세 정보 =================");
		System.out.println("주문번호: " + orderInfo.getOrderNum());
		System.out.println("주문자ID: " + orderInfo.getUserId());
		System.out.println("주문일시: " + orderInfo.getOrderDate());
		System.out.println("주문상태: " + getOrderStatusString(orderInfo.getOrderStatus()));
		System.out.println("주문금액: " + orderInfo.getOrderTotal() + "원");

		System.out.println("\n주문 상품 목록:");
		List<OrderDetailInfo> details = orderInfo.getOrderDetails();
		if (details != null && !details.isEmpty()) {
			for (OrderDetailInfo detail : details) {
				System.out.println("----------------------------------------");
				System.out.println("상품번호: " + detail.getBookCode());
				System.out.println("상품명: " + detail.getBookName());
				System.out.println("수량: " + detail.getOrderQuantity());
				System.out.println("가격: " + detail.getOrderPrice() + "원");
				System.out.println("총액: " + (detail.getOrderPrice() * detail.getOrderQuantity()) + "원");
			}
		} else {
			System.out.println("상세 정보가 없습니다.");
		}
		System.out.println("=".repeat(50));
	}
	

	public void checkOrderStatus() throws IOException {
	    System.out.println("\n=== 주문 상태 조회 ===");
	    System.out.print("조회할 주문 번호를 입력하세요: ");
	    try {
	        int orderNum = Integer.parseInt(br.readLine().trim());
	        
	        // 주문 정보 조회
	        OrderInfo orderInfo = getOrderInfo(orderNum);
	        
	        if (orderInfo != null) {
	            displayOrderDetails(orderInfo);
	            // 주문 상태만 강조해서 별도로 표시
	            System.out.println("\n현재 주문 상태: " + getOrderStatusString(orderInfo.getOrderStatus()));
	        } else {
	            System.out.println("해당 주문 정보를 찾을 수 없습니다.");
	        }
	    } catch (NumberFormatException e) {
	        System.out.println("올바른 주문 번호를 입력해주세요.");
	    } catch (Exception e) {
	        System.out.println("주문 조회 중 오류가 발생했습니다: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	// 사용자 ID를 파라미터로 받아옴
	public void checkOrderStatus(String userId, int userAuth) throws IOException {
	    System.out.println("\n=== 나의 주문 목록 ===");
	    
	    // 사용자의 주문 목록 조회
	    List<OrderInfo> userOrders = getUserOrders(userId);
	    
	    if (userOrders == null || userOrders.isEmpty()) {
	        System.out.println("주문 내역이 없습니다.");
	        return;
	    }
	    
	    // 주문 목록 출력
	    System.out.println("주문번호\t주문일자\t\t주문상태\t주문금액");
	    System.out.println("-".repeat(60));
	    
	    for (OrderInfo order : userOrders) {
	        System.out.printf("%-8d %-12s %-10s %,d원\n", 
	                order.getOrderNum(), 
	                order.getOrderDate(), 
	                getOrderStatusString(order.getOrderStatus()), 
	                order.getOrderTotal());
	    }
	    System.out.println("-".repeat(60));
	    

	    System.out.print("\n상세 조회할 주문 번호를 입력하세요 (돌아가려면 0 입력): ");
	    try {
	        int orderNum = Integer.parseInt(br.readLine().trim());
	        
	        if (orderNum == 0) {
	            return; // 메인 메뉴로 돌아가기
	        }
	        
	        // 주문 정보 조회
	        OrderInfo orderInfo = getOrderInfo(orderNum);
	        
	        if (orderInfo != null) {
	            // 일반 사용자는 자신의 주문만 볼 수 있도록 제한
	            if (userAuth != 2 && !orderInfo.getUserId().equals(userId)) {
	                System.out.println("해당 주문에 대한 접근 권한이 없습니다.");
	                return;
	            }
	            
	            displayOrderDetails(orderInfo);
	            
	            // 주문 상태 변경 기능 추가 (결제 완료 상태에서만 취소 가능)
	            if (orderInfo.getOrderStatus() == OrderStatus.PROCESSING) {
	                System.out.print("\n이 주문을 취소하시겠습니까? (Y/N): ");
	                String choice = br.readLine().trim().toUpperCase();
	                
	                if (choice.equals("Y")) {
	                    if (cancelOrder(orderNum)) {
	                        System.out.println("주문이 성공적으로 취소되었습니다.");
	                    }
	                }
	            }
	        } else {
	            System.out.println("해당 주문 정보를 찾을 수 없습니다.");
	        }
	    } catch (NumberFormatException e) {
	        System.out.println("올바른 주문 번호를 입력해주세요.");
	    } catch (Exception e) {
	        System.out.println("주문 조회 중 오류가 발생했습니다: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	// 주문 상태 문자열 반환
	private String getOrderStatusString(int status) {
		switch (status) {
		case OrderStatus.PENDING: return "결제 대기";
		case OrderStatus.PROCESSING: return "결제 완료";
		case OrderStatus.IN_DELIVERY: return "배송 중";
		case OrderStatus.DELIVERY_COM: return "배송 완료";
		case OrderStatus.CANCELLED: return "주문 취소";
		default: return "알 수 없음";
		}
	}

	// 주문 상태 관리 관련 메서드 (상수 정의)
	public static class OrderStatus {
		public static final int PENDING = 1; // 결제 대기
		public static final int PROCESSING = 2; // 결제 완료
		public static final int IN_DELIVERY = 3; // 배송 중
		public static final int DELIVERY_COM = 4; // 배송 완료
		public static final int CANCELLED = 5; // 주문 취소
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

// 주문 아이템 클래스 (주문 생성 시 사용)
class OrderItem {
	private int bookCode;
	private int quantity;
	private int price;

	public OrderItem(int bookCode, int quantity, int price) {
		this.bookCode = bookCode;
		this.quantity = quantity;
		this.price = price;
	}

	public int getBookCode() {
		return bookCode;
	}

	public int getQuantity() {
		return quantity;
	}

	public int getPrice() {
		return price;
	}
}

// 주문 정보 클래스
class OrderInfo {
	private int orderNum;
	private String userId;
	private int orderTotal;
	private int orderStatus;
	private java.sql.Date orderDate;
	private List<OrderDetailInfo> orderDetails;

	// Getter, Setter
	public int getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(int orderNum) {
		this.orderNum = orderNum;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getOrderTotal() {
		return orderTotal;
	}

	public void setOrderTotal(int orderTotal) {
		this.orderTotal = orderTotal;
	}

	public int getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(int orderStatus) {
		this.orderStatus = orderStatus;
	}

	public java.sql.Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(java.sql.Date orderDate) {
		this.orderDate = orderDate;
	}

	public List<OrderDetailInfo> getOrderDetails() {
		return orderDetails;
	}

	public void setOrderDetails(List<OrderDetailInfo> orderDetails) {
		this.orderDetails = orderDetails;
	}
}

// 주문 상세 정보 클래스
class OrderDetailInfo {
	private int detailNum;
	private int bookCode;
	private String bookName;
	private int orderQuantity;
	private int orderPrice;

	// Getter, Setter
	public int getDetailNum() {
		return detailNum;
	}

	public void setDetailNum(int detailNum) {
		this.detailNum = detailNum;
	}

	public int getBookCode() {
		return bookCode;
	}

	public void setBookCode(int bookCode) {
		this.bookCode = bookCode;
	}

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public int getOrderQuantity() {
		return orderQuantity;
	}

	public void setOrderQuantity(int orderQuantity) {
		this.orderQuantity = orderQuantity;
	}

	public int getOrderPrice() {
		return orderPrice;
	}

	public void setOrderPrice(int orderPrice) {
		this.orderPrice = orderPrice;
	}
}