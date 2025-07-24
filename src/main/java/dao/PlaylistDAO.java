package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import dto.PlaylistDTO;
import utils.ErrorLogger;

/**
 * 플레이리스트 관련 데이터베이스 액세스 객체
 * 플레이리스트 좋아요 기능을 위한 데이터 처리를 담당합니다.
 */
public class PlaylistDAO implements PlaylistDAOInterface {
    // 데이터소스 (커넥션 풀)
    private final DataSource dataSource;
    
    /**
     * 생성자를 통한 DataSource 주입
     * ContextLoaderListener에서 의존성 주입을 위해 사용됩니다.
     * 
     * @param dataSource JNDI로 얻어온 DataSource 객체
     */
    public PlaylistDAO(DataSource dataSource) {
        this.dataSource = dataSource;
        testConnection(); // 초기화 시 연결 테스트
    }
    
    /**
     * 데이터베이스 연결을 테스트하는 메서드
     */
    private void testConnection() {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("[PlaylistDAO] 데이터베이스 연결 테스트 성공");
        } catch (Exception e) {
            System.err.println("[PlaylistDAO] 데이터베이스 연결 테스트 실패: " + e.getMessage());
            ErrorLogger.logError("PlaylistDAO 데이터베이스 연결 테스트 실패", e);
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
    
    /**
     * 플레이리스트에 좋아요 추가
     * 
     * @param email 사용자 이메일
     * @param playlistId 플레이리스트 ID
     * @return 작업 성공 여부
     */
    @Override
    public boolean addLikeToPlaylist(String email, String playlistId) {
        // REPLACE INTO 사용: 이미 레코드가 있으면 업데이트, 없으면 삽입
        String sql = "REPLACE INTO liked_playlists (email, playlist_id, liked_at) VALUES (?, ?, NOW())";
        
        System.out.println("[PlaylistDAO] 플레이리스트 좋아요 추가 시도 (email: " + email + ", playlistId: " + playlistId + ")");
        
        try (Connection conn = dataSource.getConnection()) {
            // 트랜잭션 시작
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                pstmt.setString(2, playlistId);
                
                // SQL 로그 출력 - 실제 값이 포함된 쿼리 로그
                String realSql = sql.replace("?", "'?'");
                realSql = realSql.replaceFirst("'\\?'", "'" + email + "'");
                realSql = realSql.replaceFirst("'\\?'", "'" + playlistId + "'");
                System.out.println("[PlaylistDAO] 실행할 SQL: " + realSql);
                
                // 실제 SQL 실행
                int result = pstmt.executeUpdate();
                
                // 결과 로깅
                System.out.println("[PlaylistDAO] 플레이리스트 좋아요 추가 결과: " + result + "행 영향받음");
                
                // 트랜잭션 커밋
                conn.commit();
                
                return result > 0;
            } catch (SQLException e) {
                // 트랜잭션 롤백
                conn.rollback();
                
                // SQL 오류 상세 로깅
                System.err.println("[PlaylistDAO] SQL 오류 발생: " + e.getMessage());
                System.err.println("[PlaylistDAO] 오류 코드: " + e.getErrorCode());
                System.err.println("[PlaylistDAO] SQL 상태: " + e.getSQLState());
                e.printStackTrace();
                
                // 외래키 제약 조건 위반인지 확인 (사용자나 플레이리스트가 존재하지 않는 경우)
                if (e.getErrorCode() == 1452) { // MySQL의 foreign key constraint fails 에러 코드
                    System.out.println("[PlaylistDAO] 외래키 제약 조건 위반: 사용자 또는 플레이리스트가 존재하지 않음");
                    return false;
                }
                
                return false;
            }
        } catch (Exception e) {
            // 데이터베이스 연결 또는 기타 오류
            System.err.println("[PlaylistDAO] 플레이리스트 좋아요 추가 중 오류: " + e.getMessage());
            e.printStackTrace();
            ErrorLogger.logError("플레이리스트 좋아요 추가 중 오류 발생 (email: " + email + ", playlistId: " + playlistId + ")", e);
            return false;
        }
    }
    
    /**
     * 플레이리스트 좋아요 제거
     * 
     * @param email 사용자 이메일
     * @param playlistId 플레이리스트 ID
     * @return 작업 성공 여부
     */
    @Override
    public boolean removeLikeFromPlaylist(String email, String playlistId) {
        String sql = "DELETE FROM liked_playlists WHERE email = ? AND playlist_id = ?";
        
        System.out.println("[PlaylistDAO] 플레이리스트 좋아요 제거 시도 (email: " + email + ", playlistId: " + playlistId + ")");
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, playlistId);
            
            int result = pstmt.executeUpdate();
            System.out.println("[PlaylistDAO] 플레이리스트 좋아요 제거 결과: " + result + "행 영향받음");
            return result > 0;
            
        } catch (Exception e) {
            System.err.println("[PlaylistDAO] 플레이리스트 좋아요 제거 중 오류: " + e.getMessage());
            ErrorLogger.logError("플레이리스트 좋아요 제거 중 오류 발생 (email: " + email + ", playlistId: " + playlistId + ")", e);
            return false;
        }
    }
    
    /**
     * 사용자가 플레이리스트에 좋아요를 눌렀는지 확인
     * 
     * @param email 사용자 이메일
     * @param playlistId 플레이리스트 ID
     * @return 좋아요 여부
     */
    @Override
    public boolean hasUserLikedPlaylist(String email, String playlistId) {
        String sql = "SELECT COUNT(*) FROM liked_playlists WHERE email = ? AND playlist_id = ?";
        
        System.out.println("[PlaylistDAO] 플레이리스트 좋아요 확인 (email: " + email + ", playlistId: " + playlistId + ")");
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, playlistId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    boolean result = rs.getInt(1) > 0;
                    System.out.println("[PlaylistDAO] 플레이리스트 좋아요 확인 결과: " + (result ? "좋아요 함" : "좋아요 안함"));
                    return result;
                }
            }
            
        } catch (Exception e) {
            System.err.println("[PlaylistDAO] 플레이리스트 좋아요 확인 중 오류: " + e.getMessage());
            ErrorLogger.logError("플레이리스트 좋아요 확인 중 오류 발생 (email: " + email + ", playlistId: " + playlistId + ")", e);
        }
        
        return false;
    }
    
    /**
     * 사용자가 좋아요한 플레이리스트 ID 목록 조회
     * 
     * @param email 사용자 이메일
     * @return 좋아요한 플레이리스트 ID 목록
     */
    @Override
    public List<String> getLikedPlaylistIds(String email) {
        List<String> playlistIds = new ArrayList<>();
        String sql = "SELECT playlist_id FROM liked_playlists WHERE email = ? ORDER BY liked_at DESC";
        
        System.out.println("[PlaylistDAO] 사용자가 좋아요한 플레이리스트 ID 목록 조회 (email: " + email + ")");
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, email);
            
            // SQL 쿼리 로깅
            System.out.println("[PlaylistDAO] 실행 SQL: " + sql.replace("?", "'" + email + "'"));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String playlistId = rs.getString("playlist_id");
                    playlistIds.add(playlistId);
                    System.out.println("[PlaylistDAO] 좋아요 플레이리스트 ID 발견: " + playlistId);
                }
            }
            
            System.out.println("[PlaylistDAO] 좋아요한 플레이리스트 ID 목록 조회 결과: " + playlistIds.size() + "개");
            System.out.println("[PlaylistDAO] 좋아요한 플레이리스트 ID 목록: " + String.join(", ", playlistIds));
            
        } catch (Exception e) {
            System.err.println("[PlaylistDAO] 좋아요한 플레이리스트 ID 목록 조회 중 오류: " + e.getMessage());
            ErrorLogger.logError("좋아요한 플레이리스트 ID 목록 조회 중 오류 발생 (email: " + email + ")", e);
        }
        
        return playlistIds;
    }
    
    /**
     * 플레이리스트 좋아요 수 조회
     * 
     * @param playlistId 플레이리스트 ID
     * @return 좋아요 수
     */
    @Override
    public int getPlaylistLikeCount(String playlistId) {
        String sql = "SELECT COUNT(*) FROM liked_playlists WHERE playlist_id = ?";
        
        System.out.println("[PlaylistDAO] 플레이리스트 좋아요 수 조회 (playlistId: " + playlistId + ")");
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, playlistId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("[PlaylistDAO] 플레이리스트 좋아요 수: " + count);
                    return count;
                }
            }
            
        } catch (Exception e) {
            System.err.println("[PlaylistDAO] 플레이리스트 좋아요 수 조회 중 오류: " + e.getMessage());
            ErrorLogger.logError("플레이리스트 좋아요 수 조회 중 오류 발생 (playlistId: " + playlistId + ")", e);
        }
        
        return 0;
    }
} 