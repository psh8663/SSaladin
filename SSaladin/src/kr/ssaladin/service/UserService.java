package kr.ssaladin.service;

import kr.ssaladin.dao.UserDAO;

public class UserService {
	private UserDAO userDAO;

	public UserService() {
		this.userDAO = new UserDAO();
	}

	// 회원가입
	public boolean join(String userId, String userPw, String userName, String userPhone, String userAddress) {
		try {
			// 유효성 검사
			if (!isValidUserId(userId)) {
				return false;
			}
			if (!isValidUserPw(userPw)) {
				return false;
			}
			if (!isValidUserPhone(userPhone)) {
				return false;
			}

			// 중복 체크
			if (userDAO.checkUserId(userId)) {
				System.out.println("이미 존재하는 아이디입니다.");
				return false;
			}
			if (userDAO.checkUserPhone(userPhone)) {
				System.out.println("이미 존재하는 전화번호입니다.");
				return false;
			}

			// 회원가입 실행
			return userDAO.joinUser(userId, userPw, userName, userPhone, userAddress);

		} catch (Exception e) {
			System.out.println("회원가입 처리 중 오류 발생: " + e.getMessage());
			return false;
		}
	}

	// 로그인
	public int login(String userId, String userPw) {
		try {
			if (userDAO.checkLogin(userId, userPw)) {
				return userDAO.getUserAuth(userId);
			}
			System.out.println("아이디 또는 비밀번호가 올바르지 않습니다.");
			return -1;
		} catch (Exception e) {
			System.out.println("로그인 처리 중 오류 발생: " + e.getMessage());
			return -1;
		}
	}

	// 유효성 검사 - 아이디
	private boolean isValidUserId(String userId) {
		if (!userId.matches("^[A-Za-z0-9]{4,12}$")) {
			System.out.println("아이디는 4~12자의 영문자와 숫자만 가능합니다.");
			return false;
		}
		return true;
	}

	// 유효성 검사 - 비밀번호
	private boolean isValidUserPw(String userPw) {
		if (!userPw.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};:,.<>?]).{8,12}$")) {
			System.out.println("비밀번호는 8~12자, 대소문자, 숫자, 특수문자가 포함되어야 합니다.");
			return false;
		}
		return true;
	}

	// 유효성 검사 - 전화번호
	private boolean isValidUserPhone(String userPhone) {
		if (!userPhone.matches("^010-\\d{4}-\\d{4}$")) {
			System.out.println("전화번호는 010-xxxx-xxxx 형식이어야 합니다.");
			return false;
		}
		return true;
	}
}