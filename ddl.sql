create table question
(
    id         bigint auto_increment,
    content    text                        not null,
    title      varchar(255)                not null,
    category   enum ('BACKEND','FRONTEND') not null,
    created_at timestamp(6),
    updated_at timestamp(6),
    primary key (id)
);

create table subscribe
(
    id                     bigint auto_increment,
    email                  varchar(255)                not null,
    category               enum ('BACKEND','FRONTEND') not null,
    frequency              enum ('DAILY','WEEKLY')     not null default 'DAILY',
    next_question_sequence bigint                      not null default '0',
    token                  varchar(255)                not null,
    created_at             timestamp(6),
    updated_at             timestamp(6),
    deleted_at             timestamp(6),
    primary key (id),
    unique key `subscribe_token_unique` (token)
);

create table mail_event
(
    id         bigint auto_increment,
    is_success boolean      not null,
    email      varchar(255) not null,
    type       varchar(255) not null,
    created_at timestamp(6),
    updated_at timestamp(6),
    primary key (id)
);

create table admin
(
    id         bigint auto_increment,
    email      varchar(255) not null,
    created_at timestamp(6),
    updated_at timestamp(6),
    primary key (id)
);

create table temporal_subscribe
(
    id          bigint auto_increment,
    email       varchar(255) not null,
    verify_code varchar(255) not null,
    is_verified boolean      not null,
    created_at  timestamp(6),
    updated_at  timestamp(6),
    primary key (id)
);

create table subscribe_question
(
    id           bigint auto_increment,
    question_id  bigint,
    subscribe_id bigint,
    is_success   boolean not null,
    created_at   timestamp(6),
    updated_at   timestamp(6),
    primary key (id),
    constraint `fk_subscribe_question_question_id` foreign key (`question_id`) references `question` (`id`),
    constraint `fk_subscribe_question_subscribe_id` foreign key (`subscribe_id`) references `subscribe` (`id`)
);

create table admin_notice
(
    id          bigint auto_increment,
    title       varchar(255) not null,
    content     text         not null,
    reserved_at timestamp(6) not null,
    created_at  timestamp(6),
    updated_at  timestamp(6),
    primary key (id)
);

create table member
(
    id                bigint auto_increment,
    name              varchar(255) not null,
    provider_id       varchar(255) not null,
    provider          varchar(10)  not null,
    github_url        varchar(255),
    refresh_token     varchar(255) not null,
    profile_image_url varchar(255),
    created_at        timestamp(6) not null,
    updated_at        timestamp(6) not null,
    deleted_at        timestamp(6),
    primary key (id),
    unique key `member_provider_id_unique` (provider_id),
    unique key `member_refresh_token_unique` (refresh_token)
);

create table wiki
(
    id              bigint auto_increment,
    member_id       bigint       not null,
    question        varchar(255) not null,
    question_detail text,
    category        varchar(10)  not null,
    is_anonymous    boolean      not null,
    created_at      timestamp(6) not null,
    updated_at      timestamp(6) not null,
    deleted_at      timestamp(6),
    primary key (id),
    constraint `fk_wiki_member_id` foreign key (member_id) references member (id)
);

create table comment
(
    id           bigint auto_increment,
    member_id    bigint       not null,
    wiki_id      bigint       not null,
    answer       text         not null,
    is_anonymous boolean      not null,
    created_at   timestamp(6) not null,
    updated_at   timestamp(6) not null,
    deleted_at   timestamp(6),
    primary key (id),
    constraint `fk_comment_wiki_id` foreign key (wiki_id) references wiki (id),
    constraint `fk_comment_member_id` foreign key (member_id) references member (id)
);

create table comment_like
(
    id         bigint auto_increment,
    member_id  bigint       not null,
    comment_id bigint       not null,
    created_at timestamp(6) not null,
    primary key (id),
    unique key `comment_like_member_id_comment_id_unique` (member_id, comment_id),
    constraint `fk_comment_like_comment_id` foreign key (comment_id) references comment (id),
    constraint `fk_comment_like_member_id` foreign key (member_id) references member (id)
);


create table multiple_choice_option
(
    id                bigint       not null auto_increment,
    content           varchar(255) not null,
    is_correct_answer boolean      not null,
    question_id       bigint       not null,
    created_at        datetime(6)  not null,
    updated_at        datetime(6)  not null,
    primary key (id),
    constraint `fk_multiple_choice_option_question_id` foreign key (question_id) references multiple_choice_question (id)
);

create table multiple_choice_question
(
    id                         bigint       not null auto_increment,
    title                      varchar(255) not null,
    correct_answer_explanation text,
    workbook_id                bigint       not null,
    created_at                 datetime(6)  not null,
    updated_at                 datetime(6)  not null,
    primary key (id),
    constraint `fk_multiple_choice_question_workbook_id` foreign key (workbook_id) references multiple_choice_workbook (id)
);

create table multiple_choice_workbook
(
    id               bigint       not null auto_increment,
    title            varchar(255) not null,
    difficulty_level int          not null,
    category         varchar(10)  not null,
    workbook_detail  text,
    time_limit       int,
    solved_count     int          not null,
    member_id        bigint       not null,
    created_at       datetime(6)  not null,
    updated_at       datetime(6)  not null,
    primary key (id),
    constraint `fk_multiple_choice_workbook_member_id` foreign key (member_id) references member (id)
)
