<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>회원가입 - MusicMood</title>
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/static/assets/images/favicon.ico">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/login.css">
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

    <main class="login-container">
        <div class="login-box"> 
            <h2 style="text-align: center;">회원가입</h2>
            
            <!-- 에러 메시지 표시 -->
            <% if(request.getAttribute("error") != null) { %>
                <div class="error-message">
                    <%= request.getAttribute("error") %>
                </div>
            <% } %>
            
            <form class="login-form" action="${pageContext.request.contextPath}/signup.do" method="POST">
                <div class="form-group">
                    <label for="email">이메일</label>
                    <input type="email" id="email" name="email" placeholder="ex)qwer25@gmil.com" required>
                </div>

                <div class="form-group">
                    <label for="password">비밀번호</label>
                    <input type="password" id="password" name="password" required placeholder="비밀번호 6자리 이상">
                </div>
                
                <div class="form-group">
                    <label for="confirmPassword">비밀번호 확인</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" required placeholder="비밀번호 확인">
                </div>
                
                <div class="form-group">
                    <label for="name">사용자이름</label>
                    <input type="text" id="name" name="name" required placeholder="ex)홍길동">
                </div>
                 
                <div class="form-group">
                    <label for="phone">전화번호</label>
                    <input type="text" id="phone" name="phone" required placeholder="ex)010-1234-5678">
                </div>
               
				<button type="submit" class="login-button">회원가입 완료</button>
            </form>

            <div class="signup-link"> 
                <p>계정이 이미 존재하시나요? <a href="${pageContext.request.contextPath}/auth/login.do">로그인</a></p>
            </div>
        </div>
    </main>

    <script src="${pageContext.request.contextPath}/static/js/login.js"></script>
</body>
</html>