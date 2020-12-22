CREATE DATABASE seckill;
USE seckill;

set sql_mode="ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION";

CREATE TABLE user_info_tbl (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL DEFAULT '',
    gender TINYINT NOT NULL DEFAULT 0 COMMENT '1: male, 2:female',
    age TINYINT NOT NULL DEFAULT 0,
    phone VARCHAR(30) NOT NULL DEFAULT '',
    registration_mode VARCHAR(50) NOT NULL DEFAULT '' COMMENT 'by_phone, by_wechat, by_alipay',
    third_party_user_id VARCHAR(50) NOT NULL DEFAULT '' COMMENT 'wechat_id, alipay_id',
    PRIMARY KEY (id)
) CHARACTER SET utf8 COLLATE utf8_unicode_ci COMMENT 'user information table';

CREATE TABLE user_account_tbl (
    id INT NOT NULL AUTO_INCREMENT,
    encrypt_password VARCHAR(100) NOT NULL DEFAULT '',
    user_id INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX user_ind (user_id),
    FOREIGN KEY (user_id) REFERENCES user_info_tbl(id) ON DELETE CASCADE
) CHARACTER SET utf8 COLLATE utf8_unicode_ci COMMENT 'user account table';

CREATE TABLE good_tbl (
    id INT NOT NULL AUTO_INCREMENT,
    title VARCHAR(64) NOT NULL DEFAULT '',
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    description VARCHAR(500) NOT NULL DEFAULT '',
    sales INT NOT NULL DEFAULT 0,
    image_url VARCHAR(255) NOT NULL DEFAULT '',
    PRIMARY KEY (id)
) CHARACTER SET utf8 COLLATE utf8_unicode_ci COMMENT 'good table';

CREATE TABLE stock_tbl (
    id INT NOT NULL AUTO_INCREMENT,
    stock INT NOT NULL DEFAULT 0,
    good_id INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    FOREIGN KEY (good_id) REFERENCES good_tbl(id)
) CHARACTER SET utf8 COLLATE utf8_unicode_ci COMMENT 'stock table';

CREATE TABLE order_tbl (
    id VARCHAR(32) NOT NULL,
    user_id INT NOT NULL DEFAULT 0,
    good_id INT NOT NULL DEFAULT 0,
    amount INT NOT NULL DEFAULT 1,
    good_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    order_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (id),
    FOREIGN KEY (good_id) REFERENCES good_tbl(id),
    FOREIGN KEY (user_id) REFERENCES user_info_tbl(id) ON DELETE CASCADE
) CHARACTER SET utf8 COLLATE utf8_unicode_ci COMMENT 'order table';

CREATE TABLE sequence_info_tbl (
    name VARCHAR(255) NOT NULL DEFAULT '',
    current_val INT NOT NULL DEFAULT 0,
    step INT NOT NULL DEFAULT 0,
    PRIMARY KEY (name)
) CHARACTER SET utf8 COLLATE utf8_unicode_ci COMMENT 'sequence_info table';

CREATE TABLE event_tbl (
    id INT NOT NULL AUTO_INCREMENT,
    name CHAR(50) NOT NULL DEFAULT '',
    start_date DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00',
    good_id INT NOT NULL DEFAULT 0,
    deal_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (id),
    FOREIGN KEY (good_id) REFERENCES good_tbl(id)
) CHARACTER SET utf8 COLLATE utf8_unicode_ci COMMENT 'event table';


INSERT INTO sequence_info_tbl (name, current_val, step)
VALUES ("order_info", 0, 1);

INSERT INTO user_info_tbl (name, gender, age, phone, registration_mode, third_party_user_id)
VALUES ("Peter Li", 1, 30, "123456789", "by_wechat", "wechat-id-123456");

ALTER TABLE event_tbl ADD (end_date DATETIME NOT NULL DEFAULT "0000-00-00 00:00:00");

create unique index good_id_index on stock_tbl(good_id);