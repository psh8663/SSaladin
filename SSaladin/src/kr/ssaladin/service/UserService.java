package kr.ssaladin.service;

import java.util.List;

import kr.ssaladin.dao.UserDAO;
import kr.ssaladin.model.User;

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
			if (userDAO.checkUserPhone(userPhone, null)) {
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
	public int[] login(String userId, String userPw) {
		int[] result = new int[2]; // 결과 배열 선언: [0] - userAuth, [1] - userPoint

		try {
			if (userDAO.checkLogin(userId, userPw)) {
				result[0] = userDAO.getUserAuth(userId); // userAuth 가져오기
				result[1] = userDAO.getUserPoint(userId); // userPoint 가져오기
				return result;
			}
			System.out.println("아이디 또는 비밀번호가 올바르지 않습니다.");
			result[0] = -1; // 로그인 실패
			return result;
		} catch (Exception e) {
			System.out.println("로그인 처리 중 오류 발생: " + e.getMessage());
			result[0] = -1; // 로그인 실패
			return result;
		}
	}

	// 회원정보 조회
	public List<User> getAllUsers() {
		return userDAO.getAllUsersInfo();
	}

	// 회원정보 수정 조회
	public User getUserInfo(String userId) {
		return userDAO.getUserInfo(userId);
	}

	// 회원정보 수정
	public boolean updateUserInfo(String userId, String userPw, String userPhone, String userAddress) {
		try {
			// 현재 사용자 정보 조회
			User currentUser = getUserInfo(userId);

			// 비밀번호 유효성 검사
			if (!isValidUserPw(userPw)) {
				return false;
			}

			// 전화번호가 변경되었을 때만 유효성 검사 및 중복 체크 수행
			if (!userPhone.equals(currentUser.getUserPhone())) {
				if (!isValidUserPhone(userPhone)) {
					return false;
				}

				if (userDAO.checkUserPhone(userPhone, userId)) {
					System.out.println("이미 존재하는 전화번호입니다.");
					return false;
				}
			}

			return userDAO.updateUserInfo(userId, userPw, userPhone, userAddress);
		} catch (Exception e) {
			System.out.println("회원정보 수정 중 오류 발생: " + e.getMessage());
			return false;
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