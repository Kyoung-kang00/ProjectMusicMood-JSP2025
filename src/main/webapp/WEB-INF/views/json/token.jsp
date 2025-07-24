<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // 응답 헤더 설정
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    
    // 응답 데이터 가져오기
    String accessToken = (String) request.getAttribute("accessToken");
    String status = (String) request.getAttribute("status");
    String error = (String) request.getAttribute("error");
%>
{
    "status": "<%= status %>",
    <% if (accessToken != null) { %>
    "access_token": "<%= accessToken %>",
    "token_type": "Bearer"
    <% } else if (error != null) { %>
    "error": "<%= error.replace("\"", "\\\"") %>"
    <% } %>
} 