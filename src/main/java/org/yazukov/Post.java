package org.yazukov;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import java.time.LocalDateTime;

public record Post(
        Long id,
        String name,
        String content,
        LocalDateTime dateTime
) {
    public static Post from(Row row) {
        return new Post(row.getLong("id"), row.getString("name"),
                row.getString("content"), row.getLocalDateTime("date_time"));
    }

    public static Multi<Post> findAll(PgPool pgPool) {
        return pgPool.query("SELECT id, name, content, date_time FROM posts ORDER BY id ASC")
                .execute().onItem().transformToMulti(Multi.createFrom()::iterable)
                .map(Post::from);
    }

    public static Uni<Post> findById(PgPool pgPool, Long id) {
        return pgPool.preparedQuery("SELECT id, name, content, date_time FROM posts WHERE id = $1")
                .execute(Tuple.of(id)).onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public static Uni<Boolean> delete(PgPool pgPool, Long id) {
        return pgPool.preparedQuery("DELETE FROM posts WHERE id = $1").execute(Tuple.of(id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Uni<Long> save(PgPool pgPool, Post post) {
        return pgPool.preparedQuery("insert into posts(name, content, date_time) values($1, $2, $3) returning id")
                .execute(Tuple.of(post.name, post.content, post.dateTime)).onItem()
                .transform(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
    }

    public static Uni<Post> update(PgPool pgPool, Post post, Long id) {
        System.out.println(post.name + post.content + id);
        return pgPool.withTransaction(conn -> {
            return conn.preparedQuery("update posts set name = $2, content = $3, date_time = $4 where id = $1 returning id, name, content, date_time")
                    .execute(Tuple.of(id, post.name, post.content, post.dateTime))
                    .onItem().transform(RowSet::iterator)
                    .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
        });
    }
}
