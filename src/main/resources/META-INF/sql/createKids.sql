drop table kids;
create table kids (
    tenant_id varchar(10) not null,
    id int not null auto_increment,
    name varchar(100),
    age int not null,
    primary key (id)
);