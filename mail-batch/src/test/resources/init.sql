create table if not exists bucket
(
    id    bigint not null primary key,
    state blob   null
);

# 첫 페이지 조회 이후, 앞쪽 데이터가 삽입된 경우를 재연하기 위해서 2부터 시작한다.
# 참고 : ForwardReaderTest
ALTER TABLE forward_log AUTO_INCREMENT = 2;
