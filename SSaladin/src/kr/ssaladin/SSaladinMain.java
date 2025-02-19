package kr.ssaladin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import kr.ssaladin.service.UserService;

public class SSaladinMain {
   private BufferedReader br;
   private String me_id;
   private boolean flag; // 로그인 상태 체크

   private UserService userService; // UserService 객체 생성

   public SSaladinMain() {
      try {
         br = new BufferedReader(new InputStreamReader(System.in));
         userService = new UserService(); // UserService 초기화
         // 메뉴 호출
         callMenu();
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         if (br != null)
            try {
               br.close();
            } catch (IOException e) {
               e.printStackTrace();
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

      boolean loginSuccess = userService.login(userId, userPw);
      if (loginSuccess) {
         flag = true;
         me_id = userId;
         System.out.println("로그인 성공!");
         // 로그인 후 회원제 서비스 화면으로 전환
         userMenu();
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
      // 로그인 성공 후 회원제 서비스 영역
      while (flag) {
         System.out.print("1. 상품목록, 2. 회원 정보 조회 , 3. 도서 신청 게시판 ,4. 로그아웃: ");
         try {
            int no = Integer.parseInt(br.readLine());
            if (no == 1) {
               // 상품목록
               System.out.println("상품목록을 보여줍니다.");
            } else if (no == 2) {
               // 회원 정보 조회
               System.out.println("회원 정보 조회 화면");
            } else if (no == 3) {
               // 도서 신청 게시판
               System.out.println("도서 신청 게시판");
            } else if (no == 4) {
               // 종료
               System.out.println("로그아웃 완료.");
               break;
            } else {
               System.out.println("잘못된 입력입니다.");
            }
         } catch (NumberFormatException e) {
            System.out.println("[ 숫자만 입력 가능합니다. ]");
         }
      }
   }

   public static void main(String[] args) {
      new SSaladinMain();
   }
}
