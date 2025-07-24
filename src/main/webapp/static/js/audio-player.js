/**
 * audio-player.js - MusicMood 오디오 플레이어 기능
 * 트랙 미리듣기 및 오디오 플레이어 제어를 위한 JavaScript 모듈입니다.
 */
document.addEventListener('DOMContentLoaded', () => {
    // 오디오 플레이어 요소
    const audioPlayer = {
        bar: document.getElementById('audioPlayerBar'),
        cover: document.getElementById('apCover'),
        title: document.getElementById('apTitle'),
        artist: document.getElementById('apArtist'),
        playPauseBtn: document.getElementById('apPlayPause'),
        playPauseIcon: document.querySelector('#apPlayPause i'),
        progress: document.getElementById('apProgress'),
        currentTime: document.getElementById('apCurrentTime'),
        duration: document.getElementById('apDuration'),
        volumeBtn: document.getElementById('apVolume'),
        progressBar: document.querySelector('.ap-progress-bar'),
        prevBtn: document.getElementById('apPrev'),
        nextBtn: document.getElementById('apNext'),
        spotifyLink: document.getElementById('apSpotifyLink') || document.createElement('a'),
        audio: new Audio()
    };
    
    // 현재 재생중인 오디오 상태
    let isPlaying = false;
    let currentVolume = 1.0;
    let isMuted = false;
    let currentButton = null;
    
    // 재생 목록 관련 변수
    let playlist = [];
    let currentTrackIndex = -1;
    
    // 오디오 플레이어 초기화
    function initAudioPlayer() {
        // 플레이어 UI 설정
        if (audioPlayer.bar) {
            // 명시적으로 플레이어를 표시
            audioPlayer.bar.style.display = 'flex';
            audioPlayer.bar.style.visibility = 'visible';
            audioPlayer.bar.style.opacity = '1';
            audioPlayer.bar.style.transform = 'translateY(0)';
        }
        
        // Spotify 링크 버튼 초기화
        if (!document.getElementById('apSpotifyLink') && audioPlayer.bar) {
            audioPlayer.spotifyLink.id = 'apSpotifyLink';
            audioPlayer.spotifyLink.className = 'ap-spotify-link';
            audioPlayer.spotifyLink.innerHTML = '<i class="fab fa-spotify"></i>';
            audioPlayer.spotifyLink.title = 'Spotify에서 듣기';
            audioPlayer.spotifyLink.target = '_blank';
            audioPlayer.spotifyLink.style.display = 'none';
            // 적절한 위치에 삽입
            if (audioPlayer.nextBtn && audioPlayer.nextBtn.parentNode) {
                audioPlayer.nextBtn.parentNode.appendChild(audioPlayer.spotifyLink);
            } else if (audioPlayer.bar) {
                audioPlayer.bar.appendChild(audioPlayer.spotifyLink);
            }
        }
        
        // 재생/일시정지 버튼 이벤트
        audioPlayer.playPauseBtn.addEventListener('click', togglePlayPause);
        
        // 볼륨 버튼 이벤트
        audioPlayer.volumeBtn.addEventListener('click', toggleMute);
        
        // 진행 바 클릭 이벤트
        audioPlayer.progressBar.addEventListener('click', setProgress);
        
        // 이전/다음 트랙 버튼 이벤트
        if (audioPlayer.prevBtn) {
            audioPlayer.prevBtn.addEventListener('click', playPreviousTrack);
        }
        
        if (audioPlayer.nextBtn) {
            audioPlayer.nextBtn.addEventListener('click', playNextTrack);
        }
        
        // 오디오 이벤트 리스너
        audioPlayer.audio.addEventListener('timeupdate', updateProgress);
        audioPlayer.audio.addEventListener('ended', onTrackEnded);
        audioPlayer.audio.addEventListener('canplay', () => {
            audioPlayer.duration.textContent = formatTime(audioPlayer.audio.duration);
        });
        
        // 플레이어 표시 상태 설정
        document.body.style.paddingBottom = '70px';
    }
    
    // 트랙 재생 함수
    function playTrack(url, title, artist, cover, spotifyUrl, hasPreview) {
        console.log('재생 요청:', { url, title, artist, hasPreview: hasPreview || false, spotifyUrl: spotifyUrl || 'none' });
        
        // 이전 트랙 초기화
        if (isPlaying) {
            audioPlayer.audio.pause();
        }
        
        // 새 트랙 정보 설정
        audioPlayer.title.textContent = title || '트랙';
        audioPlayer.artist.textContent = artist || '-';
        
        if (cover) {
            audioPlayer.cover.src = cover;
        } else {
            // 기본 이미지 설정
            audioPlayer.cover.src = `${window.contextPath || ''}/static/assets/images/default-track.png`;
        }
        
        // Spotify 링크 버튼 설정
        if (spotifyUrl) {
            audioPlayer.spotifyLink.href = spotifyUrl;
            audioPlayer.spotifyLink.style.display = 'block';
        } else {
            audioPlayer.spotifyLink.style.display = 'none';
        }
        
        // URL이 없는 경우
        if (!url) {
            showToast('이 트랙에는 재생 가능한 URL이 없습니다.', 'warning');
            if (spotifyUrl) {
                openSpotifyLink(spotifyUrl, title);
            }
            return;
        }
        
        // URL이 Spotify 웹 URL인 경우 (미리듣기가 아님)
        if (url.includes('open.spotify.com')) {
            console.log('Spotify 링크 감지됨:', url);
            showToast(`'${title}'은(는) Spotify에서만 들을 수 있습니다.`, 'info');
            openSpotifyLink(url, title);
            return;
        }
        
        // 오디오 소스 설정 및 재생
        audioPlayer.audio.src = url;
        audioPlayer.audio.play()
            .then(() => {
                isPlaying = true;
                audioPlayer.playPauseIcon.className = 'fas fa-pause';
                audioPlayer.bar.classList.add('playing');
                
                // 모든 미리듣기 버튼 아이콘 초기화 후 현재 버튼 상태 변경
                updatePreviewButtons();
            })
            .catch(err => {
                console.error('오디오 재생 실패:', err);
                showToast('오디오 재생 중 오류가 발생했습니다.', 'error');
                
                // 재생 실패 시 Spotify로 연결 시도
                if (spotifyUrl) {
                    showToast('Spotify에서 듣기를 시도합니다...', 'info');
                    setTimeout(() => {
                        openSpotifyLink(spotifyUrl, title);
                    }, 1000);
                }
            });
    }
    
    // Spotify 링크 열기 (사용자 확인 후)
    function openSpotifyLink(url, title) {
        if (confirm(`'${title}'을(를) Spotify에서 들으시겠습니까?`)) {
            window.open(url, '_blank');
        }
    }
    
    // 모든 미리듣기 버튼 상태 업데이트
    function updatePreviewButtons() {
        // 모든 미리듣기 버튼 초기화
        document.querySelectorAll('.preview-btn').forEach(btn => {
            const icon = btn.querySelector('i');
            if (icon) {
                icon.className = 'fas fa-play';
            }
            btn.classList.remove('playing');
            
            // 현재 재생 중인 트랙의 버튼인 경우 상태 변경
            if (audioPlayer.audio.src && btn.getAttribute('data-url') === audioPlayer.audio.src) {
                if (isPlaying) {
                    icon.className = 'fas fa-pause';
                    btn.classList.add('playing');
                    currentButton = btn;
                }
            }
        });
    }
    
    // 재생/일시정지 토글
    function togglePlayPause() {
        if (!audioPlayer.audio.src) return;
        
        if (isPlaying) {
            audioPlayer.audio.pause();
            audioPlayer.playPauseIcon.className = 'fas fa-play';
            isPlaying = false;
        } else {
            audioPlayer.audio.play();
            audioPlayer.playPauseIcon.className = 'fas fa-pause';
            isPlaying = true;
        }
        
        // 미리듣기 버튼 상태 업데이트
        updatePreviewButtons();
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
    
    // 재생 완료 시 다음 트랙 재생
    function onTrackEnded() {
        audioPlayer.audio.currentTime = 0;
        audioPlayer.playPauseIcon.className = 'fas fa-play';
        isPlaying = false;
        
        // 재생 목록에 다음 트랙이 있으면 재생
        if (playlist.length > 0 && currentTrackIndex < playlist.length - 1) {
            playNextTrack();
        } else {
            resetPlayer();
        }
    }
    
    // 이전 트랙 재생
    function playPreviousTrack() {
        if (playlist.length === 0 || currentTrackIndex <= 0) return;
        
        currentTrackIndex--;
        playTrackFromPlaylist(currentTrackIndex);
    }
    
    // 다음 트랙 재생
    function playNextTrack() {
        if (playlist.length === 0 || currentTrackIndex >= playlist.length - 1) return;
        
        currentTrackIndex++;
        playTrackFromPlaylist(currentTrackIndex);
    }
    
    // 재생 목록에서 특정 인덱스의 트랙 재생
    function playTrackFromPlaylist(index) {
        if (index < 0 || index >= playlist.length) return;
        
        currentTrackIndex = index;
        const track = playlist[index];
        playTrack(
            track.preview_url, 
            track.name, 
            track.artist, 
            track.image_url, 
            track.spotify_url, 
            track.has_preview
        );
        
        // 재생 목록 UI 업데이트 (있는 경우)
        updatePlaylistUI();
    }
    
    // 재생 목록 UI 업데이트
    function updatePlaylistUI() {
        const playlistElement = document.getElementById('audioPlaylist');
        if (!playlistElement) return;
        
        // 기존 목록 초기화
        playlistElement.innerHTML = '';
        
        // 재생 목록 표시
        playlist.forEach((track, index) => {
            const item = document.createElement('li');
            item.className = 'playlist-item' + (index === currentTrackIndex ? ' current' : '');
            item.innerHTML = `
                <span class="track-title">${track.name}</span>
                <span class="track-artist">${track.artist}</span>
                <span class="track-duration">${formatTime(30)}</span>
            `;
            
            item.addEventListener('click', () => {
                playTrackFromPlaylist(index);
            });
            
            playlistElement.appendChild(item);
        });
    }
    
    // 플레이어 초기화
    function resetPlayer() {
        audioPlayer.audio.currentTime = 0;
        audioPlayer.playPauseIcon.className = 'fas fa-play';
        isPlaying = false;
        
        // 미리듣기 버튼 상태 업데이트
        updatePreviewButtons();
    }
    
    // 시간 형식 변환 (초 -> MM:SS)
    function formatTime(time) {
        const minutes = Math.floor(time / 60);
        const seconds = Math.floor(time % 60);
        return `${minutes}:${seconds < 10 ? '0' : ''}${seconds}`;
    }
    
    // 토스트 메시지 표시 (없는 경우를 대비한 간단한 구현)
    function showToast(message, type = 'info') {
        // window.showToast 함수가 있으면 사용
        if (window.showToast) {
            window.showToast(message, type);
            return;
        }
        
        // 없으면 간단한 알림 표시
        console.log(`[${type.toUpperCase()}] ${message}`);
        
        // 기존 토스트 제거
        const existingToast = document.querySelector('.toast-message');
        if (existingToast) {
            existingToast.remove();
        }
        
        // 새 토스트 생성
        const toast = document.createElement('div');
        toast.className = `toast-message ${type}`;
        toast.textContent = message;
        document.body.appendChild(toast);
        
        // 3초 후 제거
        setTimeout(() => {
            toast.remove();
        }, 3000);
    }
    
    // 재생 목록에 트랙 추가
    function addTracksToPlaylist(tracks, autoplay = false) {
        // 기존 재생 목록 유지 또는 초기화
        if (tracks.length > 0) {
            const wasEmpty = playlist.length === 0;
            
            // 추가할 수 있는 트랙인지 확인 (모든 트랙 허용, 미리듣기 URL이 없어도 spotify_url 있으면 허용)
            const playableTracks = tracks.filter(track => 
                track.preview_url || track.spotify_url
            );
            
            if (playableTracks.length === 0) {
                showToast('재생 가능한 트랙이 없습니다.', 'warning');
                return false;
            }
            
            // 미리듣기 URL이 있는 트랙과 없는 트랙의 수
            const withPreview = playableTracks.filter(track => track.has_preview).length;
            const withoutPreview = playableTracks.length - withPreview;
            
            console.log(`재생 목록에 추가: 총 ${playableTracks.length}개 트랙 (미리듣기 있음: ${withPreview}, 미리듣기 없음: ${withoutPreview})`);
            
            // 트랙 추가
            playlist = playlist.concat(playableTracks);
            
            // 재생 목록 UI 업데이트
            updatePlaylistUI();
            
            // 자동 재생 옵션이 켜져있고 재생 목록이 비어있었다면 재생 가능한 트랙 재생
            if (autoplay && wasEmpty) {
                // 미리듣기가 있는 첫 번째 트랙의 인덱스 찾기
                const firstPreviewIndex = playlist.findIndex(track => track.has_preview);
                
                if (firstPreviewIndex !== -1) {
                    // 미리듣기 URL이 있는 트랙 먼저 재생
                    currentTrackIndex = firstPreviewIndex;
                    playTrackFromPlaylist(currentTrackIndex);
                } else if (playlist.length > 0) {
                    // 없으면 첫 번째 트랙 재생 (Spotify 링크)
                    currentTrackIndex = 0;
                    playTrackFromPlaylist(currentTrackIndex);
                }
            }
            
            // 알림 표시
            let message = `${playableTracks.length}개의 트랙이 재생 목록에 추가되었습니다.`;
            if (withoutPreview > 0) {
                message += ` (${withoutPreview}개는 Spotify에서만 들을 수 있습니다)`;
            }
            showToast(message, 'success');
            return true;
        }
        return false;
    }
    
    // 플레이리스트 ID로 트랙 가져와서 재생 목록에 추가
    async function loadPlaylistTracksById(playlistId, autoplay = true) {
        try {
            showToast('플레이리스트 트랙을 가져오는 중...', 'info');
            
            const baseUrl = window.location.origin;
            const url = `${baseUrl}${window.contextPath || ''}/playlist.do?action=preview&playlistId=${playlistId}&format=json`;
            
            console.log('플레이리스트 트랙 요청 URL:', url);
            
            const response = await fetch(url);
            if (!response.ok) {
                console.error('서버 응답 오류:', response.status, response.statusText);
                throw new Error(`서버 응답 오류: ${response.status}`);
            }
            
            // 응답 텍스트를 먼저 확인
            const responseText = await response.text();
            
            // 응답이 비어 있는지 확인
            if (!responseText || responseText.trim() === '') {
                console.error('서버에서 빈 응답을 반환했습니다.');
                return await loadTracksDirectlyFromSpotify(playlistId, autoplay);
            }
            
            // 디버깅을 위한 로그
            console.log('서버 응답 텍스트:', responseText.substring(0, 200) + '...');
            
            // JSON 파싱 시도
            let data;
            try {
                data = JSON.parse(responseText);
                console.log('파싱된 JSON 데이터:', data);
            } catch (parseError) {
                console.error('JSON 파싱 오류:', parseError);
                console.error('원본 응답:', responseText);
                
                // 응답의 처음 100자와 마지막 100자 로깅 (긴 응답의 경우)
                if (responseText.length > 200) {
                    console.error('응답 시작 부분:', responseText.substring(0, 100));
                    console.error('응답 끝 부분:', responseText.substring(responseText.length - 100));
                }
                
                return await loadTracksDirectlyFromSpotify(playlistId, autoplay);
            }
            
            if (data.status === 'OK' && data.tracks && data.tracks.length > 0) {
                console.log('가져온 트랙:', data.tracks);
                
                // 미리듣기가 가능한 트랙 필터링
                const tracksWithPreview = data.tracks.filter(track => 
                    track.preview_url && (!track.has_preview || track.has_preview === true));
                
                console.log(`미리듣기 가능한 트랙: ${tracksWithPreview.length}/${data.tracks.length}`);
                
                if (tracksWithPreview.length === 0) {
                    console.log('미리듣기 가능한 트랙이 없어 Spotify API 직접 호출 시도');
                    return await loadTracksDirectlyFromSpotify(playlistId, autoplay);
                }
                
                // 기존 재생 목록 초기화 옵션 (선택적)
                if (confirm('기존 재생 목록을 초기화하고 새 플레이리스트를 재생하시겠습니까?')) {
                    clearPlaylist();
                }
                
                // 재생 목록에 트랙 추가
                addTracksToPlaylist(data.tracks, autoplay);
                
                // 플레이리스트 이름 표시
                const playlistName = data.playlistName || '플레이리스트';
                showToast(`"${playlistName}" 재생 중 (${data.tracks.length}곡)`, 'success');
                return true;
            } else if (data.status === 'OK' && (!data.tracks || data.tracks.length === 0)) {
                console.log('미리듣기 가능한 트랙이 없어 Spotify API 직접 호출 시도');
                // Spotify API를 직접 호출하여 미리듣기 URL이 있는 트랙 가져오기
                return await loadTracksDirectlyFromSpotify(playlistId, autoplay);
            } else if (data.status === 'ERROR') {
                console.error('서버 오류 응답:', data.message);
                showToast(data.message || '서버 오류가 발생했습니다.', 'error');
                return false;
            } else {
                console.warn('미리듣기 가능한 트랙이 없습니다.');
                showToast('미리듣기 가능한 트랙이 없습니다.', 'warning');
                return false;
            }
        } catch (error) {
            console.error('플레이리스트 트랙 로드 오류:', error);
            
            // 오류 발생 시 Spotify API 직접 호출 시도
            try {
                console.log('서버 오류로 인해 Spotify API 직접 호출 시도');
                return await loadTracksDirectlyFromSpotify(playlistId, autoplay);
            } catch (spotifyError) {
                console.error('Spotify API 직접 호출 오류:', spotifyError);
                showToast('트랙 정보를 가져오는 중 오류가 발생했습니다.', 'error');
                return false;
            }
        }
    }
    
    // Spotify API를 직접 호출하여 플레이리스트 트랙 가져오기
    async function loadTracksDirectlyFromSpotify(playlistId, autoplay = true) {
        try {
            console.log('[Spotify 직접 호출] 시작 - 플레이리스트 ID:', playlistId);
            
            // 액세스 토큰 가져오기
            const tokenUrl = `${window.location.origin}${window.contextPath || ''}/api/token`;
            console.log('[Spotify 직접 호출] 토큰 요청 URL:', tokenUrl);
            
            const tokenResponse = await fetch(tokenUrl);
            if (!tokenResponse.ok) {
                console.error('[Spotify 직접 호출] 토큰 요청 실패:', tokenResponse.status, tokenResponse.statusText);
                throw new Error('Spotify 액세스 토큰을 가져올 수 없습니다.');
            }
            
            const tokenData = await tokenResponse.json();
            console.log('[Spotify 직접 호출] 토큰 응답:', tokenData);
            
            if (tokenData.status !== 'SUCCESS' || !tokenData.access_token) {
                console.error('[Spotify 직접 호출] 유효한 토큰이 없습니다:', tokenData);
                throw new Error('유효한 Spotify 액세스 토큰이 없습니다.');
            }
            
            const accessToken = tokenData.access_token;
            console.log('[Spotify 직접 호출] 액세스 토큰 취득 성공 (길이):', accessToken.length);
            
            // 플레이리스트 정보 가져오기 (이름 표시용)
            const playlistUrl = `https://api.spotify.com/v1/playlists/${playlistId}`;
            console.log('[Spotify 직접 호출] 플레이리스트 정보 요청 URL:', playlistUrl);
            
            const playlistResponse = await fetch(playlistUrl, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${accessToken}`
                }
            });
            
            if (!playlistResponse.ok) {
                console.error('[Spotify 직접 호출] 플레이리스트 정보 요청 실패:', playlistResponse.status, playlistResponse.statusText);
                throw new Error(`플레이리스트 정보 요청 실패: ${playlistResponse.status}`);
            }
            
            const playlistData = await playlistResponse.json();
            console.log('[Spotify 직접 호출] 플레이리스트 정보:', {
                name: playlistData.name,
                tracks_total: playlistData.tracks?.total || 0,
                owner: playlistData.owner?.display_name || 'Unknown'
            });
            
            const playlistName = playlistData.name;
            
            // 플레이리스트 트랙 가져오기 (100개로 늘림)
            const tracksUrl = `https://api.spotify.com/v1/playlists/${playlistId}/tracks?limit=100`;
            console.log('[Spotify 직접 호출] 트랙 정보 요청 URL:', tracksUrl);
            
            const tracksResponse = await fetch(tracksUrl, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${accessToken}`
                }
            });
            
            if (!tracksResponse.ok) {
                console.error('[Spotify 직접 호출] 트랙 정보 요청 실패:', tracksResponse.status, tracksResponse.statusText);
                throw new Error(`트랙 정보 요청 실패: ${tracksResponse.status}`);
            }
            
            const tracksData = await tracksResponse.json();
            console.log('[Spotify 직접 호출] 트랙 데이터 수신:', {
                total: tracksData.total,
                items_count: tracksData.items?.length || 0
            });
            
            // 트랙 데이터 상세 로깅
            if (tracksData.items && tracksData.items.length > 0) {
                console.log('[Spotify 직접 호출] 첫 5개 트랙 정보:');
                tracksData.items.slice(0, 5).forEach((item, index) => {
                    if (item.track) {
                        console.log(`  ${index+1}. ${item.track.name} - ${item.track.artists[0]?.name || 'Unknown'} (미리듣기 URL: ${item.track.preview_url ? '있음' : '없음'})`);
                    } else {
                        console.log(`  ${index+1}. 트랙 정보 없음`);
                    }
                });
            }
            
            // 모든 트랙 정보 변환 (미리듣기 없는 트랙도 포함)
            const allTracks = tracksData.items
                .filter(item => item.track && item.track.id)
                .map(item => {
                    const track = item.track;
                    const hasPreview = !!track.preview_url;
                    const spotifyUrl = track.external_urls?.spotify || null;
                    
                    return {
                        id: track.id,
                        name: track.name,
                        artist: track.artists.map(a => a.name).join(', '),
                        preview_url: track.preview_url || spotifyUrl, // 미리듣기 URL이 없으면 Spotify URL 사용
                        has_preview: hasPreview,
                        spotify_url: spotifyUrl,
                        image_url: track.album?.images && track.album.images.length > 0 
                            ? track.album.images[0].url 
                            : `${window.contextPath || ''}/static/assets/images/default-track.png`
                    };
                });
            
            // 미리듣기 URL이 있는 트랙 필터링
            const tracksWithPreview = allTracks.filter(track => track.has_preview);
            
            console.log('[Spotify 직접 호출] 미리듣기 가능한 트랙 수:', tracksWithPreview.length);
            console.log('[Spotify 직접 호출] 전체 트랙 수:', allTracks.length);
            
            // 모든 트랙 중 하나도 없을 경우
            if (allTracks.length === 0) {
                console.warn('[Spotify 직접 호출] 트랙을 가져올 수 없습니다.');
                showToast('트랙을 가져올 수 없습니다.', 'error');
                return false;
            }
            
            // 기존 재생 목록 초기화 옵션
            let shouldClear = false;
            if (playlist.length > 0) {
                shouldClear = confirm('기존 재생 목록을 초기화하고 새 플레이리스트를 추가하시겠습니까?');
                if (shouldClear) {
                    console.log('[Spotify 직접 호출] 기존 재생 목록 초기화');
                    clearPlaylist();
                }
            }
            
            // 모든 트랙 추가 (미리듣기 없는 트랙도 포함하여 Spotify 링크로 대체)
            console.log('[Spotify 직접 호출] 재생 목록에 트랙 추가 (자동 재생:', autoplay, ')');
            const addResult = addTracksToPlaylist(allTracks, autoplay);
            
            // 성공 메시지
            if (addResult) {
                const previewMsg = tracksWithPreview.length > 0 
                    ? `(미리듣기: ${tracksWithPreview.length}개)` 
                    : '(미리듣기 불가, Spotify 링크로 대체)';
                    
                const successMessage = `"${playlistName}" 재생 목록에 추가됨 - ${allTracks.length}개 트랙 ${previewMsg}`;
                console.log('[Spotify 직접 호출] 성공:', successMessage);
                showToast(successMessage, 'success');
            } else {
                showToast('재생 목록에 트랙을 추가하지 못했습니다.', 'warning');
            }
            
            return addResult;
            
        } catch (error) {
            console.error('[Spotify 직접 호출] 오류:', error);
            showToast('Spotify API를 통한 트랙 로드 실패: ' + error.message, 'error');
            return false;
        }
    }
    
    // 재생 목록 초기화
    function clearPlaylist() {
        playlist = [];
        currentTrackIndex = -1;
        updatePlaylistUI();
        
        // 현재 재생 중인 트랙 정지
        if (isPlaying) {
            audioPlayer.audio.pause();
            audioPlayer.playPauseIcon.className = 'fas fa-play';
            isPlaying = false;
        }
        
        showToast('재생 목록이 초기화되었습니다.', 'info');
    }
    
    // 전역 오디오 플레이어 초기화
    initAudioPlayer();
    
    // 전역에 미리듣기 함수 노출
    window.playPreview = (url, title, artist, cover, spotifyUrl, hasPreview) => {
        playTrack(url, title, artist, cover, spotifyUrl, hasPreview);
    };
    
    // 전역에 재생 목록 관련 함수 노출
    window.audioPlayerAPI = {
        addTracksToPlaylist,
        loadPlaylistTracksById,
        clearPlaylist,
        playTrack,
        togglePlayPause
    };
    
    // 미리듣기 버튼 이벤트 설정
    document.addEventListener('click', (e) => {
        // 미리듣기 버튼 클릭 처리
        const btn = e.target.closest('.preview-btn');
        if (!btn) return;
        
        const url = btn.getAttribute('data-url');
        if (!url) return;
        
        const name = btn.getAttribute('data-name') || '트랙';
        const artist = btn.getAttribute('data-artist') || '-';
        const image = btn.getAttribute('data-image');
        const spotifyUrl = btn.getAttribute('data-spotify') || null;
        const hasPreview = btn.getAttribute('data-has-preview') !== 'false';
        
        // 현재 재생 중인 트랙이고 재생 중인 경우 일시정지
        if (audioPlayer.audio.src === url && isPlaying) {
            togglePlayPause();
        } else {
            // 새 트랙 재생
            playTrack(url, name, artist, image, spotifyUrl, hasPreview);
        }
        
        // 이벤트 전파 중지
        e.preventDefault();
        e.stopPropagation();
    });
}); 