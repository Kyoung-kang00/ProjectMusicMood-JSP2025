<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="dto.PlaylistDTO" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>플레이리스트 - MusicMood</title>
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/static/assets/images/favicon.ico">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/style.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/animation.css?v=1.1">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/playlist.css?v=1.4">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/search.css?v=1.1">
</head>
<body class="theme-default playlist-page">
    <!-- 전역 변수 설정은 백엔드에서 처리하도록 수정 -->
    <script>
        // contextPath 설정 - Controller에서 전달된 값 사용
        window.contextPath = '${pageContext.request.contextPath}';
        
        // 현재 로그인한 사용자 정보 - Controller에서 전달된 값 사용
        window.currentUserId = '${currentUserId}';
        
        // 좋아요한 플레이리스트 ID 목록 - Controller에서 전달된 JSON 문자열 파싱
        window.likedPlaylistIds = JSON.parse('${likedPlaylistIdsJson}');
    </script>

    <header class="header">
        <nav class="nav-container">
            <a href="${pageContext.request.contextPath}/index.do" class="logo">MusicMood</a>
            <div class="nav-links">
                <a href="${pageContext.request.contextPath}/index.do">홈</a> 
                <a href="${pageContext.request.contextPath}/chart.do">차트</a> 
                <a href="${pageContext.request.contextPath}/playlist.do" class="active">플레이리스트</a>
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

    <main class="playlist-container" style="padding-bottom: 100px; margin-bottom: 80px;">
        <!-- 네비게이션바에 가려지지 않도록 상단 여백 추가 -->
        <div class="header-spacer"></div>
        
        <section class="page-header">
            <h1>
                <% if (request.getAttribute("isLikedList") != null && (Boolean)request.getAttribute("isLikedList")) { %>
                    좋아요한 플레이리스트
                <% } else { %>
                    인기 플레이리스트
                <% } %>
            </h1>
            <p>Spotify의 인기 플레이리스트를 즐겨보세요</p>
        </section>
        
        <section class="playlists-section">
            <div class="section-header">
                <h2>
                    <% if (request.getAttribute("isLikedList") != null && (Boolean)request.getAttribute("isLikedList")) { %>
                        내가 좋아하는 플레이리스트
                    <% } else { %>
                        추천 플레이리스트
                    <% } %>
                </h2>
                <% if (request.getAttribute("isLikedList") == null) { %>
                    <a href="${pageContext.request.contextPath}/playlist.do?action=liked" class="view-all-link">
                        좋아요 목록 보기 <i class="fas fa-chevron-right"></i>
                    </a>
                <% } %>
            </div>
            
            <div id="playlistLoader" class="loader-container">
                <div class="loader"></div>
                <p>플레이리스트 불러오는 중...</p>
            </div>
            
            <div id="playlistContainer" class="playlists-grid" style="margin-bottom: 80px;">
                <!-- 플레이리스트 목록이 여기에 동적으로 로드됩니다 -->
            </div>
        </section>
    </main>
    
    <!-- 오디오 플레이어 (고정) -->
    <div id="audioPlayerBar" class="audio-player-bar" style="display: flex; visibility: visible; opacity: 1; transform: translateY(0); z-index: 9999;">
        <div class="ap-container">
            <div class="ap-info">
                <div class="ap-thumbnail">
                    <img src="${pageContext.request.contextPath}/static/assets/images/playlist-placeholder.png" id="apCover" alt="앨범 커버">
                </div>
                <div class="ap-details">
                    <div id="apTitle" class="ap-title">트랙을 선택해주세요</div>
                    <div id="apArtist" class="ap-artist">-</div>
                </div>
            </div>
            <div class="ap-controls">
                <button id="apPrev" class="ap-btn prev-btn"><i class="fas fa-step-backward"></i></button>
                <button id="apPlayPause" class="ap-btn play-btn"><i class="fas fa-play"></i></button>
                <button id="apNext" class="ap-btn next-btn"><i class="fas fa-step-forward"></i></button>
                <div class="ap-progress-container">
                    <div class="ap-time" id="apCurrentTime">0:00</div>
                    <div class="ap-progress-bar">
                        <div class="ap-progress" id="apProgress"></div>
                    </div>
                    <div class="ap-time" id="apDuration">0:30</div>
                </div>
                <button id="apVolume" class="ap-btn volume-btn"><i class="fas fa-volume-up"></i></button>
            </div>
        </div>
    </div>
    
    <!-- 재생 목록 (숨겨진 상태) -->
    <div id="audioPlaylistContainer" class="audio-playlist-container">
        <div class="playlist-header">
            <h3>재생 목록</h3>
            <button id="clearPlaylist" class="clear-btn"><i class="fas fa-trash"></i></button>
        </div>
        <ul id="audioPlaylist" class="playlist-items"></ul>
    </div>
    
    <!-- 스크립트 -->
    <script src="${pageContext.request.contextPath}/static/js/main.js?v=1.1"></script>
    <script src="${pageContext.request.contextPath}/static/js/playlist.js?v=1.4"></script>
    <script src="${pageContext.request.contextPath}/static/js/audio-player.js?v=1.1"></script>
    <script src="${pageContext.request.contextPath}/static/js/search.js?v=1.1"></script>
    <script>
        // 오디오 플레이어 관련 코드는 audio-player.js로 분리했습니다.
        // contextPath 전역 변수 설정
        window.contextPath = '${pageContext.request.contextPath}';
        
        // 페이지 로드 시 오디오 플레이어를 즉시 표시
        document.addEventListener('DOMContentLoaded', function() {
            // 오디오 플레이어 표시
            const audioPlayerBar = document.getElementById('audioPlayerBar');
            if (audioPlayerBar) {
                // 인라인 스타일로 명시적 설정
                audioPlayerBar.style.display = 'flex !important';
                audioPlayerBar.style.visibility = 'visible !important';
                audioPlayerBar.style.opacity = '1 !important';
                audioPlayerBar.style.transform = 'translateY(0) !important';
                audioPlayerBar.style.position = 'fixed';
                audioPlayerBar.style.bottom = '0';
                audioPlayerBar.style.left = '0';
                audioPlayerBar.style.right = '0';
                audioPlayerBar.style.zIndex = '9999';
                
                // 중요 스타일 추가를 위한 클래스 추가
                audioPlayerBar.classList.add('always-visible');
                
                // 속성 설정
                audioPlayerBar.setAttribute('data-visible', 'true');
            }
            
            // 페이지 하단에 여백 추가
            document.body.style.paddingBottom = '100px';
            
            // 페이지 컨테이너에 여백 추가
            const container = document.querySelector('.playlist-container');
            if (container) {
                container.style.paddingBottom = '100px';
                container.style.marginBottom = '80px';
            }
            
            // 재생 목록 클리어 버튼 이벤트 설정
            const clearBtn = document.getElementById('clearPlaylist');
            if (clearBtn) {
                clearBtn.addEventListener('click', function() {
                    if (window.audioPlayerAPI) {
                        window.audioPlayerAPI.clearPlaylist();
                    }
                });
            }
            
            // 모든 플레이리스트 아이템에 버튼 추가
            const playlistItems = document.querySelectorAll('.playlist-item');
            
            playlistItems.forEach(item => {
                const playlistId = item.getAttribute('data-id');
                const headerEl = item.querySelector('.playlist-item-header');
                const actionsEl = item.querySelector('.playlist-actions');
                
                if (!playlistId || !actionsEl) return;
                
                // 미리듣기 버튼이 없을 경우에만 추가
                if (!item.querySelector('.preview-tracks-btn')) {
                    const previewBtn = document.createElement('button');
                    previewBtn.className = 'action-btn preview-tracks-btn';
                    previewBtn.title = '플레이리스트 재생';
                    previewBtn.innerHTML = '<i class="fas fa-play"></i>';
                    
                    previewBtn.addEventListener('click', async function(e) {
                        e.stopPropagation();
                        
                        // 로딩 상태 표시
                        this.disabled = true;
                        this.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
                        
                        try {
                            // 오디오 플레이어 API를 통해 트랙 로드 및 재생
                            if (window.audioPlayerAPI) {
                                const success = await window.audioPlayerAPI.loadPlaylistTracksById(playlistId, true);
                                if (success) {
                                    this.innerHTML = '<i class="fas fa-check"></i>';
                                    setTimeout(() => {
                                        this.innerHTML = '<i class="fas fa-play"></i>';
                                        this.disabled = false;
                                    }, 1000);
                                } else {
                                    this.innerHTML = '<i class="fas fa-exclamation-circle"></i>';
                                    setTimeout(() => {
                                        this.innerHTML = '<i class="fas fa-play"></i>';
                                        this.disabled = false;
                                    }, 1000);
                                }
                            } else {
                                console.error('오디오 플레이어 API를 찾을 수 없습니다.');
                                this.innerHTML = '<i class="fas fa-exclamation-circle"></i>';
                                setTimeout(() => {
                                    this.innerHTML = '<i class="fas fa-play"></i>';
                                    this.disabled = false;
                                }, 1000);
                            }
                        } catch (error) {
                            console.error('플레이리스트 재생 오류:', error);
                            this.innerHTML = '<i class="fas fa-exclamation-circle"></i>';
                            setTimeout(() => {
                                this.innerHTML = '<i class="fas fa-play"></i>';
                                this.disabled = false;
                            }, 1000);
                        }
                    });
                    
                    // 버튼을 액션 영역에 추가
                    actionsEl.insertBefore(previewBtn, actionsEl.firstChild);
                }
            });
            
            // 재생 목록 토글 버튼 추가
            const playerElement = document.getElementById('audioPlayerBar');
            if (playerElement && !document.getElementById('togglePlaylistBtn')) {
                const toggleBtn = document.createElement('button');
                toggleBtn.id = 'togglePlaylistBtn';
                toggleBtn.className = 'toggle-playlist-btn';
                toggleBtn.innerHTML = '<i class="fas fa-list"></i>';
                toggleBtn.title = '재생 목록 표시/숨김';
                
                toggleBtn.addEventListener('click', function() {
                    const playlistContainer = document.getElementById('audioPlaylistContainer');
                    if (playlistContainer) {
                        playlistContainer.classList.toggle('show');
                        this.classList.toggle('active');
                    }
                });
                
                const controlsEl = playerElement.querySelector('.ap-controls');
                if (controlsEl) {
                    controlsEl.appendChild(toggleBtn);
                }
            }
        });
    </script>
</body>
</html> 