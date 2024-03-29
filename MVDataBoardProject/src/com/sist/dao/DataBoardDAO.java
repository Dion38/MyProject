package com.sist.dao;
import java.util.*;

import com.sun.org.apache.bcel.internal.generic.AALOAD;

import java.sql.*;
public class DataBoardDAO {
	// 연결
	private Connection conn; // Socket 첨부 (연결기기) ==> TCP
	private PreparedStatement ps; // InputStream(값을 읽어온다),OutputStream(SQL문장 전송)
	private final String URL = "jdbc:oracle:thin:@localhost:1521:ORCL";
	// new Socket("ip",포트번호) : 포트번호 (0~66535) : 0~1024 => 1521,1433,7000,8080
	
	// 드라이버 등록
	/*	
	 * thin,oci(데이터를 가지고 있다)
	 * ====
	 * 연결만 해주는 역할
	 */
	public DataBoardDAO(){
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");//리플렉션
			// 클래스의 이름을 읽어서 클래스를 제어 => Spring
			// 메모리 할당 => new를 사용하지 않고 메모리 할당                    
			// => 반드시 패키지명부터 ~ 클래스명
			// <jsp:useBean id="" class="com.sist.dao.BoardDAO"
			// 1.결합성 (의존성) 낮게 => 영향력 낮게
			// 2.응집성이 강하게 ==> 메소드 (한개의 기능만 수행이 가능하게 만든다)
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void getConnection(){
		try {
			conn = DriverManager.getConnection(URL,"scott","tiger");
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	public void disConnection(){
		try {
			if(ps!=null) ps.close();
			if(conn!=null) conn.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	public List<DataBoardVO> databoardListData(int page){
		List<DataBoardVO> list = new ArrayList<DataBoardVO>();
		try {
			getConnection();
			String sql = "SELECT no,subject,name,regdate,hit,num "
					+"FROM (SELECT no,subject,name,regdate,hit,rownum as num "
					+"FROM (SELECT no,subject,name,regdate,hit "
					+"FROM databoard ORDER BY no DESC)) "
					+"WHERE num BETWEEN ? AND ?";
			/*
			 *  View => 가상테이블
			 *  	= 단일뷰 : 테이블 한개를 연결시 사용
			 *  	= 복합뷰  : 테이블 여러개 연결 (Join SubQuery)
			 *  	= 인라인뷰 (***) FROM (SELECT~)
			 */
			ps = conn.prepareStatement(sql);
			// ? 값을 채워서 실행요청
			int rowSize = 10;
			int start = (rowSize * page)-(rowSize-1);
			int end = rowSize * page;
			// 결과값을 읽어 온다
			ps.setInt(1, start);
			ps.setInt(2, end);
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				DataBoardVO vo = new DataBoardVO();
				vo.setNo(rs.getInt(1));
				vo.setSubject(rs.getString(2));
				vo.setName(rs.getString(3));
				vo.setRegdate(rs.getDate(4));
				vo.setHit(rs.getInt(5));
				
				list.add(vo);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disConnection();
		}
		return list;
	}
	
	public void databoardInsert(DataBoardVO vo){
		try {
			getConnection();
			String sql = "INSERT INTO databoard(no,name,subject,content,pwd,filename,filesize) "
					+"VALUES((SELECT NVL(MAX(no)+1,1) FROM databoard),?,?,?,?,?,?)";
			ps = conn.prepareStatement(sql);
			ps.setString(1, vo.getName());
			ps.setString(2, vo.getSubject());
			ps.setString(3, vo.getContent());
			ps.setString(4, vo.getPwd());
			ps.setString(5, vo.getFilename());
			ps.setInt(6, vo.getFilesize());
			
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disConnection();
		}
		
	}
	
	public DataBoardVO databoardDetailData(int no){
		DataBoardVO vo = new DataBoardVO();
		try {
			getConnection();
			String sql = "UPDATE databoard SET "
					+"hit=hit+1 "
					+"WHERE no=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, no);
			ps.executeUpdate(); //조회수 증가
			
			sql = "SELECT no,name,subject,content,regdate,hit,filename,filesize "
					+"FROM databoard "
					+"WHERE no=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, no);
			ResultSet rs = ps.executeQuery();
			rs.next();
			vo.setNo(rs.getInt(1));
			vo.setName(rs.getString(2));
			vo.setSubject(rs.getString(3));
			vo.setContent(rs.getString(4));
			vo.setRegdate(rs.getDate(5));
			vo.setHit(rs.getInt(6));
			vo.setFilename(rs.getString(7));
			vo.setFilesize(rs.getInt(8));
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disConnection();
		}
		return vo;
	}
	
	public int databoardTotalPage(){
		int total = 0;
		try {
			getConnection();
			String sql = "SELECT CEIL(COUNT(*)/10.0) FROM databoard";
			ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			rs.next();
			total = rs.getInt(1);
			rs.close();
			// CEIL(올림),ROUND(반올림),TRUNC(버림)
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disConnection();
		}
		return total;
	}
	
	public DataBoardVO databoardUpdateData(int no){
		DataBoardVO vo = new DataBoardVO();
		try {
			getConnection();
			String sql = "SELECT no,name,subject,content "
					+"FROM databoard "
					+"WHERE no=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, no);
			ResultSet rs = ps.executeQuery();
			rs.next();
			vo.setNo(rs.getInt(1));
			vo.setName(rs.getString(2));
			vo.setSubject(rs.getString(3));
			vo.setContent(rs.getString(4));
			rs.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disConnection();
		}
		return vo;
	}
	
	// DAO => JSP , DAO => Model => JSP
	public boolean databoardUpdate(DataBoardVO vo){
		
		boolean bCheck = false;
		try {
			getConnection();
			// 비밀번호 검색
			String sql = "SELECT pwd FROM databoard "
					+"WHERE no=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, vo.getNo());
			ResultSet rs = ps.executeQuery();
			rs.next();
			String db_pwd = rs.getString(1);
			rs.close();
			
			if(db_pwd.equals(vo.getPwd())){
				bCheck = true;
				sql = "UPDATE databoard SET "
						+"name=?,subject=?,content=? "
						+"WHERE no=?";
				ps = conn.prepareStatement(sql);
				ps.setString(1, vo.getName());
				ps.setString(2, vo.getSubject());
				ps.setString(3, vo.getContent());
				ps.setInt(4, vo.getNo());
				
				ps.executeUpdate();
			}else{
				bCheck = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disConnection();
		}
		return bCheck;
	}
	
	public String isLogin(String id,String pwd){
		String result = "";
		try {
			getConnection();
			String sql = "SELECT COUNT(*) FROM member WHERE id=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, id);
			ResultSet rs = ps.executeQuery();
			rs.next();
			int count = rs.getInt(1);
			rs.close();
			
			if(count == 0){
				result = "NOID";
			} else{
				sql = "SELECT pwd,name FROM member WHERE id=?";
				ps = conn.prepareStatement(sql);
				ps.setString(1, id);
				rs = ps.executeQuery();
				rs.next();
				String db_pwd = rs.getString(1);
				String name = rs.getString(2);
				rs.close();
				
				if(db_pwd.equals(pwd)){
					result = name;
				} else{
					result = "NOPWD";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disConnection();
		}
		return result;
	}
	//댓글
	public List<DataBoardReplyVO> databoardReplyData(int bno){
		List<DataBoardReplyVO> list = new ArrayList<DataBoardReplyVO>();
		try {
			getConnection();
			String sql = "SELECT no,bno,id,name,msg,TO_CHAR(regdate,'YYYY-MM-DD HH24:MI:SS') "
					+"FROM databoardReply "
					+"WHERE bno=?" 
					+"ORDER BY no DESC";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, bno);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				DataBoardReplyVO vo = new DataBoardReplyVO();
				vo.setNo(rs.getInt(1));
				vo.setBno(rs.getInt(2));
				vo.setId(rs.getString(3));
				vo.setName(rs.getString(4));
				vo.setMsg(rs.getString(5));
				vo.setDbday(rs.getString(6));
				list.add(vo);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disConnection();
		}
		return list;
	}
	
	public void replyInsert(DataBoardReplyVO vo){
		try {
			getConnection();
			String sql = "INSERT INTO databoardReply VALUES("
					+"(SELECT NVL(MAX(no)+1,1) FROM databoardReply),"
					+"?,?,?,?,SYSDATE)";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, vo.getBno());
			ps.setString(2, vo.getId());
			ps.setString(3, vo.getName());
			ps.setString(4, vo.getMsg());
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disConnection();
		}
	}
	
	public void replyDelete(int no){
		try {
			getConnection();
			String sql = "DELETE FROM databoardReply WHERE no=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, no);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disConnection();
		}
	}
	
	public void replyUpdate(int no,String msg){
		try {
			getConnection();
			String sql = "UPDATE databoardReply SET msg=? WHERE no=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, msg);
			ps.setInt(2, no);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disConnection();
		}
	}
	// 1. 파일 삭제 2. 댓글 삭제 3. 실제 게시물 삭제 => 비밀번호가 일치시
	// 파일 삭제
	public DataBoardVO databoardFileInfo(int no){
		DataBoardVO vo = new DataBoardVO();
		try {
			getConnection();
			String sql = "SELECT filename,filesize FROM databoard "
					+"WHERE no=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, no);
			ResultSet rs = ps.executeQuery();
			rs.next();
			vo.setFilename(rs.getString(1));
			vo.setFilesize(rs.getInt(2));
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disConnection();
		}
		return vo;
	}
	
	public boolean databoard_delete(int no,String pwd){
		boolean bCheck = false;
		try {
			getConnection();
			// 비밀번호
			String sql = "SELECT pwd FROM databoard WHERE no=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, no);
			ResultSet rs = ps.executeQuery();
			rs.next(); // 메모리에 출력된 위치 커서이동
			String db_pwd = rs.getString(1);
			rs.close();
			
			if(db_pwd.equals(pwd)){
				bCheck = true;
				sql = "SELECT COUNT(*) FROM databoardReply WHERE bno=?";
				ps = conn.prepareStatement(sql);
				ps.setInt(1, no);
				rs = ps.executeQuery();
				rs.next();
				int count = rs.getInt(1);
				rs.close();
				
				if(count != 0){ // 댓글이 존재할 경우
					sql = "DELETE FROM databoardReply WHERE bno=?";
					ps = conn.prepareStatement(sql);
					ps.setInt(1, no);
					ps.executeUpdate();
				}
				// 실제 게시물 삭제
				sql = "DELETE FROM databoard WHERE no=?";
				ps = conn.prepareStatement(sql);
				ps.setInt(1, no);
				ps.executeUpdate();
			}else{
				bCheck = false;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disConnection();
		}
		return bCheck;
	}
}
