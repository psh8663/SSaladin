package kr.ssaladin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import kr.ssaladin.model.PointRequest;
import kr.util.DBUtil;

public class PointRequestDAO {

	// 포인트 충전요청 생성
	public boolean createRequest(String userId, int pointAmount) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean flag = false;

		String sql = "INSERT INTO point_requests " + "(request_num, user_id, point_amount, request_status) "
				+ "VALUES (seq_request_num.NEXTVAL, ?, ?, 1)";

		try {

			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.setInt(2, pointAmount);

			int result = pstmt.executeUpdate();
			if (result > 0) {
				flag = true;
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
		return flag;
	}

	// 포인트 충전요청 조회 (관리자)
	public List<PointRequest> getAllPointRequests() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<PointRequest> requests = new ArrayList<>();

		String sql = "SELECT request_num, user_id, point_amount, request_status, request_date "
				+ "FROM point_requests ORDER BY request_date ASC";

		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				PointRequest request = new PointRequest();
				request.setRequestNum(rs.getInt("request_num"));
				request.setUserId(rs.getString("user_id"));
				request.setPointAmount(rs.getInt("point_amount"));
				request.setRequestStatus(rs.getInt("request_status"));
				request.setRequestDate(rs.getTimestamp("request_date"));
				requests.add(request);
			}
		} catch (SQLException | ClassNotFoundException e) {

		} finally {

		}

		return requests;
	}
}
//
//	// 사용자: 내 포인트 충전 요청 내역 조회
//	public List<PointRequest> getUserPointRequests(String userId) {
//		Connection conn = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//		List<PointRequest> requests = new ArrayList<>();
//
//		String sql = "SELECT request_num, user_id, point_amount, request_status, "
//				+ "point_used, request_date FROM point_requests " + "WHERE user_id = ? ORDER BY request_date DESC";
//
//		try {
//			conn = DBUtil.getConnection();
//			pstmt = conn.prepareStatement(sql);
//			pstmt.setString(1, userId);
//			rs = pstmt.executeQuery();
//
//			while (rs.next()) {
//				PointRequest request = new PointRequest();
//				request.setRequestNum(rs.getInt("request_num"));
//				request.setUserId(rs.getString("user_id"));
//				request.setPointAmount(rs.getInt("point_amount"));
//				request.setRequestStatus(rs.getInt("request_status"));
//				request.setPointUsed(rs.getInt("point_used"));
//				request.setRequestDate(rs.getDate("request_date"));
//
//				requests.add(request);
//			}
//		} catch (SQLException | ClassNotFoundException e) {
//			e.printStackTrace();
//		} finally {
//			DBUtil.executeClose(rs, pstmt, conn);
//		}
//
//		return requests;
//	}
//
//	// 관리자: 포인트 요청 상태 변경 및 승인 처리
//	public boolean updatePointRequestStatus(int requestNum, int newStatus) {
//		Connection conn = null;
//		PreparedStatement pstmt = null;
//
//		String sql = "UPDATE point_requests SET request_status = ? WHERE request_num = ?";
//
//		try {
//			conn = DBUtil.getConnection();
//			// 트랜잭션 시작
//			conn.setAutoCommit(false);
//
//			pstmt = conn.prepareStatement(sql);
//			pstmt.setInt(1, newStatus);
//			pstmt.setInt(2, requestNum);
//
//			int result = pstmt.executeUpdate();
//
//			// 승인(2)인 경우 사용자 포인트 증가 처리
//			if (newStatus == 2 && result > 0) {
//				if (!addPointsToUser(requestNum, conn)) {
//					conn.rollback();
//					return false;
//				}
//			}
//
//			conn.commit();
//			return result > 0;
//		} catch (SQLException | ClassNotFoundException e) {
//			try {
//				if (conn != null) {
//					conn.rollback();
//				}
//			} catch (SQLException ex) {
//				ex.printStackTrace();
//			}
//			e.printStackTrace();
//			return false;
//		} finally {
//			try {
//				if (conn != null) {
//					conn.setAutoCommit(true);
//				}
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//			DBUtil.executeClose(null, pstmt, conn);
//		}
//	}
//
//	// 포인트 요청 승인 시 사용자 포인트 증가 처리
//	private boolean addPointsToUser(int requestNum, Connection conn) throws SQLException {
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//
//		try {
//			// 1. 요청 정보 조회
//			String getRequestSql = "SELECT user_id, point_amount FROM point_requests "
//					+ "WHERE request_num = ? AND request_status = 2";
//			pstmt = conn.prepareStatement(getRequestSql);
//			pstmt.setInt(1, requestNum);
//			rs = pstmt.executeQuery();
//
//			if (!rs.next()) {
//				return false;
//			}
//
//			String userId = rs.getString("user_id");
//			int pointAmount = rs.getInt("point_amount");
//
//			rs.close();
//			pstmt.close();
//
//			// 2. 사용자 포인트 업데이트
//			String updateUserSql = "UPDATE users SET user_point = user_point + ? WHERE user_id = ?";
//			pstmt = conn.prepareStatement(updateUserSql);
//			pstmt.setInt(1, pointAmount);
//			pstmt.setString(2, userId);
//
//			return pstmt.executeUpdate() > 0;
//		} finally {
//			if (rs != null) {
//				rs.close();
//			}
//			if (pstmt != null) {
//				pstmt.close();
//			}
//		}
//	}
//
//	// 포인트 요청 상세 조회
//	public PointRequest getPointRequest(int requestNum) {
//		Connection conn = null;
//		PreparedStatement pstmt = null;
//		ResultSet rs = null;
//		PointRequest request = null;
//
//		String sql = "SELECT r.request_num, r.user_id, u.user_name, r.point_amount, "
//				+ "r.request_status, r.point_used, r.request_date "
//				+ "FROM point_requests r JOIN users u ON r.user_id = u.user_id " + "WHERE r.request_num = ?";
//
//		try {
//			conn = DBUtil.getConnection();
//			pstmt = conn.prepareStatement(sql);
//			pstmt.setInt(1, requestNum);
//			rs = pstmt.executeQuery();
//
//			if (rs.next()) {
//				request = new PointRequest();
//				request.setRequestNum(rs.getInt("request_num"));
//				request.setUserId(rs.getString("user_id"));
//				request.setUserName(rs.getString("user_name"));
//				request.setPointAmount(rs.getInt("point_amount"));
//				request.setRequestStatus(rs.getInt("request_status"));
//				request.setPointUsed(rs.getInt("point_used"));
//				request.setRequestDate(rs.getDate("request_date"));
//			}
//		} catch (SQLException | ClassNotFoundException e) {
//			e.printStackTrace();
//		} finally {
//			DBUtil.executeClose(rs, pstmt, conn);
//		}
//
//		return request;
//	}
//}
