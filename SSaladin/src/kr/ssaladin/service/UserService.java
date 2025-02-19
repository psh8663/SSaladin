package kr.ssaladin.service;

import kr.ssaladin.dao.UserDAO;

public class UserService {

    // 회원가입
    public boolean join(String userId, String userPw, String userName, String userPhone, String userAddress) {
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

        // 유효성 검사 후 회원가입 시도
        UserDAO userDAO = new UserDAO();
        try {
            return userDAO.JoinCheck(userId, userPw, userName, userPhone, userAddress);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 로그인
    public boolean login(String userId, String userPw) {
        // 로그인 확인
        UserDAO userDAO = new UserDAO();
        try {
            return userDAO.LoginCheck(userId, userPw);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 유효성 검사 - 아이디
    private boolean isValidUserId(String userId) {
        return userId.matches("^[A-Za-z0-9]{4,12}$");
    }

    // 유효성 검사 - 비밀번호
    private boolean isValidUserPw(String userPw) {
        return userPw.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+={}:;,.<>?/\\\\|]).{8,12}$");
    }

    // 유효성 검사 - 전화번호
    private boolean isValidUserPhone(String userPhone) {
        return userPhone.matches("^010-\\d{4}-\\d{4}$");
    }
}
