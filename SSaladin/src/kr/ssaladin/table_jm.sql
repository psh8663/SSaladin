create table reviews (
	review_num number primary key,
	user_id varchar2(12) not null,
	book_code number not null,
	book_title varchar2(50) not null,
	rating number(1) not null,
	reg_date date default SYSDATE not null,
	foreign key (user_id, book_title) references users (user_id, book_title) on delete cascade
);

create sequence reviews_seq;

create table request (
	request_num number primary key,
	user_id varchar2(12) not null,
	request_content varchar2(300) not null,
	request_date date default SYSDATE not null,
	foreign key (user_id) references users (user_id) on delete cascade
);

create sequence request_seq;