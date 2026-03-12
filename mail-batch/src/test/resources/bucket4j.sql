create table if not exists bucket
(
    id    bigint not null primary key,
    state blob   null
);
