package kr.ssaladin.model;

public class OrderItem {
    private int bookCode;
    private int quantity;
    private int price;

    // 기본 생성자
    public OrderItem() {
    }

    // 매개변수가 있는 생성자
    public OrderItem(int bookCode, int quantity, int price) {
        this.bookCode = bookCode;
        this.quantity = quantity;
        this.price = price;
    }

    // Getter 메서드
    public int getBookCode() {
        return bookCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPrice() {
        return price;
    }
    
    // Setter 메서드
    public void setBookCode(int bookCode) {
        this.bookCode = bookCode;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public void setPrice(int price) {
        this.price = price;
    }
    
    // 항목의 소계 계산
    public int getSubtotal() {
        return price * quantity;
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "bookCode=" + bookCode +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}