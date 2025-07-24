<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Enumeration" %>
<%
    // 요청 디버그 정보 (개발 중에만 사용)
    boolean debugMode = false;
    StringBuilder debugInfo = new StringBuilder();
    
    if (debugMode) {
        debugInfo.append("Request Parameters:\n");
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            String value = request.getParameter(name);
            debugInfo.append(name).append("=").append(value).append("\n");
        }
        System.out.println(debugInfo.toString());
    }

    // 출력 버퍼 초기화
    out.clearBuffer();

    // 응답 헤더 설정
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    
    // 상태 메시지 가져오기
    String status = (String) request.getAttribute("status");
    String message = (String) request.getAttribute("message");
    
    if (message == null) {
        // 상태 코드에 따른 기본 메시지 설정
        if ("OK".equals(status)) {
            message = "요청이 성공적으로 처리되었습니다.";
        } else if ("BAD_REQUEST".equals(status)) {
            message = "잘못된 요청입니다.";
        } else if ("METHOD_NOT_ALLOWED".equals(status)) {
            message = "허용되지 않은 HTTP 메서드입니다.";
        } else if ("INTERNAL_SERVER_ERROR".equals(status)) {
            message = "서버 내부 오류가 발생했습니다.";
        } else {
            message = "알 수 없는 오류가 발생했습니다.";
        }
    }
    
    // JSON 이스케이프 처리
    if (message != null) {
        message = message.replace("\"", "\\\"")
                         .replace("\n", "\\n")
                         .replace("\r", "\\r")
                         .replace("\t", "\\t");
    }
    
    // 좋아요 목록 가져오기 (있는 경우)
    Object likedPlaylistIdsObj = request.getAttribute("likedPlaylistIdsJson");
    String likedPlaylistIdsJson = likedPlaylistIdsObj != null ? (String)likedPlaylistIdsObj : "[]";

    // 직접 JSON 문자열 생성
    StringBuilder json = new StringBuilder();
    json.append("{");
    json.append("\"status\":\"").append(status != null ? status : "ERROR").append("\",");
    json.append("\"message\":\"").append(message != null ? message : "").append("\",");
    json.append("\"likedPlaylistIds\":").append(likedPlaylistIdsJson);
    json.append("}");

    // 단일 출력
    %><%=json.toString()%> 