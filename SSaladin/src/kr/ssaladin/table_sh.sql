-- orders 테이블 생성
CREATE TABLE orders (
  order_num NUMBER PRIMARY KEY,
  user_id VARCHAR2(12) NOT NULL ,
  order_total NUMBER DEFAULT 0,
  order_status NUMBER DEFAULT 1 NOT NULL,
  order_date DATE DEFAULT SYSDATE NOT NULL,
  FOREIGN KEY (orders) REFERENCES users(user_id) on delete cascade
);

-- order_details 테이블 생성
CREATE TABLE order_details (
  detail_num NUMBER PRIMARY KEY,
  order_num NUMBER NOT NULL,
  book_code NUMBER NOT NULL,
  order_quantity NUMBER DEFAULT 0,
  order_price NUMBER DEFAULT 0,
  CONSTRAINT fk_order_num FOREIGN KEY (order_num) REFERENCES orders(order_num) on delete cascade
);
    

-- cart 테이블 생성
CREATE TABLE cart (
  cart_num NUMBER PRIMARY KEY,
  user_id NUMBER NOT NULL,
  book_code NUMBER NOT NULL,
  cart_quantity NUMBER DEFAULT 0 NOT NULL,
  CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) on delete cascade,
  CONSTRAINT fk_book_code FOREIGN KEY (book_code) REFERENCES books(book_code)
);

--cartnum 관련 시퀀스 생성

CREATE SEQUENCE cart_num_seq
START WITH 1
INCREMENT BY 1;

-- orders 관련 시퀀스 생성
CREATE SEQUENCE order_num_seq
START WITH 1
INCREMENT BY 1;

-- order_details 관련 시퀀스 생성
CREATE SEQUENCE detail_num_seq
START WITH 1
INCREMENT BY 1;