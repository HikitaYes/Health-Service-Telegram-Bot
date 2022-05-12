create table "users"
(
    id              int8 not null primary key,
    username         varchar(255),
);
insert into users (id, username) VALUES (505579308, 'hikita_yess');

create table "fav_medicines"
(
    id              serial primary key,
    id_user         int8 not null references users,
    medicine        varchar(255)
);
insert into fav_medicines (id_user, medicine) VALUES (505579308, 'афлокрем'), (505579308, 'антиангин');

create table "fav_address"
(
    id              serial primary key,
    id_user         int8 not null references users,
    address         varchar(255)
);
insert into fav_address (id_user, address) VALUES (505579308, 'Ленина, 51'), (505579308, 'Академика Парина, 43');