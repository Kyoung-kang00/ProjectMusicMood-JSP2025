package controller;

import java.util.Map;
import dao.UserDAO;
import dto.UserDTO;

/**
 * 프로필 설정 컨트롤러
 * 사용자 프로필 정보 조회 및 수정을 담당하는 컨트롤러
 */
public class ProfileController implements Controller {
    
    private final UserDAO userDAO;
    
    /**
     * 생성자를 통한 UserDAO 의존성 주입
     * 
     * @param userDAO 사용자 데이터 접근 객체
     */
    public ProfileController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    @Override
    public String execute(Map<String, Object> model) throws Exception {
        String method = (String) model.get("method");
        
        // 세션 가져오기
        javax.servlet.http.HttpSession session = (javax.servlet.http.HttpSession) model.get("session");
        UserDTO user = (UserDTO) session.getAttribute("user");
        
        // 세션에 사용자 정보가 없는 경우 로그인 페이지로 리다이렉트
        if (user == null) {
            return "redirect:/auth/login.do";
        }
        
        // 현재 사용자 정보를 모델에 추가
        model.put("currentUser", user);
        
        // POST 요청 처리 - 프로필 정보 업데이트
        if ("POST".equals(method)) {
            String fieldToUpdate = (String) model.get("field");
            String newValue = (String) model.get("value");
            
            System.out.println("[Profile] 필드 수정 요청: " + fieldToUpdate + " = " + newValue);
            
            if (fieldToUpdate != null && newValue != null) {
                // 사용자 정보 복사 (기존 값 유지)
                UserDTO updatedUser = new UserDTO()
                    .setEmail(user.getEmail()) // 식별자로 사용
                    .setName(user.getName() != null ? user.getName() : "")
                    .setPassword(user.getPassword() != null ? user.getPassword() : "")
                    .setPhone(user.getPhone() != null ? user.getPhone() : ""); // phone이 null이면 빈 문자열로
                
                System.out.println("[Profile] 현재 값 - 이름: " + updatedUser.getName() + 
                                   ", 전화: " + updatedUser.getPhone() + 
                                   ", 비밀번호: ******");
                
                // 수정할 필드 값만 설정
                switch (fieldToUpdate) {
                    case "name":
                        updatedUser.setName(newValue);
                        break;
                    case "password":
                        updatedUser.setPassword(newValue);
                        break;
                    case "phone":
                        updatedUser.setPhone(newValue);
                        break;
                    case "email":
                        // 이메일은 식별자로 사용되므로 특별 처리 필요
                        // 현재는 이메일 변경을 지원하지 않음
                        model.put("error", "이메일 변경은 현재 지원되지 않습니다.");
                        return "/WEB-INF/views/profile.jsp";
                }
                
                System.out.println("[Profile] 변경 후 값 - 필드: " + fieldToUpdate + 
                                   ", 새 값: " + ("password".equals(fieldToUpdate) ? "******" : newValue));
                
                // 사용자 정보 업데이트
                int result = userDAO.update(updatedUser);
                
                if (result > 0) {
                    // 업데이트 성공 시 세션 정보도 업데이트
                    // 비밀번호 변경 시에는 새 비밀번호로 조회, 그렇지 않으면 기존 비밀번호로 조회
                    String passwordForQuery = "password".equals(fieldToUpdate) ? updatedUser.getPassword() : user.getPassword();
                    UserDTO refreshedUser = userDAO.exist(user.getEmail(), passwordForQuery);
                    
                    System.out.println("[Profile] 업데이트 후 사용자 조회: " + (refreshedUser != null ? "성공" : "실패"));
                    
                    if (refreshedUser != null) {
                        session.setAttribute("user", refreshedUser);
                        model.put("currentUser", refreshedUser);
                    }
                    model.put("message", "프로필 정보가 성공적으로 업데이트 되었습니다.");
                } else {
                    model.put("error", "프로필 업데이트에 실패했습니다. 다시 시도해주세요.");
                }
            }
        }
        
        // GET 요청 처리 - 프로필 페이지 표시
        return "/WEB-INF/views/profile.jsp";
    }
} 