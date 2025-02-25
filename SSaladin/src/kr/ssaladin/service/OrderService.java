package kr.ssaladin.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import kr.ssaladin.dao.OrdersDAO;
import kr.ssaladin.dao.AdminBookDAO;
import kr.ssaladin.dao.Order_detailsDAO;
import kr.util.DBUtil;

public class OrderService {
	private Connection conn;
	private OrdersDAO ordersDAO;
	private Order_detailsDAO orderDetailsDAO;
	private AdminBookDAO adminBookDAO;

	public OrderService() throws Exception {
		try {
			conn = DBUtil.getConnection();
			ordersDAO = new OrdersDAO(conn);
			orderDetailsDAO = new Order_detailsDAO(conn);
			adminBookDAO = new AdminBookDAO();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 주문 생성 (트랜잭션 처리)
	public boolean createOrder(String userId, int orderTotal, int orderStatus, List<OrderItem> orderItems) throws ClassNotFoundException {
		try {
			conn.setAutoCommit(false); // 트랜잭션 시작

			//재고확인
			for (OrderItem item : orderItems) {
	            boolean stockAvailable = adminBookDAO.checkStock(item.getBookCode(), item.getQuantity());
	            if (!stockAvailable) {
	                conn.rollback();
	                return false;  // 재고 부족 시 주문 진행 불가
	            }
	        }
			
			// 주문 기본 정보 등록
			boolean orderInserted = ordersDAO.insertOrder(userId, orderTotal, orderStatus);
			if (!orderInserted) {
				conn.rollback();
				return false;
			}

			// 입력받고 생성 된 주문번호를 조회
			int orderNum = getLastOrderNum(userId);

			// 주문 상세 정보 등록
			for (OrderItem item : orderItems) {
				boolean detailInserted = orderDetailsDAO.insertOrderDetail(orderNum, item.getBookCode(),
						item.getQuantity(), item.getPrice());
				if (!detailInserted) {
					conn.rollback();
					return false;
				}
				// 재고 차감
				boolean stockUpdated = adminBookDAO.updateOrderStock(item.getBookCode(), item.getQuantity());
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

			conn.commit();
			return true;

		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
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

	// 주문 상태 변경
	public boolean updateOrderStatus(int orderNum, int newStatus) {
		try {
			return ordersDAO.updateOrderStatus(orderNum, newStatus);
		} catch (SQLException e) {
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
					order.setOrderTotal(rs.getInt("order_total"));
					order.setOrderStatus(rs.getInt("order_status"));
					order.setOrderDate(rs.getDate("order_date"));
					orderList.add(order);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return orderList;
	}

	// 가장 최근 주문 번호 조회 (내부 사용)
	private int getLastOrderNum(String userId) throws SQLException {
		try (ResultSet rs = ordersDAO.getUserOrders(userId)) {
			if (rs.next()) {
				return rs.getInt("order_num");
			}
			return -1;
		}
	}

	// 주문 상태 관리 관련 메서드 (상수 정의)
	public class OrderStatus {
		public static final int PENDING = 1; // 결제 대기
		public static final int PROCESSING = 2; // 결제 완료
		public static final int IN_DELIVERY = 3; // 배송 중
		public static final int DELIVERY_COM = 4; // 배송 완료
		public static final int CANCELLED = 5; // 주문 취소
	}

	// 주문 취소 기능
	public boolean cancelOrder(int orderNum) {
		try {
			return ordersDAO.updateOrderStatus(orderNum, OrderStatus.CANCELLED);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
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
	public void setOrderNum(int orderNum) {
		this.orderNum = orderNum;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setOrderTotal(int orderTotal) {
		this.orderTotal = orderTotal;
	}

	public void setOrderStatus(int orderStatus) {
		this.orderStatus = orderStatus;
	}

	public void setOrderDate(java.sql.Date orderDate) {
		this.orderDate = orderDate;
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
	public void setDetailNum(int detailNum) {
		this.detailNum = detailNum;
	}

	public void setBookCode(int bookCode) {
		this.bookCode = bookCode;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public void setOrderQuantity(int orderQuantity) {
		this.orderQuantity = orderQuantity;
	}

	public void setOrderPrice(int orderPrice) {
		this.orderPrice = orderPrice;
	}
}