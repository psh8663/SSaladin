CREATE TABLE categories (
    category_num NUMBER(2) PRIMARY KEY,
    category_name VARCHAR2(30) NOT NULL
);

CREATE SEQUENCE categories_seq 
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;
drop table books;

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
    rating_avg FLOAT,
    book_reg_date DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT fk_books_category FOREIGN KEY (category_num) REFERENCES categories (category_num) ON DELETE CASCADE
);

CREATE SEQUENCE books_seq 
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 카테고리 데이터 삽입
INSERT INTO categories (category_num, category_name) VALUES (categories_seq.NEXTVAL, '소설');
INSERT INTO categories (category_num, category_name) VALUES (categories_seq.NEXTVAL, '자기계발');
INSERT INTO categories (category_num, category_name) VALUES (categories_seq.NEXTVAL, '과학');
INSERT INTO categories (category_num, category_name) VALUES (categories_seq.NEXTVAL, '역사');
INSERT INTO categories (category_num, category_name) VALUES (categories_seq.NEXTVAL, '예술');

-- '소설' 카테고리 도서 삽입
INSERT INTO books (book_code, category_num, book_title, book_author, book_price, book_publisher, book_description, book_stock, book_status, rating_avg, book_reg_date)
VALUES (books_seq.NEXTVAL, (SELECT category_num FROM categories WHERE category_name = '소설'), 
        '나미야 잡화점의 기적', '히가시노 게이고', 15000, '현대문학', '마음을 울리는 감동적인 이야기', 50, 1, 4.8, SYSDATE);
        
INSERT INTO books (book_code, category_num, book_title, book_author, book_price, book_publisher, book_description, book_stock, book_status, rating_avg, book_reg_date)
VALUES (books_seq.NEXTVAL, (SELECT category_num FROM categories WHERE category_name = '소설'), 
        '1984', '조지 오웰', 14000, '민음사', '디스토피아 문학의 대표작', 40, 1, 4.7, SYSDATE);
        
INSERT INTO books (book_code, category_num, book_title, book_author, book_price, book_publisher, book_description, book_stock, book_status, rating_avg, book_reg_date)
VALUES (books_seq.NEXTVAL, (SELECT category_num FROM categories WHERE category_name = '소설'), 
        '데미안', '헤르만 헤세', 13000, '열린책들', '성장과 자아 발견의 이야기', 30, 1, 4.6, SYSDATE);

-- '자기계발' 카테고리 도서 삽입
INSERT INTO books (book_code, category_num, book_title, book_author, book_price, book_publisher, book_description, book_stock, book_status, rating_avg, book_reg_date)
VALUES (books_seq.NEXTVAL, (SELECT category_num FROM categories WHERE category_name = '자기계발'), 
        '부자 아빠 가난한 아빠', '로버트 기요사키', 17000, '한국경제신문', '돈을 바라보는 새로운 시각', 50, 1, 4.9, SYSDATE);

INSERT INTO books (book_code, category_num, book_title, book_author, book_price, book_publisher, book_description, book_stock, book_status, rating_avg, book_reg_date)
VALUES (books_seq.NEXTVAL, (SELECT category_num FROM categories WHERE category_name = '자기계발'), 
        '아침의 기적', '할 엘로드', 16000, '한빛비즈', '성공을 위한 아침 습관', 40, 1, 4.7, SYSDATE);

INSERT INTO books (book_code, category_num, book_title, book_author, book_price, book_publisher, book_description, book_stock, book_status, rating_avg, book_reg_date)
VALUES (books_seq.NEXTVAL, (SELECT category_num FROM categories WHERE category_name = '자기계발'), 
        '1%의 차이', '이민규', 15000, '위즈덤하우스', '작은 차이가 만드는 큰 성공', 35, 1, 4.6, SYSDATE);

-- '과학' 카테고리 도서 삽입
INSERT INTO books (book_code, category_num, book_title, book_author, book_price, book_publisher, book_description, book_stock, book_status, rating_avg, book_reg_date)
VALUES (books_seq.NEXTVAL, (SELECT category_num FROM categories WHERE category_name = '과학'), 
        '코스모스', '칼 세이건', 20000, '사이언스북스', '우주와 생명의 경이로운 이야기', 45, 1, 4.9, SYSDATE);

INSERT INTO books (book_code, category_num, book_title, book_author, book_price, book_publisher, book_description, book_stock, book_status, rating_avg, book_reg_date)
VALUES (books_seq.NEXTVAL, (SELECT category_num FROM categories WHERE category_name = '과학'), 
        '시간의 역사', '스티븐 호킹', 18000, '까치', '시간과 우주의 기원', 50, 1, 4.8, SYSDATE);

INSERT INTO books (book_code, category_num, book_title, book_author, book_price, book_publisher, book_description, book_stock, book_status, rating_avg, book_reg_date)
VALUES (books_seq.NEXTVAL, (SELECT category_num FROM categories WHERE category_name = '과학'), 
        '이기적 유전자', '리처드 도킨스', 17000, '을유문화사', '유전자의 생존 전략', 30, 1, 4.7, SYSDATE);

-- '역사' 카테고리 도서 삽입
INSERT INTO books (book_code, category_num, book_title, book_author, book_price, book_publisher, book_description, book_stock, book_status, rating_avg, book_reg_date)
VALUES (books_seq.NEXTVAL, (SELECT category_num FROM categories WHERE category_name = '역사'), 
        '사피엔스', '유발 하라리', 22000, '김영사', '인류의 역사와 미래', 60, 1, 4.9, SYSDATE);

INSERT INTO books (book_code, category_num, book_title, book_author, book_price, book_publisher, book_description, book_stock, book_status, rating_avg, book_reg_date)
VALUES (books_seq.NEXTVAL, (SELECT category_num FROM categories WHERE category_name = '역사'), 
        '총, 균, 쇠', '재레드 다이아몬드', 20000, '문학사상', '문명의 흥망성쇠', 45, 1, 4.8, SYSDATE);

INSERT INTO books (book_code, category_num, book_title, book_author, book_price, book_publisher, book_description, book_stock, book_status, rating_avg, book_reg_date)
VALUES (books_seq.NEXTVAL, (SELECT category_num FROM categories WHERE category_name = '역사'), 
        '세계사를 움직이는 다섯 가지 힘', '사토 마사루', 18000, '다산북스', '역사를 변화시킨 주요 원동력', 35, 1, 4.7, SYSDATE);

-- '예술' 카테고리 도서 삽입
INSERT INTO books (book_code, category_num, book_title, book_author, book_price, book_publisher, book_description, book_stock, book_status, rating_avg, book_reg_date)
VALUES (books_seq.NEXTVAL, (SELECT category_num FROM categories WHERE category_name = '예술'), 
        '빈센트 반 고흐', '스테판 안토르', 25000, '다빈치북스', '고흐의 삶과 작품 분석', 30, 1, 4.9, SYSDATE);

INSERT INTO books (book_code, category_num, book_title, book_author, book_price, book_publisher, book_description, book_stock, book_status, rating_avg, book_reg_date)
VALUES (books_seq.NEXTVAL, (SELECT category_num FROM categories WHERE category_name = '예술'), 
        '모나리자 미스터리', '장 피에르 트루아', 23000, '아트북스', '모나리자의 숨겨진 이야기', 25, 1, 4.8, SYSDATE);
