create table question
(
    id       bigint auto_increment,
    content  text         not null,
    title    varchar(255) not null,
    category enum ('BACKEND','FRONTEND') not null,
    primary key (id)
);

create table subscribe
(
    id       bigint auto_increment,
    email    varchar(255) not null,
    category enum ('BACKEND','FRONTEND') not null,
    primary key (id)
);

create table mail_event
(
    id         bigint auto_increment,
    date       date,
    is_success boolean      not null,
    email      varchar(255) not null,
    type       varchar(255) not null,
    primary key (id)
);

create table admin
(
    id    bigint auto_increment,
    email varchar(255) not null,
    primary key (id)
);
