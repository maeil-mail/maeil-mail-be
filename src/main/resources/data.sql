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

insert into subscribe(email, category, next_question_sequence)
values ('leehaneul990623@gmail.com', 'BACKEND', 0),
       ('leehaneul0623@gmail.com', 'FRONTEND', 0);

insert into admin(email)
values ('leehaneul990623@gmail.com'),
       ('leehaneul0623@gmail.com');
