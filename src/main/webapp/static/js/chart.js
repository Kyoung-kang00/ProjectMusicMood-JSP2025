document.addEventListener('DOMContentLoaded', () => {
    let ACCESS_TOKEN = '';

    // contextPath 정의 (없을 경우 공백으로 처리)
    const contextPath = window.contextPath || '';
    console.log("contextPath:", contextPath);

    // ✅ 서버에서 Access Token 가져오기
    async function fetchAccessToken() {
        try {
            // contextPath 처리 - localStorage에서도 확인
            const storedContextPath = localStorage.getItem('appContextPath');
            let activeContextPath = contextPath || storedContextPath || '';
            console.log("contextPath:", activeContextPath);
            
            // API 요청 URL 구성 (도메인 기준 절대 경로로 구성)
            const baseUrl = window.location.origin; // 'http://localhost:8080' 같은 형태
            let apiUrl = `${baseUrl}${activeContextPath}/api/token`;
            console.log("API 요청 URL:", apiUrl);
            
            const response = await fetch(apiUrl);
            if (!response.ok) {
                throw new Error(`토큰 요청 실패: ${response.status}`);
            }
            const data = await response.json();
            
            if (data.status === 'SUCCESS' && data.access_token) {
                ACCESS_TOKEN = data.access_token;
                console.log('✅ 토큰 가져오기 성공');
                return true;
            } else {
                throw new Error(data.error || '토큰 데이터가 올바르지 않습니다');
            }
        } catch (error) {
            console.error('❌ Access Token 가져오기 실패:', error);
            return false;
        }
    }

    // ✅ 플레이리스트 ID
    const PLAYLIST_IDS = {
        global: '0qCecIw8Om1eqmNKpltLa3',
        korea: '0utGjGYyT1ejhfqvCpGBos',
        viral: '3epDPxgQIQLZeLvifFL6fw'
    };

    // ✅ 차트 데이터 가져오기
    async function fetchChartData(chartType = 'global') {
        try {
            const playlistId = PLAYLIST_IDS[chartType];

            const tracksResponse = await fetch(`https://api.spotify.com/v1/playlists/${playlistId}/tracks`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${ACCESS_TOKEN}`
                }
            });

            if (!tracksResponse.ok) {
                throw new Error(`Tracks API 오류! 상태 코드: ${tracksResponse.status}`);
            }

            const tracksData = await tracksResponse.json();
            return tracksData.items;
        } catch (error) {
            console.error('❌ 차트 데이터 로딩 실패:', error);
            throw error;
        }
    }

    // ✅ 트랙 아이템 HTML 생성
    function createTrackItem(trackData, index) {
        const track = trackData.track;
        const albumImage = track.album.images[0]?.url || `${contextPath}/static/assets/images/default-track.png`;

        return `
            <div class="track-item">
                <div class="track-rank">${index + 1}</div>
                <img class="track-image" src="${albumImage}" alt="${track.name}">
                <div class="track-info">
                    <div class="track-name">${track.name}</div>
                    <div class="track-artist">${track.artists.map(artist => artist.name).join(', ')}</div>
                </div>
                <div class="track-duration">${formatDuration(track.duration_ms)}</div>
                <div class="track-controls">
                    ${track.preview_url ?
                        `<button class="track-btn play-btn" data-preview="${track.preview_url}">▶️</button>` :
                        `<a href="${track.external_urls.spotify}" target="_blank" class="track-btn">🎵</a>`
                    }
                    <button class="track-btn like-btn">❤️</button>
                </div>
            </div>
        `;
    }

    // ✅ 시간 포맷 함수
    function formatDuration(ms) {
        const minutes = Math.floor(ms / 60000);
        const seconds = ((ms % 60000) / 1000).toFixed(0);
        return `${minutes}:${seconds.padStart(2, '0')}`;
    }

    // ✅ 차트 렌더링
    async function renderChart(chartType = 'global') {
        const loadingSpinner = document.getElementById('loadingSpinner');
        const chartTracks = document.getElementById('chartTracks');

        try {
            loadingSpinner.style.display = 'block';
            const tracks = await fetchChartData(chartType);

            if (!tracks || tracks.length === 0) {
                throw new Error('차트 데이터가 없습니다.');
            }

            chartTracks.innerHTML = tracks
                .map((track, index) => createTrackItem(track, index))
                .join('');

            document.querySelectorAll('.play-btn').forEach(button => {
                button.addEventListener('click', function () {
                    const previewUrl = this.dataset.preview;
                    if (previewUrl) {
                        playPreview(previewUrl, this);
                    }
                });
            });
        } catch (error) {
            console.error('❌ 차트 렌더링 실패:', error);
            chartTracks.innerHTML = `
                <div class="error-message">
                    차트를 불러오는데 실패했습니다.<br>
                    ${error.message}
                </div>
            `;
        } finally {
            loadingSpinner.style.display = 'none';
        }
    }

    // ✅ 미리듣기 재생 함수
    let currentAudio = null;
    let currentButton = null;

    function playPreview(url, button) {
        if (currentAudio && currentButton) {
            currentAudio.pause();
            currentButton.textContent = '▶️';
        }

        if (currentAudio && currentButton === button) {
            currentAudio = null;
            currentButton = null;
            return;
        }

        const audio = new Audio(url);
        audio.play();
        button.textContent = '⏸️';

        audio.onended = () => {
            button.textContent = '▶️';
        };

        currentAudio = audio;
        currentButton = button;
    }

    // ✅ 필터 버튼 이벤트
    const filterButtons = document.querySelectorAll('.filter-btn');
    filterButtons.forEach(button => {
        button.addEventListener('click', () => {
            filterButtons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');

            const chartType = button.dataset.chartType;
            renderChart(chartType);
        });
    });

    // ✅ 시작할 때 토큰부터 가져온 다음 차트 렌더링
    (async function init() {
        await fetchAccessToken();
        await renderChart('global');
    })();
});
