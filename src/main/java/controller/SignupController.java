package controller;

import java.util.Map;

import dao.UserDAO;
import dto.UserDTO;

/**
 * 회원가입 컨트롤러
 * 사용자 회원가입 처리를 담당하는 컨트롤러
 * 
 * 주요 기능:
 * 1. 회원가입 폼 표시
 * 2. 회원가입 정보 유효성 검증
 * 3. 회원 정보 저장
 * 4. 회원가입 결과 처리
 */
public class SignupController implements Controller {
    
    private final UserDAO userDAO;
    
    /**
     * SignupController 생성자
     * UserDAO 의존성 주입
     * 
     * @param userDAO 사용자 데이터 접근 객체
     */
    public SignupController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    /**
     * 모델 데이터로 회원가입 요청을 처리하는 메서드
     * 
     * @param model 컨트롤러 공유 모델
     * @return 처리 결과에 따른 뷰 경로
     * @throws Exception 처리 중 예외 발생 시
     */
    @Override
    public String execute(Map<String, Object> model) throws Exception {
        String method = (String) model.get("method");
        
        if ("GET".equals(method)) {
            // 회원가입 폼 표시
            return "/WEB-INF/views/signup.jsp";
        } else if ("POST".equals(method)) {
            // 회원가입 처리 - 모델에서 필요한 데이터 추출
            String username = (String) model.get("name");
            String password = (String) model.get("password");
            String email = (String) model.get("email");
            String phone = (String) model.get("phone");
            
            // 입력값 검증
            if (username == null || password == null || email == null ||
                username.trim().isEmpty() || password.trim().isEmpty() || email.trim().isEmpty()) {
                
                // 에러 메시지 설정
                model.put("error", "모든 필드를 입력해주세요.");
                return "/WEB-INF/views/signup.jsp";
            }
                     
            try {
                // 사용자 정보 생성
                UserDTO user = new UserDTO()
                    .setEmail(email)
                    .setPassword(password)
                    .setName(username)
                    .setPhone(phone);
                
                // 사용자 정보 저장 (insert 사용)
                int result = userDAO.insert(user);
                
                if (result > 0) {
                    // 회원가입 성공 시 로그인 페이지로 리다이렉트
                    // Dispatcher가 컨텍스트 경로를 처리하므로 경로 형식 통일
                    return "redirect:auth/login.do?action=login";
                } else {
                    model.put("error", "회원가입에 실패했습니다.");
                    return "/WEB-INF/views/signup.jsp";
                }
            } catch (Exception e) {
                model.put("error", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
                return "/WEB-INF/views/signup.jsp";
            }
        }
        
        // 지원하지 않는 HTTP 메서드일 경우
        model.put("error", "지원하지 않는 요청 방식입니다.");
        return "/WEB-INF/views/error.jsp";
    }
}
