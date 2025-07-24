package controller;

import java.util.Map;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import dao.UserDAO;
import dto.UserDTO;

/**
 * 인증 컨트롤러
 * 사용자 로그인/로그아웃 처리를 담당하는 컨트롤러
 * 
 * 주요 기능:
 * 1. 로그인 처리
 * 2. 로그아웃 처리
 * 3. 세션 관리
 * 4. 인증 상태 확인
 */
public class AuthController implements Controller {
    
    // 데이터 액세스 객체
    private final UserDAO userDAO;
    
    /**
     * AuthController 생성자
     * UserDAO 의존성 주입
     * 
     * @param userDAO 사용자 데이터 접근 객체
     */
    public AuthController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * 로그인 히스토리를 저장하기 위한 내부 클래스
     */
    private static class LoginHistory {
        private String email;
        private String ipAddress;
        private Date timestamp;
        
        public LoginHistory(String email, String ipAddress, Date timestamp) {
            this.email = email;
            this.ipAddress = ipAddress;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getEmail() { return email; }
        public String getIpAddress() { return ipAddress; }
        public Date getTimestamp() { return timestamp; }
    }

    @Override
    public String execute(Map<String, Object> model) throws Exception {
        String action = (String) model.get("action");
        String method = (String) model.get("method");
        
        System.out.println("[AUTH] 요청 메서드: " + method + ", 액션: " + action);
        
        // 세션 가져오기
        // **세션은 현재 사용자의 정보를 저장하는 객체임.** 
        // 예를 들어 로그인 정보를 Dispatcher에서 가져오면 세션에 저장된 정보를 가져옴.
        javax.servlet.http.HttpSession session = 
            (javax.servlet.http.HttpSession) model.get("session");
        
        // 컨텍스트 경로 가져오기
        // **컨텍스트 경로는 현재 시스템 경로 확인하는 하는 것임.**
        // 예를 들어 현재 시스템 경로가 /MusicMood 이면 contextPath는 /MusicMood 이다.
        String contextPath = (String) model.get("contextPath");
        if (contextPath == null) {
            contextPath = "";
        }
        
        // 액션 파라미터에 따른 처리
        // **action이 logout이면 로그아웃 처리를 한다.**
        if ("logout".equals(action)) {
            // 로그아웃 처리 (세션 무효화)
            if (session != null) {
                session.invalidate();
            }
            System.out.println("[AUTH] 로그아웃 성공: 리다이렉트=/index.do");
            return "redirect:/index.do";
        } 
        
        // 로그인 처리
        // **method가 POST이면 로그인 처리를 한다.**
        if ("POST".equals(method)) {
            // 로그인 처리
            String email = (String) model.get("email");
            String password = (String) model.get("password");
            
            // 디버깅을 위한 로그 추가
            System.out.println("[AUTH] 로그인 시도: email=" + email);
            
            // 입력값 확인
            // **email과 password가 비어있으면 로그인 실패 처리를 한다.**
            if (email == null || password == null ||
                email.trim().isEmpty() || password.trim().isEmpty()) {
                model.put("error", "이메일과 비밀번호를 입력해주세요.");
                return "/WEB-INF/views/login.jsp";
            }
            
            try {
                // UserDAO의 로직 디버깅
                System.out.println("[AUTH] UserDAO.exist 호출: " + email);
                
                // 사용자 인증 (email로 사용자 조회) 
                // userDAO.exist는 userDAO에 public UserDTO exist(String email, String password) 으로 선언했음.
                UserDTO user = userDAO.exist(email, password);
                
                // 사용자 정보 디버깅
                System.out.println("[AUTH] UserDAO.exist 결과: " + (user != null ? "사용자 찾음" : "사용자 없음"));
                
                if (user != null) {
                    // 로그인 성공
                    session.setAttribute("user", user);
                    session.setAttribute("loginTime", new Date());
                    
                    // 로그 추가
                    System.out.println("[AUTH] 로그인 성공: " + email + ", 세션 ID: " + session.getId());
                    
                    // 로그인 히스토리 저장
                    saveLoginHistory(session, user, (String) model.get("remoteAddr"));
                    
                    System.out.println("[AUTH] 리다이렉트: /index.do");
                    return "redirect:/index.do";
                } else {
                    // 로그인 실패
                    System.out.println("[AUTH] 로그인 실패: " + email + " (비밀번호 불일치 또는 계정 없음)");
                    model.put("error", "이메일 또는 비밀번호가 일치하지 않습니다.");
                    return "/WEB-INF/views/login.jsp";
                }
            } catch (Exception e) {
                System.out.println("[AUTH] 로그인 처리 중 오류: " + e.getMessage());
                e.printStackTrace();
                model.put("error", "로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
                return "/WEB-INF/views/error.jsp";
            }
        }
        
        // GET 요청이거나 기본 처리 - 로그인 폼 표시
        // **method가 GET이면 로그인 폼을 표시한다.**
        return "/WEB-INF/views/login.jsp";
    }
    
    /**
     * 로그인 히스토리를 저장하는 메서드
     * 
     * @param session 세션 객체
     * @param user 로그인한 사용자 정보
     * @param remoteAddr 원격 IP 주소
     */
    private void saveLoginHistory(javax.servlet.http.HttpSession session, UserDTO user, String remoteAddr) {
        // 세션에 로그인 히스토리 저장
        @SuppressWarnings("unchecked")
        List<LoginHistory> history = (List<LoginHistory>) session.getAttribute("loginHistory");
        
        if (history == null) {
            history = new ArrayList<>();
            session.setAttribute("loginHistory", history);
        }
        
        history.add(new LoginHistory(user.getEmail(), remoteAddr, new Date()));
        
        // 최대 10개까지만 저장
        if (history.size() > 10) {
            history.remove(0);
        }
    }
} 