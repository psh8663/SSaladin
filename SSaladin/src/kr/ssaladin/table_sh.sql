-- orders 테이블 생성
CREATE TABLE orders (
  order_num NUMBER PRIMARY KEY,
  user_num NUMBER NOT NULL,
  order_total NUMBER DEFAULT 0,
  order_status NUMBER DEFAULT 1 NOT NULL,
  order_date DATE DEFAULT SYSDATE NOT NULL
);

-- order_details 테이블 생성
CREATE TABLE order_details (
  detail_num NUMBER PRIMARY KEY,
  order_num NUMBER NOT NULL,
  book_code NUMBER NOT NULL,
  order_quantity NUMBER DEFAULT 0,
  order_price NUMBER DEFAULT 0,
  CONSTRAINT fk_order_num FOREIGN KEY (order_num) REFERENCES orders(order_num)
);
    