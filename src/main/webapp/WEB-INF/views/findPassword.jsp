<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>비밀번호 찾기 - MusicMood</title>
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
        <div class="findPassword-box">
            <h2>비밀번호 찾기</h2>
            
            <!-- 오류 메시지 표시 영역 -->
            <% if (request.getAttribute("error") != null) { %>
                <div class="error-message">
                    <%= request.getAttribute("error") %>
                </div>
            <% } %>
            
            <form class="findPassword-form" action="${pageContext.request.contextPath}/findPassword.do" method="POST">
                <div class="form-group">
                    <label for="email">이메일</label>
                    <input type="email" id="email" name="email" required>
                </div>

                <div class="form-group">
                    <label for="name">사용자 이름</label>
                    <input type="text" id="name" name="name" required>
                </div>

                <button type="submit" class="findPassword-button">비밀번호 찾기</button>
               
            </form>

            <div class="auth-links">
                <a href="${pageContext.request.contextPath}/auth/login.do">로그인</a>
                <span>|</span>
                <a href="${pageContext.request.contextPath}/signup.do">회원가입</a>
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