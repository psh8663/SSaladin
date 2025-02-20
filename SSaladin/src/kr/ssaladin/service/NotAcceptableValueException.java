package kr.ssaladin.service;

public class NotAcceptableValueException extends Exception {
	
	public NotAcceptableValueException(String str) { // str 문구를 Exception 으로 넘겨서 예외 처리
		super(str);
	}
	
}
