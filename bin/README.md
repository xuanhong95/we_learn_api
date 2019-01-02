# we_learn
*** topic
  * insert
    - api: we_learn/article/insert
    - content: {"content":"Hello this is a topic", "topic_type":1, "topic_title":"Test topic"
  * update
    - api: we_learn/article/update
    - content: {"content":"Hello this is a topic was updated", "topic_type":1, "topic_title":"Test topic(updated)", "article_id": 2}
  * delete
    - api: we_learn/article/delete
    - content: {"article_id": 2}

    
    
*** note: 
SELECT `topic`.`user_id`,`topic`.`article_title`, `topic`.`create_at`, `crm_user`.`full_name`, if(a.`comments` IS NULL, 0 , a.`comments`) AS comments FROM `topic` LEFT JOIN `crm_user` ON `crm_user`.`user_id` = `topic`.`user_id` LEFT JOIN (SELECT COUNT(1) AS `comments`, `article_id` FROM `article_comment` GROUP BY `article_comment`.`article_id`) AS a ON a.`article_id` = `topic`.`article_id` WHERE `topic`.`article_title` LIKE 'a%' ORDER BY comments DESC LIMIT 0,10