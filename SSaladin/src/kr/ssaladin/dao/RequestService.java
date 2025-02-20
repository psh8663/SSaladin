package kr.ssaladin.dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RequestService {
	
	private BufferedReader br;
	private RequestDAO dao;
	
	public void requestService() {
		
		dao = new RequestDAO();
		br = new BufferedReader(new InputStreamReader(System.in));
		
		while (true) {
			System.out.println("1. 도서 요청 목록, 2. 도서 요청 작성, 3. 도서 요청 수정, 4. 도서 요청 삭제");
		} // while
		
	}

}
