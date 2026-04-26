FROM mysql:latest
ADD ./nine_forum_schema.sql /docker-entrypoint-initdb.d/nine_forum_schema.sql
RUN chown -R mysql:mysql /docker-entrypoint-initdb.d/nine_forum_schema.sql
EXPOSE 3306
CMD ["mysqld", "--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci"]