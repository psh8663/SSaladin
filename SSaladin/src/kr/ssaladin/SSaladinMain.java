package kr.ssaladin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

import kr.ssaladin.service.BookListService;
import kr.ssaladin.service.CartService;
import kr.ssaladin.service.RequestService;
import kr.ssaladin.service.UserService;
import kr.util.DBUtil;

public class SSaladinMain {
	private BufferedReader br;
	private String me_id;
	private boolean flag; // 로그인 상태 체크
	private int userAuth; // 사용자 권한 (0: 일반회원, 1: VIP, 2: 관리자)
	private Connection conn; // 데이터베이스 연결 객체
	private UserService userService; // UserService 객체 추가
	private CartService cartService; // CartService 객체 추가
	private BookListService bookListService; // BookListService 객체 추가
	private RequestService requestService; // RequestService 객체 추가

	public SSaladinMain() {
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			conn = DBUtil.getConnection(); // 데이터베이스 연결
			userService = new UserService(); // UserService 초기화
			cartService = new CartService();
			requestService = new RequestService(); // RequestService 초기화
			
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

	private void callMenu() throws IOException {
		// 로그인 체크 영역
		while (true) {
			System.out.print("1. 로그인, 2. 회원가입, 3. 종료: ");
			try {
				int no = Integer.parseInt(br.readLine());
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

	private void login() throws IOException {
		System.out.print("아이디: ");
		String userId = br.readLine();
		System.out.print("비밀번호: ");
		String userPw = br.readLine();

		// 로그인 성공 여부와 user_auth 값 받기
		this.userAuth = userService.login(userId, userPw); // 인스턴스 변수에 저장

		System.out.println("로그인한 아이디의 userAuth: " + this.userAuth);

		if (this.userAuth != -1) {
			flag = true;
			me_id = userId;
			System.out.println(userId + "님 환영합니다!");

			// 권한에 따라 메뉴 출력
			if (this.userAuth == 0) {
				userMenu(); // 일반 회원 메뉴
			} else if (this.userAuth == 2) {
				adminMenu(); // 관리자 메뉴
			}
		} else {
			System.out.println("아이디 또는 비밀번호가 틀렸습니다.");
		}
	}

	private void join() throws IOException {
		System.out.print("아이디: ");
		String userId = br.readLine();
		System.out.print("비밀번호: ");
		String userPw = br.readLine();
		System.out.print("이름: ");
		String userName = br.readLine();
		System.out.print("전화번호 (예: 010-1234-5678): ");
		String userPhone = br.readLine();
		System.out.print("주소: ");
		String userAddress = br.readLine();

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

	private void userMenu() throws IOException {
		// 로그인 성공 후 회원제 서비스 영역 (일반 사용자)
		while (flag) {
			System.out.print("1. 상품목록, 2. 회원 정보 조회 , 3. 도서 신청 게시판 , 4. 장바구니 보기, 5. 로그아웃: ");
			try {
				int no = Integer.parseInt(br.readLine());
				if (no == 1) {
					new BookListService(this);
				} else if (no == 2) {
					// 회원 정보 조회
					System.out.println("회원 정보 조회 화면");
				} else if (no == 3) {
					// 도서 신청 게시판
					System.out.println("도서 신청 게시판");
					requestService.requestService(me_id);
				} else if (no == 4) {
					// 장바구니 보기
					System.out.println("장바구니 보기");
				} else if (no == 5) {
					// 로그아웃
					System.out.println("로그아웃 완료.");
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

	// ======================================//
	// 장바구니 관리 메서드 추가중
	// + 구매기능 (장바구니에서 최종 구매) 추가구현 필요
	// 도서 목록에서 장바구니에 책을 담고, 장바구니에서 최종 구매
	// 구매 시 장바구니에 담겨있는 도서들의 포인트 합계만큼 포인트 차감 (포인트처리 클래스 작성중)

	public void manageCart() throws IOException {
		// 장바구니 관리
		boolean cartMenu = true;
		while (cartMenu) {
			System.out.println("\n=== 장바구니 관리 ===");
			System.out.print("1. 장바구니 목록 보기, 2. 장바구니 상품 수량 변경, " + "3. 장바구니 상품 삭제, 4. 뒤로가기: ");
			try {
				int no = Integer.parseInt(br.readLine());
				if (no == 1) {
//	               showCartItems();
				} else if (no == 2) {
					updateCartItemQuantity();
				} else if (no == 3) {
					deleteCartItem();
				} else if (no == 4) {
					cartMenu = false;
				} else {
					System.out.println("잘못된 입력입니다.");
				}
			} catch (NumberFormatException e) {
				System.out.println("[ 숫자만 입력 가능합니다. ]");
			}
		}
	}

	/*
	 * private void showCartItems() throws IOException { // 로그인 상태 체크 if (!flag) {
	 * // flag - false = 로그인 안된거임 System.out.println("로그인 후 장바구니 목록을 조회할 수 있습니다.");
	 * return; }
	 * 
	 * // 로그인 상태일 때만 장바구니 목록 조회 try {
	 * System.out.println("--------- 내 장바구니 목록 ---------");
	 * 
	 * // 로그인된 사용자 ID(me_id)로 장바구니 조회 cartService.getUserCartItems(me_id
	 * ).forEach(item -> System.out.println("도서코드: " + item.getProductId() +
	 * ", 도서명: " + item.getProductName() + ", 수량: " + item.getQuantity() + ", 가격: "
	 * + item.getPrice())); } catch (Exception e) {
	 * System.out.println("장바구니 목록 조회 중 오류가 발생했습니다: " + e.getMessage()); } }
	 * 
	 */

	private void updateCartItemQuantity() throws IOException {
		// 장바구니에 담긴 상품의 수량 수정
		try {
			System.out.print("수정할 상품 ID를 입력하세요: ");
			int productId = Integer.parseInt(br.readLine());
			System.out.print("새로운 수량을 입력하세요: ");
			int newQuantity = Integer.parseInt(br.readLine());

			// 로그인 후 상품 수량 수정
			boolean success = cartService.updateQuantity(me_id, null, productId, newQuantity);
			if (success) {
				System.out.println("상품 수량이 성공적으로 수정되었습니다.");
			} else {
				System.out.println("상품 수량 수정에 실패했습니다.");
			}
		} catch (NumberFormatException e) {
			System.out.println("올바른 숫자를 입력해주세요.");
		} catch (Exception e) {
			System.out.println("수량 수정 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	private void deleteCartItem() throws IOException {
		try {
			System.out.print("삭제할 상품 ID를 입력하세요: ");
			int productId = Integer.parseInt(br.readLine());

			// 수정: 로그인 후 장바구니에서 상품 삭제
			boolean success = cartService.removeFromCart(me_id, null, productId);
			if (success) {
				System.out.println("상품이 장바구니에서 삭제되었습니다.");
			} else {
				System.out.println("상품 삭제에 실패했습니다.");
			}
		} catch (NumberFormatException e) {
			System.out.println("올바른 숫자를 입력해주세요.");
		} catch (Exception e) {
			System.out.println("상품 삭제 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	private void adminMenu() throws IOException {
		// 관리자 메뉴
		while (flag) {
			System.out.print("1. 사용자 목록, 2. 상품 관리, 3. 로그아웃: ");
			try {
				int no = Integer.parseInt(br.readLine());
				if (no == 1) {
					// 사용자 목록
					System.out.println("사용자 목록을 보여줍니다.");
				} else if (no == 2) {
					// 상품 관리
					System.out.println("상품 관리 화면");
				} else if (no == 3) {
					// 로그아웃
					System.out.println("관리자 로그아웃 완료.");
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
	public String getUserId() {
	    return me_id;
	}

	public static void main(String[] args) {
		new SSaladinMain();
	}
}