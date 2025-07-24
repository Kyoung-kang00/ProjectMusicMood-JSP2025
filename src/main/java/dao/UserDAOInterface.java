package dao;

import java.util.List;
import javax.sql.DataSource;

import dto.UserDTO;

public interface UserDAOInterface {
    
    // DataSource 설정 (커넥션 풀 사용)
    public void setDataSource(DataSource ds);
    
    // 사용자 목록 조회
    List<UserDTO> selectList();
    
    // 사용자 추가
    int insert(UserDTO user);
    
    // 사용자 단일 조회 (이메일로 조회)
    UserDTO selectOne(String email);
    
    // 사용자 정보 업데이트
    int update(UserDTO user);
    
    // 로그인 시 사용자 존재 여부 확인
    UserDTO exist(String email, String password);
    
    // 비밀번호 찾기 (이메일과 이름으로 사용자 확인)
    UserDTO findPassword(String email, String name);
} 