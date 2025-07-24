package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import dto.UserDTO;
import utils.ErrorLogger;

/**
 * 사용자 관련 데이터 액세스 객체
 * 사용자 정보의 CRUD 작업을 담당
 */
public class UserDAO implements UserDAOInterface {
    private final DataSource dataSource;
    
    /**
     * 생성자를 통한 DataSource 주입
     * ContextLoaderListener에서 의존성 주입을 위해 사용됩니다.
     * 
     * @param dataSource JNDI로 얻어온 DataSource 객체
     */
    public UserDAO(DataSource dataSource) {
        this.dataSource = dataSource;
        // 초기화 시 데이터베이스 연결 테스트
        testConnection();
    }
    
    /**
     * 데이터베이스 연결을 테스트하는 메서드
     * 애플리케이션 시작 시와 주기적으로 호출하여 DB 연결 상태 확인
     */
    public void testConnection() {
        Connection conn = null;
        try {
            System.out.println("[DB 연결 테스트] 시작...");
            long startTime = System.currentTimeMillis();
            
            conn = dataSource.getConnection();
            
            long endTime = System.currentTimeMillis();
            System.out.println("[DB 연결 테스트] 성공: " + (endTime - startTime) + "ms 소요");
            
            // DB 정보 출력
            DatabaseMetaData metaData = conn.getMetaData();
            System.out.println("[DB 정보] URL: " + metaData.getURL());
            System.out.println("[DB 정보] 드라이버: " + metaData.getDriverName());
            System.out.println("[DB 정보] 사용자: " + metaData.getUserName());
        } catch (SQLException e) {
            System.err.println("[DB 연결 테스트] 실패: " + e.getMessage());
            ErrorLogger.logError("데이터베이스 연결 테스트 실패", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("[DB 연결 테스트] 연결 종료");
                } catch (SQLException e) {
                    ErrorLogger.logError("데이터베이스 연결 종료 실패", e);
                }
            }
        }
    }
    
    /**
     * 데이터소스 설정 메서드 (호환성을 위해 남겨둠)
     * 생성자 주입을 사용하므로 이 메서드는 실제로 사용되지 않습니다.
     * 
     * @param ds JNDI로 얻어온 DataSource 객체
     */
    @Override
    public void setDataSource(DataSource ds) {
        // 생성자 주입 방식으로 변경되어 이 메서드는 실제로 사용되지 않습니다.
        // 인터페이스 호환성을 위해 남겨둠
    }
    
    @Override
    public List<UserDTO> selectList() {
        List<UserDTO> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY email ASC";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            System.out.println("[DB 접근] 사용자 목록 조회 요청");
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                UserDTO user = new UserDTO()
                    .setEmail(rs.getString("email"))
                    .setPassword(rs.getString("password"))
                    .setName(rs.getString("name"))
                    .setPhone(rs.getString("phone"))
                    .setCreatedAt(rs.getString("created_at"));
                users.add(user);
            }
            System.out.println("[DB 접근] 사용자 목록 조회 성공 (" + users.size() + "건)");
            
        } catch (SQLException e) {
            System.err.println("[DB 오류] 사용자 목록 조회 실패: " + e.getMessage());
            ErrorLogger.logError("사용자 목록 조회 중 오류 발생", e);
        } finally {
            closeResources(conn, pstmt, rs);
        }
        
        return users;
    }
    
    @Override
    public int insert(UserDTO user) {
        String sql = "INSERT INTO users (email, name, password, phone) VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            System.out.println("[DB 접근] 사용자 등록 요청 (이메일: " + user.getEmail() + ")");
            conn = dataSource.getConnection();
            
            if (conn == null) {
                System.err.println("[DB 오류] 데이터베이스 연결 실패");
                ErrorLogger.logError("사용자 등록 중 오류 발생 (이메일: " + user.getEmail() + ") - 커넥션 생성 실패", null);
                return 0;
            }
            
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getPhone());
            
            int result = pstmt.executeUpdate();
            System.out.println("[DB 접근] 사용자 등록 성공 (이메일: " + user.getEmail() + ")");
            return result;
            
        } catch (SQLException e) {
            System.err.println("[DB 오류] 사용자 등록 실패: " + e.getMessage());
            ErrorLogger.logError("사용자 등록 중 오류 발생 (이메일: " + user.getEmail() + ")", e);
            return 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    @Override
    public UserDTO selectOne(String email) {
        UserDTO user = null;
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new UserDTO()
                        .setEmail(rs.getString("email"))
                        .setPassword(rs.getString("password"))
                        .setName(rs.getString("name"))
                        .setPhone(rs.getString("phone"))
                        .setCreatedAt(rs.getString("created_at"));
                }
            }
            
        } catch (Exception e) {
            ErrorLogger.logError("사용자 단일 조회 중 오류 발생 (이메일: " + email + ")", e);
        }
        
        return user;
    }
    
    @Override
    public int update(UserDTO user) {
        String sql = "UPDATE users SET password=?, name=?, phone=? WHERE email=?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getPassword());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, user.getEmail());
            
            return pstmt.executeUpdate();
            
        } catch (Exception e) {
            ErrorLogger.logError("사용자 정보 수정 중 오류 발생 (이메일: " + user.getEmail() + ")", e);
            return 0;
        }
    }
    
    @Override
    public UserDTO exist(String email, String password) {
        UserDTO user = null;
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        
        System.out.println("[UserDAO] 로그인 시도: email=" + email);
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            
            System.out.println("[UserDAO] SQL 실행: " + sql.replace("?", "'" + email + "'") + " (비밀번호는 보안상 표시 안함)");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new UserDTO()
                        .setEmail(rs.getString("email"))
                        .setPassword(rs.getString("password"))
                        .setName(rs.getString("name"))
                        .setPhone(rs.getString("phone"))
                        .setCreatedAt(rs.getString("created_at"));
                    System.out.println("[UserDAO] 로그인 성공: 사용자=" + user.getEmail() + ", 이름=" + user.getName());
                } else {
                    System.out.println("[UserDAO] 로그인 실패: 일치하는 사용자 없음 (email=" + email + ")");
                }
            }
            
        } catch (Exception e) {
            System.err.println("[UserDAO] 로그인 인증 중 오류: " + e.getMessage());
            e.printStackTrace();
            ErrorLogger.logError("사용자 로그인 인증 중 오류 발생 (이메일: " + email + ")", e);
        }
        
        return user;
    }
    
    /**
     * 사용자 인증 메서드
     * 
     * @param email 사용자 이메일
     * @param password 비밀번호
     * @return 인증된 사용자 정보, 인증 실패 시 null
     */
    public UserDTO authenticate(String email, String password) {
        // exist 메서드 호출
        return exist(email, password);
    }
    
    /**
     * 비밀번호 찾기
     * 이메일과 이름으로 사용자를 찾아 사용자 정보를 반환합니다.
     * 
     * @param email 사용자 이메일
     * @param name 사용자 이름
     * @return 사용자 정보 (찾지 못한 경우 null)
     */
    @Override
    public UserDTO findPassword(String email, String name) {
        UserDTO user = null;
        String sql = "SELECT * FROM users WHERE email = ? AND name = ?";
        
        System.out.println("[UserDAO] 비밀번호 찾기 시도: email=" + email + ", name=" + name);
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, name);
            
            System.out.println("[UserDAO] SQL 실행: " + sql.replace("?", "'" + email + "'") + " AND name='" + name + "'");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new UserDTO()
                        .setEmail(rs.getString("email"))
                        .setPassword(rs.getString("password"))
                        .setName(rs.getString("name"))
                        .setPhone(rs.getString("phone"))
                        .setCreatedAt(rs.getString("created_at"));
                    System.out.println("[UserDAO] 비밀번호 찾기 성공: 사용자=" + user.getEmail() + ", 이름=" + user.getName());
                } else {
                    System.out.println("[UserDAO] 비밀번호 찾기 실패: 일치하는 사용자 없음 (email=" + email + ", name=" + name + ")");
                }
            }
            
        } catch (Exception e) {
            System.err.println("[UserDAO] 비밀번호 찾기 중 오류: " + e.getMessage());
            e.printStackTrace();
            ErrorLogger.logError("비밀번호 찾기 중 오류 발생 (이메일: " + email + ", 이름: " + name + ")", e);
        }
        
        return user;
    }
    
    /**
     * 리소스(Connection, PreparedStatement, ResultSet)를 안전하게 닫는 유틸리티 메서드
     */
    private void closeResources(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                ErrorLogger.logError("ResultSet 종료 중 오류", e);
            }
        }
        
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                ErrorLogger.logError("PreparedStatement 종료 중 오류", e);
            }
        }
        
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                ErrorLogger.logError("Connection 종료 중 오류", e);
            }
        }
    }

} 