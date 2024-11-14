create table question
(
    id               bigint auto_increment,
    content          text         not null,
    title            varchar(255) not null,
    customized_title varchar(255),
    category         enum ('BACKEND','FRONTEND') not null,
    created_at       timestamp(6),
    updated_at       timestamp(6),
    primary key (id)
);

create table subscribe
(
    id                     bigint auto_increment,
    email                  varchar(255) not null,
    category               enum ('BACKEND','FRONTEND') not null,
    next_question_sequence bigint       not null default '0',
    token                  varchar(255) not null unique,
    created_at             timestamp(6),
    updated_at             timestamp(6),
    deleted_at             timestamp(6),
    primary key (id)
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
    primary key (id)
);
