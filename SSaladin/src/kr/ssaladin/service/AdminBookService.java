package kr.ssaladin.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

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
		}
	}

	private void adminBookManage() throws IOException, ClassNotFoundException, SQLException {
		while (true) {
			System.out.print("1.신규 도서 추가, 2.신규 카테고리 추가, 3.도서 재고 추가, 4.도서 설명 변경, 5.도서 판매중지 처리, 6.뒤로가기> ");
			try {
				int no = Integer.parseInt(br.readLine());

				if (no == 1) { // 신규 도서 추가
					blDao.selectCategories();
					int categoryNum;
					while (true) {
		                try {
		                    System.out.print("카테고리 번호 입력: ");
		                    categoryNum = Integer.parseInt(br.readLine());
		                    
		                    if (dao.checkCategory(categoryNum)) { 
		                        break;
		                    } else {
		                        System.out.println("존재하지 않는 카테고리입니다. 다시 입력하세요.");
		                    }
		                } catch (NumberFormatException e) {
		                    System.out.println("잘못된 입력입니다. 다시 입력하세요.");
		                }
		            }

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

					System.out.print("도서 상태 입력 (1: 판매중, 0: 품절, 2: 판매중지(비공개)): ");
					int bookStatus = Integer.parseInt(br.readLine());

					int result = dao.insertBook(categoryNum, bookTitle, bookAuthor, bookPrice, bookPublisher,
							bookDescription, bookStock, bookStatus);

					if (result == 1) {
						System.out.println("도서가 정상적으로 추가되었습니다.");
						dao.selectAdminBook();
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

				} else if (no == 3) {
					// 재고 추가
					dao.selectAdminBook();

					System.out.print("재고 추가할 도서 정보의 관리 번호:");
					int num =Integer.parseInt(br.readLine());
					int count = blDao.checkBCode(num);

					if (count == 1) {

						blDao.selectDetailBook(num);

						System.out.print("추가 재고:");
						int book_stock = Integer.parseInt(br.readLine());

						dao.adminUpdateStock(num, book_stock);
						System.out.println("재고가 추가되었습니다.");
						dao.updateBookStatus(num);
						dao.selectAdminBook();
						
					} else if (count == 0) {
						System.out.println("도서코드를 잘못 입력하셨습니다. 판매중지된 상품은");
					} else {
						System.out.println("정보 처리 중 오류 발생");
					}

				} else if (no == 4) {
					// 정보수정
					dao.selectAdminBook();

					System.out.print("수정할 도서 정보의 관리 번호:");
					int num = Integer.parseInt(br.readLine());
					int count = dao.checkadminBCode(num);
					if (count == 1) {

						blDao.selectDetailBook(num);

						System.out.print("도서설명:");
						String book_description = br.readLine();

						dao.updateBookDescription(num, book_description);
						System.out.println("도서 설명이 변경됐습니다.");
					} else if (count == 0) {
						System.out.println("도서코드를 잘못 입력하셨습니다.");
					} else {
						System.out.println("정보 처리 중 오류 발생");
					}

				} else if (no == 5) {
					// 정보수정
					blDao.selectBook();
					System.out.print("판매중지할 도서 정보의 관리 번호:");
					int num = Integer.parseInt(br.readLine());
					int count = blDao.checkBCode(num);
					if (count == 1) {
						dao.updateOutOfPrintStatus(num);
						System.out.println("판매중지 처리가 완료되었습니다.");
					} else if (count == 0) {
						System.out.println("도서코드를 잘못 입력하셨습니다.");
					} else {
						System.out.println("정보 처리 중 오류 발생");
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
