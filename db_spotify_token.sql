-- Spotify OAuth Token存储表
CREATE TABLE spotify_token (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    account_id INT NOT NULL UNIQUE COMMENT '用户ID',
    access_token VARCHAR(512) NOT NULL COMMENT 'Spotify访问令牌',
    refresh_token VARCHAR(512) NOT NULL COMMENT 'Spotify刷新令牌',
    token_type VARCHAR(50) DEFAULT 'Bearer' COMMENT '令牌类型',
    scope VARCHAR(512) COMMENT '授权范围',
    expires_in INT COMMENT 'Token过期时间(秒)',
    expires_at DATETIME COMMENT 'Token过期时间点',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '软删除标志',
    FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE,
    INDEX idx_account_id (account_id),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Spotify OAuth Token存储表';
