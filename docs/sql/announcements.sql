CREATE TABLE IF NOT EXISTS announcements (
    announcement_id INT NOT NULL AUTO_INCREMENT,
    thread_id INT NOT NULL,
    is_global TINYINT(1) NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (announcement_id),
    UNIQUE KEY uk_announcements_thread_scope (thread_id, is_global),
    KEY idx_announcements_scope_time (is_global, create_time),
    CONSTRAINT fk_announcements_thread
        FOREIGN KEY (thread_id) REFERENCES thread (thread_id)
        ON DELETE CASCADE
);

INSERT INTO announcements (thread_id, is_global, create_time)
SELECT thread_id, 0, COALESCE(update_time, create_time, NOW())
FROM thread
WHERE is_announcement = 1
ON DUPLICATE KEY UPDATE create_time = VALUES(create_time);

ALTER TABLE thread DROP COLUMN is_announcement;
