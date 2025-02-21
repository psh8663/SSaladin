package kr.ssaladin.model;

import java.sql.Timestamp;

public class PointRequest {
    private int requestNum;
    private String userId;
    private int pointAmount;
    private int requestStatus;
    private Timestamp requestDate;
    
    // 생성자
    public PointRequest() {}
    
    // getter와 setter 메소드
    public int getRequestNum() {
        return requestNum;
    }
    
    public void setRequestNum(int requestNum) {
        this.requestNum = requestNum;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public int getPointAmount() {
        return pointAmount;
    }
    
    public void setPointAmount(int pointAmount) {
        this.pointAmount = pointAmount;
    }
    
    public int getRequestStatus() {
        return requestStatus;
    }
    
    public void setRequestStatus(int requestStatus) {
        this.requestStatus = requestStatus;
    }
    
    public Timestamp getRequestDate() {
        return requestDate;
    }
    
    public void setRequestDate(Timestamp requestDate) {
        this.requestDate = requestDate;
    }
}