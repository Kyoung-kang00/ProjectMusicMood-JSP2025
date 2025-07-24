package controller;

import java.util.Map;

import utils.TokenManager;
import utils.ErrorLogger;

/**
 * API 토큰 관리 컨트롤러
 * API 접근을 위한 토큰을 발급하고 관리하는 컨트롤러
 * 
 * 주요 기능:
 * 1. API 접근 토큰 발급
 * 2. 토큰 유효성 검증
 * 3. 토큰 갱신
 */
public class TokenController implements Controller {
    
    // 상수 정의
    private static final String ACTION_ISSUE = "issue";
    private static final String ACTION_VALIDATE = "validate";
    
    /**
     * 클라이언트에 Spotify API 액세스 토큰을 제공합니다.
     * 
     * @param model 컨트롤러 공유 모델 (요청 및 응답 데이터 포함)
     * @return 처리 결과를 보여줄 뷰의 경로
     * @throws Exception 토큰 발급 중 오류 발생 시
     */
    @Override
    public String execute(Map<String, Object> model) throws Exception {
        String action = (String) model.get("action");
        
        System.out.println("[TokenController] 요청 처리: action=" + action);
        
        if (ACTION_ISSUE.equals(action)) {
            return issueToken(model);
        } else if (ACTION_VALIDATE.equals(action)) {
            return validateToken(model);
        } else {
            return getSpotifyAccessToken(model);
        }
    }
    
    /**
     * 새 API 토큰 발급 처리
     */
    private String issueToken(Map<String, Object> model) {
        try {
            System.out.println("[TokenController] 새 토큰 발급 시작");
            
            // 새 토큰 발급 (static 메서드로 호출)
            String token = TokenManager.issueToken();
            model.put("token", token);
            
            System.out.println("[TokenController] 새 토큰 발급 완료: " + token.substring(0, 8) + "...");
            
            return "/WEB-INF/views/api/token.jsp";
        } catch (Exception e) {
            System.err.println("[TokenController] 토큰 발급 중 오류: " + e.getMessage());
            ErrorLogger.logError("토큰 발급 중 오류", e);
            model.put("error", "토큰 발급 중 오류가 발생했습니다: " + e.getMessage());
            return "/WEB-INF/views/error.jsp";
        }
    }
    
    /**
     * 토큰 유효성 검증 처리
     */
    private String validateToken(Map<String, Object> model) {
        try {
            // 토큰 유효성 검증 (static 메서드로 호출)
            String token = (String) model.get("token");
            
            System.out.println("[TokenController] 토큰 유효성 검증: " + (token != null ? token.substring(0, 8) + "..." : "null"));
            
            boolean isValid = TokenManager.validateToken(token);
            model.put("isValid", isValid);
            
            System.out.println("[TokenController] 토큰 유효성 검증 결과: " + (isValid ? "유효함" : "유효하지 않음"));
            
            return "/WEB-INF/views/api/validate.jsp";
        } catch (Exception e) {
            System.err.println("[TokenController] 토큰 유효성 검증 중 오류: " + e.getMessage());
            ErrorLogger.logError("토큰 유효성 검증 중 오류", e);
            model.put("error", "토큰 유효성 검증 중 오류가 발생했습니다: " + e.getMessage());
            return "/WEB-INF/views/error.jsp";
        }
    }
    
    /**
     * Spotify API 액세스 토큰 요청 처리
     */
    private String getSpotifyAccessToken(Map<String, Object> model) {
        try {
            System.out.println("[TokenController] Spotify API 액세스 토큰 요청 시작");
            
            // TokenManager를 통해 유효한 액세스 토큰 요청
            String accessToken = TokenManager.getAccessToken();
            
            // 모델에 액세스 토큰 저장 (JSP에서 사용)
            model.put("accessToken", accessToken);
            model.put("status", "SUCCESS");
            
            // 응답 헤더에 Content-Type 설정을 위한 처리
            model.put("contentType", "application/json");
            
            System.out.println("[TokenController] Spotify API 액세스 토큰 요청 성공");
            
            // JSON 응답을 위한 뷰 페이지 반환
            return "/WEB-INF/views/json/token.jsp";
        } catch (Exception e) {
            // 오류 발생 시 로그 출력 및 오류 모델 설정
            System.err.println("[TokenController] Spotify API 액세스 토큰 요청 실패: " + e.getMessage());
            e.printStackTrace();
            ErrorLogger.logError("Spotify API 액세스 토큰 요청 실패", e);
            
            model.put("error", "Access Token 가져오기 실패: " + e.getMessage());
            model.put("status", "ERROR");
            
            // 응답 헤더에 Content-Type 설정을 위한 처리
            model.put("contentType", "application/json");
            
            // JSON 오류 응답을 위한 뷰 페이지 반환
            return "/WEB-INF/views/json/token.jsp";
        }
    }
} 