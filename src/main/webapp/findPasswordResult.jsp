<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="dto.UserDTO" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>비밀번호 찾기 결과 - MusicMood</title>
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/static/assets/images/favicon.ico">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/findPassword.css">
</head>
<body>
    <!-- 배경 슬라이더 추가 -->
    <div class="background-slider">
        <div class="slide" style="background-image: url('${pageContext.request.contextPath}/static/assets/images/5.png')"></div>
        <div class="slide" style="background-image: url('${pageContext.request.contextPath}/static/assets/images/6.png')"></div>
        <div class="slide" style="background-image: url('${pageContext.request.contextPath}/static/assets/images/7.png')"></div>
    </div>

    <header class="header">
        <nav class="nav-container">
            <a href="${pageContext.request.contextPath}/index.do" class="logo">MusicMood</a>
        </nav>
    </header>

    <main class="findPassword-container">
        <div class="findPassword-box result-box">
            <h2>비밀번호 찾기 결과</h2>
            
            <% 
            UserDTO foundUser = (UserDTO) request.getAttribute("foundUser");
            if (foundUser != null) {
            %>
                <div class="result-message success">
                    <p>회원님의 정보를 찾았습니다.</p>
                </div>
                
                <div class="user-info">
                    <div class="info-row">
                        <span class="label">이메일:</span>
                        <span class="value"><%= foundUser.getEmail() %></span>
                    </div>
                    <div class="info-row">
                        <span class="label">이름:</span>
                        <span class="value"><%= foundUser.getName() %></span>
                    </div>
                    <div class="info-row">
                        <span class="label">비밀번호:</span>
                        <span class="value"><%= foundUser.getPassword() %></span>
                    </div>
                </div>
            <% } else { %>
                <div class="result-message error">
                    <p>회원 정보를 찾을 수 없습니다.</p>
                </div>
            <% } %>
            
            <div class="action-buttons">
                <a href="${pageContext.request.contextPath}/auth/login.do" class="button login-button">로그인 하기</a>
                <a href="${pageContext.request.contextPath}/index.do" class="button home-button">홈으로</a>
            </div>
        </div>
    </main>

    <script src="${pageContext.request.contextPath}/static/js/login.js"></script>
    <script>
    // contextPath 설정 - 현재 시스템 경로 확인
    const contextPath = '${pageContext.request.contextPath}';
    console.log('JSP에서 설정된 contextPath:', contextPath);

    // localStorage에도 저장하여 다른 페이지에서도 사용할 수 있게 함
    if (contextPath) {
        localStorage.setItem('appContextPath', contextPath);
    }
    </script>
</body>
</html> 