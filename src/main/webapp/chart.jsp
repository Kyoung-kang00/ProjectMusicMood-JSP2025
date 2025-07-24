<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>차트 - MusicMood</title>
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/static/assets/images/favicon.ico">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/chart.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/search.css?v=1.2">
</head>
<body>
    <header class="header">
        <nav class="nav-container">
            <a href="${pageContext.request.contextPath}/index.do" class="logo">MusicMood</a>
            <div class="nav-links">
                <a href="${pageContext.request.contextPath}/index.do">홈</a>
                <a href="${pageContext.request.contextPath}/chart.do" class="active">차트</a>
                <a href="${pageContext.request.contextPath}/playlist.do">플레이리스트</a>
                <a href="#">최신음악</a>
            </div>
            <div class="auth-section">
                <div class="search-container">
                    <form class="search-form" autocomplete="off" onsubmit="return false;">
                        <input type="text" id="searchInput" class="search-input" placeholder="가수 또는 곡 검색...">
                        <button id="searchBtn" class="icon-btn" type="button">🔍</button>
                    </form>
                </div>
                <%-- 로그인 상태에 따라 다른 UI 표시 --%>
                <% if(session.getAttribute("user") != null) { 
                    // 로그인 된 상태
                    dto.UserDTO user = (dto.UserDTO)session.getAttribute("user");
                %>
                    <div class="user-menu">
                        <span class="user-greeting"><%= user.getName() %></span>
                        <div class="user-dropdown">
                            <button class="user-dropdown-btn">▼</button>
                            <div class="user-dropdown-content">
                                <a href="${pageContext.request.contextPath}/playlist.do?action=liked">내 플레이리스트</a>
                                <a href="${pageContext.request.contextPath}/profile.do">프로필 설정</a>
                                <a href="${pageContext.request.contextPath}/auth/login.do?action=logout">로그아웃</a>
                            </div>
                        </div>
                    </div>
                <% } else { %>
                    <%-- 로그인 되지 않은 상태 --%>
                    <a href="${pageContext.request.contextPath}/auth/login.do" class="login-btn">로그인</a>
                <% } %>
            </div>
        </nav>
    </header>

    <main class="chart-container">
        <section class="chart-header">
            <h2>실시간 차트</h2>
            <div class="chart-filters">
                <button class="filter-btn active" data-chart-type="global">글로벌</button>
                <button class="filter-btn" data-chart-type="korea">국내</button>
                <button class="filter-btn"data-chart-type="viral">해외</button>
            </div>
        </section>

        <section class="chart-list">
            <div class="chart-loading" id="loadingSpinner">
                <div class="spinner"></div>
                <p>차트 로딩중...</p>
            </div>
            <div class="chart-tracks" id="chartTracks">
                <!-- JavaScript로 채워질 영역 -->
            </div>
        </section>
    </main>

    <script>
        const contextPath = '${pageContext.request.contextPath}';
        if (contextPath) {
            localStorage.setItem('appContextPath', contextPath);
        }
    </script>
    <script src="${pageContext.request.contextPath}/static/js/search.js?v=1.1"></script>
    <script src="${pageContext.request.contextPath}/static/js/chart.js?v=1.1"></script>
    <script src="${pageContext.request.contextPath}/static/js/main.js?v=1.1"></script>
</body>
</html>