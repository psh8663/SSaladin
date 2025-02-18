CREATE TABLE users (
    user_id VARCHAR2(12) PRIMARY KEY,
    user_auth NUMBER(1) NOT NULL,
    user_point NUMBER(9) NOT NULL,
    user_name VARCHAR2(20) NOT NULL,
    user_pw VARCHAR2(12) NOT NULL,
    user_phone VARCHAR2(15) NOT NULL,
    user_address VARCHAR2(50) NOT NULL,
    user_date DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT chk_user_auth CHECK (user_auth IN (0, 1, 2)),
    CONSTRAINT chk_user_id CHECK (REGEXP_LIKE(user_id, '^[A-Za-z0-9]{6,12}$')),
    CONSTRAINT chk_user_phone CHECK (REGEXP_LIKE(user_phone, '^010-\d{4}-\d{4}$')),
    CONSTRAINT chk_user_pw CHECK (REGEXP_LIKE(user_pw, '^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,12}$'))
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

