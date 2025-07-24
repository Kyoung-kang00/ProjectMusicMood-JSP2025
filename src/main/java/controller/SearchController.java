package controller;

import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import utils.TokenManager;

/**
 * 음악 검색 컨트롤러
 * 음악 검색 기능을 처리하는 컨트롤러
 * 
 * 주요 기능:
 * 1. 음악 검색 요청 처리
 * 2. 검색 결과 정렬 및 필터링
 * 3. 검색 결과 페이지네이션
 * 4. 검색 히스토리 관리
 */
public class SearchController implements Controller {
    
    // 검색 결과 기본 개수
    private static final int DEFAULT_LIMIT = 20;
    
    /**
     * 음악 검색 처리를 수행합니다.
     * 
     * @param model 컨트롤러 공유 모델
     * @return 뷰 페이지 경로
     * @throws Exception API 요청 중 예외 발생 시
     */
    @Override
    public String execute(Map<String, Object> model) throws Exception {
        // 검색어 파라미터 추출
        String query = (String) model.get("query");
        
        // 검색어가 없으면 검색 폼 페이지로 이동
        if (query == null || query.trim().isEmpty()) {
            return "/WEB-INF/views/search/form.jsp";
        }
        
        // 검색 타입 파라미터 추출 (기본값: track)
        String type = (String) model.get("type");
        if (type == null || type.trim().isEmpty()) {
            type = "track";
        }
        
        // 페이지 관련 파라미터 처리
        int limit = DEFAULT_LIMIT;
        int offset = 0;
        
        String limitStr = (String) model.get("limit");
        String offsetStr = (String) model.get("offset");
        
        if (limitStr != null && !limitStr.trim().isEmpty()) {
            try {
                limit = Integer.parseInt(limitStr);
                if (limit < 1 || limit > 50) {
                    limit = DEFAULT_LIMIT;
                }
            } catch (NumberFormatException e) {
                // 파싱 오류 시 기본값 사용
            }
        }
        
        if (offsetStr != null && !offsetStr.trim().isEmpty()) {
            try {
                offset = Integer.parseInt(offsetStr);
                if (offset < 0) {
                    offset = 0;
                }
            } catch (NumberFormatException e) {
                // 파싱 오류 시 기본값 사용
            }
        }
        
        try {
            // Spotify API 액세스 토큰 가져오기
            String accessToken = TokenManager.getAccessToken();
            
            // 검색 요청 URL 구성
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String apiUrl = "https://api.spotify.com/v1/search?q=" + encodedQuery + 
                            "&type=" + type + "&limit=" + limit + "&offset=" + offset;
            
            // API 요청 실행
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            
            // 응답 코드 확인
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                // 응답 데이터 읽기
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                // 검색 결과와 파라미터를 모델에 저장
                model.put("searchResult", response.toString());
                model.put("query", query);
                model.put("type", type);
                model.put("limit", String.valueOf(limit));
                model.put("offset", String.valueOf(offset));
                
                // 검색 결과 페이지로 이동
                return "/WEB-INF/views/search/result.jsp";
            } else {
                // API 오류 시 에러 메시지 설정
                model.put("errorMessage", "Spotify API 오류: " + responseCode);
                return "/WEB-INF/views/search/form.jsp";
            }
        } catch (Exception e) {
            // 예외 발생 시 에러 메시지 설정
            model.put("errorMessage", "검색 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "/WEB-INF/views/search/form.jsp";
        }
    }
} 