package homework.org.app.repository.mysql;

import homework.org.app.model.Label;
import homework.org.app.model.Post;
import homework.org.app.model.Status;
import homework.org.app.model.Writer;
import homework.org.app.repository.PostRepository;
import homework.org.app.util.ConnectionPoolManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MySqlPostRepository implements PostRepository {

    private final ConnectionPoolManager connectionPoolManager;

    public MySqlPostRepository(ConnectionPoolManager connectionPoolManager) {
        this.connectionPoolManager = connectionPoolManager;
    }

    private static final String DELETE_SQL = """
            UPDATE post SET status = 'DELETED' 
            WHERE id = ? AND status != 'DELETED';
            """;
    private static final String SAVE_POST_SQL = """
            INSERT INTO post (content, status, writer_id) 
            VALUES (?, ?, ?);
            """;
    private static final String SAVE_POST_LABEL_SQL = """
            INSERT INTO post_label(post_id, label_id)
            values (?, ?);
            """;
    private static final String GET_ALL_SQL = """
            SELECT * FROM post
            WHERE status != 'DELETED';
            """;
    private static final String UPDATE_SQL = """
            UPDATE post SET content = ?, updated = CURRENT_TIMESTAMP, status = ?
            WHERE id = ?;
            """;
    private static final String GET_BY_ID_SQL = """
            SELECT * FROM post 
            WHERE id = ? AND status != 'DELETED';
            """;


    @Override
    public Post getById(Long id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        try (var connection = connectionPoolManager.get();
             var prepStatement = connection.prepareStatement(GET_BY_ID_SQL)) {
            prepStatement.setLong(1, id);

            var resultSet = prepStatement.executeQuery();
            if (resultSet.next()) {
                return mapRowToPost(resultSet);
            }
            return null;
        }
    }

    @Override
    public List<Post> getAll() throws SQLException {
        List<Post> result = new ArrayList<>();
        try(var connection = connectionPoolManager.get();
            var prepStatement = connection.prepareStatement(GET_ALL_SQL)) {
            var resultSet = prepStatement.executeQuery();
            while (resultSet.next()) {
                result.add(mapRowToPost(resultSet));
            }
        }
        return result;
    }

    @Override
    public Post save(Post post) throws SQLException {
        try (var connection = connectionPoolManager.get();
             var statement = connection.prepareStatement(SAVE_POST_SQL,
                     Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getContent());
            statement.setString(2, post.getStatus().name());
            statement.setLong(3, post.getWriter().getId());

            statement.executeUpdate();

            try (var keys = statement.getGeneratedKeys()){
                if (keys.next()) {
                    post.setId(keys.getLong(1));
                    return post;
                }
            }
            throw  new SQLException("Failed to get generate Id");
        }
    }

    public Post save1(Post post) throws SQLException {
        try (var connection = connectionPoolManager.get()) {
            connection.setAutoCommit(false);

            try {
                long postId = savePost(connection, SAVE_POST_SQL, post);
                post.setId(postId);

                if (post.getLabels() != null && !post.getLabels().isEmpty()) {
                    savePostLabels(connection, SAVE_POST_LABEL_SQL, post);
                }
                connection.commit();
                return post;
            } catch (SQLException e) {
                connection.rollback();
                throw  new RuntimeException("Failed to save post ", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Connection error ", e);
        }
    }

    private long savePost(Connection connection, String sql, Post post) throws SQLException {
        try (var postStatement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            postStatement.setString(1, post.getContent());
            postStatement.setString(2, post.getStatus()
                                           .name());
            postStatement.setLong(3, post.getWriter()
                                         .getId());

            postStatement.executeUpdate();
            try (var generatedKeys = postStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
                throw new RuntimeException("Failed to get generated post ID");
            }
        }
    }

    private void savePostLabels(Connection connection, String sql, Post post) throws SQLException {
        try (var labelStatement = connection.prepareStatement(sql)){
            for (Label label: post.getLabels()) {
                labelStatement.setLong(1, post.getId());
                labelStatement.setLong(2, label.getId());
                labelStatement.addBatch();
            }
            labelStatement.executeBatch();
        }
    }

    @Override
    public Post update(Post post) throws SQLException {
        if (post == null || post.getId() == null) {
            throw new IllegalArgumentException("Post and post ID must not be null");
        }
        try (var connection = connectionPoolManager.get()){
            connection.setAutoCommit(false);
            try {
                try (var prepStatement = connection.prepareStatement(UPDATE_SQL)){
                    prepStatement.setString(1, post.getContent());
                    prepStatement.setString(2, post.getStatus().name());
                    prepStatement.setLong(3,post.getId());

                    if (prepStatement.executeUpdate() == 0) {
                        throw new RuntimeException("Post not found");
                    }
                }

                if (post.getLabels() != null) {
                    updatePostLabels(connection, post);
                }

                connection.commit();
                return post;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Override
    public void deleteById(Long id) throws SQLException {
        try (var connection = connectionPoolManager.get();
             var prepStatement = connection.prepareStatement(DELETE_SQL)) {
            prepStatement.setLong(1, id);
            prepStatement.executeUpdate();
        }
    }

    private void updatePostLabels(Connection connection, Post post) throws SQLException {
        try (var deleteStatement = connection.prepareStatement(
                "DELETE FROM post_label WHERE post_id = ?")) {
            deleteStatement.setLong(1, post.getId());
            deleteStatement.executeUpdate();
        }

        if (!post.getLabels().isEmpty()) {
            try (var insertStatement = connection.prepareStatement(
                    "INSERT INTO post_label (post_id, label_id) VALUES (?, ?)")) {
                for (Label label : post.getLabels()) {
                    if (label != null && label.getId() != null) {
                        insertStatement.setLong(1, post.getId());
                        insertStatement.setLong(2, label.getId());
                        insertStatement.addBatch();
                    }
                }
                insertStatement.executeBatch();
            }
        }
    }

    private Post mapRowToPost(ResultSet resultSet) throws SQLException {
        Post post = new Post();
        post.setId(resultSet.getLong("id"));
        post.setContent(resultSet.getString("content"));
        post.setCreated(resultSet.getTimestamp("created").toLocalDateTime());
        post.setUpdated(resultSet.getTimestamp("updated").toLocalDateTime());
        post.setStatus(Status.valueOf(resultSet.getString("status")));

        Writer writer = new Writer();
        writer.setId(resultSet.getLong("writer_id"));
        post.setWriter(writer);
        return post;
    }
}
