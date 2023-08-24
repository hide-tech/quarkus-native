create table public.posts(
    id bigserial primary key,
    name varchar(255),
    content varchar(1500),
    date_time timestamp
);