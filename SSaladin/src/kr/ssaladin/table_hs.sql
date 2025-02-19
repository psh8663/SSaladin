CREATE TABLE users (
    user_id VARCHAR2(12) PRIMARY KEY,
    user_auth NUMBER(1) NOT NULL,
    user_point NUMBER(9) NOT NULL,
    user_name VARCHAR2(20) NOT NULL,
    user_pw VARCHAR2(12) NOT NULL,
    user_phone VARCHAR2(15) NOT NULL,
    user_address VARCHAR2(50) NOT NULL,
    user_date DATE DEFAULT SYSDATE NOT NULL
);


CREATE SEQUENCE seq_request_num
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE TABLE point_requests (
    request_num NUMBER PRIMARY KEY,
    user_id VARCHAR2(12) NOT NULL,
    point_amount NUMBER(9) NOT NULL,
    request_status NUMBER(1) NOT NULL,
    point_used NUMBER(9) NOT NULL,
    request_date DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT chk_request_status CHECK (request_status IN (1, 2, 3))
);

s