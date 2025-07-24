<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="dto.PlaylistDTO" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>좋아요한 플레이리스트 - MusicMood</title>
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
                                <a href="${pageContext.request.contextPath}/playlist.do?action=liked" class="active">내 플레이리스트</a>
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

    <main class="playlist-container">
        <!-- 네비게이션바에 가려지지 않도록 상단 여백 추가 -->
        <div class="header-spacer"></div>
        
        <section class="page-header">
            <h1>좋아요한 플레이리스트</h1>
            <p>내가 좋아요한 플레이리스트 모음</p>
        </section>
        
        <section class="playlists-section">
            <div class="section-header">
                <h2>내가 좋아하는 플레이리스트</h2>
                <a href="${pageContext.request.contextPath}/playlist.do" class="view-all-link">
                    모든 플레이리스트 보기 <i class="fas fa-chevron-right"></i>
                </a>
            </div>
            
            <div id="playlistLoader" class="loader-container">
                <div class="loader"></div>
                <p>플레이리스트 불러오는 중...</p>
            </div>
            
            <div id="playlistContainer" class="playlists-grid">
                <!-- 플레이리스트 목록이 여기에 동적으로 로드됩니다 -->
            </div>
        </section>
    </main>
    
    <!-- 오디오 플레이어 (고정) -->
    <div id="audioPlayerBar" class="audio-player-bar">
        <div class="ap-container">
            <div class="ap-info">
                <div class="ap-thumbnail">
                    <img src="${pageContext.request.contextPath}/static/assets/images/default-track.png" id="apCover" alt="앨범 커버">
                </div>
                <div class="ap-details">
                    <div id="apTitle" class="ap-title">트랙을 선택해주세요</div>
                    <div id="apArtist" class="ap-artist">-</div>
                </div>
            </div>
            <div class="ap-controls">
                <button id="apPlayPause" class="ap-btn play-btn"><i class="fas fa-play"></i></button>
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
    
    <!-- 스크립트 -->
    <script src="${pageContext.request.contextPath}/static/js/main.js?v=1.1"></script>
    <script src="${pageContext.request.contextPath}/static/js/playlist.js?v=1.4"></script>
    <script src="${pageContext.request.contextPath}/static/js/search.js?v=1.1"></script>
    <script>
        // 오디오 플레이어 관련 전역 변수 설정
        document.addEventListener('DOMContentLoaded', () => {
            // 오디오 플레이어 요소
            const audioPlayer = {
                bar: document.getElementById('audioPlayerBar'),
                cover: document.getElementById('apCover'),
                title: document.getElementById('apTitle'),
                artist: document.getElementById('apArtist'),
                playPauseBtn: document.getElementById('apPlayPause'),
                playPauseIcon: document.getElementById('apPlayPause').querySelector('i'),
                progress: document.getElementById('apProgress'),
                currentTime: document.getElementById('apCurrentTime'),
                duration: document.getElementById('apDuration'),
                volumeBtn: document.getElementById('apVolume'),
                progressBar: document.querySelector('.ap-progress-bar'),
                audio: new Audio()
            };
            
            // 현재 재생중인 오디오 상태
            let isPlaying = false;
            let currentVolume = 1.0;
            let isMuted = false;
            
            // 오디오 플레이어 초기화
            function initAudioPlayer() {
                // 재생/일시정지 버튼 이벤트
                audioPlayer.playPauseBtn.addEventListener('click', togglePlayPause);
                
                // 볼륨 버튼 이벤트
                audioPlayer.volumeBtn.addEventListener('click', toggleMute);
                
                // 진행 바 클릭 이벤트
                audioPlayer.progressBar.addEventListener('click', setProgress);
                
                // 오디오 이벤트 리스너
                audioPlayer.audio.addEventListener('timeupdate', updateProgress);
                audioPlayer.audio.addEventListener('ended', resetPlayer);
                audioPlayer.audio.addEventListener('canplay', () => {
                    audioPlayer.duration.textContent = formatTime(audioPlayer.audio.duration);
                });
                
                // 미리듣기 버튼에 이벤트 추가
                document.addEventListener('click', (e) => {
                    // 미리듣기 버튼 클릭 처리
                    if (e.target.closest('.preview-btn')) {
                        const btn = e.target.closest('.preview-btn');
                        const previewUrl = btn.getAttribute('data-url');
                        if (!previewUrl) return;
                        
                        // 트랙 정보 가져오기
                        let trackName = '트랙';
                        let artistName = '아티스트';
                        let coverUrl = '${pageContext.request.contextPath}/static/assets/images/default-track.png';
                        
                        // 트랙 아이템 또는 부모 요소에서 정보 추출
                        const trackItem = btn.closest('.track-preview-item') || btn.closest('.track-item');
                        if (trackItem) {
                            const nameEl = trackItem.querySelector('.track-name');
                            const artistEl = trackItem.querySelector('.track-artist');
                            const imgEl = trackItem.querySelector('img');
                            
                            if (nameEl) trackName = nameEl.textContent;
                            if (artistEl) artistName = artistEl.textContent;
                            if (imgEl) coverUrl = imgEl.src;
                        }
                        
                        // 현재 재생 중인 트랙과 같은지 확인
                        if (isPlaying && audioPlayer.audio.src === previewUrl) {
                            // 일시정지
                            togglePlayPause();
                        } else {
                            // 새 트랙 재생
                            playTrack(previewUrl, trackName, artistName, coverUrl);
                        }
                    }
                });
            }
            
            // 트랙 재생 함수
            function playTrack(url, title, artist, cover) {
                // 이전 트랙 초기화
                if (isPlaying) {
                    audioPlayer.audio.pause();
                }
                
                // 새 트랙 정보 설정
                audioPlayer.title.textContent = title;
                audioPlayer.artist.textContent = artist;
                audioPlayer.cover.src = cover;
                
                // 오디오 소스 설정 및 재생
                audioPlayer.audio.src = url;
                audioPlayer.audio.play()
                    .then(() => {
                        isPlaying = true;
                        audioPlayer.playPauseIcon.classList.replace('fa-play', 'fa-pause');
                        audioPlayer.bar.classList.add('playing');
                    })
                    .catch(err => {
                        console.error('오디오 재생 실패:', err);
                        showToast('오디오 재생 중 오류가 발생했습니다.', 'error');
                    });
            }
            
            // 재생/일시정지 토글
            function togglePlayPause() {
                if (!audioPlayer.audio.src) return;
                
                if (isPlaying) {
                    audioPlayer.audio.pause();
                    audioPlayer.playPauseIcon.classList.replace('fa-pause', 'fa-play');
                    isPlaying = false;
                } else {
                    audioPlayer.audio.play();
                    audioPlayer.playPauseIcon.classList.replace('fa-play', 'fa-pause');
                    isPlaying = true;
                }
            }
            
            // 음소거 토글
            function toggleMute() {
                const volumeIcon = audioPlayer.volumeBtn.querySelector('i');
                
                if (isMuted) {
                    audioPlayer.audio.volume = currentVolume;
                    volumeIcon.className = 'fas fa-volume-up';
                    isMuted = false;
                } else {
                    currentVolume = audioPlayer.audio.volume;
                    audioPlayer.audio.volume = 0;
                    volumeIcon.className = 'fas fa-volume-mute';
                    isMuted = true;
                }
            }
            
            // 진행 상태 업데이트
            function updateProgress() {
                const duration = audioPlayer.audio.duration || 30;
                const currentTime = audioPlayer.audio.currentTime;
                const progressPercent = (currentTime / duration) * 100;
                
                audioPlayer.progress.style.width = `${progressPercent}%`;
                audioPlayer.currentTime.textContent = formatTime(currentTime);
            }
            
            // 진행 바 클릭 처리
            function setProgress(e) {
                const width = audioPlayer.progressBar.clientWidth;
                const clickX = e.offsetX;
                const duration = audioPlayer.audio.duration || 30;
                
                audioPlayer.audio.currentTime = (clickX / width) * duration;
            }
            
            // 플레이어 초기화
            function resetPlayer() {
                audioPlayer.audio.currentTime = 0;
                audioPlayer.playPauseIcon.classList.replace('fa-pause', 'fa-play');
                isPlaying = false;
            }
            
            // 시간 형식 변환 (초 -> MM:SS)
            function formatTime(time) {
                const minutes = Math.floor(time / 60);
                const seconds = Math.floor(time % 60);
                return `${minutes}:${seconds < 10 ? '0' : ''}${seconds}`;
            }
            
            // 전역 오디오 플레이어 초기화
            initAudioPlayer();
            
            // 전역에 미리듣기 함수 노출
            window.playPreview = (url, title, artist, cover) => {
                playTrack(url, title, artist, cover);
            };
        });
    </script>
</body>
</html> 