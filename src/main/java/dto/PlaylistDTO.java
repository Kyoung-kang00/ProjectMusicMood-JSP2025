package dto;

import java.sql.Timestamp;

/**
 * 플레이리스트 좋아요 데이터 전송 객체 (DTO)
 * 사용자가 좋아요한 플레이리스트 정보를 저장하고 전달하는 객체입니다.
 */
public class PlaylistDTO {
    // 필드
    private String email;           // 사용자 이메일
    private String playlist_id;     // 플레이리스트 ID
    private Timestamp liked_at;     // 좋아요 누른 시간
    
    // 생성자
    public PlaylistDTO() {
        // 기본 생성자
    }
    
    /**
     * 전체 필드를 초기화하는 생성자
     */
    public PlaylistDTO(String email, String playlist_id, Timestamp liked_at) {
        this.email = email;
        this.playlist_id = playlist_id;
        this.liked_at = liked_at;
    }
    
    // 빌더 패턴을 위한 정적 내부 클래스
    public static class Builder {
        private String email;           // 사용자 이메일
        private String playlist_id;     // 플레이리스트 ID
        private Timestamp liked_at;     // 좋아요 누른 시간
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder playlistId(String playlist_id) {
            this.playlist_id = playlist_id;
            return this;
        }
        
        public Builder likedAt(Timestamp liked_at) {
            this.liked_at = liked_at;
            return this;
        }
        
        public PlaylistDTO build() {
            return new PlaylistDTO(email, playlist_id, liked_at);
        }
    }
    
    /**
     * 빌더 패턴을 위한 정적 팩토리 메서드
     */
    public static Builder builder() {
        return new Builder();
    }

    // Getter & Setter
    public String getEmail() {
        return email;
    }

    public PlaylistDTO setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPlaylistId() {
        return playlist_id;
    }

    public PlaylistDTO setPlaylistId(String playlist_id) {
        this.playlist_id = playlist_id;
        return this;
    }

    public Timestamp getLikedAt() {
        return liked_at;
    }

    public PlaylistDTO setLikedAt(Timestamp liked_at) {
        this.liked_at = liked_at;
        return this;
    }
    
    @Override
    public String toString() {
        return "PlaylistDTO [email=" + email + ", playlist_id=" + playlist_id + 
                ", liked_at=" + liked_at + "]";
    }
} 