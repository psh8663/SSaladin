create table reviews (
	review_num number primary key,
	user_id varchar2(12) not null,
	book_code number not null,
	book_title varchar2(50) not null,
	reviews_content varchar2(500) not null,
	rating number(1) not null,
	reg_date date default SYSDATE not null,
	foreign key (user_id) references users (user_id) on delete cascade
	foreign key (book_code) references books (book_code) on delete cascade
	
);

create sequence reviews_seq;

create table request_books (
	request_num number primary key,
	user_id varchar2(12) not null,
	request_content varchar2(300) not null,
	request_date date default SYSDATE not null,
	foreign key (user_id) references users (user_id) on delete cascade
);

create sequence request_books_seq;

-- 리뷰 insert하는 sql문
INSERT INTO reviews (user_id, review_num, book_code, book_title, reviews_content, rating, reg_date)
SELECT 'user1', reviews_seq.NEXTVAL, b.book_code, b.book_title, '너무 재밌어요', 5, SYSDATE
FROM books b
WHERE b.book_code = '1';

-- 요청글 insert하는 sql문
INSERT INTO request_books (request_num, user_id, request_content, request_date) 
VALUES (request_books_seq.nextval, user1, 'J.K. 롤링 작가의 해리 포터와 마법사의 돌 입고 신청합니다.', SYSDATE);					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					
					