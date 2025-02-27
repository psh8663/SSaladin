package kr.ssaladin.model;

import java.sql.Date;
import java.util.List;

public class OrderInfo {
    private int orderNum;
    private String userId;
    private int orderTotal;
    private int orderStatus;
    private Date orderDate;
    private List<OrderDetailInfo> orderDetails;

    // 기본 생성자
    public OrderInfo() {
    }
    
    // 매개변수가 있는 생성자
    public OrderInfo(int orderNum, String userId, int orderTotal, int orderStatus, Date orderDate) {
        this.orderNum = orderNum;
        this.userId = userId;
        this.orderTotal = orderTotal;
        this.orderStatus = orderStatus;
        this.orderDate = orderDate;
    }

    // Getter, Setter 메서드
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

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public List<OrderDetailInfo> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetailInfo> orderDetails) {
        this.orderDetails = orderDetails;
    }
    
    @Override
    public String toString() {
        return "OrderInfo{" +
                "orderNum=" + orderNum +
                ", userId='" + userId + '\'' +
                ", orderTotal=" + orderTotal +
                ", orderStatus=" + orderStatus +
                ", orderDate=" + orderDate +
                '}';
    }
}