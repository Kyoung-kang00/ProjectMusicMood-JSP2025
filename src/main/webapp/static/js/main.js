document.addEventListener('DOMContentLoaded', () => {
	let ACCESS_TOKEN = '';

	// contextPath 정의 (없을 경우 공백으로 처리)
	const contextPath = window.contextPath || '';
	console.log("contextPath:", contextPath);

	// Access Token 가져오기
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
			if (!response.ok) throw new Error('Token request failed');
			const data = await response.json();
			ACCESS_TOKEN = data.access_token;
			return ACCESS_TOKEN;
		} catch (error) {
			console.error('Token fetch error:', error);
			return null;
		}
	}

	// 사용자 메뉴 초기화
	function initUserMenu() {
		const userMenu = document.querySelector('.user-menu');
		const userDropdown = document.querySelector('.user-dropdown');
		
		if (userMenu && userDropdown) {
			// 모바일 대응을 위한 터치 이벤트
			userMenu.addEventListener('click', (e) => {
				e.stopPropagation();
				const content = userDropdown.querySelector('.user-dropdown-content');
				
				// 이미 표시된 경우 토글 기능
				if (window.getComputedStyle(content).display === 'block') {
					content.style.display = 'none';
				} else {
					content.style.display = 'block';
				}
			});
			
			// 외부 클릭 시 닫기
			document.addEventListener('click', () => {
				const content = userDropdown.querySelector('.user-dropdown-content');
				if (content) {
					content.style.display = 'none';
				}
			});
		}
	}

	// 플레이리스트 ID
	const PLAYLISTS = [
		'2jqJzW5rXwu9FpUdGNdo2W', // 드라이브 플레이리스트
		'1Krm4ydj8K9nhqsbzXW9Zg', // 잔잔한 플레이리스트
		'7k6QVcqBgBIjQYdwVReZBG', // 파티 + 클럽 플레이리스트
		'6sJ5fgYe6zpxkOtsl1s8MG',  // 수고했어, 오늘도
		'29LZX2dHXmL9vyUZQwQtHF', // SUMMER HIT KPOP
		'6Ve9tZAEZrzVMnd58NszvB', // MEGA HIT REMIX
		'1q43xX4kCl9VSIta8NBWNC', // 2000년대 사랑노래
		'2vbVutkblurEK8uqQkIeLu', // WORKPLACE KPOP
	];

	// 시간 포맷 함수
	function formatDuration(ms) {
		const minutes = Math.floor(ms / 60000);
		const seconds = ((ms % 60000) / 1000).toFixed(0);
		return `${minutes}:${seconds.padStart(2, '0')}`;
	}

	// 플레이리스트 정보 가져오기
	async function fetchPlaylistInfo(playlistId) {
		try {
			// 플레이리스트 정보 가져오기
			const playlistResponse = await fetch(`https://api.spotify.com/v1/playlists/${playlistId}`, {
				headers: {
					'Authorization': `Bearer ${ACCESS_TOKEN}`
				}
			});
			if (!playlistResponse.ok) throw new Error('Playlist fetch failed');
			const playlist = await playlistResponse.json();

			return playlist;
		} catch (error) {
			console.error('Playlist fetch error:', error);
			return null;
		}
	}

	// 슬라이더 초기화
	function initSlider() {
		const slides = document.querySelectorAll('.slide');
		let currentSlide = 0;
		const slideInterval = 5000;

		if (slides.length > 0) {
			slides[0].classList.add('active');
		}

		function nextSlide() {
			slides[currentSlide].classList.remove('active');
			currentSlide = (currentSlide + 1) % slides.length;
			slides[currentSlide].classList.add('active');
		}

		setInterval(nextSlide, slideInterval);
	}

	// 플레이리스트 슬라이더 기능
	function initPlaylistSlider() {
		// 각 feature 카드에 인덱스 부여
		document.querySelectorAll('.feature-card').forEach((card, index) => {
			card.style.setProperty('--index', index);
		});
		
		// 각 플레이리스트 카드에 인덱스 부여
		document.querySelectorAll('.playlist-card').forEach((card, index) => {
			card.style.setProperty('--index', index);
		});
		
		// 각 차트 링크에 인덱스 부여
		document.querySelectorAll('.chart-link').forEach((link, index) => {
			link.style.setProperty('--index', index);
		});
		
		// 슬라이더 기능
		const slider = document.querySelector('.playlist-slider');
		const prevBtn = document.querySelector('.slider-control.prev');
		const nextBtn = document.querySelector('.slider-control.next');
		
		if (slider && prevBtn && nextBtn) {
			prevBtn.addEventListener('click', () => {
				slider.scrollBy({ left: -300, behavior: 'smooth' });
			});
			
			nextBtn.addEventListener('click', () => {
				slider.scrollBy({ left: 300, behavior: 'smooth' });
			});
		}
		
		// 탭 기능
		const tabs = document.querySelectorAll('.feature-tab');
		tabs.forEach(tab => {
			tab.addEventListener('click', () => {
				tabs.forEach(t => t.classList.remove('active'));
				tab.classList.add('active');
			});
		});
	}

	// 네비게이션 링크 강조 기능
	function highlightCurrentNavLink() {
		// 현재 페이지 URL 가져오기
		const currentPath = window.location.pathname;
		
		// 모든 네비게이션 링크 요소 가져오기
		const navLinks = document.querySelectorAll('.nav-links a');
		
		// 현재 경로에 맞는 링크 강조
		navLinks.forEach(link => {
			const linkPath = link.getAttribute('href');
			
			// 링크 경로가 현재 경로에 포함되어 있으면 active 클래스 추가
			if (linkPath && currentPath.includes(linkPath)) {
				link.classList.add('active');
			} else {
				link.classList.remove('active');
			}
		});
	}

	// 플레이리스트 썸네일 업데이트
	async function updatePlaylistThumbnails() {
		try {
			if (!ACCESS_TOKEN) {
				await fetchAccessToken();
			}

			const playlistCards = document.querySelectorAll('.playlist-card');
			if (!playlistCards.length) return;

			// 각 카드마다 플레이리스트 정보 가져와서 업데이트
			for (let i = 0; i < Math.min(playlistCards.length, PLAYLISTS.length); i++) {
				const playlistId = PLAYLISTS[i];
				const card = playlistCards[i];
				const playlist = await fetchPlaylistInfo(playlistId);
				
				if (playlist && playlist.images && playlist.images.length > 0) {
					// 썸네일 이미지 업데이트
					const thumbnailImg = card.querySelector('.playlist-thumbnail img');
					if (thumbnailImg) {
						thumbnailImg.src = playlist.images[0].url;
					}
					
					// 플레이리스트 정보 업데이트
					const titleElement = card.querySelector('.playlist-title');
					if (titleElement && playlist.name) {
						titleElement.textContent = playlist.name;
					}
					
					// 제작자 정보 업데이트 (있다면)
					const creatorElement = card.querySelector('.playlist-creator');
					if (creatorElement && playlist.owner && playlist.owner.display_name) {
						creatorElement.textContent = playlist.owner.display_name;
					}
				}
			}
		} catch (error) {
			console.error('Error updating playlist thumbnails:', error);
		}
	}

	// 무한대 이퀄라이저 초기화
	function initInfinityEqualizer() {
		const equalizer = document.querySelector('.infinity-equalizer-section');
		if (!equalizer) return;
		
		// 이퀄라이저 바 개별 높이 랜덤화
		const bars = document.querySelectorAll('.eq-bar');
		bars.forEach(bar => {
			// 초기 높이와 애니메이션 시간 랜덤화
			const height = 20 + Math.random() * 50;
			const duration = 0.8 + Math.random() * 0.8;
			bar.style.height = `${height}px`;
			bar.style.animationDuration = `${duration}s`;
		});
		
		// 무한대 모양 초기화
		const infinityShape = document.querySelector('.infinity-shape');
		if (infinityShape) {
			infinityShape.style.transition = 'transform 0.5s ease';
		}
		
		// 텍스트 초기화
		const leftText = equalizer.querySelector('.left-text');
		const rightText = equalizer.querySelector('.right-text');
		
		if (leftText && rightText) {
			// 초기 상태 설정
			leftText.style.transform = 'translateX(-20px)';
			rightText.style.transform = 'translateX(20px)';
			leftText.style.opacity = '0';
			rightText.style.opacity = '0';
			leftText.style.transition = 'all 0.8s ease';
			rightText.style.transition = 'all 0.8s ease';
		}
		
		// 스크롤에 의한 특별 효과 추가
		let lastScrollY = window.scrollY;
		let scrollAnimating = false;
		
		function handleScroll() {
			if (scrollAnimating) return;
			
			const currentScrollY = window.scrollY;
			const rect = equalizer.getBoundingClientRect();
			const isInView = rect.top < window.innerHeight && rect.bottom > 0;
			
			if (isInView) {
				// 스크롤 방향에 따라 이퀄라이저 효과 변경
				const scrollDirection = currentScrollY > lastScrollY ? 'down' : 'up';
				
				// 이퀄라이저 효과 강화
				bars.forEach(bar => {
					const currentHeight = parseInt(bar.style.height) || 20;
					let newHeight;
					
					if (scrollDirection === 'down') {
						newHeight = currentHeight + Math.random() * 15;
					} else {
						newHeight = Math.max(20, currentHeight - Math.random() * 15);
					}
					
					bar.style.height = `${newHeight}px`;
				});
				
				// 무한대 모양 확대/축소
				if (infinityShape) {
					scrollAnimating = true;
					
					if (scrollDirection === 'down') {
						infinityShape.style.transform = 'scale(1.05)';
					} else {
						infinityShape.style.transform = 'scale(0.95)';
					}
					
					// 애니메이션 리셋
					setTimeout(() => {
						infinityShape.style.transform = 'scale(1)';
						scrollAnimating = false;
					}, 300);
				}
				
				// 텍스트 효과
				if (leftText && rightText) {
					leftText.style.opacity = '1';
					rightText.style.opacity = '1';
					leftText.style.transform = 'translateX(0)';
					rightText.style.transform = 'translateX(0)';
				}
			} else {
				// 화면에서 벗어났을 때 텍스트 효과 초기화
				if (leftText && rightText) {
					leftText.style.opacity = '0';
					rightText.style.opacity = '0';
					
					// 스크롤 방향에 따라 다른 방향으로 준비
					if (currentScrollY > lastScrollY) {  // 다운 스크롤
						leftText.style.transform = 'translateX(-20px)';
						rightText.style.transform = 'translateX(20px)';
					} else {  // 업 스크롤
						leftText.style.transform = 'translateX(20px)';
						rightText.style.transform = 'translateX(-20px)';
					}
				}
			}
			
			lastScrollY = currentScrollY;
		}
		
		// 스크롤 이벤트에 이퀄라이저 효과 추가
		window.addEventListener('scroll', handleScroll);
		
		// 초기 체크 (이미 화면에 보이는 요소들)
		setTimeout(handleScroll, 300);
	}

	// 스크롤 애니메이션 초기화
	function initScrollAnimations() {
		const animatedSections = document.querySelectorAll('.scroll-animated');
		const body = document.body;
		let lastScrollTop = 0;
		let scrollDirection = 'down';
		let animationTimeout;
		const animationResetDelay = 100; // 애니메이션 리셋 딜레이 (ms)
		let ticking = false;
		
		// 초기 체크 (이미 화면에 보이는 요소들)
		checkVisibility();
		
		// 스크롤 이벤트 리스너
		window.addEventListener('scroll', () => {
			const st = window.pageYOffset || document.documentElement.scrollTop;
			
			// 스크롤 방향 감지
			if (st > lastScrollTop) {
				scrollDirection = 'down';
				body.classList.remove('scrolling-up');
				body.classList.add('scrolling-down');
			} else if (st < lastScrollTop) {
				scrollDirection = 'up';
				body.classList.remove('scrolling-down');
				body.classList.add('scrolling-up');
			}
			
			lastScrollTop = st <= 0 ? 0 : st; // iOS 바운스 효과를 위한 조건
			
			// 스크롤 이벤트 쓰로틀링
			if (!ticking) {
				window.requestAnimationFrame(() => {
					checkVisibility();
					ticking = false;
				});
				ticking = true;
			}
		});
		
		function checkVisibility() {
			// 현재 볼 수 있는 섹션들의 범위 계산
			const viewportHeight = window.innerHeight || document.documentElement.clientHeight;
			const viewportTop = window.pageYOffset || document.documentElement.scrollTop;
			const viewportBottom = viewportTop + viewportHeight;

			animatedSections.forEach(section => {
				const rect = section.getBoundingClientRect();
				const sectionTop = rect.top + viewportTop;
				const sectionBottom = sectionTop + rect.height;
				
				// 섹션이 화면에 보이는지 확인
				const isVisible = (
					(sectionTop <= viewportBottom * 0.9) && 
					(sectionBottom >= viewportTop + viewportHeight * 0.1)
				);
				
				const hasAnimation = section.classList.contains('animated');
				
				// 무한대 이퀄라이저 섹션은 별도 처리
				const isInfinitySection = section.classList.contains('infinity-equalizer-section');
				
				// 섹션이 뷰포트에 들어오면 애니메이션 추가
				if (isVisible && !hasAnimation) {
					// 트랜지션 방향 설정
					section.classList.add('animated');
					
					// 무한대 이퀄라이저 섹션은 특별 효과 적용
					if (isInfinitySection) {
						// 이퀄라이저 바 활성화
						const bars = section.querySelectorAll('.eq-bar');
						bars.forEach((bar, i) => {
							bar.style.animationPlayState = 'running';
							bar.style.animationDelay = `${i * 0.1}s`;
						});
						
						// 무한대 텍스트 효과
						const leftText = section.querySelector('.left-text');
						const rightText = section.querySelector('.right-text');
						
						if (leftText && rightText) {
							leftText.style.opacity = '0';
							rightText.style.opacity = '0';
							
							setTimeout(() => {
								leftText.style.transition = 'all 0.8s ease 0.3s';
								rightText.style.transition = 'all 0.8s ease 0.6s';
								leftText.style.opacity = '1';
								rightText.style.opacity = '1';
								leftText.style.transform = 'translateX(0)';
								rightText.style.transform = 'translateX(0)';
							}, 100);
						}
					} else {
						// 자식 요소들에 스타인 지정 (체인 효과)
						const childItems = section.querySelectorAll('.feature-card, .playlist-card, .chart-link');
						childItems.forEach((item, index) => {
							item.style.transitionDelay = `${0.1 * index + 0.2}s`;
						});
					}
				}
				// 섹션이 뷰포트에서 완전히 벗어나면 애니메이션 리셋
				else if (!isVisible && hasAnimation) {
					// 트랜지션 효과가 보이지 않을 때 제거 (화면 밖에서 초기화)
					clearTimeout(section.resetTimer);
					section.resetTimer = setTimeout(() => {
						section.classList.remove('animated');
						
						// 무한대 이퀄라이저는 바 애니메이션 일시정지
						if (isInfinitySection) {
							const bars = section.querySelectorAll('.eq-bar');
							bars.forEach(bar => {
								bar.style.animationPlayState = 'paused';
							});
							
							const leftText = section.querySelector('.left-text');
							const rightText = section.querySelector('.right-text');
							
							if (leftText && rightText) {
								leftText.style.opacity = '0';
								rightText.style.opacity = '0';
								
								// 스크롤 방향에 따라 다른 방향으로 준비
								if (scrollDirection === 'down') {
									leftText.style.transform = 'translateX(-20px)';
									rightText.style.transform = 'translateX(20px)';
								} else {
									leftText.style.transform = 'translateX(20px)';
									rightText.style.transform = 'translateX(-20px)';
								}
							}
						} else {
							// 카드 요소들도 초기화 (트랜지션 끄고 진행)
							const childItems = section.querySelectorAll('.feature-card, .playlist-card, .chart-link');
							childItems.forEach(item => {
								item.style.transition = 'none';
								// 강제 리플로우로 즉시 적용
								item.offsetHeight;
								item.style.transition = '';
							});
						}
					}, animationResetDelay);
				}
			});
		}
	}

	// 초기화 함수
	async function init() {
		try {
			initUserMenu();
			highlightCurrentNavLink();
			initSlider();
			initPlaylistSlider();
			initScrollAnimations();
			initInfinityEqualizer();
			await fetchAccessToken();
			await updatePlaylistThumbnails();
		} catch (error) {
			console.error('Initialization error:', error);
		}
	}

	// 페이지 로드 시 초기화
	init();
});