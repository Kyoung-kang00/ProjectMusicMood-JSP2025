/**
 * playlist.js - MusicMood 플레이리스트 페이지 기능
 */
document.addEventListener('DOMContentLoaded', () => {
    let ACCESS_TOKEN = '';
    let isTokenRefreshing = false;

    // contextPath 정의 (없을 경우 공백으로 처리)
    const contextPath = window.contextPath || '';
    
    // 플레이리스트 ID 배열 직접 정의
    const PLAYLISTS = [
        '2jqJzW5rXwu9FpUdGNdo2W', // 드라이브 플레이리스트
        '1Krm4ydj8K9nhqsbzXW9Zg', // 잔잔한 플레이리스트
        '7k6QVcqBgBIjQYdwVReZBG', // 파티 + 클럽 플레이리스트
        '6sJ5fgYe6zpxkOtsl1s8MG', // 수고했어, 오늘도
        '29LZX2dHXmL9vyUZQwQtHF', // SUMMER HIT KPOP
        '6Ve9tZAEZrzVMnd58NszvB', // MEGA HIT REMIX
        '1q43xX4kCl9VSIta8NBWNC', // 2000년대 사랑노래
        '2vbVutkblurEK8uqQkIeLu'  // WORKPLACE KPOP
    ];
    
    // 페이지 초기화 함수
    async function init() {
        try {
            await fetchAccessToken();
            
            // 디버그 로그 추가 - 좋아요 목록 확인
            console.log('💖 좋아요 목록 초기화:', window.likedPlaylistIds);
            
            // 현재 페이지가 플레이리스트 목록 페이지인지 확인
            if (document.getElementById('playlistContainer')) {
                // 좋아요한 플레이리스트 페이지인지 확인
                const isLikedPage = document.querySelector('.page-header h1')?.textContent.includes('좋아요한');
                console.log('현재 페이지:', isLikedPage ? '좋아요한 플레이리스트' : '일반 플레이리스트');
                
                await loadPlaylists(isLikedPage);
            }
            
            // 현재 페이지가 플레이리스트 상세 페이지인지 확인
            if (document.getElementById('tracksList') && window.playlistId) {
                await loadPlaylistTracks(window.playlistId);
                setupLikeButton();
            }
            
            // 좋아요한 플레이리스트 표시
            updateLikedPlaylists();
        } catch (error) {
            console.error('페이지 초기화 중 오류 발생:', error);
            showErrorMessage('페이지 초기화 중 오류가 발생했습니다.');
        }
    }

    // 토큰 가져오기
    async function fetchAccessToken() {
        try {
            if (isTokenRefreshing) {
                return false;
            }
            
            isTokenRefreshing = true;
            const baseUrl = window.location.origin;
            let apiUrl = `${baseUrl}${contextPath}/api/token`;
            
            console.log('🔑 Spotify API 토큰 요청 중...');
            const response = await fetch(apiUrl);
            
            if (!response.ok) {
                throw new Error(`토큰 요청 실패: ${response.status}`);
            }
            
            const data = await response.json();
            
            if (data.status === 'SUCCESS' && data.access_token) {
                ACCESS_TOKEN = data.access_token;
                console.log('✅ 토큰 가져오기 성공');
                isTokenRefreshing = false;
                return true;
            } else {
                throw new Error(data.error || '토큰 데이터가 올바르지 않습니다');
            }
        } catch (error) {
            console.error('❌ Access Token 가져오기 실패:', error);
            isTokenRefreshing = false;
            return false;
        }
    }

    // 플레이리스트 목록 가져오기
    async function loadPlaylists(isLikedPage = false) {
        const container = document.getElementById('playlistContainer');
        const loader = document.getElementById('playlistLoader');
        
        if (!container) return;
        
        try {
            loader.style.display = 'flex';
            
            if (!ACCESS_TOKEN) {
                const tokenResult = await fetchAccessToken();
                if (!tokenResult) {
                    throw new Error('유효한 토큰을 가져올 수 없습니다.');
                }
            }
            
            // 직접 정의한 PLAYLISTS 배열 사용
            const playlistIds = isLikedPage ? window.likedPlaylistIds : PLAYLISTS;
            
            if (!playlistIds || !playlistIds.length) {
                if (isLikedPage) {
                    // 좋아요한 플레이리스트가 없는 경우
                    loader.style.display = 'none';
                    container.innerHTML = `
                        <div class="no-playlists">
                            <i class="far fa-heart"></i>
                            <p>좋아요한 플레이리스트가 없습니다.</p>
                            <a href="${contextPath}/playlist.do" class="view-all-link">플레이리스트 둘러보기</a>
                        </div>
                    `;
                    return;
                }
                throw new Error('플레이리스트 ID가 정의되지 않았습니다.');
            }
            
            console.log('로드할 플레이리스트 ID 목록:', playlistIds);
            
            // 플레이리스트 정보를 담을 배열
            const playlistsData = [];
            
            // 각 플레이리스트 ID에 대해 정보 가져오기
            for (const playlistId of playlistIds) {
                try {
                    const response = await fetch(`https://api.spotify.com/v1/playlists/${playlistId}`, {
                        method: 'GET',
                        headers: {
                            'Authorization': `Bearer ${ACCESS_TOKEN}`
                        }
                    });
                    
                    if (response.status === 401) {
                        // 토큰이 만료된 경우 재발급 시도
                        console.log('🔄 토큰 만료, 재발급 시도...');
                        const refreshResult = await fetchAccessToken();
                        if (refreshResult) {
                            // 다시 처음부터 로드
                            return loadPlaylists(isLikedPage);
                        } else {
                            throw new Error('토큰 재발급 실패');
                        }
                    }
                    
                    if (!response.ok) {
                        console.error(`플레이리스트 ID ${playlistId} 가져오기 실패: ${response.status}`);
                        continue;
                    }
                    
                    const playlist = await response.json();
                    playlistsData.push(playlist);
                } catch (err) {
                    console.error(`플레이리스트 ID ${playlistId} 처리 중 오류:`, err);
                }
            }
            
            // 로딩 인디케이터 숨기기
            loader.style.display = 'none';
            
            // 플레이리스트 목록 표시
            renderPlaylists(playlistsData, container, isLikedPage);
        } catch (error) {
            console.error('플레이리스트 로딩 오류:', error);
            loader.style.display = 'none';
            container.innerHTML = `
                <div class="error-message">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>플레이리스트를 불러오는데 실패했습니다.</p>
                    <p class="error-details">${error.message}</p>
                    <button class="retry-btn" onclick="location.reload()">다시 시도</button>
                </div>
            `;
        }
    }

    // 플레이리스트 렌더링
    function renderPlaylists(playlists, container, isLikedPage = false) {
        if (!playlists || playlists.length === 0) {
            if (isLikedPage) {
                container.innerHTML = `
                    <div class="no-playlists">
                        <i class="far fa-heart"></i>
                        <p>좋아요한 플레이리스트가 없습니다.</p>
                        <a href="${contextPath}/playlist.do" class="view-all-link">플레이리스트 둘러보기</a>
                    </div>
                `;
            } else {
                container.innerHTML = '<div class="no-playlists">이용 가능한 플레이리스트가 없습니다.</div>';
            }
            return;
        }
        
        // 플레이리스트 HTML 생성
        const playlistsHTML = playlists.map((playlist, index) => {
            const image = playlist.images && playlist.images.length > 0 
                ? playlist.images[0].url 
                : `${contextPath}/static/assets/images/default-playlist.png`;
                
            // 좋아요 상태 확인
            const isLiked = window.likedPlaylistIds && Array.isArray(window.likedPlaylistIds) && 
                            window.likedPlaylistIds.includes(playlist.id);
            const heartIcon = isLiked ? 'fas fa-heart' : 'far fa-heart';
            
            // 설명 표시 로직 - 설명이 없는 경우 빈 문자열 반환
            const descriptionHTML = playlist.description ? 
                `<p class="playlist-desc">${playlist.description}</p>` : 
                '';
                
            return `
                <div class="playlist-item" data-id="${playlist.id}" style="--index: ${index}">
                    <div class="playlist-item-header">
                        <div class="playlist-image">
                            <img src="${image}" alt="${playlist.name}">
                            <div class="play-overlay">
                                <i class="fas fa-play"></i>
                            </div>
                        </div>
                        <div class="playlist-details">
                            <h3 class="playlist-name">${playlist.name}</h3>
                            <p class="playlist-owner">By ${playlist.owner.display_name}</p>
                            ${descriptionHTML}
                        </div>
                        <div class="playlist-actions">
                            <button class="action-btn preview-tracks-btn" data-id="${playlist.id}" title="플레이리스트 재생">
                                <i class="fas fa-play"></i>
                            </button>
                            <button class="action-btn like-btn ${isLiked ? 'liked' : ''}" data-id="${playlist.id}">
                                <i class="${heartIcon}"></i>
                            </button>
                            <button class="action-btn expand-btn" data-id="${playlist.id}">
                                <i class="fas fa-chevron-down"></i>
                            </button>
                        </div>
                    </div>
                    <div class="playlist-tracks-preview" id="tracks-${playlist.id}"></div>
                </div>
            `;
        }).join('');
        
        container.innerHTML = playlistsHTML;
        
        // 이벤트 리스너 설정
        setupPlaylistEventListeners();
    }

    // 플레이리스트 이벤트 리스너 설정
    function setupPlaylistEventListeners() {
        // 미리듣기 버튼 이벤트
        document.querySelectorAll('.preview-tracks-btn').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                e.stopPropagation();
                
                const playlistId = btn.getAttribute('data-id');
                if (!playlistId) return;
                
                // 로딩 상태 표시
                btn.disabled = true;
                const originalIcon = btn.innerHTML;
                btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
                
                try {
                    // 오디오 플레이어 API를 통해 트랙 로드 및 재생
                    if (window.audioPlayerAPI) {
                        const success = await window.audioPlayerAPI.loadPlaylistTracksById(playlistId, true);
                        if (success) {
                            btn.innerHTML = '<i class="fas fa-check"></i>';
                            setTimeout(() => {
                                btn.innerHTML = originalIcon;
                                btn.disabled = false;
                            }, 1000);
                        } else {
                            btn.innerHTML = '<i class="fas fa-exclamation-circle"></i>';
                            setTimeout(() => {
                                btn.innerHTML = originalIcon;
                                btn.disabled = false;
                            }, 1000);
                        }
                    } else {
                        console.error('오디오 플레이어 API를 찾을 수 없습니다.');
                        btn.innerHTML = '<i class="fas fa-exclamation-circle"></i>';
                        setTimeout(() => {
                            btn.innerHTML = originalIcon;
                            btn.disabled = false;
                        }, 1000);
                    }
                } catch (error) {
                    console.error('플레이리스트 재생 오류:', error);
                    btn.innerHTML = '<i class="fas fa-exclamation-circle"></i>';
                    setTimeout(() => {
                        btn.innerHTML = originalIcon;
                        btn.disabled = false;
                    }, 1000);
                }
            });
        });
        
        // 좋아요 버튼 이벤트
        document.querySelectorAll('.like-btn').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                e.stopPropagation();
                
                if (!window.currentUserId || window.currentUserId === 'null') {
                    alert('로그인이 필요한 기능입니다.');
                    window.location.href = `${contextPath}/auth/login.do`;
                    return;
                }
                
                const playlistId = btn.getAttribute('data-id');
                const icon = btn.querySelector('i');
                const isLiked = icon.classList.contains('fas');
                
                try {
                    // 버튼 상태 변경을 미리 표시하여 사용자 경험 향상
                    btn.disabled = true;
                    btn.style.opacity = '0.7';
                    
                    const action = isLiked ? 'remove' : 'add';
                    const result = await toggleLikePlaylist(playlistId, action);
                    
                    if (result) {
                        // 아이콘 업데이트
                        if (isLiked) {
                            icon.classList.replace('fas', 'far');
                            btn.classList.remove('liked');
                            showToast('플레이리스트 좋아요를 취소했습니다.');
                        } else {
                            icon.classList.replace('far', 'fas');
                            btn.classList.add('liked');
                            showToast('플레이리스트를 좋아요 했습니다.');
                        }
                    }
                } catch (error) {
                    console.error('좋아요 처리 오류:', error);
                    showToast('좋아요 처리 중 오류가 발생했습니다.', 'error');
                } finally {
                    btn.disabled = false;
                    btn.style.opacity = '1';
                }
            });
        });
        
        // 확장 버튼 이벤트 (트랙 미리보기 표시/숨김)
        document.querySelectorAll('.expand-btn').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                e.stopPropagation();
                
                const playlistId = btn.getAttribute('data-id');
                const tracksContainer = document.getElementById(`tracks-${playlistId}`);
                const icon = btn.querySelector('i');
                const isExpanded = tracksContainer.classList.contains('expanded');
                
                // 모든 트랙 컨테이너 접기
                document.querySelectorAll('.playlist-tracks-preview').forEach(container => {
                    if (container.id !== `tracks-${playlistId}` && container.classList.contains('expanded')) {
                        container.classList.remove('expanded');
                        const otherBtn = container.parentElement.querySelector('.expand-btn i');
                        otherBtn.classList.replace('fa-chevron-up', 'fa-chevron-down');
                    }
                });
                
                if (isExpanded) {
                    // 이미 펼쳐져 있다면 접기
                    tracksContainer.classList.remove('expanded');
                    icon.classList.replace('fa-chevron-up', 'fa-chevron-down');
                } else {
                    // 접혀있다면 펼치기
                    tracksContainer.classList.add('expanded');
                    icon.classList.replace('fa-chevron-down', 'fa-chevron-up');
                    
                    // 트랙 정보가 없으면 로드
                    if (tracksContainer.children.length === 0) {
                        await loadPlaylistPreviewTracks(playlistId, tracksContainer);
                    }
                }
            });
        });
        
        // 플레이리스트 아이템 자체의 클릭 이벤트를 모두 방지
        document.querySelectorAll('.playlist-item').forEach(item => {
            item.style.cursor = 'default'; // 포인터 커서 제거
            
            // 클릭 이벤트 무효화
            item.addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation();
                // 아무 동작도 하지 않음
                return false;
            });
        });
    }
    
    // 좋아요한 플레이리스트 상태 업데이트
    function updateLikedPlaylistsState(playlistId, isLiked) {
        if (!window.likedPlaylistIds) {
            window.likedPlaylistIds = [];
        }
        
        if (isLiked) {
            // 좋아요 추가
            if (!window.likedPlaylistIds.includes(playlistId)) {
                window.likedPlaylistIds.push(playlistId);
                console.log('로컬 상태 업데이트: 좋아요 추가', playlistId);
                console.log('현재 좋아요 목록:', window.likedPlaylistIds);
            }
        } else {
            // 좋아요 제거
            window.likedPlaylistIds = window.likedPlaylistIds.filter(id => id !== playlistId);
            console.log('로컬 상태 업데이트: 좋아요 제거', playlistId);
            console.log('현재 좋아요 목록:', window.likedPlaylistIds);
        }
        
        // UI 업데이트
        updateLikedPlaylists();
    }
    
    // 좋아요한 플레이리스트 UI 업데이트
    function updateLikedPlaylists() {
        try {
            if (!window.likedPlaylistIds || !Array.isArray(window.likedPlaylistIds)) {
                console.log('좋아요한 플레이리스트가 없거나 배열이 아닙니다.');
                window.likedPlaylistIds = [];
                return;
            }
            
            console.log('좋아요 UI 업데이트 - 현재 좋아요 목록:', window.likedPlaylistIds);
            
            // 모든 좋아요 버튼 초기화
            document.querySelectorAll('.like-btn').forEach(btn => {
                const playlistId = btn.getAttribute('data-id');
                const icon = btn.querySelector('i');
                const isLiked = window.likedPlaylistIds.includes(playlistId);
                
                // 좋아요 상태 설정
                if (isLiked) {
                    btn.classList.add('liked');
                    if (icon && icon.classList.contains('far')) {
                        icon.classList.replace('far', 'fas');
                    }
                } else {
                    btn.classList.remove('liked');
                    if (icon && icon.classList.contains('fas')) {
                        icon.classList.replace('fas', 'far');
                    }
                }
                
                console.log(`플레이리스트 ${playlistId} 좋아요 상태:`, isLiked ? '좋아요됨' : '좋아요 안됨');
            });
        } catch (error) {
            console.error('좋아요 UI 업데이트 중 오류:', error);
        }
    }
    
    // 간단한 토스트 메시지 표시
    function showToast(message, type = 'success') {
        // 기존 토스트 제거
        const existingToast = document.querySelector('.toast-message');
        if (existingToast) {
            existingToast.remove();
        }
        
        const toast = document.createElement('div');
        toast.className = `toast-message ${type}`;
        
        // 아이콘 추가
        let icon = '';
        if (type === 'success') icon = '<i class="fas fa-check-circle"></i> ';
        else if (type === 'error') icon = '<i class="fas fa-exclamation-circle"></i> ';
        else if (type === 'warning') icon = '<i class="fas fa-exclamation-triangle"></i> ';
        
        toast.innerHTML = icon + message;
        
        document.body.appendChild(toast);
        
        // 애니메이션 시작
        setTimeout(() => {
            toast.classList.add('show');
        }, 10);
        
        // 3초 후 사라짐
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => {
                toast.remove();
            }, 300);
        }, 3000);
    }
    
    // 에러 메시지 표시
    function showErrorMessage(message) {
        const container = document.querySelector('main') || document.body;
        
        const errorDiv = document.createElement('div');
        errorDiv.className = 'error-message global';
        errorDiv.innerHTML = `
            <i class="fas fa-exclamation-circle"></i>
            <p>${message}</p>
            <button class="retry-btn" onclick="location.reload()">다시 시도</button>
        `;
        
        container.prepend(errorDiv);
    }

    // 트랙 미리보기 로드
    async function loadPlaylistPreviewTracks(playlistId, container) {
        try {
            container.innerHTML = '<div class="loading-tracks">트랙 불러오는 중...</div>';
            
            const response = await fetch(`https://api.spotify.com/v1/playlists/${playlistId}/tracks?limit=20`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${ACCESS_TOKEN}`
                }
            });
            
            if (!response.ok) {
                throw new Error('트랙 가져오기 실패');
            }
            
            const data = await response.json();
            const tracks = data.items;
            
            if (!tracks || tracks.length === 0) {
                container.innerHTML = '<div class="no-tracks">이 플레이리스트에는 트랙이 없습니다.</div>';
                return;
            }
            
            // 트랙 HTML 생성 - 전체 표시 및 스크롤 가능하도록 수정
            const tracksHTML = `
                <div class="tracks-header">
                    <div class="track-number">#</div>
                    <div class="track-info-header">제목</div>
                    <div class="track-album-header">앨범</div>
                    <div class="track-duration-header">재생시간</div>
                    <div class="track-actions-header"></div>
                </div>
                <div class="tracks-list">
                    ${tracks.map((item, index) => {
                        const track = item.track;
                        if (!track) return '';
                        
                        const albumImage = track.album.images && track.album.images.length > 0 
                            ? track.album.images[track.album.images.length - 1].url 
                            : `${contextPath}/static/assets/images/default-track.png`;
                        
                        const artistNames = track.artists.map(a => a.name).join(', ');
                        const spotifyUrl = track.external_urls ? track.external_urls.spotify : null;
                        const hasPreview = !!track.preview_url;
                        
                        return `
                            <div class="track-preview-item" data-spotify-url="${spotifyUrl || ''}">
                                <div class="track-number">${index + 1}</div>
                                <div class="track-info-container">
                                    <img class="track-thumbnail" src="${albumImage}" alt="${track.name}">
                                    <div class="track-preview-info">
                                        <div class="track-name">${track.name}</div>
                                        <div class="track-artist">${artistNames}</div>
                                    </div>
                                </div>
                                <div class="track-album">${track.album.name}</div>
                                <div class="track-duration">${formatDuration(track.duration_ms)}</div>
                                <div class="track-preview-actions">
                                    ${track.preview_url ? 
                                        `<button class="preview-btn" 
                                            data-url="${track.preview_url}" 
                                            data-spotify="${spotifyUrl || ''}" 
                                            data-has-preview="true"
                                            title="미리듣기">
                                            <i class="fas fa-play"></i>
                                        </button>` : 
                                        `<button class="preview-btn" 
                                            data-url="${spotifyUrl}" 
                                            data-spotify="${spotifyUrl || ''}" 
                                            data-has-preview="false"
                                            title="Spotify에서 듣기">
                                            <i class="fab fa-spotify"></i>
                                        </button>`
                                    }
                                    <button class="add-to-playlist-btn" data-id="${track.id}" title="내 플레이리스트에 추가">
                                        <i class="fas fa-plus"></i>
                                    </button>
                                </div>
                            </div>
                        `;
                    }).join('')}
                </div>
            `;
            
            container.innerHTML = tracksHTML;
            
            // 미리듣기 버튼 이벤트 설정
            setupPreviewButtons(container);
            
        } catch (error) {
            console.error('트랙 로딩 오류:', error);
            container.innerHTML = '<div class="error-message">트랙을 불러오는데 실패했습니다.</div>';
        }
    }

    // 플레이리스트 상세 페이지 트랙 로드
    async function loadPlaylistTracks(playlistId) {
        const container = document.getElementById('tracksList');
        const loader = document.getElementById('tracksLoader');
        
        if (!container) return;
        
        try {
            loader.style.display = 'flex';
            
            const response = await fetch(`https://api.spotify.com/v1/playlists/${playlistId}/tracks`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${ACCESS_TOKEN}`
                }
            });
            
            if (!response.ok) {
                throw new Error('트랙 가져오기 실패');
            }
            
            const data = await response.json();
            const tracks = data.items;
            
            // 로딩 인디케이터 숨기기
            loader.style.display = 'none';
            
            if (!tracks || tracks.length === 0) {
                container.innerHTML = '<div class="no-tracks">이 플레이리스트에는 트랙이 없습니다.</div>';
                return;
            }
            
            // 트랙 HTML 생성
            const tracksHTML = tracks.map((item, index) => {
                const track = item.track;
                if (!track) return '';
                
                const albumImage = track.album.images && track.album.images.length > 0 
                    ? track.album.images[1]?.url || track.album.images[0].url 
                    : `${contextPath}/static/assets/images/default-track.png`;
                
                const spotifyUrl = track.external_urls ? track.external_urls.spotify : null;
                const hasPreview = !!track.preview_url;
                
                return `
                    <div class="track-item" data-id="${track.id}" data-spotify-url="${spotifyUrl || ''}">
                        <div class="track-rank">${index + 1}</div>
                        <img class="track-image" src="${albumImage}" alt="${track.name}">
                        <div class="track-info">
                            <div class="track-name">${track.name}</div>
                            <div class="track-artist">${track.artists.map(a => a.name).join(', ')}</div>
                        </div>
                        <div class="track-album">${track.album.name}</div>
                        <div class="track-duration">${formatDuration(track.duration_ms)}</div>
                        <div class="track-controls">
                            ${track.preview_url ? 
                                `<button class="track-btn preview-btn" 
                                    data-url="${track.preview_url}"
                                    data-spotify="${spotifyUrl || ''}"
                                    data-has-preview="true">
                                    <i class="fas fa-play"></i>
                                </button>` : 
                                `<button class="track-btn preview-btn"
                                    data-url="${spotifyUrl}"
                                    data-spotify="${spotifyUrl || ''}"
                                    data-has-preview="false">
                                    <i class="fab fa-spotify"></i>
                                </button>`
                            }
                            <button class="track-btn add-btn" data-id="${track.id}" data-name="${track.name}" 
                                    data-artist="${track.artists[0].name}" data-album="${track.album.name}" 
                                    data-image="${albumImage}">
                                <i class="fas fa-plus"></i>
                            </button>
                        </div>
                    </div>
                `;
            }).join('');
            
            container.innerHTML = tracksHTML;
            
            // 미리듣기 버튼 이벤트 설정
            setupPreviewButtons(container);
            
        } catch (error) {
            console.error('트랙 로딩 오류:', error);
            loader.style.display = 'none';
            container.innerHTML = `
                <div class="error-message">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>트랙을 불러오는데 실패했습니다.</p>
                    <button class="retry-btn" onclick="location.reload()">다시 시도</button>
                </div>
            `;
        }
    }

    // 미리듣기 버튼 이벤트 설정
    let currentAudio = null;
    let currentButton = null;

    function setupPreviewButtons(container) {
        container.querySelectorAll('.preview-btn').forEach(button => {
            button.addEventListener('click', function(e) {
                e.stopPropagation();
                
                const previewUrl = this.getAttribute('data-url');
                if (!previewUrl) return;
                
                const icon = this.querySelector('i');
                
                // 트랙 정보 가져오기
                let trackName = '트랙';
                let artistName = '아티스트';
                let coverUrl = `${contextPath}/static/assets/images/default-track.png`;
                let spotifyUrl = this.getAttribute('data-spotify') || null;
                const hasPreview = this.getAttribute('data-has-preview') !== 'false';
                
                // 트랙 아이템 또는 부모 요소에서 정보 추출
                const trackItem = this.closest('.track-item, .track-preview-item');
                if (trackItem) {
                    const nameEl = trackItem.querySelector('.track-name');
                    const artistEl = trackItem.querySelector('.track-artist');
                    const imageEl = trackItem.querySelector('.track-image, .track-thumbnail');
                    
                    if (nameEl) trackName = nameEl.textContent;
                    if (artistEl) artistName = artistEl.textContent;
                    if (imageEl && imageEl.src) coverUrl = imageEl.src;
                    
                    // Spotify URL이 없으면 트랙 아이템에서 찾기
                    if (!spotifyUrl && trackItem.getAttribute('data-spotify-url')) {
                        spotifyUrl = trackItem.getAttribute('data-spotify-url');
                    }
                }
                
                // 이미 재생 중인지 확인
                if (currentButton === this && currentAudio && !currentAudio.paused) {
                    // 일시정지
                    currentAudio.pause();
                    icon.className = 'fas fa-play';
                    currentButton = null;
                    return;
                }
                
                // 이전 재생 초기화
                if (currentButton && currentButton !== this) {
                    const prevIcon = currentButton.querySelector('i');
                    if (prevIcon) prevIcon.className = 'fas fa-play';
                }
                
                // 오디오 플레이어 API 사용
                if (window.playPreview) {
                    window.playPreview(previewUrl, trackName, artistName, coverUrl, spotifyUrl, hasPreview);
                    icon.className = 'fas fa-pause';
                    currentButton = this;
                } else {
                    // 오디오 플레이어 API가 없는 경우 기본 오디오 객체 사용
                    if (currentAudio) {
                        currentAudio.pause();
                    }
                    
                    currentAudio = new Audio(previewUrl);
                    currentAudio.play()
                        .then(() => {
                            icon.className = 'fas fa-pause';
                            currentButton = this;
                            
                            currentAudio.addEventListener('ended', () => {
                                icon.className = 'fas fa-play';
                                currentButton = null;
                            });
                        })
                        .catch(err => {
                            console.error('오디오 재생 실패:', err);
                            icon.className = 'fas fa-exclamation-circle';
                            setTimeout(() => {
                                icon.className = 'fas fa-play';
                            }, 2000);
                            
                            // 스포티파이 URL이 있으면 열기 제안
                            if (spotifyUrl) {
                                if (confirm(`'${trackName}'은(는) 미리듣기를 재생할 수 없습니다. Spotify에서 들으시겠습니까?`)) {
                                    window.open(spotifyUrl, '_blank');
                                }
                            }
                        });
                }
            });
        });
    }

    // 좋아요 버튼 설정 (상세 페이지)
    function setupLikeButton() {
        const likeBtn = document.getElementById('likePlaylist');
        
        if (likeBtn && window.currentUserId) {
            likeBtn.addEventListener('click', async function() {
                const playlistId = this.getAttribute('data-playlist-id');
                const action = this.getAttribute('data-action');
                const likeText = document.getElementById('likeText');
                
                try {
                    const result = await toggleLikePlaylist(playlistId, action);
                    
                    if (result) {
                        // 버튼 상태 업데이트
                        if (action === 'add') {
                            this.setAttribute('data-action', 'remove');
                            this.classList.add('liked');
                            likeText.textContent = 'Liked';
                        } else {
                            this.setAttribute('data-action', 'add');
                            this.classList.remove('liked');
                            likeText.textContent = 'Like';
                        }
                    }
                } catch (error) {
                    console.error('좋아요 처리 오류:', error);
                }
            });
        }
    }

    // 플레이리스트 좋아요 토글
    async function toggleLikePlaylist(playlistId, action) {
        try {
            if (!window.currentUserId || window.currentUserId === 'null') {
                alert('로그인이 필요한 기능입니다.');
                window.location.href = `${contextPath}/auth/login.do`;
                return false;
            }
            
            // 디버깅 로그 추가
            console.log('좋아요 요청 파라미터:', { playlistId, action, userId: window.currentUserId });
            console.log('현재 좋아요 목록:', window.likedPlaylistIds);
            
            // 작업 전 좋아요 상태 확인
            const isLiked = window.likedPlaylistIds && Array.isArray(window.likedPlaylistIds) && 
                          window.likedPlaylistIds.includes(playlistId);
            console.log(`플레이리스트 ${playlistId} 현재 좋아요 상태:`, isLiked ? '좋아요됨' : '좋아요 안됨');
            
            // 요청과 현재 상태가 일치하는지 확인
            if ((action === 'add' && isLiked) || (action === 'remove' && !isLiked)) {
                console.log('이미 원하는 상태입니다. 서버 요청 생략');
                return true;
            }
            
            // URL 설정
            const baseUrl = window.location.origin;
            const url = `${baseUrl}${contextPath}/playlist.do`;
            
            console.log('요청 URL:', url);
            
            // URLSearchParams를 사용하여 폼 데이터 생성 (FormData 대신)
            const params = new URLSearchParams();
            params.append('action', 'like'); // 컨트롤러에서 처리할 액션
            params.append('playlistId', playlistId); // 플레이리스트 ID
            params.append('likeAction', action); // add 또는 remove
            
            // 파라미터 로깅
            console.log('--- 전송할 파라미터 ---');
            for (const [key, value] of params.entries()) {
                console.log(`${key}: ${value}`);
            }
            
            // 요청 전송 (POST 방식, URLSearchParams 사용)
            console.log('POST 요청 전송 중...');
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                },
                body: params
            });
            
            // 응답 확인
            console.log('서버 응답 상태:', response.status, response.statusText);
            if (!response.ok) {
                throw new Error(`서버 응답 오류: ${response.status}`);
            }
            
            const text = await response.text();
            console.log('서버 응답 내용:', text);
            
            // JSON 응답인지 확인
            let result;
            try {
                result = JSON.parse(text);
                console.log('JSON 파싱 결과:', result);
            } catch (e) {
                console.error('JSON 파싱 오류:', e);
                // JSON이 아닌 경우 성공으로 처리 (JSP가 반환될 수 있음)
                return true;
            }
            
            if (result && result.status === 'OK') {
                console.log(`✅ 플레이리스트 ${action === 'add' ? '좋아요' : '좋아요 취소'} 성공`);
                
                // 서버에서 받은 좋아요 목록으로 상태 업데이트
                if (result.likedPlaylistIds) {
                    window.likedPlaylistIds = result.likedPlaylistIds;
                    console.log('서버에서 받은 좋아요 목록으로 업데이트:', window.likedPlaylistIds);
                    updateLikedPlaylists();
                } else {
                    // 이전 방식으로 폴백
                    updateLikedPlaylistsState(playlistId, action === 'add');
                }
                
                return true;
            } else if (result) {
                throw new Error(result.message || '알 수 없는 오류가 발생했습니다.');
            } else {
                // 결과가 없는 경우 성공으로 처리
                return true;
            }
        } catch (error) {
            console.error('❌ 플레이리스트 좋아요 처리 오류:', error);
            showToast('좋아요 처리 중 오류가 발생했습니다: ' + error.message, 'error');
            return false;
        }
    }

    // 시간 포맷 함수
    function formatDuration(ms) {
        const minutes = Math.floor(ms / 60000);
        const seconds = ((ms % 60000) / 1000).toFixed(0);
        return `${minutes}:${seconds.padStart(2, '0')}`;
    }

    // 페이지 초기화
    init();
}); 