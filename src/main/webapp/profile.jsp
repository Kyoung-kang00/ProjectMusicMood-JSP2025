<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>프로필 설정 - MusicMood</title>
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/static/assets/images/favicon.ico">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/profile.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/search.css?v=1.2">
</head>
<body class="theme-default">

    <header class="header">
        <nav class="nav-container">
            <a href="${pageContext.request.contextPath}/index.do" class="logo">MusicMood</a>
            <div class="nav-links">
                <a href="${pageContext.request.contextPath}/index.do">홈</a> 
                <a href="${pageContext.request.contextPath}/chart.do">차트</a> 
                <a href="${pageContext.request.contextPath}/playlist.do">플레이리스트</a>
                <a href="#">최신음악</a>
            </div>
            <div class="auth-section">
                <div class="search-container">
                    <form class="search-form" autocomplete="off" onsubmit="return false;">
                        <input type="text" id="searchInput" class="search-input" placeholder="가수 또는 곡 검색...">
                        <button id="searchBtn" class="search-btn" type="button"><i class="fas fa-search"></i></button>
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
                                <a href="${pageContext.request.contextPath}/playlist.do?action=mylist">내 플레이리스트</a>
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

    <main class="profile-container">
        <div class="profile-box">
            <h2>프로필 설정</h2>
            
            <% if(request.getAttribute("message") != null) { %>
            <div class="success-message">
                <%= request.getAttribute("message") %>
            </div>
            <% } %>
            
            <% if(request.getAttribute("error") != null) { %>
            <div class="error-message">
                <%= request.getAttribute("error") %>
            </div>
            <% } %>
            
            <div class="profile-info">
                <div class="profile-field">
                    <div class="field-label">이메일</div>
                    <div class="field-value" id="email-value">
                        <% 
                            dto.UserDTO currentUser = (dto.UserDTO)request.getAttribute("currentUser");
                            String email = currentUser.getEmail();
                            // 이메일을 일부만 표시하고 나머지는 *로 마스킹
                            String maskedEmail = maskEmail(email);
                        %>
                        <%= maskedEmail %>
                    </div>
                    <button type="button" class="edit-btn" data-field="email">수정하기</button>
                </div>
                
                <div class="profile-field">
                    <div class="field-label">이름</div>
                    <div class="field-value" id="name-value">
                        <% 
                            String name = currentUser.getName();
                            // 이름의 일부만 표시
                            String maskedName = maskName(name);
                        %>
                        <%= maskedName %>
                    </div>
                    <button type="button" class="edit-btn" data-field="name">수정하기</button>
                </div>
                
                <div class="profile-field">
                    <div class="field-label">휴대폰 번호</div>
                    <div class="field-value" id="phone-value">
                        <% 
                            String phone = currentUser.getPhone();
                            // 전화번호 마스킹
                            String maskedPhone = maskPhone(phone);
                        %>
                        <%= maskedPhone %>
                    </div>
                    <button type="button" class="edit-btn" data-field="phone">수정하기</button>
                </div>
                
                <div class="profile-field">
                    <div class="field-label">비밀번호</div>
                    <div class="field-value" id="password-value">
                        ●●●●●●●●
                    </div>
                    <button type="button" class="edit-btn" data-field="password">수정하기</button>
                </div>
            </div>
        </div>
    </main>

    <%!
    // 이메일 마스킹 메소드
    String maskEmail(String email) {
        if(email == null || email.isEmpty() || !email.contains("@")) {
            return "정보 없음";
        }
        
        int atIndex = email.indexOf('@');
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        // 사용자 이름의 절반 이상을 마스킹
        int showLength = Math.max(1, username.length() / 3);
        String visiblePart = username.substring(0, showLength);
        String maskedPart = "";
        for(int i = 0; i < username.length() - showLength; i++) {
            maskedPart += "*";
        }
        
        return visiblePart + maskedPart + domain;
    }
    
    // 이름 마스킹 메소드
    String maskName(String name) {
        if(name == null || name.isEmpty()) {
            return "정보 없음";
        }
        
        if(name.length() <= 2) {
            return name.substring(0, 1) + "*";
        }
        
        String visible = name.substring(0, 1);
        String masked = "";
        for(int i = 0; i < name.length() - 1; i++) {
            masked += "*";
        }
        
        return visible + masked;
    }
    
    // 전화번호 표시 메소드 (마스킹 제거)
    String maskPhone(String phone) {
        if(phone == null || phone.isEmpty()) {
            return "정보 없음";
        }
        
        // 그대로 전화번호 반환
        return phone;
    }
    %>

    <script>
    // contextPath 설정 - 현재 시스템 경로 확인
    const contextPath = '${pageContext.request.contextPath}';
    
    // 사용자 실제 데이터 (JavaScript에서 접근용)
    const userData = {
        email: '<%= currentUser.getEmail() %>',
        name: '<%= currentUser.getName() %>',
        phone: '<%= currentUser.getPhone() != null ? currentUser.getPhone() : "" %>',
        // 보안상 비밀번호는 포함하지 않음
    };
    </script>
    <script src="${pageContext.request.contextPath}/static/js/search.js"></script>
    <script src="${pageContext.request.contextPath}/static/js/profile.js"></script>
</body>
</html> 