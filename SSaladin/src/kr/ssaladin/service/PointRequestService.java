package kr.ssaladin.service;

import kr.ssaladin.dao.PointRequestDAO;

public class PointRequestService {
	private PointRequestDAO pointRequestDAO;

	public PointRequestService() {
		this.pointRequestDAO = new PointRequestDAO();
	}

	public boolean requestCharge(String userId, int pointAmount) {
		// 포인트 충전 요청 생성
		if (pointAmount < 1000) {
			System.out.println("충전 금액은 최소 1,000원 이상이어야 합니다.");
			return false;
		}
		return pointRequestDAO.createRequest(userId, pointAmount);
	}

//	// 포인트 요청 상태 업데이트 (예시)
//	public boolean updateRequestStatus(int requestNum, int newStatus) {
//		// 상태 업데이트 로직 추가 (예: 충전 요청 처리 완료)
//		return pointRequestDAO.updateRequestStatus(requestNum, newStatus);
//	}
}