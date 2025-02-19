package kr.ssaladin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SSaladinMain {
   private BufferedReader br;
   private String me_id;
   private boolean flag;
   
      public SSaladinMain() {
            try {
               br = new BufferedReader(new InputStreamReader(System.in));
               //메뉴 호출
               callMenu();
            } catch (Exception e) {
               e.printStackTrace();
            } finally {
               if(br!=null) try {br.close();} catch (IOException e){}
            }
         }



private void callMenu() throws IOException {
   //로그인 체크 영역
   while(true) {
      System.out.print("1. 로그인, 2.회원가입, 3.종료: ");
      try {
         int no = Integer.parseInt(br.readLine());
         if(no==1) {
            //로그인
            
         }else if(no == 2) {
            //회원가입
         }else if(no == 3) {
            //종료
            System.out.println("프로그램을 종료합니다.");
            break;
         }else {
            System.out.println("잘못된 입력입니다.");
         }
      } catch (NumberFormatException e) {
         System.out.println("[ 숫자만 입력 가능합니다. ]");
      }
      
   }
   //로그인 성공 후 회원제 서비스 영역
   while(flag) {
      System.out.print("1. 상품목록, 2. 회원 정보 조회 , 3. 도서 신청 게시판 ,4. 종료: ");
      try {
         int no = Integer.parseInt(br.readLine());
         if(no==1) {
            //로그인
         }else if(no == 2) {
            //회원 정보 조회
         }else if(no == 3) {
        	 //도서 신청 게시판
         }else if(no == 4) {
            //종료
            System.out.println("프로그램을 종료합니다.");
            break;
         }else {
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
