package controller;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;

import dao.PlaylistDAO;
import dto.PlaylistDTO;
import dto.UserDTO;
import service.SpotifyService;
import utils.ErrorLogger;
import utils.TokenManager;

/**
 * 플레이리스트 관련 모든 기능을 처리하는 통합 컨트롤러
 * action 파라미터에 따라 다양한 기능을 제공합니다:
 * - list: 플레이리스트 목록 조회
 * - like: 플레이리스트 좋아요 추가/제거
 * - liked: 좋아요한 플레이리스트 목록 조회
 * - preview: 트랙 미리듣기 정보 조회
 */
public class PlaylistController implements Controller {
    
    // 데이터 액세스 객체
    private final PlaylistDAO playlistDAO;
    // Spotify API 서비스
    private final SpotifyService spotifyService;
    
    /**
     * 생성자를 통한 PlaylistDAO 객체 주입
     * ContextLoaderListener에서 의존성 주입을 위해 사용됩니다.
     * 
     * @param playlistDAO 플레이리스트 데이터 액세스 객체
     */
    public PlaylistController(PlaylistDAO playlistDAO) {
        this.playlistDAO = playlistDAO;
        this.spotifyService = new SpotifyService();
    }
    
    /**
     * 생성자를 통한 PlaylistDAOInterface 객체 주입
     * ContextLoaderListener에서 의존성 주입을 위해 사용됩니다.
     * 
     * @param playlistDAO 플레이리스트 데이터 액세스 인터페이스
     */
    public PlaylistController(dao.PlaylistDAOInterface playlistDAO) {
        this.playlistDAO = (PlaylistDAO)playlistDAO;
        this.spotifyService = new SpotifyService();
    }
    
    /**
     * 모델 데이터로 플레이리스트 관련 요청을 처리하는 메서드
     * 
     * @param model 컨트롤러 공유 모델
     * @return 처리 결과에 따른 뷰 경로
     * @throws Exception 처리 중 예외 발생 시
     */
    @Override
    public String execute(Map<String, Object> model) throws Exception {
        // 사용자 로그인 확인
        Object sessionObj = model.get("session");
        HttpSession session = null;
        String email = null;
        
        // 세션 객체 확인 및 형변환
        if (sessionObj != null) {
            // HttpSession 타입으로 안전하게 변환
            session = (HttpSession) sessionObj;
            
            // 세션에서 사용자 정보 가져오기
            UserDTO user = (UserDTO) session.getAttribute("user");
            if (user != null) {
                email = user.getEmail();
            }
        }
        
        // 액션 파라미터에 따라 메서드 분기
        String action = (String) model.get("action");
        
        // 액션이 지정되지 않은 경우 기본값(list)으로 처리
        if (action == null || action.trim().isEmpty()) {
            action = "list";
        }
        
        // 로그인 확인이 필요한 액션 목록
        if (action.equals("like") && email == null) {
            return "redirect:auth.do?action=login";
        }
        
        // 액션에 따라 메서드 분기
        switch (action) {
            case "list":
                return listPlaylists(model, email);
            case "like":
                return likePlaylist(model, email);
            case "liked":
                return listLikedPlaylists(model, email);
            case "preview":
                return getPlaylistPreview(model, email);
            default:
                // 잘못된 액션인 경우 목록 페이지로 이동
                return listPlaylists(model, email);
        }
    }
    
    /**
     * 플레이리스트 목록 조회
     */
    private String listPlaylists(Map<String, Object> model, String email) throws Exception {
        // 로그인한 경우 좋아요한 플레이리스트 ID 목록을 가져옴
        List<String> likedPlaylistIds = null;
        if (email != null) {
            likedPlaylistIds = playlistDAO.getLikedPlaylistIds(email);
            model.put("likedPlaylistIds", likedPlaylistIds);
            
            // JSP에서 사용할 사용자 ID 설정
            model.put("currentUserId", email);
            
            // 좋아요 목록을 JSON 형태로 변환하여 전달
            String likedPlaylistIdsJson = convertToJsonArray(likedPlaylistIds);
            model.put("likedPlaylistIdsJson", likedPlaylistIdsJson);
        } else {
            // 로그인하지 않은 경우 기본값 설정
            model.put("currentUserId", "null");
            model.put("likedPlaylistIdsJson", "[]");
        }
        
        // 플레이리스트 메인 페이지 반환
        return "/WEB-INF/views/playlist.jsp";
    }
    
    /**
     * 문자열 목록을 JSON 배열 형태로 변환
     */
    private String convertToJsonArray(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            sb.append("\"").append(items.get(i)).append("\"");
            if (i < items.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * 플레이리스트 좋아요 추가/제거
     */
    private String likePlaylist(Map<String, Object> model, String email) throws Exception {
        // HTTP 메서드 확인 (POST만 허용)
        String method = (String) model.get("method");
        if (!"POST".equals(method)) {
            System.out.println("[PlaylistController] HTTP 메서드 오류: " + method + " (POST만 허용)");
            model.put("status", "METHOD_NOT_ALLOWED");
            model.put("message", "POST 메서드만 허용됩니다.");
            return "/WEB-INF/views/json/status.jsp";
        }
        
        // 이메일 확인 (로그인 필수)
        if (email == null) {
            System.out.println("[PlaylistController] 인증 오류: 로그인되지 않은 사용자");
            model.put("status", "UNAUTHORIZED");
            model.put("message", "로그인이 필요한 기능입니다.");
            return "/WEB-INF/views/json/status.jsp";
        }
        
        // 디버그 로그
        System.out.println("[PlaylistController] 좋아요 요청 처리 시작: email=" + email);
        
        // 원시 요청 객체 가져오기
        HttpServletRequest request = (HttpServletRequest) model.get("request");
        
        // 모든 요청 파라미터 로깅
        System.out.println("[PlaylistController] 요청 파라미터 목록:");
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            String value = request.getParameter(name);
            System.out.println("  - " + name + ": " + value);
        }
        
        // 필요한 파라미터 추출
        String playlistId = request.getParameter("playlistId");
        String likeAction = request.getParameter("likeAction");
        
        // 디버그 로그
        System.out.println("[PlaylistController] 추출한 파라미터: playlistId=" + playlistId + ", likeAction=" + likeAction);
        
        // 필수 파라미터 유효성 검사
        if (playlistId == null || likeAction == null) {
            System.out.println("[PlaylistController] 파라미터 오류: 필수 파라미터 누락");
            model.put("status", "BAD_REQUEST");
            model.put("message", "필수 파라미터가 누락되었습니다: playlistId 또는 likeAction");
            return "/WEB-INF/views/json/status.jsp";
        }
        
        try {
            boolean result;
            
            if ("add".equals(likeAction)) {
                // 좋아요 추가
                System.out.println("[PlaylistController] 좋아요 추가 시도: email=" + email + ", playlistId=" + playlistId);
                result = playlistDAO.addLikeToPlaylist(email, playlistId);
                System.out.println("[PlaylistController] 좋아요 추가 결과: " + (result ? "성공" : "실패"));
            } else if ("remove".equals(likeAction)) {
                // 좋아요 제거
                System.out.println("[PlaylistController] 좋아요 제거 시도: email=" + email + ", playlistId=" + playlistId);
                result = playlistDAO.removeLikeFromPlaylist(email, playlistId);
                System.out.println("[PlaylistController] 좋아요 제거 결과: " + (result ? "성공" : "실패"));
            } else {
                System.out.println("[PlaylistController] 잘못된 액션: " + likeAction);
                model.put("status", "BAD_REQUEST");
                model.put("message", "유효하지 않은 좋아요 액션입니다: " + likeAction);
                return "/WEB-INF/views/json/status.jsp";
            }
            
            // 결과에 따라 상태 설정
            if (result) {
                model.put("status", "OK");
                model.put("message", "add".equals(likeAction) ? "플레이리스트를 좋아요 했습니다." : "플레이리스트 좋아요를 취소했습니다.");
                
                // 추가 정보 제공 - 업데이트된 좋아요 목록
                System.out.println("[PlaylistController] 업데이트된 좋아요 목록 조회 시작");
                List<String> updatedLikedPlaylistIds = playlistDAO.getLikedPlaylistIds(email);
                model.put("likedPlaylistIds", updatedLikedPlaylistIds);
                model.put("likedPlaylistIdsJson", convertToJsonArray(updatedLikedPlaylistIds));
                System.out.println("[PlaylistController] 업데이트된 좋아요 목록 조회 완료: " + updatedLikedPlaylistIds.size() + "개");
            } else {
                System.out.println("[PlaylistController] 좋아요 처리 실패");
                model.put("status", "INTERNAL_SERVER_ERROR");
                model.put("message", "add".equals(likeAction) ? "좋아요 추가 실패" : "좋아요 제거 실패");
            }
            
            return "/WEB-INF/views/json/status.jsp";
        } catch (Exception e) {
            System.err.println("[PlaylistController] 좋아요 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            ErrorLogger.logError("플레이리스트 좋아요 처리 중 오류 (playlistId: " + playlistId + ", 액션: " + likeAction + ")", e);
            model.put("status", "INTERNAL_SERVER_ERROR");
            model.put("message", "좋아요 처리 중 오류: " + e.getMessage());
            return "/WEB-INF/views/json/status.jsp";
        }
    }
    
    /**
     * 좋아요한 플레이리스트 목록 조회
     */
    private String listLikedPlaylists(Map<String, Object> model, String email) throws Exception {
        if (email == null) {
            return "redirect:auth.do?action=login";
        }

        try {
            // DAO를 통해 좋아요한 플레이리스트 ID 목록 조회
            List<String> likedPlaylistIds = playlistDAO.getLikedPlaylistIds(email);
            
            // 디버그 로그 추가
            System.out.println("[PlaylistController] 좋아요한 플레이리스트 ID 목록 조회 결과:");
            for (String id : likedPlaylistIds) {
                System.out.println("  - " + id);
            }
            
            model.put("likedPlaylistIds", likedPlaylistIds);
            model.put("isLikedList", true);
            model.put("currentUserId", email);
            
            // 좋아요 목록을 JSON 형태로 변환하여 전달
            String likedPlaylistIdsJson = convertToJsonArray(likedPlaylistIds);
            model.put("likedPlaylistIdsJson", likedPlaylistIdsJson);
            
            // 좋아요한 플레이리스트 전용 페이지로 반환
            return "/WEB-INF/views/liked_playlist.jsp";
        } catch (Exception e) {
            ErrorLogger.logError("좋아요한 플레이리스트 목록 조회 중 오류", e);
            model.put("errorMessage", "좋아요한 플레이리스트를 불러오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/error.jsp";
        }
    }
    
    /**
     * 플레이리스트의 미리듣기 가능한 트랙 목록을 가져옵니다.
     */
    private String getPlaylistPreview(Map<String, Object> model, String email) throws Exception {
        System.out.println("[PlaylistController] 플레이리스트 미리듣기 요청 처리 시작");
        
        // 액세스 토큰 가져오기 (세션에서 또는 서비스를 통해)
        String accessToken = null;
        HttpSession session = (HttpSession) model.get("session");
        HttpServletRequest request = (HttpServletRequest) model.get("request");
        String playlistId = request.getParameter("playlistId");
        
        System.out.println("[PlaylistController] 요청 파라미터 - 플레이리스트 ID: " + playlistId);
        
        if (playlistId == null || playlistId.trim().isEmpty()) {
            System.err.println("[PlaylistController] 유효하지 않은 플레이리스트 ID");
            model.put("status", "BAD_REQUEST");
            model.put("message", "플레이리스트 ID가 필요합니다.");
            return "/WEB-INF/views/json/status.jsp";
        }
        
        // 세션에서 토큰 가져오기 시도
        if (session != null && session.getAttribute("spotify_access_token") != null) {
            accessToken = (String) session.getAttribute("spotify_access_token");
            System.out.println("[PlaylistController] 세션에서 토큰 가져옴 (길이: " + accessToken.length() + ")");
        } else {
            System.out.println("[PlaylistController] 세션에 토큰 없음, TokenManager 사용 시도");
            try {
                // TokenManager를 통해 토큰 가져오기 시도
                accessToken = utils.TokenManager.getAccessToken();
                
                if (accessToken == null || accessToken.isEmpty()) {
                    System.err.println("[PlaylistController] TokenManager에서 토큰 가져오기 실패");
                    model.put("status", "ERROR");
                    model.put("message", "Spotify 액세스 토큰을 가져올 수 없습니다.");
                    return "/WEB-INF/views/json/status.jsp";
                }
                
                System.out.println("[PlaylistController] TokenManager에서 토큰 가져옴 (길이: " + accessToken.length() + ")");
                
                // 세션에 토큰 저장 (있는 경우)
                if (session != null) {
                    session.setAttribute("spotify_access_token", accessToken);
                    System.out.println("[PlaylistController] 토큰을 세션에 저장함");
                }
            } catch (Exception e) {
                System.err.println("[PlaylistController] 토큰 가져오기 오류: " + e.getMessage());
                e.printStackTrace();
                model.put("status", "ERROR");
                model.put("message", "Spotify 액세스 토큰을 가져오는 중 오류: " + e.getMessage());
                return "/WEB-INF/views/json/status.jsp";
            }
        }
        
        try {
            System.out.println("[PlaylistController] 플레이리스트 이름 가져오기 시작");
            // SpotifyService를 통해 플레이리스트 이름 가져오기
            String playlistName = spotifyService.getPlaylistName(playlistId);
            System.out.println("[PlaylistController] 플레이리스트 이름: " + (playlistName != null ? playlistName : "알 수 없음"));
            
            System.out.println("[PlaylistController] 플레이리스트 트랙 가져오기 시작");
            // Spotify API로 트랙 정보 가져오기
            List<Map<String, Object>> tracks = spotifyService.getPlaylistTracks(accessToken, playlistId);
            System.out.println("[PlaylistController] 가져온 트랙 수: " + (tracks != null ? tracks.size() : 0));
            
            // 결과를 모델에 저장
            model.put("tracks", tracks);
            model.put("playlistId", playlistId);
            model.put("playlistName", playlistName != null ? playlistName : "Playlist");
            model.put("status", "OK");
            
            // JSON 응답 또는 JSP 뷰 반환
            String format = request.getParameter("format");
            System.out.println("[PlaylistController] 응답 형식: " + format);
            
            if ("json".equals(format)) {
                return "/WEB-INF/views/json/preview_tracks.jsp";
            } else {
                return "/WEB-INF/views/preview_tracks.jsp";
            }
            
        } catch (Exception e) {
            System.err.println("[PlaylistController] 플레이리스트 정보 가져오기 오류: " + e.getMessage());
            e.printStackTrace();
            ErrorLogger.logError("트랙 미리듣기 정보 조회 중 오류 (playlistId: " + playlistId + ")", e);
            
            model.put("status", "ERROR");
            model.put("message", "트랙 정보를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            return "/WEB-INF/views/json/status.jsp";
        }
    }
} 