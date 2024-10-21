window.addEventListener('DOMContentLoaded', event => {
    const sidebarToggle = document.body.querySelector('#sidebarToggle');
    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', event => {
            event.preventDefault();
            document.body.classList.toggle('sb-sidenav-toggled');
            localStorage.setItem('sb|sidebar-toggle', document.body.classList.contains('sb-sidenav-toggled'));
        });
    }

    const datatablesSimple = document.getElementById('datatablesSimple');
    if (datatablesSimple) {
        new simpleDatatables.DataTable(datatablesSimple);
    }

    document.getElementById('createQuestionBtn').addEventListener('click', function () {
        document.getElementById('questionFormTitle').innerText = '질문 생성';
        document.getElementById('questionForm').classList.remove('hidden');
    });

    document.getElementById('cancelBtn').addEventListener('click', function () {
        document.getElementById('questionForm').classList.add('hidden');
    });

    document.querySelectorAll('.edit-btn').forEach(button => {
        button.addEventListener('click', function () {
            const row = this.closest('tr'); // 클릭한 버튼의 행 찾기
            const title = row.children[1].textContent; // 질문 제목
            const detail = row.children[2].textContent; // 질문 상세
            const category = row.children[3].textContent; // 카테고리

            // 수정 폼에 기존 값 로드
            document.getElementById('questionFormTitle').innerText = '질문 수정';
            document.getElementById('questionTitle').value = title;

            // detail에 들어간 값에서 id 파싱해가지고, 백엔드 서버로 단건 조회 api 호출
            document.getElementById('questionDetail').value = detail;
            document.getElementById('questionCategory').value = category;

            // 수정 폼 보이기
            document.getElementById('questionForm').classList.remove('hidden');
        });
    });

    const questionDetailInput = document.getElementById('questionDetail');
    const markdownPreview = document.getElementById('markdownPreview');

    questionDetailInput.addEventListener('input', function () {
        const markdownText = questionDetailInput.value;
        markdownPreview.innerHTML = marked(markdownText);
    });
});
