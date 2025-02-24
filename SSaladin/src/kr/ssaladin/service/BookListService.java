package kr.ssaladin.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

import kr.ssaladin.SSaladinMain;
import kr.ssaladin.dao.BookListDAO;
import kr.ssaladin.dao.ReviewsDAO;

public class BookListService {
    private BufferedReader br;
    private BookListDAO dao;
    private String userId;  // 현재 로그인한 사용자 ID 저장
    private ReviewsDAO rDAO;

    public BookListService(SSaladinMain sSaladinMain) {
        try {
            this.userId = sSaladinMain.getUserId();  // 로그인한 사용자 ID 가져오기
            br = new BufferedReader(new InputStreamReader(System.in));
            dao = new BookListDAO();
            rDAO = new ReviewsDAO();
            booklist();
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    // 메뉴
    private void booklist() throws IOException, ClassNotFoundException, SQLException {
        while (true) {
            System.out.print("1.전체 도서 목록 조회 2.제목 검색 조회 3.카테고리별 조회 4.베스트셀러 조회 6.뒤로가기> ");
            try {
                int no = Integer.parseInt(br.readLine());
                if (no == 1) {
                    // 전체 도서 목록 조회
                    dao.selectBook();
                    System.out.print("도서코드 선택: ");
                    int num = Integer.parseInt(br.readLine());
                    showBookDetails(num);
                } else if (no == 2) {
                    // 도서 제목 검색 기능
                    System.out.print("검색할 도서 제목 입력: ");
                    String title = br.readLine();
                    dao.selectBookByTitle(title);
                    System.out.print("도서코드 선택: ");
                    int num = Integer.parseInt(br.readLine());
                    showBookDetails(num);
                } else if (no == 3) {
                    // 카테고리별 도서 검색
                    dao.selectCategories(); // 카테고리 목록 출력
                    System.out.print("조회할 카테고리 번호 선택: ");
                    int categoryNum = Integer.parseInt(br.readLine());
                    dao.selectBooksByCategory(categoryNum); // 해당 카테고리의 도서 출력
                    System.out.print("도서코드 선택: ");
                    int num = Integer.parseInt(br.readLine());
                    showBookDetails(num);
                } else if (no == 4) {
                    // 베스트셀러 도서 조회
                    dao.selectBestSeller(); // 베스트셀러 도서 조회
                    System.out.print("도서코드 선택: ");
                    int num = Integer.parseInt(br.readLine());
                    showBookDetails(num);
                } else if (no == 6) {
                    break;
                } else {
                    System.out.println("잘못 입력했습니다.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[숫자만 입력 가능]");
            }
        }
    }


    // 도서 상세 정보 조회 및 장바구니 기능
    private void showBookDetails(int num) throws IOException {
        int count = dao.checkBCode(num);
        if (count == 1) {
            dao.selectDetailBook(num); // 상세 정보 출력
            while (true) {
                System.out.print("1. 장바구니에 담기 2. 리뷰 보기 3.메뉴로 돌아가기 > ");
                int option = Integer.parseInt(br.readLine());
                if (option == 1) {
                	
                	try {
						// Cartservice 객체 생성
                		CartService cartservice = new CartService();
                		
                		// 장바구니에 해당 도서 추가 ( 도서 담는 초기 수량은 1로 설정 )
                		boolean result = cartservice.addToCart(userId, null, num, 1);
                		
                		if(result) {
                		       // 로그인한 사용자 ID를 활용하여 장바구니 추가
                            System.out.println(userId + "님의 장바구니에 도서를 담았습니다.");
                		}else {System.out.println("상품 담기에 실패했습니다.");}
					} catch (Exception e) {
						System.out.println("장바구니 담기 중 오류가 발생했습니다." + e.getMessage());
						e.printStackTrace();
					}
                } else if (option == 2) {
                    // 리뷰 보기
                    System.out.println("리뷰를 확인합니다.");
                    rDAO.detailSelectRivews(num);
                } else if (option == 3) {
                    break; // 메뉴로 돌아가기
                } else {
                    System.out.println("잘못 입력했습니다. 다시 선택하세요.");
                }
            }
        } else if (count == 0) {
            System.out.println("번호를 잘못 입력했습니다.");
        } else {
            System.out.println("정보 처리 중 오류 발생");
        }
    }
}
