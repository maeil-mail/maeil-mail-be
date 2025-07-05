create table maeilmail.admin
(
    id         bigint auto_increment
        primary key,
    email      varchar(255) not null,
    created_at timestamp(6) null,
    updated_at timestamp(6) null
);

create table maeilmail.admin_notice
(
    id          bigint auto_increment
        primary key,
    title       varchar(255) not null,
    content     text         not null,
    reserved_at date         not null,
    created_at  timestamp(6) null,
    updated_at  timestamp(6) null
);

create table maeilmail.mail_event
(
    id         bigint auto_increment
        primary key,
    created_at timestamp(6) null,
    is_success tinyint(1)   not null,
    email      varchar(255) not null,
    type       varchar(255) not null,
    updated_at timestamp(6) null
);

create table maeilmail.member
(
    id                bigint auto_increment
        primary key,
    name              varchar(255) not null,
    provider_id       varchar(255) not null,
    provider          varchar(10)  not null,
    github_url        varchar(255) null,
    refresh_token     varchar(255) not null,
    profile_image_url varchar(255) null,
    created_at        timestamp(6) not null,
    updated_at        timestamp(6) not null,
    deleted_at        timestamp(6) null,
    constraint member_provider_id_unique
        unique (provider_id),
    constraint member_refresh_token_unique
        unique (refresh_token)
);

create table maeilmail.multiple_choice_workbook
(
    id               bigint auto_increment
        primary key,
    title            varchar(255) not null,
    difficulty_level int          not null,
    category         varchar(10)  not null,
    workbook_detail  text         null,
    time_limit       int          null,
    solved_count     int          not null,
    member_id        bigint       not null,
    created_at       timestamp(6) not null,
    updated_at       timestamp(6) not null,
    constraint fk_multiple_choice_workbook_member_id
        foreign key (member_id) references maeilmail.member (id)
);

create table maeilmail.multiple_choice_question
(
    id                         bigint auto_increment
        primary key,
    title                      varchar(255) not null,
    correct_answer_explanation text         null,
    workbook_id                bigint       not null,
    created_at                 timestamp(6) not null,
    updated_at                 timestamp(6) not null,
    constraint fk_multiple_choice_question_workbook_id
        foreign key (workbook_id) references maeilmail.multiple_choice_workbook (id)
);

create table maeilmail.multiple_choice_option
(
    id                bigint auto_increment
        primary key,
    content           varchar(255) not null,
    is_correct_answer tinyint(1)   not null,
    question_id       bigint       not null,
    created_at        timestamp(6) not null,
    updated_at        timestamp(6) not null,
    constraint fk_multiple_choice_option_question_id
        foreign key (question_id) references maeilmail.multiple_choice_question (id)
);

create table maeilmail.question
(
    id         bigint auto_increment
        primary key,
    content    text                         null,
    title      varchar(255)                 not null,
    category   enum ('BACKEND', 'FRONTEND') not null,
    created_at timestamp(6)                 null,
    updated_at timestamp(6)                 null
);

create table maeilmail.subscribe
(
    id                     bigint auto_increment
        primary key,
    email                  varchar(255)                             not null,
    category               enum ('BACKEND', 'FRONTEND')             not null,
    created_at             timestamp(6)                             null,
    updated_at             timestamp(6)                             null,
    next_question_sequence bigint                   default 0       not null,
    deleted_at             timestamp(6)                             null,
    token                  varchar(255)             default ''      not null,
    frequency              enum ('DAILY', 'WEEKLY') default 'DAILY' not null,
    constraint subscribe_token_unique
        unique (token)
);

create index idx_subscribe_created_at
    on maeilmail.subscribe (created_at);

create index idx_subscribe_email
    on maeilmail.subscribe (email);

create table maeilmail.subscribe_question
(
    id           bigint auto_increment
        primary key,
    question_id  bigint       null,
    subscribe_id bigint       null,
    is_success   tinyint(1)   not null,
    created_at   timestamp(6) null,
    updated_at   timestamp(6) null,
    constraint fk_subscribe_question_question_id
        foreign key (question_id) references maeilmail.question (id),
    constraint fk_subscribe_question_subscribe_id
        foreign key (subscribe_id) references maeilmail.subscribe (id)
);

create table maeilmail.temporal_subscribe
(
    id          bigint auto_increment
        primary key,
    email       varchar(255) not null,
    verify_code varchar(255) not null,
    is_verified tinyint(1)   not null,
    created_at  timestamp(6) null,
    updated_at  timestamp(6) null
);

create table maeilmail.wiki
(
    id              bigint auto_increment
        primary key,
    member_id       bigint       not null,
    question        varchar(255) not null,
    question_detail text         null,
    category        varchar(10)  not null,
    is_anonymous    tinyint(1)   not null,
    created_at      timestamp(6) not null,
    updated_at      timestamp(6) not null,
    deleted_at      timestamp(6) null,
    constraint fk_wiki_member_id
        foreign key (member_id) references maeilmail.member (id)
);

create table maeilmail.comment
(
    id           bigint auto_increment
        primary key,
    member_id    bigint       not null,
    wiki_id      bigint       not null,
    answer       text         not null,
    is_anonymous tinyint(1)   not null,
    created_at   timestamp(6) not null,
    updated_at   timestamp(6) not null,
    deleted_at   timestamp(6) null,
    constraint fk_comment_member_id
        foreign key (member_id) references maeilmail.member (id),
    constraint fk_comment_wiki_id
        foreign key (wiki_id) references maeilmail.wiki (id)
);

create table maeilmail.comment_like
(
    id         bigint auto_increment
        primary key,
    member_id  bigint       not null,
    comment_id bigint       not null,
    created_at timestamp(6) not null,
    constraint comment_like_member_id_comment_id_unique
        unique (member_id, comment_id),
    constraint fk_comment_like_comment_id
        foreign key (comment_id) references maeilmail.comment (id),
    constraint fk_comment_like_member_id
        foreign key (member_id) references maeilmail.member (id)
);
