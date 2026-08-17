ALTER TABLE `post`
    ADD COLUMN `reply_to` int NULL DEFAULT NULL AFTER `thread_id`;

ALTER TABLE `post`
    ADD INDEX `idx_post_reply_to` (`reply_to` ASC) USING BTREE;

ALTER TABLE `post`
    ADD CONSTRAINT `db_post_ibfk_3`
        FOREIGN KEY (`reply_to`) REFERENCES `post` (`post_id`)
        ON DELETE RESTRICT ON UPDATE RESTRICT;
