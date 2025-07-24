package frontController;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dto.UserDTO;
import controller.Controller;
import listeners.ContextLoaderListener;
import utils.ErrorLogger;

// 프론트 컨트롤러 - 모든 .do 요청 및 /api/* 요청을 처리
@WebServlet(urlPatterns = {"*.do", "/api/*"}) 
public class Dispatcher extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    public void init() throws ServletException {
        super.init();
        // 애플리케이션 컨텍스트 초기화
        ContextLoaderListener.contextInitialized();
    }
    
    public void destroy() {
        // 애플리케이션 컨텍스트 정리
        ContextLoaderListener.contextDestroyed();
        super.destroy();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        // 전체 요청 URI와 컨텍스트 경로 확인 (디버깅용)
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        System.out.println("요청 URI: " + requestURI);
        System.out.println("컨텍스트 경로: " + contextPath);
        
        // 요청 경로를 읽어온다 (예: /auth/login.do, /member/list.do)
        String servletPath = req.getServletPath();
        System.out.println("서블릿 경로: " + servletPath);
        
        // API 경로 정규화
        if (servletPath.startsWith("/api/") || servletPath.equals("/api")) {
            // API 요청이면 /token.do로 매핑 (TokenController를 호출하기 위함)
            servletPath = "/token.do";
            System.out.println("API 요청 감지: " + servletPath + " (원본 경로: " + req.getRequestURI() + ")");
        }
        
        // 업캐스팅을 위해 Controller 인터페이스 타입 선언
        Controller pageController = null;
        
        try {
            // 모델 데이터를 담을 HashMap 준비
            HashMap<String, Object> model = new HashMap<>();
            model.put("session", req.getSession()); // 세션 넣기
            
            // 요청과 응답 객체를 모델에 추가
            model.put("request", req);
            model.put("response", resp);
            
            // 요청 메서드 정보 추가
            model.put("method", req.getMethod());
            
            // 컨텍스트 경로 추가
            model.put("contextPath", req.getContextPath());
            
            // URL 파라미터 처리 - 모든 요청 파라미터 추출
            extractParameters(req, model);
            
            // POST 요청의 경우 필요한 파라미터 추가
            if (servletPath.equals("/auth/login.do") && req.getMethod().equals("POST")) {
                String email = req.getParameter("email");
                String password = req.getParameter("password");
                
                if (email != null && password != null) {
                    // 로그인 정보 모델에 추가
                    model.put("loginInfo", new UserDTO()
                        .setEmail(email)
                        .setPassword(password));
                }
            }
            
            // ServletContext에서 해당 경로에 맞는 컨트롤러 가져오기
            ServletContext sc = this.getServletContext();
            pageController = (Controller) sc.getAttribute(servletPath);
            
            // 컨트롤러를 찾지 못한 경우 ContextLoaderListener에서 가져오기 시도
            if (pageController == null) {
                pageController = ContextLoaderListener.getController(servletPath);
                
                // 컨트롤러를 찾으면 ServletContext에 등록
                if (pageController != null) {
                    sc.setAttribute(servletPath, pageController);
                }
            }
            
            if(pageController == null) {
                String errorMessage = "요청한 서비스를 찾을 수 없습니다: " + servletPath;
                ErrorLogger.logError(errorMessage);
                
                // API 요청의 경우 JSON 오류 응답 제공
                if (servletPath.startsWith("/api") || req.getRequestURI().contains("/api/")) {
                    resp.setContentType("application/json; charset=UTF-8");
                    resp.getWriter().write("{\"status\":\"ERROR\",\"error\":\"" + errorMessage + "\"}");
                    return;
                }
                
                throw new Exception(errorMessage);
            }
            
            // 페이지 컨트롤러 실행 후 뷰 경로 받기
            String viewUrl = pageController.execute(model);
            
            // Content-Type 설정 확인
            if (model.containsKey("contentType")) {
                resp.setContentType((String)model.get("contentType"));
            }
            
            // 모델에 담긴 데이터를 request 속성으로 복사
            for(String key: model.keySet()) {
                req.setAttribute(key, model.get(key));
            }
            
            // 리다이렉트 또는 포워드 처리
            if(viewUrl.startsWith("redirect:")) {
                String redirectUrl = viewUrl.substring(9); // "redirect:" 제거 후 리다이렉트
                
                // 절대경로(/)로 시작하지 않고 상대경로인 경우 컨텍스트 경로 추가
                if (!redirectUrl.startsWith("/") && !redirectUrl.startsWith("http")) {
                    redirectUrl = req.getContextPath() + "/" + redirectUrl;
                } else if (redirectUrl.startsWith("/")) {
                    // 절대경로지만 외부 URL이 아닌 경우도 컨텍스트 경로 추가
                    redirectUrl = req.getContextPath() + redirectUrl;
                }
                
                System.out.println("[Dispatcher] 리다이렉트: " + redirectUrl);
                resp.sendRedirect(redirectUrl);
                return;
            } else {
                RequestDispatcher rd = req.getRequestDispatcher(viewUrl);
                rd.include(req, resp);
            }
        } catch (Exception e) {
            // 에러 로깅
            ErrorLogger.logError("요청 처리 중 오류 발생: " + servletPath, e);
            
            // API 요청의 경우 JSON 오류 응답
            if (servletPath.startsWith("/api") || req.getRequestURI().contains("/api/")) {
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().write("{\"status\":\"ERROR\",\"error\":\"" + e.getMessage() + "\"}");
                return;
            }
            
            // 에러 정보를 request에 저장
            req.setAttribute("error", e);
            req.setAttribute("errorMessage", "페이지를 처리하는 중 오류가 발생했습니다.");
            
            // 에러 페이지로 포워드
            RequestDispatcher rd = req.getRequestDispatcher("/WEB-INF/views/error.jsp");
            rd.forward(req, resp);
        }
    }
    
    /**
     * 요청 파라미터를 모델에 추출하는 유틸리티 메서드
     */
    private void extractParameters(HttpServletRequest req, HashMap<String, Object> model) {
        // 요청 파라미터 이름 목록 가져오기
        java.util.Enumeration<String> paramNames = req.getParameterNames();
        
        // 각 파라미터의 값을 모델에 추가
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            String value = req.getParameter(name);
            
            // 파라미터 값이 있으면 모델에 추가
            if (value != null && !value.isEmpty()) {
                model.put(name, value);
                System.out.println("[파라미터 추출] " + name + " = " + value);
            }
        }
        
        // 다중 값 파라미터 처리 (체크박스 등)
        for (String name : req.getParameterMap().keySet()) {
            String[] values = req.getParameterValues(name);
            if (values != null && values.length > 1) {
                model.put(name + "List", values);
                System.out.println("[다중 파라미터 추출] " + name + "List = " + String.join(", ", values));
            }
        }
    }
}