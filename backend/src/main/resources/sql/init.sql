-- ============================================================
-- ABMSA System Database Initialization Script
-- ============================================================

CREATE DATABASE IF NOT EXISTS abmsa
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE abmsa;

-- ============================================================
-- Table: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users
(
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    username   VARCHAR(50)  NOT NULL UNIQUE COMMENT 'Login username',
    password   VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed password',
    email      VARCHAR(100)          COMMENT 'Email address',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'System users';

-- ============================================================
-- Table: keywords
-- ============================================================
CREATE TABLE IF NOT EXISTS keywords
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL COMMENT 'Owner user ID',
    keyword         VARCHAR(100) NOT NULL COMMENT 'Keyword to monitor',
    description     TEXT                  COMMENT 'Description',
    is_enabled      TINYINT      NOT NULL DEFAULT 1 COMMENT '1=enabled 0=disabled',
    crawl_frequency VARCHAR(50)           COMMENT 'Cron expression',
    last_crawl_time DATETIME              COMMENT 'Last crawl timestamp',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'Monitored keywords';

-- ============================================================
-- Table: sub_targets
-- ============================================================
CREATE TABLE IF NOT EXISTS sub_targets
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    keyword_id  BIGINT       NOT NULL COMMENT 'Parent keyword ID',
    target_name VARCHAR(100) NOT NULL COMMENT 'Aspect target name',
    description TEXT                  COMMENT 'Description',
    is_enabled  TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_keyword_id (keyword_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'Aspect sub-targets per keyword';

-- ============================================================
-- Table: monitor_tasks
-- ============================================================
CREATE TABLE IF NOT EXISTS monitor_tasks
(
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    keyword_id    BIGINT      NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/SUCCESS/FAILED',
    trigger_type  VARCHAR(20) NOT NULL DEFAULT 'AUTO' COMMENT 'AUTO/MANUAL',
    start_time    DATETIME             COMMENT 'Task start time',
    end_time      DATETIME             COMMENT 'Task end time',
    tweet_count   INT                  COMMENT 'Crawled tweet count',
    error_message TEXT                 COMMENT 'Error message if failed',
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_keyword_id (keyword_id),
    KEY idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'Monitor / crawl tasks';

-- ============================================================
-- Table: crawled_tweets
-- ============================================================
CREATE TABLE IF NOT EXISTS crawled_tweets
(
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    keyword_id    BIGINT        NOT NULL,
    tweet_id      VARCHAR(64)            COMMENT 'External tweet ID',
    content       TEXT          NOT NULL COMMENT 'Tweet text content',
    author_name   VARCHAR(100)           COMMENT 'Author screen name',
    publish_time  DATETIME               COMMENT 'Original publish time',
    image_url     VARCHAR(500)           COMMENT 'Attached image URL',
    is_analyzed   TINYINT       NOT NULL DEFAULT 0 COMMENT '0=pending 1=done',
    crawl_task_id BIGINT                 COMMENT 'Related monitor task ID',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_keyword_id (keyword_id),
    KEY idx_is_analyzed (is_analyzed),
    KEY idx_task_id (crawl_task_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'Crawled tweets';

-- ============================================================
-- Table: analysis_records
-- ============================================================
CREATE TABLE IF NOT EXISTS analysis_records
(
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    crawled_tweet_id BIGINT                 COMMENT 'Source tweet ID (nullable for manual)',
    user_id          BIGINT                 COMMENT 'User who triggered analysis',
    input_text       TEXT          NOT NULL,
    input_image_url  VARCHAR(500)           COMMENT 'Input image URL',
    target           VARCHAR(100)  NOT NULL COMMENT 'Aspect target',
    sentiment        VARCHAR(20)   NOT NULL COMMENT 'positive/neutral/negative',
    confidence       DOUBLE                 COMMENT 'Prediction confidence',
    positive_prob    DOUBLE                 COMMENT 'Positive probability',
    neutral_prob     DOUBLE                 COMMENT 'Neutral probability',
    negative_prob    DOUBLE                 COMMENT 'Negative probability',
    analysis_type    VARCHAR(20)   NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL/AUTO',
    target_source    VARCHAR(30)            COMMENT 'USER_DEFINED/AUTO_EXTRACTED',
    keyword_id       BIGINT                 COMMENT 'Related keyword',
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_keyword_id (keyword_id),
    KEY idx_sentiment (sentiment),
    KEY idx_analysis_type (analysis_type),
    KEY idx_created_at (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'Sentiment analysis records';

-- ============================================================
-- Table: crawl_logs
-- ============================================================
CREATE TABLE IF NOT EXISTS crawl_logs
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    keyword_id     BIGINT      NOT NULL,
    task_id        BIGINT               COMMENT 'Related monitor task',
    status         VARCHAR(20)          COMMENT 'SUCCESS/FAILED',
    message        TEXT                 COMMENT 'Log message',
    tweet_count    INT                  COMMENT 'Tweets crawled',
    analysis_count INT                  COMMENT 'Analyses performed',
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_keyword_id (keyword_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'Crawl operation logs';

-- ============================================================
-- Sample Data
-- ============================================================

-- Default admin user  (password: admin123)
INSERT INTO users (username, password, email, created_at, updated_at)
VALUES ('admin',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoOe5XjABkVTQFd6ZFb9Kh6JF5B0Uyq4GK',
        'admin@abmsa.com',
        NOW(), NOW())
ON DUPLICATE KEY UPDATE id = id;

-- Sample keywords
INSERT INTO keywords (user_id, keyword, description, is_enabled, crawl_frequency, created_at, updated_at)
VALUES
    (1, 'Apple', 'Apple Inc. products and news', 1, '0 0 */1 * * ?', NOW(), NOW()),
    (1, 'Tesla', 'Tesla electric vehicles and Elon Musk', 1, '0 0 */2 * * ?', NOW(), NOW()),
    (1, 'ChatGPT', 'OpenAI ChatGPT and AI developments', 1, '0 0 */1 * * ?', NOW(), NOW());

-- Sample sub-targets for Apple keyword
INSERT INTO sub_targets (keyword_id, target_name, description, is_enabled, created_at, updated_at)
VALUES
    (1, 'iPhone', 'Apple iPhone product line', 1, NOW(), NOW()),
    (1, 'MacBook', 'Apple MacBook laptops', 1, NOW(), NOW()),
    (1, 'Tim Cook', 'Apple CEO', 1, NOW(), NOW());

-- Sample sub-targets for Tesla keyword
INSERT INTO sub_targets (keyword_id, target_name, description, is_enabled, created_at, updated_at)
VALUES
    (2, 'Model 3', 'Tesla Model 3 sedan', 1, NOW(), NOW()),
    (2, 'Autopilot', 'Tesla Autopilot feature', 1, NOW(), NOW()),
    (2, 'Elon Musk', 'Tesla CEO', 1, NOW(), NOW());

-- Sample analysis records
INSERT INTO analysis_records
    (user_id, input_text, target, sentiment, confidence, positive_prob, neutral_prob, negative_prob,
     analysis_type, target_source, keyword_id, created_at)
VALUES
    (1, 'The new iPhone 15 Pro is absolutely amazing! Best camera I have ever used.',
     'iPhone', 'positive', 0.921, 0.921, 0.052, 0.027, 'MANUAL', 'USER_DEFINED', 1, NOW()),
    (1, 'Tesla Autopilot scared me today, it nearly missed the exit.',
     'Autopilot', 'negative', 0.875, 0.061, 0.064, 0.875, 'MANUAL', 'USER_DEFINED', 2, NOW()),
    (1, 'ChatGPT is useful but sometimes it makes things up.',
     'ChatGPT', 'neutral', 0.712, 0.183, 0.712, 0.105, 'MANUAL', 'USER_DEFINED', 3, NOW()),
    (1, 'MacBook Pro M3 performance benchmarks are insane!',
     'MacBook', 'positive', 0.945, 0.945, 0.033, 0.022, 'AUTO', 'AUTO_EXTRACTED', 1, NOW()),
    (1, 'I am worried about Tesla Model 3 quality control issues.',
     'Model 3', 'negative', 0.803, 0.089, 0.108, 0.803, 'AUTO', 'AUTO_EXTRACTED', 2, NOW());
