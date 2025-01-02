--   12월 30일 월요일인 경우 :
--        기대 전송 질문 건수 : 12건
--        실제 전송 질문 건수 : 7건
--        성공 건수 : 5건
--        실패 건수 : 2건
--   12월 31일 화요일인 경우 :
--        기대 전송 질문 건수 : 8건
--        실제 전송 질문 건수 : 8건
--        성공 건수 : 8건
--        실패 건수 : 0건

insert into question(title, content, category)
values ('백엔드 질문 1',
        '백엔드 질문 1 내용',
        'BACKEND');

insert into subscribe(email, category, next_question_sequence, token, created_at, frequency)
values ('test1@gmail.com', 'BACKEND', 0, 'test-token-1', '2024-11-01', 'WEEKLY'),
       ('test2@gmail.com', 'FRONTEND', 0, 'test-token-2', '2024-11-01', 'DAILY'),
       ('test3@naver.com', 'BACKEND', 0, 'test-token-3', '2024-11-01', 'DAILY'),
       ('test4@naver.com', 'BACKEND', 0, 'test-token-4', '2024-11-01', 'DAILY'),
       ('test5@naver.com', 'BACKEND', 0, 'test-token-5', '2024-11-01', 'DAILY'),
       ('test6@naver.com', 'BACKEND', 0, 'test-token-6', '2024-11-01', 'DAILY'),
       ('test7@naver.com', 'BACKEND', 0, 'test-token-7', '2024-11-01', 'DAILY'),
       ('test8@naver.com', 'BACKEND', 0, 'test-token-8', '2024-11-01', 'DAILY'),
       ('test9@naver.com', 'BACKEND', 0, 'test-token-9', '2024-12-30 07:00:00', 'DAILY');

insert into subscribe(email, category, next_question_sequence, token, created_at, deleted_at, frequency)
values ('test10@naver.com', 'FRONTEND', 0, 'test-token-10', '2024-11-01', '2024-11-02', 'DAILY');

insert into subscribe_question(subscribe_id, question_id, is_success, created_at)
values (1, 1, true, '2024-12-30 07:10'),
       (1, 1, true, '2024-12-30 07:10'),
       (1, 1, true, '2024-12-30 07:10'),
       (1, 1, true, '2024-12-30 07:10'),
       (1, 1, true, '2024-12-30 07:10'),
       (1, 1, false, '2024-12-30 07:10'),
       (1, 1, false, '2024-12-30 07:10'),
       (1, 1, true, '2024-12-31 07:10'),
       (1, 1, true, '2024-12-31 07:10'),
       (1, 1, true, '2024-12-31 07:10'),
       (1, 1, true, '2024-12-31 07:10'),
       (1, 1, true, '2024-12-31 07:10'),
       (1, 1, true, '2024-12-31 07:10'),
       (1, 1, true, '2024-12-31 07:10'),
       (1, 1, true, '2024-12-31 07:10');
