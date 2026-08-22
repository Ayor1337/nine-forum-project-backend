-- 将历史 TipTap image 节点迁移至独立 images_urls 字段，并从正文删除图片节点。
-- 依赖 20260821_thread_post_images_urls.sql 已先执行。
-- 本脚本可重复执行：已移除的 image 节点不会再次追加到 images_urls。

DROP PROCEDURE IF EXISTS backfill_thread_tiptap_images;
DROP PROCEDURE IF EXISTS backfill_post_tiptap_images;

DELIMITER //

CREATE PROCEDURE backfill_thread_tiptap_images()
BEGIN
    DECLARE done BOOLEAN DEFAULT FALSE;
    DECLARE current_id INT;
    DECLARE image_type_path TEXT;
    DECLARE image_node_path TEXT;
    DECLARE image_url TEXT;
    DECLARE thread_cursor CURSOR FOR
        SELECT thread_id
        FROM `thread`
        WHERE JSON_VALID(content)
          AND JSON_SEARCH(content, 'one', 'image', NULL, '$**.type') IS NOT NULL;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN thread_cursor;
    thread_loop: LOOP
        FETCH thread_cursor INTO current_id;
        IF done THEN
            LEAVE thread_loop;
        END IF;

        image_loop: LOOP
            SELECT JSON_UNQUOTE(JSON_SEARCH(content, 'one', 'image', NULL, '$**.type'))
            INTO image_type_path
            FROM `thread`
            WHERE thread_id = current_id;

            IF image_type_path IS NULL THEN
                LEAVE image_loop;
            END IF;

            SET image_node_path = LEFT(image_type_path, CHAR_LENGTH(image_type_path) - CHAR_LENGTH('.type'));
            SELECT JSON_UNQUOTE(JSON_EXTRACT(content, CONCAT(image_node_path, '.attrs.src')))
            INTO image_url
            FROM `thread`
            WHERE thread_id = current_id;

            UPDATE `thread`
            SET images_urls = IF(image_url IS NULL OR TRIM(image_url) = '',
                                 images_urls,
                                 JSON_ARRAY_APPEND(images_urls, '$', image_url)),
                content = JSON_REMOVE(content, image_node_path)
            WHERE thread_id = current_id;
        END LOOP;
    END LOOP;
    CLOSE thread_cursor;
END//

CREATE PROCEDURE backfill_post_tiptap_images()
BEGIN
    DECLARE done BOOLEAN DEFAULT FALSE;
    DECLARE current_id INT;
    DECLARE image_type_path TEXT;
    DECLARE image_node_path TEXT;
    DECLARE image_url TEXT;
    DECLARE post_cursor CURSOR FOR
        SELECT post_id
        FROM post
        WHERE JSON_VALID(content)
          AND JSON_SEARCH(content, 'one', 'image', NULL, '$**.type') IS NOT NULL;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN post_cursor;
    post_loop: LOOP
        FETCH post_cursor INTO current_id;
        IF done THEN
            LEAVE post_loop;
        END IF;

        image_loop: LOOP
            SELECT JSON_UNQUOTE(JSON_SEARCH(content, 'one', 'image', NULL, '$**.type'))
            INTO image_type_path
            FROM post
            WHERE post_id = current_id;

            IF image_type_path IS NULL THEN
                LEAVE image_loop;
            END IF;

            SET image_node_path = LEFT(image_type_path, CHAR_LENGTH(image_type_path) - CHAR_LENGTH('.type'));
            SELECT JSON_UNQUOTE(JSON_EXTRACT(content, CONCAT(image_node_path, '.attrs.src')))
            INTO image_url
            FROM post
            WHERE post_id = current_id;

            UPDATE post
            SET images_urls = IF(image_url IS NULL OR TRIM(image_url) = '',
                                 images_urls,
                                 JSON_ARRAY_APPEND(images_urls, '$', image_url)),
                content = JSON_REMOVE(content, image_node_path)
            WHERE post_id = current_id;
        END LOOP;
    END LOOP;
    CLOSE post_cursor;
END//

DELIMITER ;

CALL backfill_thread_tiptap_images();
CALL backfill_post_tiptap_images();

DROP PROCEDURE backfill_thread_tiptap_images;
DROP PROCEDURE backfill_post_tiptap_images;
