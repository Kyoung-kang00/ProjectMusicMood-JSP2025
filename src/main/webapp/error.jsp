<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>MusicMood - 에러 발생</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
    <style>
        .error-container {
            max-width: 800px;
            margin: 50px auto;
            padding: 30px;
            background-color: #fff;
            border-radius: 10px;
            box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
            text-align: center;
        }
        
        .error-icon {
            font-size: 80px;
            margin-bottom: 20px;
            color: #ff5252;
        }
        
        .error-title {
            font-size: 28px;
            margin-bottom: 20px;
            color: #333;
        }
        
        .error-message {
            font-size: 18px;
            margin-bottom: 30px;
            color: #666;
            line-height: 1.5;
        }
        
        .btn-home {
            display: inline-block;
            padding: 10px 20px;
            background-color: #4285f4;
            color: white;
            border-radius: 5px;
            text-decoration: none;
            font-weight: bold;
            transition: background-color 0.3s;
        }
        
        .btn-home:hover {
            background-color: #3367d6;
        }
        
        .error-details {
            margin-top: 40px;
            text-align: left;
            padding: 20px;
            background-color: #f5f5f5;
            border-radius: 5px;
            font-family: monospace;
            font-size: 14px;
            color: #666;
            overflow-x: auto;
        }
        
        .error-help {
            margin-top: 30px;
            font-size: 16px;
            color: #666;
        }
    </style>
</head>
<body>
    <div class="error-container">
        <div class="error-icon">⚠️</div>
        <h1 class="error-title">오류가 발생했습니다</h1>
        
        <p class="error-message">
            ${errorMessage != null ? errorMessage : '요청을 처리하는 중 예상치 못한 오류가 발생했습니다.'}
        </p>
        
        <a href="${pageContext.request.contextPath}/index.do" class="btn-home">홈으로 돌아가기</a>
        
        <p class="error-help">
            문제가 계속되면 관리자에게 문의해주세요.
        </p>
        
        <% if(request.getAttribute("error") != null) { %>
            <div class="error-details">
                <strong>에러 정보 (개발자용):</strong><br>
                <%= ((Exception)request.getAttribute("error")).toString() %>
            </div>
        <% } %>
    </div>
</body>
</html> 