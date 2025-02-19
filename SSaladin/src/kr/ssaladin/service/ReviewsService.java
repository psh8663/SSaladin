package kr.ssaladin.service;

import java.io.BufferedReader;
import java.io.IOException;

import kr.ssaladin.dao.ReviewsDAO;

public class ReviewsService {
	
	private BufferedReader br;
	private ReviewsDAO dao;
	
	// 
	public void udtReview() {
		System.out.println("수정할 리뷰 글의 번호 : ");
	}
	
	// 평점 범위 벗어난 경우 다시 입력하게 하는 함수
	public int parseInputRating(String item) throws IOException{
		
		while (true) {
			System.out.println(item);
			try {
				
			} catch (NumberFormatException e) {
				System.out.println("1~5 숫자만 입력하세요");
			} /*catch (NotAcceptableValueException e) {
				System.out.println(e.getMessage());
			}*/ // try_catch
		} // while
	}
	

}
