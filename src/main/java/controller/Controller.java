package controller;

import java.util.Map;

/**
 * 컨트롤러 인터페이스
 * 모든 컨트롤러 클래스가 구현해야 하는 기본 인터페이스
 * 
 * 주요 기능:
 * 1. 클라이언트의 요청을 처리하는 메서드 정의
 * 2. 요청 처리 결과에 따른 뷰 선택
 * 3. 모델과 뷰 사이의 중재자 역할
 */
public interface Controller {
    
    /**
     * 모델 데이터로 요청을 처리하는 메서드
     * 프론트 컨트롤러인 Dispatcher에서 호출하는 메서드
     * 
     * @param model 컨트롤러 공유 모델
     * @return 처리 결과에 따른 뷰 경로
     * @throws Exception 처리 중 예외 발생 시
     */
    String execute(Map<String, Object> model) throws Exception;
} 