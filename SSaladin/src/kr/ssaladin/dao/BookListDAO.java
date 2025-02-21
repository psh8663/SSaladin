package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import kr.util.DBUtil;


public class BookListDAO {
	//전체 도서 목록 보기
	public void selectBook() {
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    String sql = null;
	    try {
	        conn = DBUtil.getConnection();
	        sql = "SELECT book_code, book_title, '(' || book_author || ')', CONCAT(book_price, '원') AS book_price FROM BOOKS";
	        pstmt = conn.prepareStatement(sql);
	        rs = pstmt.executeQuery();
	        System.out.println("------------------");
	        if(rs.next()) {
	            System.out.println("도서코드\t도서명\t저자명\t\t\t\t가격");
	            do {
	            	System.out.print(rs.getInt(1));  // book_code
	                System.out.print("\t");
	                System.out.print(rs.getString(2));  // book_title
	                System.out.print(rs.getString(3));  // book_author
	                System.out.print("\t\t\t\t");
	                System.out.println(rs.getString(4));  // book_price
	            } while(rs.next());
	        } else {
	            System.out.println("등록된 도서가 없습니다.");
	        }
	        System.out.println("------------------");
	    } catch(Exception e) {
	        e.printStackTrace();
	    } finally {
	        DBUtil.executeClose(rs, pstmt, conn);
	    }
	}
	//조회하는 도서코드가 존재하는지 여부 체크
		public int checkBCode(int num) {
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			String sql = null;
			int count = 0;
			try {
				conn = DBUtil.getConnection();
				sql = "SELECT * FROM books WHERE book_code=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, num);
				rs = pstmt.executeQuery();
				if(rs.next()) {
					count = 1;//레코드가 존재할 때 1 저장
				}
			}catch(Exception e) {
				count = -1; //오류 발생
			}finally {
				DBUtil.executeClose(rs, pstmt, conn);
			}	
			return count;
		}
		//책 상세보기
		public void selectDetailBook(int num) {
			Connection conn = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			String sql = null;
			try {
				conn = DBUtil.getConnection();
				sql = "SELECT * FROM books b, categories c WHERE b.category_num=c.category_num and book_code=? ";
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, num);
				rs = pstmt.executeQuery();
				if(rs.next()) {
					System.out.println("도서코드 : " + rs.getInt("book_code"));
					System.out.println("카테고리명 : " + rs.getString("category_name"));
					System.out.println("도서명 : " + rs.getString("book_title"));
					System.out.println("저자명 : " + rs.getString("book_author"));
					System.out.println("가격 : " + rs.getInt("book_price"));
					System.out.println("출판사 : " + rs.getString("book_publisher"));
					System.out.println("설명 : " + rs.getString("book_description"));
					System.out.println("상품상태(0:품절, 1:판매중, 2:판매중지): " + rs.getInt("book_status"));
					System.out.println("평균평점 : " + rs.getFloat("rating_avg")); //수정요함(avg)
					System.out.println("등록일 : " + rs.getDate("book_reg_date"));
					
				}else {
					System.out.println("검색된 정보가 없습니다.");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				//자원정리
				DBUtil.executeClose(rs, pstmt, conn);
			}
		}
		public void selectBookByTitle(String title) {
		    Connection conn = null;
		    PreparedStatement pstmt = null;
		    ResultSet rs = null;
		    String sql = "SELECT * FROM books b JOIN categories c ON b.category_num = c.category_num WHERE b.book_title LIKE ?";
		    
		    try {
		        conn = DBUtil.getConnection();
		        pstmt = conn.prepareStatement(sql);
		        pstmt.setString(1, "%" + title + "%"); // 부분 검색
		        rs = pstmt.executeQuery();

		        boolean found = false;
		        while (rs.next()) {
		            found = true;
		            System.out.println("도서코드 : " + rs.getInt("book_code"));
		            System.out.println("카테고리명 : " + rs.getString("category_name"));
		            System.out.println("도서명 : " + rs.getString("book_title"));
		            System.out.println("저자명 : " + rs.getString("book_author"));
		            System.out.println("가격 : " + rs.getInt("book_price"));
//		            System.out.println("출판사 : " + rs.getString("book_publisher"));
//		            System.out.println("설명 : " + rs.getString("book_description"));
//		            System.out.println("상품상태(0:품절, 1:판매중, 2:판매중지): " + rs.getInt("book_status"));
//		            System.out.println("평균평점 : " + rs.getFloat("rating_avg"));
//		            System.out.println("등록일 : " + rs.getDate("book_reg_date"));
		            System.out.println("------------------------------------------------");
		        }

		        if (!found) {
		            System.out.println("검색된 도서가 없습니다.");
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		    } finally {
		        DBUtil.executeClose(rs, pstmt, conn);
		    }
		}
		// BookListDAO.java
		// 1. 모든 카테고리를 조회하는 메서드
		public void selectCategories() {
		    Connection conn = null;
		    PreparedStatement pstmt = null;
		    ResultSet rs = null;
		    String sql = "SELECT category_num, category_name FROM categories";
		    
		    try {
		        conn = DBUtil.getConnection();
		        pstmt = conn.prepareStatement(sql);
		        rs = pstmt.executeQuery();

		        System.out.println("=== 카테고리 목록 ===");
		        while (rs.next()) {
		            System.out.println(rs.getInt("category_num") + ". " + rs.getString("category_name"));
		        }
		        System.out.println("===================");

		    } catch (Exception e) {
		        e.printStackTrace();
		    } finally {
		        DBUtil.executeClose(rs, pstmt, conn);
		    }
		}

		// 2. 선택한 카테고리에 속한 책들을 조회하는 메서드
		public void selectBooksByCategory(int categoryNum) {
		    Connection conn = null;
		    PreparedStatement pstmt = null;
		    ResultSet rs = null;
		    String sql = "SELECT c.category_name, b.book_code, b.book_title, b.book_author, b.book_price " +
	                 "FROM books b JOIN categories c ON b.category_num = c.category_num " +
	                 "WHERE b.category_num = ?";

		    try {
		        conn = DBUtil.getConnection();
		        pstmt = conn.prepareStatement(sql);
		        pstmt.setInt(1, categoryNum);
		        rs = pstmt.executeQuery();

		        boolean found = false;
		        String categoryName = null;

		        while (rs.next()) {
		            if (categoryName == null) { // 첫 번째 결과에서 카테고리 이름을 가져옴
		                categoryName = rs.getString("category_name");
		                System.out.println("=== " + categoryName + " 카테고리의 도서 목록 ===");
		            }
		            found = true;
		            System.out.println("도서코드: " + rs.getInt("book_code"));
		            System.out.println("도서명: " + rs.getString("book_title"));
		            System.out.println("저자명: " + rs.getString("book_author"));
		            System.out.println("가격: " + rs.getInt("book_price"));
		            System.out.println("----------------------------------");
		        }

		        if (!found) {
		            System.out.println("해당 카테고리에 등록된 도서가 없습니다.");
		        }

		    } catch (Exception e) {
		        e.printStackTrace();
		    } finally {
		        DBUtil.executeClose(rs, pstmt, conn);
		    }
		}



}
