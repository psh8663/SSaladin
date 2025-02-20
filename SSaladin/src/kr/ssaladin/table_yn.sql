CREATE TABLE categories (
    category_num NUMBER(2) PRIMARY KEY,
    category_name VARCHAR2(30) NOT NULL
);

CREATE SEQUENCE categories_seq 
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

--INSERT INTO categories (category_num, category_name) VALUES (categories_seq.NEXTVAL, '소설');


CREATE TABLE books (
    book_code NUMBER PRIMARY KEY,
    category_num NUMBER(2) NOT NULL, 
    book_title VARCHAR2(50) NOT NULL,
    book_author VARCHAR2(30) NOT NULL,
    book_price NUMBER(9) NOT NULL,
    book_publisher VARCHAR2(50) NOT NULL,
    book_description VARCHAR2(300) NOT NULL,
    book_stock NUMBER(9) NOT NULL,
    book_status NUMBER(1) NOT NULL,
    rating_avg FLOAT NOT NULL,
    book_reg_date DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT fk_books_category FOREIGN KEY (category_num) REFERENCES categories (category_num) ON DELETE CASCADE
);

CREATE SEQUENCE books_seq 
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;
/*    
INSERT INTO books (
    book_code, category_num, book_title, book_author, book_price, 
    book_publisher, book_description, book_stock, book_status, rating_avg, book_reg_date
) VALUES (
    books_seq.NEXTVAL, 
    (SELECT category_num FROM categories WHERE category_name = '소설'), 
    '나미야 잡화점의 기적', 
    '히가시노 게이고', 
    15000, 
    '현대문학', 
    '마음을 울리는 감동적인 이야기', 
    50, 
    1, 
    4.8, 
    SYSDATE
);
*/