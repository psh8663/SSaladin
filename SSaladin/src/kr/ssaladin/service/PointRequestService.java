package kr.ssaladin.service;

import java.util.List;

import kr.ssaladin.dao.PointRequestDAO;
import kr.ssaladin.model.PointRequest;

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
	
	public List<PointRequest> getAllRequests() {
        return pointRequestDAO.getAllPointRequests();
    }

    // 요청 상태를 문자열로 변환하는 유틸리티 메소드
    public String getStatusString(int status) {
        switch(status) {
            case 1: return "대기중";
            case 2: return "승인됨";
            case 3: return "거절됨";
            default: return "알 수 없음";
        }
    }
}

//	// 포인트 요청 상태 업데이트 (예시)
//	public boolean updateRequestStatus(int requestNum, int newStatus) {
//		// 상태 업데이트 로직 추가 (예: 충전 요청 처리 완료)
//		return pointRequestDAO.updateRequestStatus(requestNum, newStatus);
//	}
