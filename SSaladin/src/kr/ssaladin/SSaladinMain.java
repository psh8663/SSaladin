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
			cartService = new CartService();
			pointRequestService = new PointRequestService();
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
			System.out.print("1. 로그인, 2. 회원가입, 3. 종료: ");
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
			System.out.println(userId + "님 환영합니다!");

			// userAuth 값에 따라 등급 출력
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

			System.out.println("회원 등급: " + userRole);
			System.out.println("보유 포인트: " + this.userPoint); // 포인트 출력

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
			System.out.print("1. 상품목록, 2. 마이페이지 , 3. 도서 신청 게시판 , 4. 장바구니 보기, 5. 로그아웃: ");
			try {
				int no = Integer.parseInt(br.readLine());
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
					manageCart();
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
			System.out.println("\n=== 마이페이지 ===");
			System.out.print("1. 회원정보 수정, 2. 포인트 충전, 3. 장바구니, 4. 구매내역, 5. 리뷰 관리 6. 뒤로가기: ");
			try {
				int no = Integer.parseInt(br.readLine());
				if (no == 1) {
					updateUserInfo();
				} else if (no == 2) {
					// 포인트 충전
					chargePoint();
				} else if (no == 3) {
					// 장바구니
					manageCart();
				} else if (no == 4) {
					// 구매내역
					System.out.println("구매내역 페이지");
					// 구매내역 페이지 구현 필요
				} else if (no == 5) {
					// 리뷰 관리
					reviewsService.reviewService(me_id);
				} else if (no == 6) {
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
		// 포인트 충전
		System.out.println("충전할 금액을 입력하세요: ");
		try {
			int chargeAmount = Integer.parseInt(br.readLine());

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

	// ======================================//

	// + 구매기능 (장바구니에서 최종 구매) 추가구현 필요
	// 도서 목록에서 장바구니에 책을 담고, 장바구니에서 최종 구매

	public void manageCart() throws IOException {
		// 장바구니 관리
		boolean cartMenu = true;
		while (cartMenu) {
			System.out.println("\n==================================== 장바구니 관리 ======================================");
			System.out.println();
			System.out.println("1. 장바구니 목록 보기, 2. 장바구니 상품 수량 변경, " + "3. 장바구니 상품 삭제, 4. 구매하기, 5. 뒤로가기: ");
			try {
				int no = Integer.parseInt(br.readLine());
				if (no == 1) {
					// 장바구니 상품 목록 조회
					showCartItems();
				} else if (no == 2) {
					// 장바구니에 있는 상품의 수량 변경
					updateCartItemQuantity();
				} else if (no == 3) {
					// 장바구니 내의 상품 삭제
					deleteCartItem();
				} else if (no == 4) {
					// 장바구니의 상품 구매
                     purchaseCartItem(me_id);
				} else if (no == 5) {
					// 
					cartMenu = false;
				} else {
					System.out.println("잘못된 입력입니다.");
				}
			} catch (NumberFormatException e) {
				System.out.println("[ 숫자만 입력 가능합니다. ]");
			}
		}
	}

	private void showCartItems() throws IOException {
		// 로그인 상태 체크

		if (!flag) {
			System.out.println("로그인 후 장바구니 목록을 조회할 수 있습니다.");
			return;
		}

		// 로그인 상태일 때만 장바구니 목록 조회
		try {
			System.out.println("==================================== 내 장바구니 목록 ====================================");
			System.out.println();

			// 장바구니 항목을 가져오기
			List<CartItem> cartItems = cartService.getUserCartItems(me_id);

			// 장바구니가 비어있는지 확인
			if (cartItems.isEmpty()) {
				System.out.println("장바구니에 담긴 상품이 없습니다.");
			} else {
				// 장바구니 목록 출력
				cartItems.forEach(item -> System.out.println("주문번호: "+ item.getCartNum() + ", 도서코드: " + item.getBookCode() + ", 도서명: "
						+ item.getBookTitle() + ", 수량: " + item.getCartQuantity() + ", 가격: " + item.getBookPrice()));
			}
		} catch (Exception e) {
			System.out.println("장바구니 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	private void updateCartItemQuantity() throws IOException {
		// 장바구니에 담긴 상품의 수량 수정
		showCartItems();
		System.out.println();
		System.out.println("-".repeat(86));

		try {
			System.out.print("수량을 수정할 상품의 도서 코드를 입력하세요: ");
			int bookCode = Integer.parseInt(br.readLine());
			System.out.print("새로운 수량을 입력하세요: ");
			int newQuantity = Integer.parseInt(br.readLine());

			// 로그인 후 상품 수량 수정
			boolean success = cartService.updateQuantity(me_id, bookCode, newQuantity);
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
	//장바구니 상품 삭제
		showCartItems();
		System.out.println();
		System.out.println("-".repeat(66));
		System.out.println();
		try {
			System.out.print("장바구니에서 삭제 할 주문 번호를 입력하세요: ");
			int productId = Integer.parseInt(br.readLine());
			
			
			// 수정: 로그인 후 장바구니에서 상품 삭제
			boolean success = cartService.removeFromCart(productId);
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

		while (flag) {
			System.out.print("1. 사용자 목록, 2. 도서 상품 관리, 3. 포인트 충전 요청 관리, 4. 리뷰/요청 관리 5. 로그아웃: ");
			try {
				int no = Integer.parseInt(br.readLine());
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
	
	public void purchaseCartItem(String me_id) throws IOException {
	//장바구니 상품 구매
		try {
			// 장바구니 항목 조회
			List<CartItem> cartItems = cartService.getUserCartItems(me_id);
			
			if (cartItems == null || cartItems.isEmpty()) {
				System.out.println("장바구니가 비어있습니다.");
				return;
			}
			
			// 총액 계산
			int totalAmount = cartService.calculateTotal(me_id);
			
			// 현재 포인트 확인
			if (totalAmount > userPoint) {
				System.out.println("포인트가 부족합니다. 현재 포인트: " + userPoint + "원, 필요 포인트: " + totalAmount + "원");
				System.out.println("포인트를 충전하시겠습니까? (1: 예, 2: 아니오)");
				int choice = Integer.parseInt(br.readLine());
				
				if (choice == 1) {
					chargePoint();
					return;
				} else {
					return;
				}
			}
			
			// 구매 확인
			System.out.println("총 구매 금액: " + totalAmount + "원");
			System.out.println("구매하시겠습니까? (1: 예, 2: 아니오)");
			int confirm = Integer.parseInt(br.readLine());
			
			if (confirm == 1) {
				boolean success = cartService.processPurchase(me_id, cartItems, totalAmount);
				
				if (success) {
					System.out.println("구매가 완료되었습니다.");
					// 포인트 차감 후 업데이트
					userPoint -= totalAmount;
					System.out.println("잔여 포인트: " + userPoint + "원");
				} else {
					System.out.println("구매 처리 중 오류가 발생했습니다.");
				}
			}
		} catch (NumberFormatException e) {
			System.out.println("올바른 숫자를 입력해주세요.");
		} catch (Exception e) {
			System.out.println("구매 처리 중 오류가 발생했습니다: " + e.getMessage());
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
				System.out.println("[ 숫자만 입력 가능]");
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

			System.out.println("요청번호\t사용자ID\t요청금액\t상태\t요청일자");
			System.out.println("----------------------------------------");

			for (PointRequest request : requests) {
				System.out.printf("%d\t%s\t%d\t%s\t%s%n", request.getRequestNum(), request.getUserId(),
						request.getPointAmount(), pointRequestService.getStatusString(request.getRequestStatus()),
						request.getRequestDate());
			}

			System.out.println("\n1. 요청 처리하기 2. 이전 메뉴로 돌아가기");
			int choice = Integer.parseInt(br.readLine());

			if (choice == 1) {
				System.out.print("처리할 요청 번호를 입력하세요: ");
				int requestNum = Integer.parseInt(br.readLine());

				System.out.print("처리 방법을 선택하세요 (2: 승인, 3: 거절): ");
				int status = Integer.parseInt(br.readLine());

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

	public static void main(String[] args) {
		new SSaladinMain();
	}
}