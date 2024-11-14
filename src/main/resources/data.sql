insert into question(title, content, category)
values ('백엔드 질문 1',
        '백엔드 질문 1 내용',
        'BACKEND'),
       ('백엔드 질문 2',
        '백엔드 질문 2 내용',
        'BACKEND'),
       ('백엔드 질문 3',
        '백엔드 질문 3',
        'BACKEND'),
       ('프론트 질문 1',
        '프론트 질문 1 내용',
        'FRONTEND'),
       ('프론트 질문 2',
        '프론트 질문 2 내용',
        'FRONTEND'),
       ('백엔드 질문 4',
        '백엔드 질문 4 내용',
        'BACKEND'),
       ('프론트 질문 3',
        '프론트 질문 3 내용',
        'FRONTEND');

insert into subscribe(email, category, next_question_sequence, token, created_at)
values ('leehaneul990623@gmail.com', 'BACKEND', 0, 'test-token-1', '2024-11-01'),
       ('leehaneul0623@gmail.com', 'FRONTEND', 0, 'test-token-2', '2024-11-01'),
       ('gosmdochee@gmail.com', 'BACKEND', 0, 'test-token-3', '2024-11-01');

insert into admin(email)
values ('leehaneul990623@gmail.com'),
       ('leehaneul0623@gmail.com');

insert into subscribe_question(subscribe_id, question_id, is_success)
values (3, 2, true),
       (3, 7, true),
       (3, 1, false),
       (3, 5, true);
