--定义数据表

--


CREATE TABLE game_record(
record_id SERIAL8 PRIMARY KEY NOT NULL ,
room_id BIGINT NOT NULL ,
start_time BIGINT NOT NULL ,
end_time BIGINT NOT NULL ,
file_path TEXT NOT NULL
);


create TABLE user_record_map(
user_id VARCHAR(32) NOT NULL ,
record_id BIGINT NOT NULL ,
room_id BIGINT NOT NULL,
user_nickname text NOT NULL
);


create table code_for_download(
code_id SERIAL8 PRIMARY KEY NOT NULL ,
deadline BIGINT NOT NULL ,
code varchar(64) NOT NULL
);