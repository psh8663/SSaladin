package kr.ssaladin.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.ssaladin.dao.BookListDAO;

public class BooksService {
	private BufferedReader br;
	private BookListDAO dao;

	public static void main(String[] args) {
		new BooksService();
	}

	public BooksService() {
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
			System.out.print("1.전체 도서 목록 보기, 2.도서 상세페이지 6.종료>");
			try {
				int no = Integer.parseInt(br.readLine());
				if (no == 1) {
					// 전체 책목록 보기
					dao.selectBook();
				} else if (no == 2) {
					// 상세정보
					dao.selectBook();
					System.out.print("도서코드 선택:");
					int num = Integer.parseInt(br.readLine());
					int count = dao.checkBCode(num);
					if (count == 1) {
						dao.selectDetailBook(num);
					} else if (count == 0) {
						System.out.println("번호를 잘못 입력했습니다.");
					} else {
						System.out.println("정보 처리 중 오류 발생");
					}
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
}