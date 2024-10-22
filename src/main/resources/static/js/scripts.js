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

    const questionDetailInput = document.getElementById('questionDetail');
    const markdownPreview = document.getElementById('markdownPreview');

    document.getElementById('createQuestionBtn').addEventListener('click', function () {
        document.getElementById('questionFormTitle').innerText = '질문 생성';
        document.getElementById('questionIdWrapper').classList.add('hidden');

        markdownPreview.innerHTML = "";
        document.getElementById('questionId').value = "";
        document.getElementById('questionTitle').value = "";
        document.getElementById('questionCategory').value = "";
        document.getElementById('questionDetail').value = "";
        document.getElementById('questionForm').classList.remove('hidden');
    });

    document.getElementById('cancelBtn').addEventListener('click', function () {
        document.getElementById('questionForm').classList.add('hidden');
    });

    document.querySelectorAll('.edit-btn').forEach(button => {
        button.addEventListener('click', async function () {
            const row = this.closest('tr');
            const id = row.children[0].textContent;
            const title = row.children[1].textContent;
            const category = row.children[3].textContent;
            const res = await fetch(`/question/${id}`, {
                method: 'GET', headers: {
                    Accept: 'application/json'
                }
            })
            const json = await res.json();

            markdownPreview.innerHTML = marked(json.content);
            document.getElementById('questionFormTitle').innerText = '질문 수정';
            document.getElementById('questionId').value = id;
            document.getElementById('questionTitle').value = title;
            document.getElementById('questionCategory').value = category;
            document.getElementById('questionDetail').value = json.content;
            document.getElementById('questionIdWrapper').classList.remove('hidden');
            document.getElementById('questionForm').classList.remove('hidden');
        });
    });

    questionDetailInput.addEventListener('input', function () {
        const markdownText = questionDetailInput.value;
        markdownPreview.innerHTML = marked(markdownText);
    });
});
