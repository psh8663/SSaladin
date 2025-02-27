package kr.ssaladin.model;

public class OrderDetailInfo {
    private int detailNum;
    private int bookCode;
    private String bookName;
    private int orderQuantity;
    private int orderPrice;

    // 기본 생성자
    public OrderDetailInfo() {
    }

    // 매개변수가 있는 생성자
    public OrderDetailInfo(int detailNum, int bookCode, String bookName, int orderQuantity, int orderPrice) {
        this.detailNum = detailNum;
        this.bookCode = bookCode;
        this.bookName = bookName;
        this.orderQuantity = orderQuantity;
        this.orderPrice = orderPrice;
    }

    // Getter, Setter 메서드
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
    
    // 항목의 소계 계산
    public int getSubtotal() {
        return orderPrice * orderQuantity;
    }
    
    @Override
    public String toString() {
        return "OrderDetailInfo{" +
                "detailNum=" + detailNum +
                ", bookCode=" + bookCode +
                ", bookName='" + bookName + '\'' +
                ", orderQuantity=" + orderQuantity +
                ", orderPrice=" + orderPrice +
                '}';
    }
}