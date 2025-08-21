--liquibase formatted sql
--changeset yourname:1 splitStatements:true endDelimiter:;

CREATE TABLE IF NOT EXISTS label (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  status ENUM('ACTIVE', 'UNDER_REVIEW', 'DELETED') NOT NULL
);

--changeset yourname:2

CREATE TABLE IF NOT EXISTS writer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    status ENUM('ACTIVE', 'UNDER_REVIEW', 'DELETED') NOT NULL
);

--changeset yourname:3

CREATE TABLE IF NOT EXISTS post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'UNDER_REVIEW', 'DELETED') NOT NULL
);

--changeset yourname:4

CREATE TABLE IF NOT EXISTS writer_post (
  writer_id BIGINT NOT NULL,
  post_id BIGINT NOT NULL UNIQUE,
  PRIMARY KEY (writer_id, post_id),
  FOREIGN KEY (writer_id) REFERENCES writer(id),
  FOREIGN KEY (post_id) REFERENCES post(id)
);

--changeset yourname:4

CREATE TABLE IF NOT EXISTS post_label (
    post_id BIGINT NOT NULL,
    label_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, label_id),
    FOREIGN KEY (post_id) REFERENCES post(id),
    FOREIGN KEY (label_id) REFERENCES label(id)
);