package kr.ssaladin.service;

import java.sql.Connection;
import kr.ssaladin.dao.UserDAO;
import kr.util.DBUtil;

public class UserService {
    private Connection conn;
    private UserDAO userDAO;

    public UserService(Connection conn) {
        this.conn = conn;
        this.userDAO = new UserDAO(conn);
    }
    
    // 아이디 중복 체크
    public boolean isUserIdDuplicate(String userId) {
        try {
            return userDAO.isUserIdExists(userId); // DB에서 아이디 중복 여부 확인
        } catch (Exception e) {
            System.out.println("아이디 중복 확인 오류: " + e.getMessage());
            return false;
        }
    }

    // 전화번호 중복 체크
    public boolean isUserPhoneDuplicate(String userPhone) {
        try {
            return userDAO.isUserPhoneExists(userPhone); // DB에서 전화번호 중복 여부 확인
        } catch (Exception e) {
            System.out.println("전화번호 중복 확인 오류: " + e.getMessage());
            return false;
        }
    }

    // 회원가입
    public boolean join(String userId, String userPw, String userName, String userPhone, String userAddress) {
        try {
            // 유효성 검사
            if (!isValidUserId(userId)) {
                throw new IllegalArgumentException("아이디는 6~12자의 영문자와 숫자만 가능합니다.");
            }
            if (!isValidUserPw(userPw)) {
                throw new IllegalArgumentException("비밀번호는 8~12자, 대소문자, 숫자, 특수문자가 포함되어야 합니다.");
            }
            if (!isValidUserPhone(userPhone)) {
                throw new IllegalArgumentException("전화번호는 010-xxxx-xxxx 형식이어야 합니다.");
            }

            // 중복 체크
            if (isUserIdDuplicate(userId)) {
                throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
            }
            if (isUserPhoneDuplicate(userPhone)) {
                throw new IllegalArgumentException("이미 존재하는 전화번호입니다.");
            }

            // 유효성 검사 후 회원가입 시도
            boolean result = userDAO.JoinCheck(userId, userPw, userName, userPhone, userAddress);
            if (!result) {
                throw new Exception("회원가입에 실패하였습니다. 다시 시도해주세요.");
            }

            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("입력 오류: " + e.getMessage()); // 유효성 검사 실패 시 메시지 출력
            return false;
        } catch (Exception e) {
            System.out.println("회원가입 오류: " + e.getMessage()); // 다른 예외 처리
            return false;
        }
    }

    // 로그인
    public int login(String userId, String userPw) {
        try {
            boolean result = userDAO.LoginCheck(userId, userPw);
            
            if (!result) {
                throw new Exception("아이디 또는 비밀번호가 올바르지 않습니다.");
            }
            
            int userAuth = userDAO.getUserAuth(userId);
            return userAuth;
        } catch (Exception e) {
            System.out.println("로그인 오류: " + e.getMessage()); // 예외 발생 시 메시지 출력
            return -1;
        }
    }

    // 유효성 검사 - 아이디
    private boolean isValidUserId(String userId) {
        if (!userId.matches("^[A-Za-z0-9]{4,12}$")) {
            throw new IllegalArgumentException("아이디는 4~12자의 영문자와 숫자만 가능합니다.");
        }
        return true;
    }
    
    // 유효성 검사 - 비밀번호
    private boolean isValidUserPw(String userPw) {
        if (!userPw.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+={}:;,.<>?/\\\\|]).{8,12}$")) {
            throw new IllegalArgumentException("비밀번호는 8~12자, 대소문자, 숫자, 특수문자가 포함되어야 합니다.");
        }
        return true;
    }

    // 유효성 검사 - 전화번호
    private boolean isValidUserPhone(String userPhone) {
        if (!userPhone.matches("^010-\\d{4}-\\d{4}$")) {
            throw new IllegalArgumentException("전화번호는 010-xxxx-xxxx 형식이어야 합니다.");
        }
        return true;
    }
}