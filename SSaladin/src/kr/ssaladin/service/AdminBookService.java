package kr.ssaladin.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.ssaladin.dao.AdminBookDAO;
import kr.ssaladin.dao.BookListDAO;

public class AdminBookService {
	private BufferedReader br;
	private AdminBookDAO dao;
	private BookListDAO blDao;

	public AdminBookService() {
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			dao = new AdminBookDAO();
			blDao = new BookListDAO();
			adminBookManage();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 자원정리
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
				}
		}
	}

	private void adminBookManage() throws IOException, ClassNotFoundException {
		while (true) {
			System.out.print("1.신규 도서 추가, 2.신규 카테고리 추가, 6.종료> ");
			try {
				int no = Integer.parseInt(br.readLine());

				if (no == 1) { // 신규 도서 추가 기능
					blDao.selectCategories();
					System.out.print("카테고리 번호 입력: ");
					int categoryNum = Integer.parseInt(br.readLine());

					System.out.print("도서 제목 입력: ");
					String bookTitle = br.readLine();

					System.out.print("저자 입력: ");
					String bookAuthor = br.readLine();

					System.out.print("가격 입력: ");
					int bookPrice = Integer.parseInt(br.readLine());

					System.out.print("출판사 입력: ");
					String bookPublisher = br.readLine();

					System.out.print("도서 설명 입력: ");
					String bookDescription = br.readLine();

					System.out.print("재고 입력: ");
					int bookStock = Integer.parseInt(br.readLine());

					System.out.print("도서 상태 입력 (1: 판매중, 0: 품절): ");
					int bookStatus = Integer.parseInt(br.readLine());

					int result = dao.insertBook(categoryNum, bookTitle, bookAuthor, bookPrice, bookPublisher,
							bookDescription, bookStock, bookStatus);

					if (result == 1) {
						System.out.println("도서가 정상적으로 추가되었습니다.");
						blDao.selectBook();
					} else {
						System.out.println("도서 추가 실패!");
					}

				} else if (no == 2) { // 신규 카테고리 추가
					System.out.print("추가할 카테고리명 입력: ");
					String categoryName = br.readLine();
					int result = dao.insertCategory(categoryName);

					if (result == 1) {
						System.out.println("카테고리가 정상적으로 추가되었습니다.");
						blDao.selectCategories();
						
					}

				} else if (no == 6) { 
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

	public static void main(String[] args) {
		new AdminBookService();
	}
}
