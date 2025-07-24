package controller;

import java.util.Map;

/**
 * 메인 페이지 컨트롤러
 * 애플리케이션의 메인 페이지를 처리하는 컨트롤러
 * 
 * 주요 기능:
 * 1. 메인 페이지 요청 처리
 * 2. 기본적인 웰컴 페이지 제공
 */
public class IndexController implements Controller {
    
    /**
     * 모델 데이터로 메인 페이지 요청을 처리하는 메서드
     * 
     * @param model 컨트롤러 공유 모델
     * @return 메인 페이지의 뷰 경로
     * @throws Exception 처리 중 예외 발생 시
     */
    @Override
    public String execute(Map<String, Object> model) throws Exception {
        // 간단히 메인 페이지로 이동
        return "/WEB-INF/views/index.jsp";
    }
}
