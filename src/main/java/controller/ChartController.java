package controller;

import java.util.Map;

/**
 * 차트 컨트롤러
 * 음악 차트 페이지를 처리하는 컨트롤러
 * 
 * 주요 기능:
 * 1. 차트 페이지 표시
 * 2. 차트 데이터 관리
 */
public class ChartController implements Controller {

    /**
     * 차트 컨트롤러의 주요 실행 메서드
     * 
     * @param model 컨트롤러 공유 모델 (요청 및 응답 데이터 포함)
     * @return 처리 결과를 보여줄 뷰의 경로
     * @throws Exception 처리 중 오류 발생 시
     */
    @Override
    public String execute(Map<String, Object> model) throws Exception {
        System.out.println("[CHART] 차트 페이지 요청 처리");
        
        // 차트 페이지를 표시
        return "/WEB-INF/views/chart.jsp";
    }
} 