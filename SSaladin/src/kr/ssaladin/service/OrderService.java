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

import kr.ssaladin.dao.OrdersDAO;
import kr.ssaladin.dao.AdminBookDAO;
import kr.ssaladin.dao.Order_detailsDAO;
import kr.ssaladin.dao.UserDAO;
import kr.ssaladin.model.OrderInfo;
import kr.ssaladin.model.OrderDetailInfo;
import kr.ssaladin.model.OrderItem;
import kr.ssaladin.service.CartService.CartItem;
import kr.util.DBUtil;

public class OrderService {
    // 상수 정의
    public static class OrderStatus {
        public static final int PENDING = 1;      // 결제 대기
        public static final int PROCESSING = 2;   // 결제 완료
        public static final int IN_DELIVERY = 3;  // 배송 중
        public static final int DELIVERY_COM = 4; // 배송 완료
        public static final int CANCELLED = 5;    // 주문 취소
    }
    
    // 멤버 변수
    private Connection conn;
    private OrdersDAO ordersDAO;
    private Order_detailsDAO orderDetailsDAO;
    private AdminBookDAO adminBookDAO;
    private UserDAO userDAO;
    private BufferedReader br;

    // 생성자
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

    // 장바구니에서 주문 생성
    public int createOrderFromCart(String userId, List<CartItem> items, int totalAmount) throws ClassNotFoundException {
        try {
            conn.setAutoCommit(false); // 트랜잭션 시작
            
            // 재고 확인
            if (!checkStock(items)) {
                conn.rollback();
                return -1;
            }
            
            // 주문 생성
            int orderNum = createOrderTransaction(userId, totalAmount, OrderStatus.PROCESSING);
            if (orderNum == -1) {
                conn.rollback();
                return -1;
            }
            
            // 주문 상세 정보 생성 및 재고 업데이트
            if (!createOrderDetails(orderNum, items)) {
                conn.rollback();
                return -1;
            }
            
            // 포인트 차감
            if (!updateUserPoint(userId, -totalAmount)) {
                conn.rollback();
                return -1;
            }
            
            conn.commit();
            System.out.println("주문이 성공적으로 완료되었습니다. 주문번호: " + orderNum);
            return orderNum;
            
        } catch (SQLException e) {
            handleTransactionError(e);
            return -1;
        } finally {
            resetAutoCommit();
        }
    }
    
    // 직접 주문 생성
    public boolean createOrder(String userId, int orderTotal, int orderStatus, List<OrderItem> orderItems) throws ClassNotFoundException {
        try {
            conn.setAutoCommit(false); // 트랜잭션 시작
            
            // 재고 확인
            if (!checkOrderItemsStock(orderItems)) {
                conn.rollback();
                return false;
            }
            
            // 주문 생성
            int orderNum = createOrderTransaction(userId, orderTotal, orderStatus);
            if (orderNum == -1) {
                conn.rollback();
                return false;
            }
            
            // 주문 상세 정보 생성 및 재고 업데이트
            if (!createOrderItemDetails(orderNum, orderItems)) {
                conn.rollback();
                return false;
            }
            
            // 포인트 차감
            if (!updateUserPoint(userId, -orderTotal)) {
                conn.rollback();
                return false;
            }
            
            conn.commit();
            System.out.println("주문이 성공적으로 완료되었습니다. 주문번호: " + orderNum);
            return true;
            
        } catch (SQLException e) {
            handleTransactionError(e);
            return false;
        } finally {
            resetAutoCommit();
        }
    }
    
    // 재고 확인 (장바구니 상품)
    private boolean checkStock(List<CartItem> items) throws SQLException, ClassNotFoundException {
        for (CartItem item : items) {
            if (!adminBookDAO.checkStock(item.getBookCode(), item.getCartQuantity())) {
                System.out.println("도서코드 " + item.getBookCode() + "의 재고가 부족합니다.");
                return false;
            }
        }
        return true;
    }
    
    // 재고 확인 (주문 상품)
    private boolean checkOrderItemsStock(List<OrderItem> items) throws SQLException, ClassNotFoundException {
        for (OrderItem item : items) {
            if (!adminBookDAO.checkStock(item.getBookCode(), item.getQuantity())) {
                System.out.println("도서코드 " + item.getBookCode() + "의 재고가 부족합니다.");
                return false;
            }
        }
        return true;
    }
    
    // 주문 생성 트랜잭션
    private int createOrderTransaction(String userId, int orderTotal, int orderStatus) throws SQLException {
        // 주문 기본 정보 등록
        boolean orderInserted = ordersDAO.insertOrder(userId, orderTotal, orderStatus);
        if (!orderInserted) {
            System.out.println("주문 정보 등록에 실패했습니다.");
            return -1;
        }
        
        // 방금 생성한 주문번호 조회
        int orderNum = ordersDAO.getLastOrderNumByUser(userId);
        if (orderNum == -1) {
            System.out.println("주문번호 조회에 실패했습니다.");
            return -1;
        }
        
        return orderNum;
    }
    
    // 주문 상세 정보 생성 (장바구니)
    private boolean createOrderDetails(int orderNum, List<CartItem> items) throws SQLException, ClassNotFoundException {
        for (CartItem item : items) {
            // 주문 상세 정보 등록
            boolean detailInserted = orderDetailsDAO.insertOrderDetail(
                    orderNum, item.getBookCode(), item.getCartQuantity(), item.getBookPrice());
            
            if (!detailInserted) {
                System.out.println("주문 상세 정보 등록에 실패했습니다.");
                return false;
            }
            
            // 재고 업데이트
            if (!updateBookStock(item.getBookCode(), item.getCartQuantity())) {
                return false;
            }
        }
        return true;
    }
    
    // 주문 상세 정보 생성 (직접 주문)
    private boolean createOrderItemDetails(int orderNum, List<OrderItem> items) throws SQLException, ClassNotFoundException {
        for (OrderItem item : items) {
            // 주문 상세 정보 등록
            boolean detailInserted = orderDetailsDAO.insertOrderDetail(
                    orderNum, item.getBookCode(), item.getQuantity(), item.getPrice());
            
            if (!detailInserted) {
                System.out.println("주문 상세 정보 등록에 실패했습니다.");
                return false;
            }
            
            // 재고 업데이트
            if (!updateBookStock(item.getBookCode(), item.getQuantity())) {
                return false;
            }
        }
        return true;
    }
    
    // 도서 재고 및 상태 업데이트
    private boolean updateBookStock(int bookCode, int quantity) throws SQLException, ClassNotFoundException {
        // 재고 차감
        boolean stockUpdated = adminBookDAO.updateOrderStock(bookCode, quantity);
        if (!stockUpdated) {
            System.out.println("재고 차감에 실패했습니다.");
            return false;
        }
        
        // 재고가 0일 경우 품절 상태로 변경
        boolean statusUpdated = adminBookDAO.updateBookStatus(bookCode);
        if (!statusUpdated) {
            System.out.println("도서 상태 업데이트에 실패했습니다.");
            return false;
        }
        
        return true;
    }
    
    // 주문 취소 기능
    public boolean cancelOrder(int orderNum) {
        try {
            conn.setAutoCommit(false); // 트랜잭션 시작
            
            // 주문 정보 확인
            OrderInfo orderInfo = getOrderInfo(orderNum);
            if (orderInfo == null) {
                System.out.println("해당 주문 정보가 없습니다.");
                return false;
            }
            
            // 취소 가능 상태 확인
            if (!isOrderCancellable(orderInfo)) {
                return false;
            }
            
            // 주문 상태를 '취소'로 변경
            if (!ordersDAO.updateOrderStatus(orderNum, OrderStatus.CANCELLED)) {
                System.out.println("주문 상태 변경에 실패했습니다.");
                conn.rollback();
                return false;
            }
            
            // 도서 재고 복구
            if (!restoreOrderStock(orderInfo.getOrderDetails())) {
                conn.rollback();
                return false;
            }
            
            // 환불 전 포인트 조회
            int beforeRefundPoint = getUserCurrentPoint(orderInfo.getUserId());
            
            // 사용자 포인트 환불
            int refund = orderInfo.getOrderTotal();
            if (!updateUserPoint(orderInfo.getUserId(), refund)) {
                System.out.println("포인트 환불에 실패했습니다.");
                conn.rollback();
                return false;
            }
            
            // 환불 후 포인트 조회
            int afterRefundPoint = getUserCurrentPoint(orderInfo.getUserId());
            
            conn.commit();
            
            // 환불 성공 메시지 출력
            System.out.println("주문이 성공적으로 취소되었습니다.");
            System.out.println("환불 된 포인트: " + refund + "원");
            System.out.println("현재 포인트: " + afterRefundPoint + "원");
            
            // UI 포인트 업데이트
            updateMainInstancePoint(orderInfo.getUserId(), afterRefundPoint);
            
            return true;
            
        } catch (SQLException e) {
            handleTransactionError(e);
            return false;
        } finally {
            resetAutoCommit();
        }
    }
    
    // 주문 취소 가능 여부 확인
    private boolean isOrderCancellable(OrderInfo orderInfo) {
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
        
        return true;
    }
    
    // 도서 재고 복구
    private boolean restoreOrderStock(List<OrderDetailInfo> details) throws SQLException {
        for (OrderDetailInfo detail : details) {
            boolean stockUpdated = restoreStock(detail.getBookCode(), detail.getOrderQuantity());
            if (!stockUpdated) {
                System.out.println("재고 복구에 실패했습니다.");
                return false;
            }
            
            // 도서 상태 업데이트 (품절 -> 판매중)
            updateBookStatusAfterCancel(detail.getBookCode());
        }
        return true;
    }
    
    // 메인 인스턴스 포인트 업데이트
    private void updateMainInstancePoint(String userId, int newPoint) {
        try {
            // SSaladinMain 클래스의 현재 인스턴스를 가져옴
            java.lang.reflect.Method getCurrentInstance = Class.forName("kr.ssaladin.SSaladinMain").getMethod("getCurrentInstance");
            Object mainInstance = getCurrentInstance.invoke(null);
            
            if (mainInstance != null) {
                // 현재 로그인한 사용자 ID 가져오기
                java.lang.reflect.Method getUserId = mainInstance.getClass().getMethod("getUserId");
                String currentUserId = (String) getUserId.invoke(mainInstance);
                
                // 현재 로그인한 사용자와 주문 취소한 사용자가 같은 경우에만 포인트 업데이트
                if (currentUserId != null && currentUserId.equals(userId)) {
                    java.lang.reflect.Method setUserPoint = mainInstance.getClass().getMethod("setUserPoint", int.class);
                    setUserPoint.invoke(mainInstance, newPoint);
                }
            }
        } catch (Exception e) {
            System.out.println("인스턴스에 변경된 포인트 값 전달 중 오류 발생: " + e.getMessage());
        }
    }
    
    // 사용자의 현재 포인트 조회
    private int getUserCurrentPoint(String userId) throws SQLException {
        String sql = "SELECT user_point FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt("user_point") : 0;
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
            orderInfo.setOrderDetails(getOrderDetailsByOrderNum(orderNum));
            
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
        displayOrderList(userOrders);
        
        // 상세 조회 및 취소 기능
        handleOrderActions(userId, userAuth);
    }
    
    // 주문 목록 표시
    private void displayOrderList(List<OrderInfo> orders) {
        System.out.println("주문번호\t주문일자\t\t주문상태\t주문금액");
        System.out.println("-".repeat(60));
        
        for (OrderInfo order : orders) {
            System.out.printf("%-8d %-12s %-10s %,d원\n", 
                    order.getOrderNum(), 
                    order.getOrderDate(), 
                    getOrderStatusString(order.getOrderStatus()), 
                    order.getOrderTotal());
        }
        System.out.println("-".repeat(60));
    }
    
    // 주문 상세 조회 및 취소 처리
    private void handleOrderActions(String userId, int userAuth) throws IOException {
        System.out.print("\n상세 조회할 주문 번호를 입력하세요 (돌아가려면 0 입력): ");
        try {
            int orderNum = Integer.parseInt(br.readLine().trim());
            
            if (orderNum == 0) {
                return; // 메인 메뉴로 돌아가기
            }
            
            // 주문 정보 조회
            OrderInfo orderInfo = getOrderInfo(orderNum);
            
            if (orderInfo != null) {
                // 접근 권한 확인
                if (userAuth != 2 && !orderInfo.getUserId().equals(userId)) {
                    System.out.println("해당 주문에 대한 접근 권한이 없습니다.");
                    return;
                }
                
                displayOrderDetails(orderInfo);
                
                // 결제 완료 상태인 경우 취소 옵션 제공
                if (orderInfo.getOrderStatus() == OrderStatus.PROCESSING) {
                    offerCancelOption(orderNum);
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
    
    // 주문 취소 옵션 제공
    private void offerCancelOption(int orderNum) throws IOException {
        System.out.print("\n이 주문을 취소하시겠습니까? (Y/N): ");
        String choice = br.readLine().trim().toUpperCase();
        
        if (choice.equals("Y")) {
            if (cancelOrder(orderNum)) {
                System.out.println("주문이 성공적으로 취소되었습니다.");
            }
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
                        promptAndShowOrderDetails(userId, false);
                        break;
                    case 3:
                        promptAndCancelOrder(userId);
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
    
    // 주문 상세 정보 조회 프롬프트
    private void promptAndShowOrderDetails(String userId, boolean isAdmin) throws IOException {
        System.out.print("조회할 주문번호 입력: ");
        try {
            int orderNum = Integer.parseInt(br.readLine());
            OrderInfo orderInfo = getOrderInfo(orderNum);
            
            if (orderInfo != null && (isAdmin || orderInfo.getUserId().equals(userId))) {
                displayOrderDetails(orderInfo);
            } else {
                System.out.println("해당 주문 정보가 없거나 접근 권한이 없습니다.");
            }
        } catch (NumberFormatException e) {
            System.out.println("유효한 주문번호를 입력해주세요.");
        }
    }
    
    // 주문 취소 프롬프트
    private void promptAndCancelOrder(String userId) throws IOException {
        System.out.print("취소할 주문번호 입력: ");
        try {
            int orderNum = Integer.parseInt(br.readLine());
            OrderInfo orderInfo = getOrderInfo(orderNum);
            
            if (orderInfo != null && orderInfo.getUserId().equals(userId)) {
                System.out.print("정말 취소하시겠습니까? (Y/N): ");
                String confirm = br.readLine().trim().toUpperCase();
                
                if (confirm.equals("Y")) {
                    if (cancelOrder(orderNum)) {
                        System.out.println("주문이 취소되었습니다.");
                    }
                }
            } else {
                System.out.println("해당 주문 정보가 없거나 접근 권한이 없습니다.");
            }
        } catch (NumberFormatException e) {
            System.out.println("유효한 주문번호를 입력해주세요.");
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
                        promptAndShowOrderDetails(null, true);
                        break;
                    case 3:
                        promptAndChangeOrderStatus();
                        break;
                    case 4:
                        promptAndCancelOrderAdmin();
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
    
    // 주문 상태 변경 프롬프트
    private void promptAndChangeOrderStatus() throws IOException {
        System.out.print("상태 변경할 주문번호 입력: ");
        try {
            int orderNum = Integer.parseInt(br.readLine());
            OrderInfo orderInfo = getOrderInfo(orderNum);
            
            if (orderInfo != null) {
                System.out.println("현재 상태: " + getOrderStatusString(orderInfo.getOrderStatus()));
                System.out.println("1. 결제대기, 2. 결제완료, 3. 배송중, 4. 배송완료, 5. 주문취소");
                System.out.print("변경할 상태 선택: ");
                int newStatus = Integer.parseInt(br.readLine());
                
                if (newStatus >= 1 && newStatus <= 5) {
                    if (processOrder(orderNum, newStatus)) {
                        System.out.println("주문 상태가 변경되었습니다.");
                    }
                } else {
                    System.out.println("잘못된 상태 값입니다.");
                }
            } else {
                System.out.println("해당 주문 정보가 없습니다.");
            }
        } catch (NumberFormatException e) {
            System.out.println("유효한 숫자를 입력해주세요.");
        }
    }
    
    // 관리자용 주문 취소 프롬프트
    private void promptAndCancelOrderAdmin() throws IOException {
        System.out.print("취소할 주문번호 입력: ");
        try {
            int orderNum = Integer.parseInt(br.readLine());
            OrderInfo orderInfo = getOrderInfo(orderNum);
            
            if (orderInfo != null) {
                System.out.print("정말 취소하시겠습니까? (Y/N): ");
                String confirm = br.readLine().trim().toUpperCase();
                
                if (confirm.equals("Y")) {
                    if (cancelOrder(orderNum)) {
                        System.out.println("주문이 취소되었습니다.");
                    }
                }
            } else {
                System.out.println("해당 주문 정보가 없습니다.");
            }
        } catch (NumberFormatException e) {
            System.out.println("유효한 주문번호를 입력해주세요.");
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
    
    // 트랜잭션 오류 처리
    private void handleTransactionError(SQLException e) {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        System.out.println("처리 중 오류 발생: " + e.getMessage());
        e.printStackTrace();
    }
    
    // 자동 커밋 상태 복구
    private void resetAutoCommit() {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
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