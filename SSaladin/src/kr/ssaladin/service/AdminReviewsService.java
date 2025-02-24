package kr.ssaladin.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import kr.ssaladin.dao.BookListDAO;
import kr.ssaladin.dao.Order_detailsDAO;
import kr.ssaladin.dao.OrdersDAO;
import kr.ssaladin.dao.ReviewsDAO;
import kr.util.DBUtil;

public class AdminReviewsService {

	private BufferedReader br;
	private ReviewsDAO dao;
	private Order_detailsDAO odDao;

	public void aReviewService(String userId) throws IOException {
		
		dao = new ReviewsDAO();
		br = new BufferedReader(new InputStreamReader(System.in));
		
		int reviewNum;
		int bookCode;
		
		while(true) {
			System.out.print("1.리뷰 보기, 2. 리뷰 삭제, 3. 이전으로 돌아가기>");
			try {
				int no = Integer.parseInt(br.readLine());

				if (no == 1) { // 리뷰 보기
					dao.selectReviews();
					System.out.println("확인 할 리뷰 번호 : ");
					reviewNum = Integer.parseInt(br.readLine());
					dao.detailSelectRivews_reviewNum(reviewNum);
					
				} else if (no == 2) { // 리뷰 삭제
					dao.selectReviews_userId(userId);
					System.out.print("삭제할 리뷰의 번호 : ");
					reviewNum = Integer.parseInt(br.readLine());
					int count = dao.checkReviews(reviewNum);

					if (count == 1) {
						dao.deleteReviews(userId, reviewNum);
					} else if (count == 0) {
						System.out.println("번호를 잘못 입력했습니다.");
					} else {
						System.out.println("정보 처리 중 오류 발생");
					} // if
				} else if (no == 3) { // 상세정보로 돌아가기
					break;
				} // if
			} catch (NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
			}
		} // while

	}

	// 평점 범위 벗어난 경우 다시 입력하게 하는 함수
	public int parseInputRating(String item) throws IOException{

		while (true) {
			System.out.println(item);
			try {
				int rating = Integer.parseInt(br.readLine());
				if (rating<0 || rating>5) {
					throw new NotAcceptableValueException("0~5 숫자만 입력하세요");
				} // if
				return rating;
			} catch (NumberFormatException e) {
				System.out.println("0~5 숫자만 입력하세요");
			} catch (NotAcceptableValueException e) {
				System.out.println(e.getMessage());
			}// try_catch
		} // while
	}
	



}
