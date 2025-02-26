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
			System.out.println("포인트 충전요청 생성시 오류가 발생 했습니다.");
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
		return flag;
	}

	// 포인트 요청 상태 변경 및 승인 처리 (관리자)
	public boolean updateRequestStatus(int requestNum, int newStatus) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean flag = false;

		String sql = "UPDATE point_requests SET request_status = ? WHERE request_num = ?";

		try {
			conn = DBUtil.getConnection();
			conn.setAutoCommit(false); // 트랜잭션 시작

			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, newStatus);
			pstmt.setInt(2, requestNum);

			int result = pstmt.executeUpdate();

			// 승인(상태값 2)인 경우 사용자 포인트 증가 처리
			if (newStatus == 2 && result > 0) {
				if (addPointsToUser(requestNum, conn)) {
					flag = true;
					conn.commit();
				} else {
					conn.rollback();
				}
			} else if (result > 0) {
				flag = true;
				conn.commit();
			}
		} catch (SQLException | ClassNotFoundException e) {
			try {
				if (conn != null) {
					conn.rollback();
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) {
					conn.setAutoCommit(true); // 트랜잭션 설정 복구
				}
			} catch (SQLException e) {
				System.out.println("포인트 요청 승인시 오류가 발생했습니다.");
				e.printStackTrace();
			}
			DBUtil.executeClose(null, pstmt, conn);
		}
		return flag;
	}

	// 포인트 요청 승인 시 사용자 포인트 증가 처리
	private boolean addPointsToUser(int requestNum, Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean flag = false;

		String selectsql = "SELECT user_id, point_amount FROM point_requests WHERE request_num = ?";

		try {
			pstmt = conn.prepareStatement(selectsql);
			pstmt.setInt(1, requestNum);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				String userId = rs.getString("user_id");
				int pointAmount = rs.getInt("point_amount");

				String updatesql = "UPDATE users SET user_point = user_point + ? WHERE user_id = ?";
				pstmt = conn.prepareStatement(updatesql);
				pstmt.setInt(1, pointAmount);
				pstmt.setString(2, userId);

				int result = pstmt.executeUpdate();
				flag = (result > 0);
			}
		} catch (SQLException e) {
			System.out.println("포인트를 충전 시킬때 오류가 발생 했습니다.");
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, null);
		}
		return flag;
	}

	// 포인트 충전요청 조회 (유저)

	public List<PointRequest> getUserPointRequests(String userId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<PointRequest> requests = new ArrayList<>();

		String sql = "SELECT request_num, user_id, point_amount, request_status, request_date "
				+ "FROM point_requests WHERE user_id = ? " + "ORDER BY request_date ASC";

		try {
			conn = DBUtil.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				PointRequest request = new PointRequest();
				request.setRequestNum(rs.getInt("request_num"));
				request.setUserId(rs.getString("user_id"));
				request.setPointAmount(rs.getInt("point_amount"));
				request.setRequestStatus(rs.getInt("request_status"));
				request.setRequestDate(rs.getDate("request_date"));
				requests.add(request);
			}
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("충전요청 목록을 가져오는데 오류가 발생했습니다.");
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}

		return requests;
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
				request.setRequestDate(rs.getDate("request_date"));
				requests.add(request);
			}
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("충전요청 목록을 가져오는데 오류가 발생했습니다.");
			e.printStackTrace();
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}

		return requests;
	}
}