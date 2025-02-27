package kr.ssaladin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import kr.ssaladin.model.PointRequest;
import kr.ssaladin.model.User;
import kr.ssaladin.dao.UserDAO;
import kr.ssaladin.dao.PointRequestDAO;
import kr.ssaladin.service.AdminBookService;
import kr.ssaladin.service.AdminRequestService;
import kr.ssaladin.service.AdminReviewsService;
import kr.ssaladin.service.BookListService;
import kr.ssaladin.service.CartService;
import kr.ssaladin.service.CartService.CartItem;
import kr.ssaladin.service.OrderService;
import kr.ssaladin.service.RequestService;
import kr.ssaladin.service.ReviewsService;
import kr.ssaladin.service.UserService;
import kr.ssaladin.service.PointRequestService;
import kr.util.DBUtil;

public class SSaladinMain {
	private BufferedReader br;
	private String me_id;
	private boolean flag; // 로그인 상태 체크
	private int userAuth; // 사용자 권한 (0: 일반회원, 1: VIP, 2: 관리자)
	private int userPoint; // 사용자 포인트
	private Connection conn; // 데이터베이스 연결 객체
	private UserService userService;
	private CartService cartService;
	private OrderService orderService;
	private BookListService bookListService;
	private PointRequestService pointRequestService; //
	private RequestService requestService; // RequestService 객체 추가
	private ReviewsService reviewsService; // ReviewsService 객체 추가
	private AdminReviewsService arvService; // AdminReviewsService 객체 추가
	private AdminRequestService arqService; // AdminRequestService 객체 추가

	public SSaladinMain() {
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			conn = DBUtil.getConnection(); // 데이터베이스 연결
			userService = new UserService(); // UserService 초기화
			cartService = new CartService(this);
			pointRequestService = new PointRequestService();
			orderService = new OrderService();	// OrderService 초기화
			reviewsService = new ReviewsService(); // ReviewsService 초기화
			requestService = new RequestService(); // RequestService 초기화
			arvService = new AdminReviewsService(); // AdminReviewsService 초기화
			arqService = new AdminRequestService(); // AdminRequestService 초기화

			// 메뉴 호출
			callMenu();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 프로그램 종료 시 데이터베이스 연결 종료
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 첫 화면
	private void callMenu() throws IOException {
		// 로그인 체크 영역
		while (true) {
			System.out.print("1. 로그인, 2. 회원가입, 3. 종료: \n>");
			try {
				int no = Integer.parseInt(br.readLine().trim());
				if (no == 1) {
					// 로그인
					login();
				} else if (no == 2) {
					// 회원가입
					join();
				} else if (no == 3) {
					// 종료
					System.out.println("프로그램을 종료합니다.");
					break;
				} else {
					System.out.println("잘못된 입력입니다.");
				}
			} catch (NumberFormatException e) {
				System.out.println("[ 숫자만 입력 가능합니다. ]");
			}
		}
	}

//
	// 로그인 페이지
	private void login() throws IOException {
		System.out.print("아이디: ");
		String userId = br.readLine().trim();
		System.out.print("비밀번호: ");
		String userPw = br.readLine().trim();

		// 로그인 성공 여부와 user_auth 값 받기
		int[] loginResult = userService.login(userId, userPw);
		this.userAuth = loginResult[0];
		this.userPoint = loginResult[1];

		if (this.userAuth != -1) {
			flag = true;
			me_id = userId;
			System.out.println("\n" + userId + "님 환영합니다!");

			// 권한에 따라 메뉴 출력
			if (this.userAuth == 0) {
				userMenu(); // 일반 회원 메뉴
			} else if (this.userAuth == 2) {
				adminMenu(); // 관리자 메뉴
			}
		}
	}

	// 회원가입 페이지
	private void join() throws IOException {
		System.out.print("아이디: ");
		String userId = br.readLine().trim();
		System.out.print("비밀번호: ");
		String userPw = br.readLine().trim();
		System.out.print("이름: ");
		String userName = br.readLine().trim();
		System.out.print("전화번호 (예: 010-1234-5678): ");
		String userPhone = br.readLine().trim();
		System.out.print("주소: ");
		String userAddress = br.readLine().trim();

		try {
			boolean isJoined = userService.join(userId, userPw, userName, userPhone, userAddress);
			if (isJoined) {
				System.out.println("회원가입 성공!");
			} else {
				System.out.println("회원가입 실패.");
			}
		} catch (IllegalArgumentException e) {
			System.out.println("회원가입 실패: " + e.getMessage());
		}
	}

	// 로그인 후 메뉴 (유저)
	private void userMenu() throws IOException {
		while (flag) {
			// 사용자 정보를 상단에 표시
			String userRole = "";
			switch (this.userAuth) {
			case 0:
				userRole = "일반회원";
				break;
			case 1:
				userRole = "VIP";
				break;
			case 2:
				userRole = "관리자";
				break;
			default:
				userRole = "비회원";
				break;
			}

			System.out.println("\n=====================================================");
			System.out.println(me_id + "님 (" + userRole + ") | 보유 포인트: " + this.userPoint + "원");
			System.out.println("=====================================================");

			System.out.print("1. 상품목록, 2. 마이페이지 , 3. 도서 신청 게시판 , 4. 장바구니 보기, 5. 로그아웃: \n> ");
			try {
				int no = Integer.parseInt(br.readLine().trim());
				if (no == 1) {
					new BookListService(this);
				} else if (no == 2) {
					// 마이페이지
					myPage();
				} else if (no == 3) {
					// 도서 신청 게시판
					System.out.println("도서 신청 게시판");
					requestService.requestService(me_id);
				} else if (no == 4) {
					cartService.manageCart(me_id, this.userPoint);
					System.out.println("장바구니 보기");
				} else if (no == 5) {
					// 로그아웃
					System.out.println("로그아웃 되었습니다.");
					flag = false; // 로그인 상태 해제
					break;
				} else {
					System.out.println("잘못된 입력입니다.");
				}
			} catch (NumberFormatException e) {
				System.out.println("[ 숫자만 입력 가능합니다. ]");
			}
		}
	}

	// 마이페이지
	private void myPage() throws IOException {
		// 마이페이지 메뉴
		while (flag) {
			// 사용자 정보를 상단에 표시
			String userRole = "";
			switch (this.userAuth) {
			case 0:
				userRole = "일반회원";
				break;
			case 1:
				userRole = "VIP";
				break;
			case 2:
				userRole = "관리자";
				break;
			default:
				userRole = "비회원";
				break;
			}

			System.out.println("\n=====================================================");
			System.out.println(me_id + "님 (" + userRole + ") | 보유 포인트: " + this.userPoint + "원");
			System.out.println("=====================================================");

			System.out.println("\n=== 마이페이지 ===");
			System.out.print("1. 회원정보 수정, 2. 포인트 충전, 3. 장바구니, 4. 주문 현황 조회, 5. 리뷰 관리, 6. 회원 탈퇴, 7. 뒤로가기: ");
			try {
				int no = Integer.parseInt(br.readLine());
				if (no == 1) {
					updateUserInfo();
				} else if (no == 2) {
					// 포인트 충전
					chargePoint();
				} else if (no == 3) {
					// 장바구니
					cartService.manageCart(me_id, this.userPoint);
				} else if (no == 4) {
					 // 주문 조회
	                System.out.println("나의 주문 조회");
	                orderService.checkOrderStatus(me_id, this.userAuth);
				} else if (no == 5) {
					// 리뷰 관리
					reviewsService.reviewService(me_id);
				} else if (no == 6) {
					// 회원 탈퇴
					withdrawUser();
				} else if (no == 7) {
					// 뒤로가기
					break; // 마이페이지 메뉴 종료
				} else {
					System.out.println("잘못된 입력입니다.");
				}
			} catch (NumberFormatException e) {
				System.out.println("[ 숫자만 입력 가능합니다. ]");
			}
		}
	}

	// 회원 탈퇴 메소드
	private void withdrawUser() throws IOException {
		System.out.println("\n=== 회원 탈퇴 ===");
		System.out.println("※ 주의: 회원 탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.");
		System.out.print("비밀번호를 입력하세요: ");
		String password = br.readLine().trim();

		System.out.print("정말 탈퇴하시겠습니까? (Y/N): ");
		String confirm = br.readLine().trim().toUpperCase();

		if (confirm.equals("Y")) {
			boolean success = userService.deleteUser(me_id, password);
			if (success) {
				System.out.println("회원 탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.");
				flag = false; // 로그인 상태 해제
				// 첫 화면으로 돌아가기
				callMenu();
			} else {
				System.out.println("회원 탈퇴에 실패했습니다. 비밀번호를 확인해주세요.");
			}
		} else {
			System.out.println("회원 탈퇴가 취소되었습니다.");
		}
	}

	private void updateUserInfo() {
		System.out.println("\n=== 회원정보 수정 ===");

		try {
			// 현재 로그인한 사용자 정보 조회
			User currentUser = userService.getUserInfo(me_id);
			if (currentUser == null) {
				System.out.println("사용자 정보를 찾을 수 없습니다.");
				return;
			}

			// 새로운 정보 입력 받기
			System.out.print("새로운 비밀번호 (변경하지 않으려면 엔터): ");
			String newPw = br.readLine().trim();
			if (newPw.isEmpty()) {
				newPw = currentUser.getUserPw();
			}

			System.out.print("새로운 전화번호 (예: 010-1234-5678, 변경하지 않으려면 엔터): ");
			String newPhone = br.readLine().trim();
			if (newPhone.isEmpty()) {
				newPhone = currentUser.getUserPhone();
			}

			System.out.print("새로운 주소 (변경하지 않으려면 엔터): ");
			String newAddress = br.readLine().trim();
			if (newAddress.isEmpty()) {
				newAddress = currentUser.getUserAddress();
			}

			// 회원정보 업데이트 시도
			boolean updateSuccess = userService.updateUserInfo(me_id, newPw, newPhone, newAddress);

			if (updateSuccess) {
				System.out.println("회원정보가 성공적으로 수정되었습니다.");
			} else {
				System.out.println("회원정보 수정에 실패했습니다. 입력하신 정보를 확인해주세요.");
			}

		} catch (IOException e) {
			System.out.println("입력 오류가 발생했습니다: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("처리 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	// 포인트 충전 페이지
	private void chargePoint() throws IOException {
		// 먼저 사용자가 요청한 포인트 충전 내역 출력
		System.out.println("\n=== 요청한 포인트 충전 내역 ===");
		List<PointRequest> userRequests = pointRequestService.getUserPointRequests(me_id); // 사용자 충전 요청 목록 가져오기

		if (userRequests.isEmpty()) {
			System.out.println("충전 요청 내역이 없습니다.");
		} else {
			System.out.println("요청번호\t사용자ID\t요청금액\t\t상태\t요청일자");
			System.out.println("----------------------------------------");

			for (PointRequest request : userRequests) {
				System.out.printf("%d\t%s\t%-9d\t%s\t%s%n", request.getRequestNum(), request.getUserId(),
						request.getPointAmount(), pointRequestService.getStatusString(request.getRequestStatus()),
						request.getRequestDate());
			}

			// 그 후에 충전할 금액을 입력 받음
			System.out.println("충전할 금액을 입력하세요: ");
			try {
				int chargeAmount = Integer.parseInt(br.readLine().trim());

				// 포인트 충전 요청을 DB에 생성 (PointRequestDAO 사용)
				boolean requestCreated = pointRequestService.requestCharge(me_id, chargeAmount);

				if (requestCreated) {
					System.out.println("현재 보유 포인트: " + userPoint + "원");
					System.out.println("충전 요청금액: " + chargeAmount + "원");
				} else {
					System.out.println("포인트 충전 요청에 실패했습니다. 잠시 후 다시 시도해주세요.");
				}
			} catch (NumberFormatException e) {
				// 입력이 숫자가 아닐 때
				System.out.println("올바른 금액을 입력해주세요.");
			} catch (Exception e) {
				// 그 외의 예외가 발생했을 때
				System.out.println("포인트 충전 중 오류가 발생했습니다: " + e.getMessage());
			}
		}
	}

	private void adminMenu() throws IOException {

		while (flag) {
			// 사용자 정보를 상단에 표시
			String userRole = "";
			switch (this.userAuth) {
			case 0:
				userRole = "일반회원";
				break;
			case 1:
				userRole = "VIP";
				break;
			case 2:
				userRole = "관리자";
				break;
			default:
				userRole = "비회원";
				break;
			}

			System.out.println("\n=====================================================");
			System.out.println(me_id + "님 (" + userRole + ") | 보유 포인트: " + this.userPoint + "원");
			System.out.println("=====================================================");

			System.out.print("1. 사용자 목록, 2. 도서 상품 관리, 3. 포인트 충전 요청 관리, 4. 리뷰/요청 관리 5. 로그아웃: ");
			try {
				int no = Integer.parseInt(br.readLine().trim());
				if (no == 1) {
					displayUserList();
				} else if (no == 2) {
					System.out.println("도서 상품의 추가/수정/삭제");
					new AdminBookService();
				} else if (no == 3) {
					managePointRequests();
				} else if (no == 4) {
					reviewRequest();
				} else if (no == 5) {
					System.out.println("관리자 로그아웃 완료.");
					flag = false;
					break;
				} else {
					System.out.println("잘못된 입력입니다.");
				}
			} catch (NumberFormatException e) {
				System.out.println("[ 숫자만 입력 가능합니다. ]");
			}
		}
	}

	private void displayUserList() {

		System.out.println(
				"\n======================================================== 사용자 목록 ========================================================");
		List<User> userList = userService.getAllUsers();

		if (userList.isEmpty()) {
			System.out.println("등록된 사용자가 없습니다.");
		} else {

			for (User user : userList) {
				System.out.printf("ID: %-12s 이름: %-10s 전화번호: %-15s 주소: %-20s 보유 포인트: %-10d 가입일: %-12s\n",
						user.getUserId(), user.getUserName(), user.getUserPhone(), user.getUserAddress(),
						user.getUserPoint(), user.getUser_date());
			}
		}
		// 회원 탈퇴 (관리자)
		System.out.println("\n1. 회원 탈퇴, 2. 이전 메뉴");
		try {
			int choice = Integer.parseInt(br.readLine().trim());
			if (choice == 1) {
				System.out.print("탈퇴시킬 회원의 ID를 입력하세요: ");
				String userId = br.readLine().trim();

				// 관리자 자신은 탈퇴시킬 수 없음
				if (userId.equals(me_id)) {
					System.out.println("관리자 계정입니다(권한 없음)");
					return;
				}

				System.out.print("정말 이 회원을 탈퇴시키겠습니까? (Y/N): ");
				String confirm = br.readLine().trim().toUpperCase();

				if (confirm.equals("Y")) {
					boolean success = userService.adminDeleteUser(userId);
					if (success) {
						System.out.println(userId + " 회원이 성공적으로 탈퇴되었습니다.");
					} else {
						System.out.println("회원 탈퇴에 실패했습니다. 존재하지 않는 ID입니다.");
					}
				}
			}
		} catch (IOException | NumberFormatException e) {
			System.out.println("입력 오류");
		}
	}

	private void reviewRequest() throws NumberFormatException, IOException {
		while (true) {
			try {
				System.out.print("1. 리뷰 관리, 2. 요청 관리, 3. 이전으로 돌아가기");
				int num = Integer.parseInt(br.readLine());
				if (num == 1) {
					arvService.aReviewService(me_id);
				} else if (num == 2) {
					arqService.aRequestService(me_id);
				} else if (num == 3) {
					break;
				} // if
			} catch (NumberFormatException e) {
				System.out.println("[ 숫자로 입력해야 합니다.]");
			} // try_catch
		} // while
	}

	private void managePointRequests() throws IOException {
		while (true) {
			System.out.println("\n=== 포인트 충전 요청 관리 ===");
			List<PointRequest> requests = pointRequestService.getAllRequests();

			if (requests.isEmpty()) {
				System.out.println("처리할 포인트 충전 요청이 없습니다.");
				break;
			}

			System.out.println("요청번호\t사용자ID\t요청금액\t\t상태\t요청일자");
			System.out.println("----------------------------------------");

			for (PointRequest request : requests) {
				System.out.printf("%d\t%s\t%-9d\t%s\t%s%n", request.getRequestNum(), request.getUserId(),
						request.getPointAmount(), pointRequestService.getStatusString(request.getRequestStatus()),
						request.getRequestDate());
			}

			System.out.println("\n1. 요청 처리하기 2. 이전 메뉴로 돌아가기");
			int choice = Integer.parseInt(br.readLine().trim());

			if (choice == 1) {
				System.out.print("처리할 요청 번호를 입력하세요: ");
				int requestNum = Integer.parseInt(br.readLine().trim());

				System.out.print("처리 방법을 선택하세요 (2: 승인, 3: 거절): ");
				int status = Integer.parseInt(br.readLine().trim());

				boolean success = pointRequestService.processRequest(requestNum, status);

				if (success) {
					System.out.println("요청이 성공적으로 처리되었습니다.");
					if (status == 2) {
						System.out.println("사용자의 포인트가 증가되었습니다.");
					}
				} else {
					System.out.println("요청 처리에 실패했습니다.");
				}
			} else if (choice == 2) {
				return;
			}
		}
	}

	public String getUserId() {
		return me_id;
	}

	public int getUserPoint() {
		return userPoint;
	}

	public void setUserPoint(int userPoint) {
		this.userPoint = userPoint;
	}

	public static void main(String[] args) {
		new SSaladinMain();
	}
}