/**
 * common.js - MusicMood 공통 JavaScript 기능
 */
document.addEventListener('DOMContentLoaded', () => {
    console.log('MusicMood 공통 스크립트 로드됨');
    
    // 컨텍스트 경로 설정 (JSP에서 주입되지 않은 경우를 위한 기본값)
    if (typeof contextPath === 'undefined') {
        window.contextPath = '';
    }
    
    // 모바일 네비게이션 토글
    const menuButton = document.querySelector('.mobile-menu-button');
    if (menuButton) {
        const mainNav = document.querySelector('.main-nav');
        menuButton.addEventListener('click', () => {
            mainNav.classList.toggle('active');
        });
    }
    
    // API 호출 유틸리티 함수
    window.apiCall = async (url, options = {}) => {
        try {
            const defaultOptions = {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            };
            
            const response = await fetch(url, { ...defaultOptions, ...options });
            
            if (!response.ok) {
                throw new Error(`API 오류: ${response.status}`);
            }
            
            return await response.json();
        } catch (error) {
            console.error('API 호출 오류:', error);
            throw error;
        }
    };
    
    // 페이지 로딩 스피너
    window.showLoading = () => {
        const spinner = document.createElement('div');
        spinner.className = 'loading-spinner';
        spinner.innerHTML = '<div class="spinner"></div>';
        document.body.appendChild(spinner);
    };
    
    window.hideLoading = () => {
        const spinner = document.querySelector('.loading-spinner');
        if (spinner) {
            spinner.remove();
        }
    };
    
    // 알림 메시지 표시
    window.showMessage = (message, type = 'info', duration = 3000) => {
        const toast = document.createElement('div');
        toast.className = `toast-message ${type}`;
        toast.innerText = message;
        
        document.body.appendChild(toast);
        
        // 애니메이션 적용
        setTimeout(() => {
            toast.classList.add('show');
        }, 10);
        
        // 자동 제거
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => {
                toast.remove();
            }, 300);
        }, duration);
    };
    
    // 날짜 포맷 함수
    window.formatDate = (dateString) => {
        const options = { year: 'numeric', month: 'long', day: 'numeric' };
        return new Date(dateString).toLocaleDateString('ko-KR', options);
    };
    
    // 음악 재생 시간 포맷
    window.formatDuration = (ms) => {
        if (!ms) return '0:00';
        const minutes = Math.floor(ms / 60000);
        const seconds = Math.floor((ms % 60000) / 1000);
        return `${minutes}:${seconds.toString().padStart(2, '0')}`;
    };
}); 