<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>로그인 - MusicMood</title>
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
            <h2>로그인</h2>
            <form class="login-form" action="${pageContext.request.contextPath}/auth/login.do" method="POST">
                <div class="form-group">
                    <label for="email">이메일</label>
                    <input type="email" id="email" name="email" required>
                </div>

                <div class="form-group">
                    <label for="password">비밀번호</label>
                    <input type="password" id="password" name="password" required>
                </div>

                <% if(request.getAttribute("error") != null) { %>
                <div class="error-message">
                    <%= request.getAttribute("error") %>
                </div>
                <% } %>

                <div class="form-options">
                    <label class="remember-me">
                        <input type="checkbox" name="remember">
                        로그인 상태 유지
                    </label>
                    <a href="${pageContext.request.contextPath}/findPassword.do" class="forgot-password">비밀번호 찾기</a>
                </div>
                <button type="submit" class="login-button">로그인</button>
            </form>

            <div class="social-login">
                <p>소셜 계정으로 로그인</p>
                <div class="social-buttons">
                    <button type="button" class="social-btn google">
                        <svg class="social-icon" viewBox="0 0 24 24">
                            <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                            <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                            <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                            <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                        </svg>
                        Google로 계속하기
                    </button>
                    <button type="button" class="social-btn kakao">
                        <svg class="social-icon" viewBox="0 0 24 24">
                            <path d="M12 3c5.799 0 10.5 3.664 10.5 8.185 0 4.52-4.701 8.184-10.5 8.184-.941 0-1.856-.077-2.734-.222l-5.58 3.616 1.97-5.913C3.356 15.19 1.5 13.352 1.5 11.185 1.5 6.664 6.201 3 12 3z" fill="#000000"/>
                        </svg>
                        카카오로 계속하기
                    </button>
                </div>
            </div>

            <div class="signup-link">
                <p>계정이 없으신가요? <a href="${pageContext.request.contextPath}/signup.do">회원가입</a></p>
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