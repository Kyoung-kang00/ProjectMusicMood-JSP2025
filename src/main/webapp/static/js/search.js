/**
 * search.js - MusicMood 검색 기능
 */
document.addEventListener('DOMContentLoaded', () => {
	console.log("검색 기능 초기화");

	// DOM 요소 가져오기
	const searchBtn = document.getElementById('searchBtn');
	const searchInput = document.getElementById('searchInput');
	const searchForm = document.querySelector('.search-form');

	// contextPath 정의 (없을 경우 공백으로 처리)
	const contextPath = window.contextPath || '';
	
	console.log("searchBtn:", searchBtn);
	console.log("searchInput:", searchInput);
	console.log("searchForm:", searchForm);
	console.log("contextPath:", contextPath);

	if (!searchBtn || !searchInput || !searchForm) {
		console.error("검색 관련 DOM 요소를 찾을 수 없습니다!");
		return;
	}

	// 토큰 변수
	let accessToken = '';

	// 검색창 토글
	searchBtn.addEventListener('click', (e) => {
		e.stopPropagation();
		searchForm.classList.toggle('active');

		if (searchForm.classList.contains('active')) {
			setTimeout(() => searchInput.focus(), 300);
		} else {
			// 검색창 닫을 때 결과도 함께 닫기
			removeSearchResults();
			searchInput.value = '';
		}

		console.log("검색창 토글:", searchForm.classList.contains('active'));
	});

	// 검색창 내부 클릭
	searchForm.addEventListener('click', (e) => {
		e.stopPropagation();
	});

	// 외부 클릭 시 닫기
	document.addEventListener('click', () => {
		if (searchForm.classList.contains('active')) {
			searchForm.classList.remove('active');
			removeSearchResults();
		}
	});

	// 기존 검색 결과 요소 제거 함수
	function removeSearchResults() {
		const existingResults = document.getElementById('searchResultsFixed');
		if (existingResults) existingResults.remove();
	}

	// 검색 입력 처리
	let searchTimeout;
	searchInput.addEventListener('input', () => {
		const query = searchInput.value.trim();
		console.log("검색어 입력:", query);

		clearTimeout(searchTimeout);

		// 기존 검색 결과 제거
		removeSearchResults();

		if (query.length < 2) {
			return;
		}

		// 로딩 표시 생성
		createSearchResults('<div class="search-loading">검색 중</div>');

		searchTimeout = setTimeout(() => {
			performSearch(query);
		}, 500);
	});

	// 검색 결과 요소 생성 함수
	function createSearchResults(html) {
		removeSearchResults();
		const searchResultsFixed = document.createElement('div');
		searchResultsFixed.id = 'searchResultsFixed';
		searchResultsFixed.innerHTML = `
			<button class="search-close-btn">✕</button>
			${html}
		`;
		searchForm.appendChild(searchResultsFixed);
		
		// 검색 결과 위치 조정
		positionSearchResults(searchResultsFixed);
		
		searchResultsFixed.querySelector('.search-close-btn').addEventListener('click', (e) => {
			e.stopPropagation();
			removeSearchResults();
		});
		searchResultsFixed.addEventListener('click', (e) => {
			e.stopPropagation();
		});
		return searchResultsFixed;
	}
	
	// 검색 결과 위치 조정 함수
	function positionSearchResults(resultsElement) {
		if (!resultsElement) return;
		
		const formRect = searchForm.getBoundingClientRect();
		
		// 모바일 여부 확인
		const isMobile = window.innerWidth <= 768;
		
		if (isMobile) {
			// 모바일에서는 폼 아래에 전체 너비로 표시
			resultsElement.style.top = `${formRect.bottom}px`;
			resultsElement.style.right = '20px';
			resultsElement.style.left = 'auto';
			resultsElement.style.width = 'calc(100vw - 40px)';
			resultsElement.style.maxWidth = '400px';
		} else {
			// 데스크톱에서는 우측 정렬
			resultsElement.style.top = `${formRect.bottom + 5}px`;
			resultsElement.style.right = '0';
		}
	}

	// 토큰 가져오기
	async function fetchAccessToken() {
		try {
			console.log("토큰 요청 시작");
			
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
			console.log("토큰 응답:", data);

			if (data.status === 'SUCCESS' && data.access_token) {
				accessToken = data.access_token;
				console.log("토큰 설정 완료:", accessToken);
				return accessToken;
			} else {
				throw new Error(data.error || "토큰 데이터가 올바르지 않습니다");
			}
		} catch (error) {
			console.error('토큰 가져오기 오류:', error);
			throw error;
		}
	}

	// 검색 실행 함수
	async function performSearch(query) {
		try {
			console.log("검색 시작:", query);

			if (!accessToken) {
				console.log("토큰 요청 필요");
				await fetchAccessToken();
			}

			// 검색 API 요청
			const apiUrl = `https://api.spotify.com/v1/search?q=${encodeURIComponent(query)}&type=artist,track&limit=3&market=KR`;
			console.log("API 요청:", apiUrl);

			const response = await fetch(apiUrl, {
				headers: {
					'Authorization': `Bearer ${accessToken}`
				}
			});

			console.log("API 응답 상태:", response.status);

			if (!response.ok) {
				if (response.status === 401) {
					console.log("토큰 만료, 갱신");
					accessToken = '';
					await fetchAccessToken();
					return performSearch(query);
				}
				throw new Error(`API 오류: ${response.status}`);
			}

			const data = await response.json();
			console.log("검색 성공");

			// 결과 표시
			displaySearchResults(data, query);

		} catch (error) {
			console.error('검색 오류:', error);
			createSearchResults(`
        <div class="no-results">
          검색 중 오류가 발생했습니다.<br>
          ${error.message}
        </div>
      `);
		}
	}

	// 검색 결과 표시
	function displaySearchResults(data, query) {
		console.log("결과 표시 시작");
		console.log("아티스트:", data.artists?.items?.length || 0);
		console.log("트랙:", data.tracks?.items?.length || 0);

		// 결과 없음
		if (!data.artists?.items?.length && !data.tracks?.items?.length) {
			createSearchResults(`
        <div class="no-results">
          "${query}"에 대한 검색 결과가 없습니다.
        </div>
      `);
			return;
		}

		let resultsHTML = '';

		// 아티스트 결과
		if (data.artists?.items?.length > 0) {
			resultsHTML += `<div class="search-category">아티스트</div>`;

			data.artists.items.forEach(artist => {
				const artistImage = artist.images?.length > 0
					? artist.images[0].url
					: './static/assets/images/default-artist.png';

				const genreInfo = '대한민국/가수/그룹';

				resultsHTML += `
          <div class="search-result-item artist" data-type="artist" data-id="${artist.id}">
            <img src="${artistImage}" alt="${artist.name}" class="result-image">
            <div class="result-info">
              <div class="result-name">${artist.name}</div>
              <div class="result-detail">${genreInfo}</div>
            </div>
          </div>
        `;
			});
		}

		// 트랙 결과
		if (data.tracks?.items?.length > 0) {
			resultsHTML += `<div class="search-category">트랙</div>`;

			data.tracks.items.forEach(track => {
				const trackImage = track.album?.images?.length > 0
					? track.album.images[0].url
					: './static/assets/images/default-track.png';

				const artistNames = track.artists?.map(a => a.name).join(', ') || '알 수 없는 아티스트';

				resultsHTML += `
          <div class="search-result-item track" data-type="track" data-id="${track.id}">
            <img src="${trackImage}" alt="${track.name}" class="result-image">
            <div class="result-info">
              <div class="result-name">${track.name}</div>
              <div class="result-detail">${artistNames}</div>
            </div>
          </div>
        `;
			});
		}

		// 결과 표시
		const searchResultsFixed = createSearchResults(resultsHTML);
		console.log("결과 표시 완료");

		// 결과 항목 클릭 이벤트
		searchResultsFixed.querySelectorAll('.search-result-item').forEach(item => {
			item.addEventListener('click', () => {
				const type = item.dataset.type;
				const id = item.dataset.id;

				console.log(`${type} 클릭:`, id);

				if (type === 'artist') {
					// 아티스트 페이지로 이동
					alert(`아티스트 ID: ${id} 페이지로 이동`);
					// window.location.href = `artist.jsp?id=${id}`;
				} else if (type === 'track') {
					// 트랙 정보 표시
					alert(`트랙 ID: ${id} 정보 표시`);
					// window.location.href = `track.jsp?id=${id}`;
				}

				// 검색창 닫기
				searchForm.classList.remove('active');
				removeSearchResults();
			});
		});
	}

	// 초기 토큰 요청
	fetchAccessToken().catch(error => {
		console.error("초기 토큰 요청 실패:", error);
	});

	// 창 크기 변경 시 검색 결과 위치 조정
	window.addEventListener('resize', () => {
		const results = document.getElementById('searchResultsFixed');
		if (results) {
			positionSearchResults(results);
		}
	});
});