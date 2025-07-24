package filter;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 인증 필터
 * 보호된 리소스에 대한 접근을 제어하는 필터
 * 
 * 주요 기능:
 * 1. 특정 URL 패턴에 대한 접근 제어
 * 2. 로그인하지 않은 사용자의 접근 차단
 * 3. 인증된 사용자만 접근 가능한 리소스 보호
 * 
 * 보호되는 URL 패턴:
 * - /member/* : 회원 관련 페이지
 * - /playlist/private/* : 비공개 플레이리스트
 * - /settings/* : 설정 페이지
 */
@WebFilter(
    urlPatterns = {
        "/member/*",
        "/playlist/private/*",
        "/settings/*"
    }
)
public class AuthenticationFilter implements Filter {
    
    /**
     * 필터 초기화 메서드
     * 필터가 생성될 때 한 번만 호출됨
     * 
     * @param filterConfig 필터 설정 정보를 담고 있는 객체
     * @throws ServletException 필터 초기화 중 오류 발생 시
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 현재는 특별한 초기화 작업이 필요 없음
    }
    
    /**
     * 실제 인증 검사를 수행하는 메서드
     * 보호된 리소스에 대한 모든 요청마다 호출됨
     * 
     * @param request 클라이언트의 요청 객체
     * @param response 서버의 응답 객체
     * @param chain 다음 필터 또는 서블릿으로 요청을 전달하는 객체
     * @throws IOException 입출력 오류 발생 시
     * @throws ServletException 서블릿 처리 중 오류 발생 시
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // HTTP 요청/응답 객체로 변환
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        
        // 현재 세션 가져오기 (새로운 세션은 생성하지 않음)
        HttpSession session = req.getSession(false);
        
        // 세션이 없거나 사용자 정보가 없는 경우 (로그인하지 않은 상태)
        if (session == null || session.getAttribute("user") == null) {
            // 로그인 페이지로 리다이렉트
            resp.sendRedirect(req.getContextPath() + "/auth/login.do");
            return;
        }
        
        // 인증된 사용자인 경우 다음 필터 또는 서블릿으로 요청 전달
        chain.doFilter(request, response);
    }
    
    /**
     * 필터 종료 시 호출되는 메서드
     * 필터가 제거될 때 한 번만 호출됨
     * 리소스 정리 등의 작업을 수행
     */
    @Override
    public void destroy() {
        // 현재는 특별한 정리 작업이 필요 없음
    }
} 