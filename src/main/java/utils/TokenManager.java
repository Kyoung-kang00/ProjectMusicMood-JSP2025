package utils;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;

/**
 * Spotify API 액세스 토큰을 관리하는 유틸리티 클래스
 * 토큰 발급, 재발급 및 캐싱 기능을 제공합니다.
 */
public class TokenManager {
    // 로깅
    private static final Logger logger = Logger.getLogger(TokenManager.class.getName());
    
    // 토큰 정보를 저장하는 정적 변수
    private static String accessToken = null;  // 액세스 토큰 값
    private static long expireTime = 0;        // 토큰 만료 시간 (UNIX timestamp ms)

    // Spotify API 자격 증명 (클라이언트 ID 및 시크릿)
    private static final String CLIENT_ID = "74f9a7ebc98d444eb786e13aee2e6d06";
    private static final String CLIENT_SECRET = "812873876a1c4201a6ec0b2ccb07f911";
    
    // 토큰 요청을 위한 URL 및 타임아웃 설정
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";
    private static final int CONNECTION_TIMEOUT = 10000; // 10초
    private static final int READ_TIMEOUT = 10000; // 10초

    // 토큰 유효성 검증을 위한 토큰 저장소
    private static Map<String, Long> validTokens = new HashMap<>();
    
    // 토큰 만료 기간 (24시간)
    private static final long TOKEN_VALIDITY_PERIOD = 24 * 60 * 60 * 1000;
    
    // 토큰 갱신 임계값 (만료 5분 전)
    private static final long TOKEN_REFRESH_THRESHOLD = 5 * 60 * 1000;

    /**
     * 새로운 API 접근 토큰을 발급
     * 
     * @return 발급된 API 토큰
     */
    public static String issueToken() {
        logger.info("새 API 액세스 토큰 발급 시작");
        
        // UUID를 사용하여 고유한 토큰 생성
        String token = UUID.randomUUID().toString();
        
        // 토큰 유효 기간 설정 (24시간)
        long expiresAt = System.currentTimeMillis() + TOKEN_VALIDITY_PERIOD;
        
        // 유효한 토큰 목록에 저장
        validTokens.put(token, expiresAt);
        
        logger.info("새 API 액세스 토큰 발급 완료: " + token.substring(0, 8) + "...");
        
        return token;
    }
    
    /**
     * 토큰의 유효성을 검증
     * 
     * @param token 검증할 토큰
     * @return 유효 여부
     */
    public static boolean validateToken(String token) {
        // 토큰이 없으면 유효하지 않음
        if (token == null || token.trim().isEmpty()) {
            logger.warning("유효하지 않은 토큰: 토큰이 null이거나 비어 있습니다.");
            return false;
        }
        
        // 토큰이 저장소에 없으면 유효하지 않음
        if (!validTokens.containsKey(token)) {
            logger.warning("유효하지 않은 토큰: 저장소에 없는 토큰입니다.");
            return false;
        }
        
        // 토큰 만료 시간 확인
        long expiresAt = validTokens.get(token);
        boolean isValid = expiresAt > System.currentTimeMillis();
        
        // 만료된 토큰은 목록에서 제거
        if (!isValid) {
            logger.info("만료된 토큰 제거: " + token.substring(0, 8) + "...");
            validTokens.remove(token);
        } else {
            logger.info("토큰 유효성 검증 성공: " + token.substring(0, 8) + "...");
        }
        
        return isValid;
    }

    /**
     * 유효한 액세스 토큰을 가져옵니다.
     * 토큰이 없거나 만료된 경우 자동으로 새 토큰을 발급받습니다.
     * 
     * @return Spotify API 액세스 토큰
     * @throws IOException API 연결 또는 요청 중 오류 발생 시
     */
    public static synchronized String getAccessToken() throws IOException {
        long now = System.currentTimeMillis();
        
        System.out.println("[TokenManager] getAccessToken 호출됨");

        // 토큰이 없거나, 만료되었거나, 만료 임계값(5분) 전이면 새로 발급
        if (accessToken == null || now >= expireTime - TOKEN_REFRESH_THRESHOLD) {
            if (accessToken == null) {
                System.out.println("[TokenManager] 액세스 토큰이 없음, 새 토큰 요청");
            } else {
                System.out.println("[TokenManager] 토큰 만료 또는 곧 만료 예정 (현재: " + now + ", 만료: " + expireTime + ", 차이: " + (expireTime - now) + "ms)");
            }
            logger.info("토큰 만료 또는 미발급 상태. 새 토큰 요청 시작");
            refreshToken();
        } else {
            System.out.println("[TokenManager] 유효한 토큰 반환 (만료까지 " + ((expireTime - now) / 1000) + "초 남음)");
            logger.fine("유효한 액세스 토큰 반환 (만료까지 " + ((expireTime - now) / 1000) + "초 남음)");
        }

        return accessToken;
    }

    /**
     * Spotify API에서 새 액세스 토큰을 요청하고 저장합니다.
     * 
     * @throws IOException API 연결 또는 요청 중 오류 발생 시
     */
    private static void refreshToken() throws IOException {
        HttpURLConnection conn = null;
        try {
            System.out.println("[TokenManager] refreshToken 시작");
            logger.info("Spotify API에서 새 액세스 토큰 요청 시작");
            
            // Spotify 토큰 엔드포인트 URL 생성
            URL url = new URL(TOKEN_URL);
            System.out.println("[TokenManager] 토큰 요청 URL: " + TOKEN_URL);
            
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);

            // Basic 인증 헤더 생성 (Base64 인코딩)
            String auth = CLIENT_ID + ":" + CLIENT_SECRET;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            
            System.out.println("[TokenManager] 인증 헤더 설정 완료, CLIENT_ID 길이: " + CLIENT_ID.length() + ", CLIENT_SECRET 길이: " + CLIENT_SECRET.length());

            // 요청 본문에 grant_type 파라미터 추가
            try (OutputStream os = conn.getOutputStream()) {
                String requestBody = "grant_type=client_credentials";
                os.write(requestBody.getBytes());
                os.flush();
                System.out.println("[TokenManager] 요청 본문 작성 완료: " + requestBody);
            }

            // 응답 코드 확인
            int responseCode = conn.getResponseCode();
            System.out.println("[TokenManager] 응답 코드: " + responseCode);
            
            if (responseCode != 200) {
                // 오류 응답 읽기
                StringBuilder errorResponse = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                }
                
                String errorMessage = "Spotify API 요청 실패. 응답 코드: " + responseCode + ", 오류 메시지: " + errorResponse.toString();
                System.err.println("[TokenManager] " + errorMessage);
                logger.severe(errorMessage);
                throw new IOException(errorMessage);
            }

            // 응답 데이터 읽기
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            // JSON 응답 파싱
            String responseStr = response.toString();
            System.out.println("[TokenManager] 응답 데이터 (일부): " + (responseStr.length() > 50 ? responseStr.substring(0, 50) + "..." : responseStr));
            
            accessToken = extractValue(responseStr, "access_token");
            
            if (accessToken == null || accessToken.isEmpty()) {
                String errorMessage = "액세스 토큰 추출 실패. 응답: " + responseStr;
                System.err.println("[TokenManager] " + errorMessage);
                logger.severe(errorMessage);
                throw new IOException(errorMessage);
            }
            
            System.out.println("[TokenManager] 액세스 토큰 추출 성공 (길이: " + accessToken.length() + ")");
            
            int expiresIn = Integer.parseInt(extractValue(responseStr, "expires_in"));
            System.out.println("[TokenManager] 토큰 만료 시간: " + expiresIn + "초");

            // 현재 시간 + expires_in(초) - 5초(여유분)
            expireTime = System.currentTimeMillis() + (expiresIn * 1000) - (5 * 1000);
            
            System.out.println("[TokenManager] 새 토큰 발급 완료. 만료 시간: " + new java.util.Date(expireTime));
            logger.info("새 토큰 발급 완료. 만료 시간: " + new java.util.Date(expireTime));
            
        } catch (Exception e) {
            System.err.println("[TokenManager] 토큰 갱신 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            logger.log(Level.SEVERE, "토큰 갱신 중 오류 발생: " + e.getMessage(), e);
            throw new IOException("토큰 갱신 실패: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
                System.out.println("[TokenManager] 연결 종료");
            }
        }
    }

    /**
     * JSON 문자열에서 특정 키의 값을 추출합니다.
     * 
     * @param json JSON 형식의 문자열
     * @param key 추출할 값의 키
     * @return 키에 해당하는 값, 키가 없는 경우 null
     */
    private static String extractValue(String json, String key) {
        // 간단한 JSON 파싱 (실제 서비스에서는 JSON 라이브러리 사용 권장)
        String[] parts = json.split("\"" + key + "\":");
        if (parts.length > 1) {
            String value = parts[1].split("[,}]")[0].replaceAll("\"", "").trim();
            return value;
        }
        return null;
    }
    
    /**
     * 유효하지 않은 토큰을 정리합니다.
     * 만료된 토큰을 저장소에서 제거합니다.
     */
    public static void cleanupExpiredTokens() {
        logger.info("만료된 토큰 정리 시작. 현재 토큰 수: " + validTokens.size());
        
        long now = System.currentTimeMillis();
        int removedCount = 0;
        
        // 저장소에서 만료된 토큰 제거
        for (Map.Entry<String, Long> entry : new HashMap<>(validTokens).entrySet()) {
            if (entry.getValue() <= now) {
                validTokens.remove(entry.getKey());
                removedCount++;
            }
        }
        
        logger.info("만료된 토큰 정리 완료. 제거된 토큰 수: " + removedCount + ", 남은 토큰 수: " + validTokens.size());
    }
}
