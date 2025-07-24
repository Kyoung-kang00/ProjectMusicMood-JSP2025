package controller;

import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import utils.TokenManager;

/**
 * 음악 추천 컨트롤러
 * 사용자에게 맞춤형 음악을 추천하는 컨트롤러
 * 
 * 주요 기능:
 * 1. 사용자 기반 음악 추천
 * 2. 장르/분위기 기반 음악 추천
 * 3. 실시간 인기 음악 추천
 * 4. 추천 결과 필터링 및 정렬
 */
public class RecommendController implements Controller {
    
    /**
     * 모델 데이터로 음악 추천 요청을 처리하는 메서드
     * 
     * @param model 컨트롤러 공유 모델
     * @return 처리 결과에 따른 뷰 경로
     * @throws Exception 처리 중 예외 발생 시
     */
    @Override
    public String execute(Map<String, Object> model) throws Exception {
        // 추천 타입 확인 (user, genre, mood, popular)
        String type = (String) model.get("type"); 
        String genre = (String) model.get("genre");
        String mood = (String) model.get("mood");
        
        // 결과 제한 개수 설정
        int limit = 20; // 기본값
        String limitStr = (String) model.get("limit");
        
        try {
            if (limitStr != null && !limitStr.trim().isEmpty()) {
                limit = Integer.parseInt(limitStr);
            }
        } catch (NumberFormatException e) {
            // 기본값 사용
        }
        
        try {
            // 추천 결과 값을 모델에 추가
            model.put("type", type);
            model.put("genre", genre);
            model.put("mood", mood);
            model.put("limit", limit);
            model.put("recommendations", new ArrayList<>());
            
            // 추천 히스토리 저장
            saveRecommendationHistory(model, type, genre, mood);
            
            return "/WEB-INF/views/recommendations.jsp";
        } catch (Exception e) {
            model.put("error", "추천 결과를 가져오는 중 오류가 발생했습니다.");
            return "/WEB-INF/views/error.jsp";
        }
    }
    
    /**
     * 추천 히스토리를 저장하는 메서드
     * 
     * @param model 컨트롤러 공유 모델
     * @param type 추천 유형
     * @param genre 장르
     * @param mood 분위기
     */
    private void saveRecommendationHistory(Map<String, Object> model, 
                                         String type, String genre, String mood) {
        // 세션에서 추천 히스토리 가져오기
        Map<String, Object> session = (Map<String, Object>) model.get("session");
        
        if (session == null) {
            return; // 세션이 없으면 저장하지 않음
        }
        
        @SuppressWarnings("unchecked")
        List<RecommendationHistory> history = (List<RecommendationHistory>) 
            session.get("recommendationHistory");
        
        if (history == null) {
            history = new ArrayList<>();
            session.put("recommendationHistory", history);
        }
        
        history.add(new RecommendationHistory(type, genre, mood, new Date()));
        
        // 최대 10개까지만 저장
        if (history.size() > 10) {
            history.remove(0);
        }
    }
    
    /**
     * 추천 히스토리를 저장하기 위한 내부 클래스
     */
    private static class RecommendationHistory {
        private String type;
        private String genre;
        private String mood;
        private Date timestamp;
        
        public RecommendationHistory(String type, String genre, String mood, 
                                   Date timestamp) {
            this.type = type;
            this.genre = genre;
            this.mood = mood;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getType() { return type; }
        public String getGenre() { return genre; }
        public String getMood() { return mood; }
        public Date getTimestamp() { return timestamp; }
    }
} 