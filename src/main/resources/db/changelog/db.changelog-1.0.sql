--liquibase formatted sql
--changeset dgizzyatov:1 splitStatements:true endDelimiter:;

CREATE TABLE IF NOT EXISTS label (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL
);

--changeset dgizzyatov:2

CREATE TABLE IF NOT EXISTS writer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL
);

--changeset dgizzyatov:3

CREATE TABLE IF NOT EXISTS post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  content TEXT NOT NULL,
  created TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  status ENUM('ACTIVE', 'UNDER_REVIEW', 'DELETED') NOT NULL,
  writer_id BIGINT,
  FOREIGN KEY(writer_id) REFERENCES writer(id)
);

--changeset dgizzyatov:4

CREATE TABLE IF NOT EXISTS post_label (
    post_id BIGINT NOT NULL,
    label_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, label_id),
    FOREIGN KEY (post_id) REFERENCES post(id),
    FOREIGN KEY (label_id) REFERENCES label(id)
);
/