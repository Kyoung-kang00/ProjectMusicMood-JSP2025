package controller;

import java.util.Map;

import dao.UserDAO;
import dto.UserDTO;
import utils.ErrorLogger;

public class FindPasswordController implements Controller {
	
	private final UserDAO userDAO;
    
    /**
     * FindPasswordController 생성자
     * UserDAO 의존성 주입
     * 
     * @param userDAO 사용자 데이터 접근 객체
     */
    public FindPasswordController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    /**
     * 모델 데이터로 비밀번호 찾기 요청을 처리하는 메서드
     * 
     * @param model 컨트롤러 공유 모델
     * @return 처리 결과에 따른 뷰 경로
     * @throws Exception 처리 중 예외 발생 시
     */
    @Override
    public String execute(Map<String, Object> model) throws Exception {
        String method = (String) model.get("method");
        
        System.out.println("[FindPasswordController] 요청 처리 시작: " + method);
        
        if ("GET".equals(method)) {
            // 비밀번호 찾기 폼 표시
            return "/WEB-INF/views/findPassword.jsp";
        } else if ("POST".equals(method)) {
        	
        	System.out.println("[FindPasswordController] 비밀번호 찾기 로직 실행");
            
            // 비밀번호 찾기 처리 - 모델에서 필요한 데이터 추출
        	String email = (String) model.get("email");
            String name = (String) model.get("name");
            
            // 입력값 검증
            if (email == null || name == null || 
                email.trim().isEmpty() || name.trim().isEmpty()) {
                
                // 에러 메시지 설정
                model.put("error", "이메일과 이름을 모두 입력해주세요.");
                return "/WEB-INF/views/findPassword.jsp";
            }
            
            try {
                // 비밀번호 찾기 로직 실행
                UserDTO foundUser = userDAO.findPassword(email, name);
                
                if (foundUser != null) {
                    // 찾은 비밀번호를 모델에 저장
                    model.put("foundUser", foundUser);
                    model.put("message", "비밀번호를 찾았습니다.");
                    
                    // 비밀번호 찾기 결과 페이지로 이동
                    return "/WEB-INF/views/findPasswordResult.jsp";
                } else {
                    // 사용자 정보가 일치하지 않는 경우
                    model.put("error", "입력하신 정보와 일치하는 계정이 없습니다.");
                    return "/WEB-INF/views/findPassword.jsp";
                }
                
            } catch (Exception e) {
                ErrorLogger.logError("비밀번호 찾기 중 오류 발생", e);
                model.put("error", "비밀번호 찾기 중 오류가 발생했습니다: " + e.getMessage());
                return "/WEB-INF/views/findPassword.jsp";
            }
        }
        
        // 지원하지 않는 HTTP 메서드일 경우
        model.put("error", "지원하지 않는 요청 방식입니다.");
        return "/WEB-INF/views/error.jsp";
    }
}
