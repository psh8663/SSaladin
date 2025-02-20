package kr.ssaladin.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.ssaladin.dao.ReviewsDAO;

public class ReviewsService {

	private BufferedReader br;
	private ReviewsDAO dao;

	public void reiviewService(String userId, int reviewNum ) throws IOException {
		dao = new ReviewsDAO();
		br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("1.리뷰 목록 보기, 2.리뷰 작성, 3. 리뷰 수정, 4. 리뷰 삭제>");
		try {
			int no = Integer.parseInt(br.readLine());

			if (no == 1) {
				dao.selectReviews();

			} else if (no == 2) {

				System.out.println("제목 : ");
				String bookTitle = br.readLine();

				System.out.println("내용 : ");
				String reviewsContent = br.readLine();

				int rating = parseInputRating("평점 : ");
				dao.insertReviews(userId, bookTitle, reviewsContent, rating);

			} else if (no == 3) {
				dao.selectReviews();
				System.out.println("수정할 리뷰글의 번호 : ");
				reviewNum = Integer.parseInt(br.readLine());
				int count = dao.checkReviews(reviewNum);

				if (count == 1) {
					System.out.println("내용 : ");
					String reviewsContent = br.readLine();

					int rating = parseInputRating("평점 : ");

					dao.updateReviews(userId, reviewNum, reviewsContent, rating);
				} else if (count==0) {
					System.out.println("번호를 잘못 입력했습니다.");
				} else {
					System.out.println("정보 처리 중 오류 발생");
				}// if

			} else if (no == 4) {
				dao.selectReviews();
				System.out.println("삭제할 리뷰의 번호 : ");
				int num = Integer.parseInt(br.readLine());
				int count = dao.checkReviews(reviewNum);
				
				if (count == 1) {
					dao.deleteReviews(userId, reviewNum);
				} else if (count == 0) {
					System.out.println("번호를 잘못 입력했습니다.");
				} else {
					System.out.println("정보 처리 중 오류 발생");
				} // if
			} // if
		} catch (NumberFormatException e) {
			System.out.println("[숫자만 입력 가능]");
		}


	}

	// 평점 범위 벗어난 경우 다시 입력하게 하는 함수
	public int parseInputRating(String item) throws IOException{

		while (true) {
			System.out.println(item);
			try {
				int num = Integer.parseInt(br.readLine());
				if (num<0 || num>5) {
					throw new NotAcceptableValueException("0~5 숫자만 입력하세요");
				} // if
			} catch (NumberFormatException e) {
				System.out.println("0~5 숫자만 입력하세요");
			} catch (NotAcceptableValueException e) {
				System.out.println(e.getMessage());
			}// try_catch
		} // while
	}


}
