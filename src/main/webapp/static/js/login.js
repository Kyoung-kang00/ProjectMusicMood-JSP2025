document.addEventListener('DOMContentLoaded', () => {
	const slides = document.querySelectorAll('.background-slider .slide');
	let currentSlide = 0;
	const slideInterval = 5000; // 5초마다 슬라이드 변경

	// 초기 슬라이드 설정
	slides[currentSlide].classList.add('active');

	// 슬라이드 변경 함수
	function nextSlide() {
		slides[currentSlide].classList.remove('active');
		currentSlide = (currentSlide + 1) % slides.length;
		slides[currentSlide].classList.add('active');
	}

	// 자동 슬라이드 시작
	setInterval(nextSlide, slideInterval);

	// 이미지 프리로딩
	function preloadImages() {
		const imageUrls = [
			'./assets/images/5.png',
			'./assets/images/6.png',
			'./assets/images/7.png'
		];

		imageUrls.forEach(url => {
			const img = new Image();
			img.src = url;
		});
	}

	preloadImages();
});