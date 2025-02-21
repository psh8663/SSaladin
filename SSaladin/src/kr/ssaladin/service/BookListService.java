package kr.ssaladin.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.ssaladin.dao.BookListDAO;

public class BookListService {
    private BufferedReader br;
    private BookListDAO dao;

    public static void main(String[] args) {
        new BookListService();
    }

    public BookListService() {
        try {
            br = new BufferedReader(new InputStreamReader(System.in));
            dao = new BookListDAO();
            booklist();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                }
        }
    }

    // 메뉴
    private void booklist() throws IOException {
        while (true) {
            System.out.print("1.전체 도서 목록 조회 2.도서 검색 조회 3.카테고리별 도서 조회 6.종료>");
            try {
                int no = Integer.parseInt(br.readLine());
                if (no == 1) {
                    // 전체 도서 목록 조회
                    dao.selectBook();
                    System.out.print("도서코드 선택:");
                    int num = Integer.parseInt(br.readLine());
                    showBookDetails(num); // 모듈화된 메서드 호출
                } else if (no == 2) {
                    // 도서 제목 검색 기능
                    System.out.print("검색할 도서 제목 입력: ");
                    String title = br.readLine();
                    dao.selectBookByTitle(title);
                    System.out.print("도서코드 선택:");
                    int num = Integer.parseInt(br.readLine());
                    showBookDetails(num); // 모듈화된 메서드 호출
                } else if (no == 3) {
                    // 카테고리별 도서 검색
                    dao.selectCategories(); // 카테고리 목록 출력
                    System.out.print("조회할 카테고리 번호 선택: ");
                    int categoryNum = Integer.parseInt(br.readLine());
                    dao.selectBooksByCategory(categoryNum); // 해당 카테고리의 도서 출력
                } else if (no == 6) {
                    // 종료
                    System.out.println("프로그램 종료");
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
                System.out.print("1. 장바구니에 담기 2.리뷰 보기 3.메뉴로 돌아가기 >");
                int option = Integer.parseInt(br.readLine());
                if (option == 1) {
                    // 장바구니 추가 기능 (dao에 메서드 추가 필요)
                    System.out.println("장바구니에 담았습니다.");
                } else if (option == 2) {
                    //리뷰보기
                	System.out.println("리뷰를 확인합니다.");
                } else if (option == 3) {
                    break; // 메뉴로 돌아가기
                }else {
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
