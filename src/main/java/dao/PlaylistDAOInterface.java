package dao;

import java.util.List;
import javax.sql.DataSource;

import dto.PlaylistDTO;

/**
 * 플레이리스트 데이터 액세스 인터페이스
 * 플레이리스트 좋아요 관련 데이터베이스 작업에 대한 인터페이스를 정의합니다.
 */
public interface PlaylistDAOInterface {
    
    /**
     * 데이터소스 설정 메서드
     * ContextLoaderListener에서 의존성 주입을 위해 사용됩니다.
     * 
     * @param ds JNDI로 얻어온 DataSource 객체
     */
    void setDataSource(DataSource ds);
    
    /**
     * 사용자가 좋아요한 플레이리스트 ID 목록 조회
     * 
     * @param email 사용자 이메일
     * @return 좋아요한 플레이리스트 ID 목록
     */
    List<String> getLikedPlaylistIds(String email);
    
    /**
     * 플레이리스트 좋아요 추가
     * 
     * @param email 사용자 이메일
     * @param playlistId 플레이리스트 ID
     * @return 작업 성공 여부
     */
    boolean addLikeToPlaylist(String email, String playlistId);
    
    /**
     * 플레이리스트 좋아요 제거
     * 
     * @param email 사용자 이메일
     * @param playlistId 플레이리스트 ID
     * @return 작업 성공 여부
     */
    boolean removeLikeFromPlaylist(String email, String playlistId);
    
    /**
     * 사용자가 플레이리스트에 좋아요를 눌렀는지 확인
     * 
     * @param email 사용자 이메일
     * @param playlistId 플레이리스트 ID
     * @return 좋아요 여부
     */
    boolean hasUserLikedPlaylist(String email, String playlistId);
    
    /**
     * 플레이리스트 좋아요 수 조회
     * 
     * @param playlistId 플레이리스트 ID
     * @return 좋아요 수
     */
    int getPlaylistLikeCount(String playlistId);
} 