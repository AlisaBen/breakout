--定义数据表
--1.账户信息表
CREATE TABLE account_info(
account_id SERIAL8 PRIMARY KEY NOT NULL ,
email VARCHAR(64) NOT NULL ,
secure VARCHAR(128) NOT NULL ,
account_type INTEGER NOT NULL , --账户类型，1：标识管理员，2标识普通用户
create_time BIGINT NOT NULL DEFAULT 0,
nickname VARCHAR(32) NOT NULL DEFAULT '',
company_name VARCHAR(64) NOT NULL DEFAULT '',
company_desc VARCHAR(128) NOT NULL DEFAULT ''
);

--2.app表
CREATE TABLE app(
app_id SERIAL8 PRIMARY KEY NOT NULL ,
account_id BIGINT NOT NULL ,
app_name VARCHAR(32) NOT NULL ,
app_desc VARCHAR(256) NOT NULL DEFAULT '',
app_img VARCHAR (64) NOT NULL DEFAULT '',
create_time BIGINT NOT NULL DEFAULT 0
);

--3.app用户的第一次访问时间
CREATE TABLE app_user_first_visit(
app_id BIGINT NOT NULL ,
user_id VARCHAR(256) NOT NULL,
channel VARCHAR(16) NOT NULL DEFAULT 'ALL',--代表每个新用户第一次是从那个渠道来的，ALL代表事件未标识渠道
first_visit_time BIGINT NOT NULL DEFAULT 0,
PRIMARY KEY (app_id,user_id,channel)
);
create index index1_app_user_first_visit on app_user_first_visit(app_id,user_id);

--4.事件采集表
CREATE TABLE collect_event_data(
data_id BIGINT PRIMARY KEY NOT NULL, --data_id SERIAL8 PRIMARY KEY NOT NULL
app_id BIGINT NOT NULL,
event_type BIGINT NOT NULL , --标识事件是自定义事件还是无卖点定义的基本事件
user_id VARCHAR (256) NOT NULL ,
channel VARCHAR(16) NOT NULL DEFAULT 'ALL',
timestamp BIGINT NOT NULL DEFAULT 0,
data jsonb NOT NULL DEFAULT '{}'
--{"url":"http://sd","ip":"10.23.2.23"}
);
create index index1_collect_event_data on collect_event_data(app_id,timestamp);
create index index2_collect_event_data on collect_event_data(app_id,user_id,timestamp);

--5。app用户信息采集表
CREATE TABLE collect_user_info(
id SERIAL8 PRIMARY KEY NOT NULL ,
app_id BIGINT NOT NULL ,
user_id VARCHAR (256) NOT NULL ,
nickname VARCHAR (256) NOT NULL DEFAULT '',
head_img VARCHAR (256) NOT NULL DEFAULT '',
bbs_id VARCHAR(256) NOT NULL DEFAULT ''
);
create index index1_collect_user_info on collect_user_info(app_id,user_id);


--6.app概览表 新用户，访问次数，人数，总用户数 使用es机制
CREATE TABLE app_count_data(
id SERIAL8 PRIMARY KEY NOT NULL ,
app_id BIGINT NOT NULL ,
channel VARCHAR (16) NOT NULL ,
interval_type VARCHAR(32) NOT NULL ,
last_data_id BIGINT NOT NULL ,
timestamp BIGINT NOT NULL DEFAULT 0,
new_users BIGINT NOT NULL DEFAULT 0,
visit_times BIGINT NOT NULL DEFAULT 0,
visit_users BIGINT NOT NULL DEFAULT 0,
all_users BIGINT NOT NULL DEFAULT 0,
user_ids jsonb NOT NULL DEFAULT '{}' -- {"user_ids":["1","2"]}
);
create index index1_app_count_data on app_count_data(app_id,channel,interval_type,timestamp);

--7.自定义事件 埋点事件
CREATE TABLE custom_event_definition(
  custom_id SERIAL8 PRIMARY KEY NOT NULL,
  app_id BIGINT NOT NULL,
  event_name VARCHAR(255) NOT NULL DEFAULT '',
  property jsonb NOT NULL DEFAULT '{}',
  --属性：属性1#属性2#属性3 {[{"property_name":"11","property_name_cn":"sss","property_type:1"}...]}
  timestamp BIGINT NOT NULL DEFAULT 0
);

--8 分析事件定义
CREATE TABLE event_definition(
  event_id SERIAL8 PRIMARY KEY NOT NULL,
  app_id BIGINT NOT NULL,
  event_name VARCHAR(255) DEFAULT '' NOT NULL ,
  event_type BIGINT DEFAULT 0 NOT NULL , --时间类型：自定义事件id或者无卖点采集事件id
  filter_condition jsonb DEFAULT '{}' NOT NULL , --筛选条件
  -- format:条件1;and/or;条件2... 条件1：属性#type@判断@值，eg：price#
  --type：1代表属性，2代表count of 3代表distinct count，4代表sum
  --判断：>=,<=,=,!=,contain
  --值：3
  --{[{"condition":{"property_name":"","type":1,"judge_type":"","value":""},"operation_next":"and"}]}
  timestamp BIGINT DEFAULT 0 NOT NULL
);


--9分群
CREATE TABLE segment(
  seg_id SERIAL8 PRIMARY KEY NOT NULL ,
  app_id BIGINT NOT NULL,
  seg_name VARCHAR(255) DEFAULT '' NOT NULL ,
  filter_condition jsonb DEFAULT '{}' NOT NULL ,
  -- format:条件1%and/or%条件2... 条件1：属性#type#判断#值，eg：price#
  --type：1代表属性，2代表count of 3代表distinct count，4代表sum
  --判断：>=,<=,=,!=,contain
  --值：3
  timestamp BIGINT DEFAULT 0 NOT NULL,
  grain VARCHAR(20) DEFAULT '' NOT NULL --按月或按天
);

--10 激活码
CREATE TABLE power_key(
  key VARCHAR(100)  PRIMARY KEY NOT NULL,
  app_id  BIGINT NOT NULL,
  create_time BIGINT DEFAULT 0 NOT NULL
);

--11 账户绑定的微信用户信息
CREATE TABLE wx_user(
  id SERIAL8 PRIMARY KEY NOT NULL,
  open_id VARCHAR(256)  DEFAULT '' NOT NULL,
  nickname VARCHAR(256) DEFAULT '' NOT NULL,
  head_img VARCHAR(256) DEFAULT '' NOT NULL,
  create_time BIGINT DEFAULT 0 NOT NULL,
  last_login_time BIGINT DEFAULT 0 NOT NULL
);

--12 账户绑定的微信用户关系
CREATE TABLE wx_bind_app(
id SERIAL8 PRIMARY KEY NOT NULL,
openId VARCHAR(256)  DEFAULT '' NOT NULL,
app_id  BIGINT DEFAULT 0 NOT NULL
);

--13 白名单：
CREATE TABLE app_white_user(
  id SERIAL8 PRIMARY KEY NOT NULL,
  app_id BIGINT NOT NULL,
  user_id VARCHAR(256) DEFAULT '' NOT NULL
);

--14 app渠道配置，
CREATE TABLE app_channel(
app_id BIGINT NOT NULL ,
channel VARCHAR(16) NOT NULL ,
channel_name_cn VARCHAR(32) NOT NULL ,
create_time BIGINT NOT NULL DEFAULT 0,
 PRIMARY KEY (app_id,channel)
);

--15.事件采集事件表，标记用户行为事件是否第一次访问
CREATE TABLE collect_event_data_with_first(
data_id SERIAL8 PRIMARY KEY NOT NULL,
app_id BIGINT NOT NULL,
event_type BIGINT NOT NULL , --标识事件是自定义事件还是无卖点定义的基本事件
user_id VARCHAR (256) NOT NULL ,
channel VARCHAR(16) NOT NULL DEFAULT 'ALL',
is_first_visit INTEGER NOT NULL DEFAULT 0, --1:标识第一次访问，0：不是第一次访问
is_first_visit_channel INTEGER NOT NULL DEFAULT 0, --1:标识第一次通过该渠道访问app
timestamp BIGINT NOT NULL DEFAULT 0
);
create index index1_collect_event_data_with_first on collect_event_data_with_first(app_id,timestamp);
create index index2_collect_event_data_with_first on collect_event_data_with_first(app_id,is_first_visit,timestamp);
create index index3_collect_event_data_with_first on collect_event_data_with_first(app_id,data_id);
create index index4_collect_event_data_with_first on collect_event_data_with_first(app_id,channel,data_id);



--16.爬取记录表
CREATE TABLE fetcher_record(
key VARCHAR(16) PRIMARY KEY NOT NULL,
last_id BIGINT NOT NULL DEFAULT 0
);


ALTER SEQUENCE app_white_user_id_seq restart with 1000;
alter sequence custom_event_definition_custom_id_seq restart with 2000;