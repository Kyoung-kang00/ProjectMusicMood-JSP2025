package filter;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 인코딩 필터
 * 모든 요청과 응답에 대해 UTF-8 인코딩을 적용하는 필터
 * 
 * 주요 기능:
 * 1. 모든 요청에 대해 UTF-8 인코딩 설정
 * 2. 모든 응답에 대해 UTF-8 인코딩 설정
 * 3. 한글 깨짐 현상 방지
 */
@WebFilter(
    urlPatterns = "/*",  // 모든 URL 패턴에 대해 필터 적용
    initParams = {
        @WebInitParam(name = "encoding", value = "UTF-8")  // 기본 인코딩 설정
    }
)
public class EncodingFilter implements Filter {
    /**
     * 인코딩 설정을 저장하는 변수
     * 필터 초기화 시 web.xml 또는 애노테이션에서 설정된 값을 저장
     */
    private String encoding;
    
    /**
     * 필터 초기화 메서드
     * 필터가 생성될 때 한 번만 호출됨
     * 
     * @param filterConfig 필터 설정 정보를 담고 있는 객체
     * @throws ServletException 필터 초기화 중 오류 발생 시
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // web.xml 또는 애노테이션에서 설정된 인코딩 값을 가져옴
        encoding = filterConfig.getInitParameter("encoding");
        // 설정된 값이 없으면 기본값으로 UTF-8 사용
        if (encoding == null) {
            encoding = "UTF-8";
        }
    }
    
    /**
     * 실제 필터링 작업을 수행하는 메서드
     * 모든 요청마다 호출됨
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
        
        // 요청 인코딩 설정 (클라이언트로부터 받는 데이터의 인코딩)
        req.setCharacterEncoding(encoding);
        // 응답 인코딩 설정 (클라이언트로 보내는 데이터의 인코딩)
        resp.setCharacterEncoding(encoding);
        
        // 다음 필터 또는 서블릿으로 요청 전달
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