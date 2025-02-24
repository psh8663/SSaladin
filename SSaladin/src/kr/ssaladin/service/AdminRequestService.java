package kr.ssaladin.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.ssaladin.dao.RequestDAO;

public class AdminRequestService {
	
	private BufferedReader br;
	private RequestDAO dao;
	
	public void requestService(String userId) throws IOException {
		
		dao = new RequestDAO();
		br = new BufferedReader(new InputStreamReader(System.in));
		int requestNum;
		
		while (true) {
			System.out.println("1. 도서 요청 목록, 2. 도서 요청 삭제, 3. 이전으로 돌아가기");
			try {
				int no = Integer.parseInt(br.readLine());
				
				if (no == 1) { // 요청 목록
					
					dao.selectRequest();
					
				} else if (no == 2) { // 요청 삭제
					dao.selectRequest();
					System.out.print("삭제할 요청글 번호 : ");
					requestNum = Integer.parseInt(br.readLine());
					int count = dao.checkRequest(requestNum);
					
					if (count == 1) {
						dao.deleteRequest(userId, requestNum);
					} else if (count == 0) {
						System.out.println("번호를 잘못 입력했습니다.");
					} else {
						System.out.println("정보 처리 중 오류 발생");
					} // if
				} else if (no == 3) { // 이전으로
					break;
				} // if
			} catch (NumberFormatException e) {
				System.out.println("[숫자만 입력 가능]");
			}
		} // while
		
	}

}
















