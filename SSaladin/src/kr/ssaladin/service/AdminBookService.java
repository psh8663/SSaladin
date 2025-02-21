package kr.ssaladin.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import kr.ssaladin.dao.AdminBookDAO;


public class AdminBookService {
	private BufferedReader br;
	private AdminBookDAO dao;
	
	public AdminBookService() {
		try {
			br = new BufferedReader(
					new InputStreamReader(System.in));
			dao = new AdminBookDAO();
			adminbookmanage();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			//자원정리
			if(br!=null)try {br.close();}catch(IOException e) {}
		}
	}

	private void adminbookmanage() throws IOException{
		while (true) {
	        System.out.print("1.카테고리 추가, 6.종료> ");
	        try {
	            int no = Integer.parseInt(br.readLine());
	            if (no == 1) {
	                System.out.print("추가할 카테고리명 입력: ");
	                String categoryName = br.readLine();
	                int result = dao.insertCategory(categoryName);
	                if (result == 1) {
	                    System.out.println("카테고리가 정상적으로 추가되었습니다.");
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
