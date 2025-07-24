document.addEventListener('DOMContentLoaded', () => {
    // 전역 변수 및 상수
    const editButtons = document.querySelectorAll('.edit-btn');
    
    // 모든 수정 버튼에 이벤트 리스너 추가
    editButtons.forEach(btn => {
        btn.addEventListener('click', handleEditButtonClick);
    });
    
    // 사용자 메뉴 초기화 (네비게이션 메뉴)
    initUserMenu();
    
    // 수정 버튼 클릭 처리 함수
    function handleEditButtonClick(event) {
        const btn = event.currentTarget;
        const field = btn.dataset.field;
        const fieldDiv = btn.closest('.profile-field');
        const valueDiv = fieldDiv.querySelector('.field-value');
        
        // 이미 편집 중인 경우 중복 클릭 방지
        if (fieldDiv.querySelector('.edit-form')) {
            return;
        }
        
        // 현재 표시된 값 저장
        const displayedValue = valueDiv.innerText;
        
        // 실제 데이터 값 (마스킹 되지 않은 원본)
        let originalValue = '';
        
        if (field === 'password') {
            originalValue = ''; // 비밀번호는 항상 빈 값에서 시작
        } else if (field === 'email' || field === 'name' || field === 'phone') {
            originalValue = userData[field] || '';
        }
        
        // 폼 생성
        const editForm = document.createElement('div');
        editForm.className = 'edit-form';
        
        // 입력 타입 결정
        const inputType = field === 'password' ? 'password' : 'text';
        
        // 입력 필드 생성
        const input = document.createElement('input');
        input.type = inputType;
        input.className = 'edit-input';
        input.value = originalValue;
        input.placeholder = getPlaceholderForField(field);
        
        if (field === 'email') {
            input.disabled = true; // 이메일은 수정 불가능하게 설정
            input.style.opacity = '0.7';
            setTimeout(() => {
                alert('이메일은 현재 수정할 수 없습니다.');
                removeEditForm();
            }, 500);
            return;
        }
        
        // 버튼 컨테이너
        const buttonsContainer = document.createElement('div');
        buttonsContainer.className = 'edit-form-buttons';
        
        // 저장 버튼
        const saveButton = document.createElement('button');
        saveButton.type = 'button';
        saveButton.className = 'save-btn';
        saveButton.textContent = '저장';
        saveButton.addEventListener('click', () => saveChanges(field, input.value));
        
        // 취소 버튼
        const cancelButton = document.createElement('button');
        cancelButton.type = 'button';
        cancelButton.className = 'cancel-btn';
        cancelButton.textContent = '취소';
        cancelButton.addEventListener('click', removeEditForm);
        
        // 폼 조립
        buttonsContainer.appendChild(saveButton);
        buttonsContainer.appendChild(cancelButton);
        editForm.appendChild(input);
        editForm.appendChild(buttonsContainer);
        
        // 값 표시 영역 숨김 및 폼 추가
        valueDiv.style.display = 'none';
        btn.style.display = 'none';
        fieldDiv.appendChild(editForm);
        
        // 입력 필드에 포커스
        input.focus();
    }
    
    // 필드별 placeholder 텍스트 반환
    function getPlaceholderForField(field) {
        switch(field) {
            case 'email': return '이메일';
            case 'name': return '이름을 입력하세요';
            case 'phone': return '휴대폰 번호 (예: 010-1234-5678)';
            case 'password': return '새 비밀번호';
            default: return '';
        }
    }
    
    // 변경사항 저장
    function saveChanges(field, value) {
        // 입력값 검증
        if (!validateInput(field, value)) {
            return;
        }
        
        // URL 인코딩된 폼 데이터 구성
        const encodedData = `field=${encodeURIComponent(field)}&value=${encodeURIComponent(value)}`;
        
        // 서버에 변경사항 저장 요청
        fetch(`${contextPath}/profile.do`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: encodedData
        })
        .then(response => response.text())
        .then(() => {
            // 페이지 새로고침하여 변경사항 반영
            window.location.reload();
        })
        .catch(error => {
            console.error('프로필 업데이트 오류:', error);
            alert('프로필 업데이트 중 오류가 발생했습니다. 다시 시도해주세요.');
            removeEditForm();
        });
    }
    
    // 입력값 검증
    function validateInput(field, value) {
        switch(field) {
            case 'name':
                if (!value.trim()) {
                    alert('이름을 입력해주세요.');
                    return false;
                }
                if (value.length > 20) {
                    alert('이름은 최대 20자까지 입력 가능합니다.');
                    return false;
                }
                return true;
                
            case 'password':
                if (!value.trim()) {
                    alert('비밀번호를 입력해주세요.');
                    return false;
                }
                if (value.length < 4) {
                    alert('비밀번호는 최소 4자 이상 입력해주세요.');
                    return false;
                }
                return true;
                
            case 'phone':
                // 전화번호 형식 검증 (010-1234-5678 형식)
                const phonePattern = /^01[0-9]-\d{3,4}-\d{4}$/;
                if (!phonePattern.test(value)) {
                    alert('올바른 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)');
                    return false;
                }
                return true;
                
            default:
                return true;
        }
    }
    
    // 편집 폼 제거
    function removeEditForm() {
        const editForm = document.querySelector('.edit-form');
        if (editForm) {
            const fieldDiv = editForm.closest('.profile-field');
            const valueDiv = fieldDiv.querySelector('.field-value');
            const btn = fieldDiv.querySelector('.edit-btn');
            
            valueDiv.style.display = 'block';
            btn.style.display = 'block';
            editForm.remove();
        }
    }
    
    // 사용자 메뉴 초기화
    function initUserMenu() {
        const userMenu = document.querySelector('.user-menu');
        const userDropdown = document.querySelector('.user-dropdown');
        
        if (userMenu && userDropdown) {
            userMenu.addEventListener('click', (e) => {
                e.stopPropagation();
                const content = userDropdown.querySelector('.user-dropdown-content');
                
                if (window.getComputedStyle(content).display === 'block') {
                    content.style.display = 'none';
                } else {
                    content.style.display = 'block';
                }
            });
            
            document.addEventListener('click', () => {
                const content = userDropdown.querySelector('.user-dropdown-content');
                if (content) {
                    content.style.display = 'none';
                }
            });
        }
    }
}); 