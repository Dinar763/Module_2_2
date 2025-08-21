package homework.org.app.repository.mysql;

import homework.org.app.model.Label;
import homework.org.app.model.Post;
import homework.org.app.model.Status;
import homework.org.app.model.Writer;
import homework.org.app.repository.WriterRepository;
import homework.org.app.util.ConnectionPoolManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlWriterRepository implements WriterRepository {

    private final ConnectionPoolManager connectionPoolManager;

    public MySqlWriterRepository(ConnectionPoolManager connectionPoolManager) {
        this.connectionPoolManager = connectionPoolManager;
    }

    private static final String DELETE_SQL = """
            UPDATE writer SET status = 'DELETED' 
            WHERE id = ? AND status != 'DELETED';
            """;
    private static final String SAVE_WRITER_SQL = """
            INSERT INTO writer (firstname, lastname, status) 
            VALUES (?, ?, ?);
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
            SELECT * FROM writer
            WHERE status != 'DELETED';
            """;
    private static final String UPDATE_SQL = """
            UPDATE writer SET firstname = ?, lastname = ?, status = ?
            WHERE id = ?;
            """;
    private static final String GET_BY_ID_SQL = """
            SELECT * FROM writer 
            WHERE id = ? AND status != 'DELETED';
            """;
    private static final String FIND_BY_NAME_SQL = """
            SELECT * FROM writer 
            WHERE firstname = ? AND lastname = ? 
            AND status != 'DELETED'
            LIMIT 1;
            """;


    @Override
    public Writer getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        try (var connection = connectionPoolManager.get();
             var prepStatement = connection.prepareStatement(GET_BY_ID_SQL)) {
            prepStatement.setLong(1, id);

            var resultSet = prepStatement.executeQuery();
            if (resultSet.next()) {
                return mapRowToWriter(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get writer by id: " + id, e);
        }
    }

    @Override
    public List<Writer> getAll() {
        List<Writer> result = new ArrayList<>();
        try(var connection = connectionPoolManager.get();
            var prepStatement = connection.prepareStatement(GET_ALL_SQL)) {
            var resultSet = prepStatement.executeQuery();
            while (resultSet.next()) {
                result.add(mapRowToWriter(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get writers: " + e);
        }
        return result;
    }

    @Override
    public Writer save(Writer writer) {
        if (writer == null || writer.getStatus() == null
                || writer.getFirstname() == null || writer.getLastname() == null) {
            throw new IllegalArgumentException("All writer fields must be filled");
        }
        try (var connection = connectionPoolManager.get()){
            connection.setAutoCommit(false);

            try {
                long writerId = saveWriter(connection, writer);
                writer.setId(writerId);

                if (writer.getPosts() != null && !writer.getPosts().isEmpty()) {
                    saveWriterPosts(connection, writer);
                }
                connection.commit();
                return writer;
            } catch (SQLException e) {
                connection.rollback();
                throw  new RuntimeException("Failed to save writer ", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e ){
            throw  new RuntimeException("Failed to connection database ", e);
        }
    }

    private long saveWriter(Connection connection,
                            Writer writer) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SAVE_WRITER_SQL,
                Statement.RETURN_GENERATED_KEYS)){
            statement.setString(1, writer.getFirstname());
            statement.setString(2,writer.getLastname());
            statement.setString(3, writer.getStatus().name());

            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()){
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
                throw new RuntimeException("Failed to get generated writer ID");
            }
        }
    }

    private void saveWriterPosts (Connection connection,
                                  Writer writer) throws SQLException {
        if (writer.getId() == null) {
            throw new IllegalStateException("Writer must have ID before saving posts");
        }

        for (Post post : writer.getPosts()) {
            if (post == null || post.getContent() == null || post.getStatus() == null) {
                continue;
            }
            try (PreparedStatement statement = connection.prepareStatement(SAVE_POST_SQL,
                    Statement.RETURN_GENERATED_KEYS)){
                statement.setString(1, post.getContent());
                statement.setString(2, post.getStatus().name());
                statement.setLong(3, writer.getId());

                statement.executeUpdate();

                try (ResultSet resultSet = statement.getGeneratedKeys()){
                    if (resultSet.next()) {
                        post.setId(resultSet.getLong(1));
                    }
                }

                if (post.getLabels() != null && !post.getLabels().isEmpty()) {
                    savePostLabels(connection, post);
                }
            }
        }
    }

    private void savePostLabels(Connection connection,
                                 Post post) throws SQLException {
        try (var labelStatement = connection.prepareStatement(SAVE_POST_LABEL_SQL)){
            List<Label> labels = post.getLabels();
            if (labels == null || labels.isEmpty()) return;
            for (Label label: post.getLabels()) {
                labelStatement.setLong(1, post.getId());
                labelStatement.setLong(2, label.getId());
                labelStatement.addBatch();
            }
            labelStatement.executeBatch();
        }
    }

    @Override
    public Writer update(Writer writer) {
        if (writer == null || writer.getId() == null) {
            throw new IllegalArgumentException("Writer and writer ID must not be null");
        }

        try (var connection = connectionPoolManager.get()) {
            connection.setAutoCommit(false);
            try {
                try (var prepStatement = connection.prepareStatement(UPDATE_SQL)){
                    prepStatement.setString(1, writer.getFirstname());
                    prepStatement.setString(2, writer.getLastname());
                    prepStatement.setString(3, writer.getStatus().name());
                    prepStatement.setLong(4,writer.getId());

                    if (prepStatement.executeUpdate() == 0) {
                        throw new RuntimeException("Writer not found");
                    }
                }

                if (writer.getPosts() != null) {
                    saveWriterPosts(connection, writer);
                }

                connection.commit();
                return writer;
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Failed to update ", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (var connection = connectionPoolManager.get();
             var prepStatement = connection.prepareStatement(DELETE_SQL)) {
            prepStatement.setLong(1, id);
            prepStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete writer with id: " + id, e);
        }
    }

    @Override
    public Writer findByName(String firstname, String lastname) throws SQLException {
        try (Connection conn = connectionPoolManager.get();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_NAME_SQL)) {

            stmt.setString(1, firstname);
            stmt.setString(2, lastname);

            ResultSet rs = stmt.executeQuery();
            return rs.next() ? mapRowToWriter(rs) : null;
        }
    }

    private Writer mapRowToWriter(ResultSet resultSet) throws SQLException {
        Writer writer = new Writer();
        writer.setId(resultSet.getLong("id"));
        writer.setFirstname(resultSet.getString("firstName"));
        writer.setLastname(resultSet.getString("lastName"));

        String status = resultSet.getString("status");
        writer.setStatus(status != null ? Status.valueOf(status) : Status.ACTIVE);
        writer.setPosts(new ArrayList<>());
        return writer;
    }
}
