package classwork.starter;

import homework.org.app.model.Label;
import homework.org.app.model.Post;
import homework.org.app.model.Status;
import homework.org.app.model.Writer;
import homework.org.app.util.ConnectionPoolManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcRunner {

    private static final String SAVE_POST_SQL = """
            INSERT INTO post (content, status, writer_id) 
            VALUES (?, ?, ?);
            """;
    private static final String SAVE_LABELS_SQL = """
            INSERT INTO post_label(post_id, label_id)
            values (?, ?);
            """;

    private static final String DELETE_SQL = """
            UPDATE label SET status = 'DELETED' 
            WHERE id = ? AND status != 'DELETED';
            """;
    private static final String SAVE_SQL = """
            INSERT INTO label (name) VALUES (?);
            """;
    private static final String GET_ALL_SQL = """
            SELECT * FROM label;
            """;
    private static final String UPDATE_SQL = """
            UPDATE label SET name = ? WHERE id = ?;
            """;
    private static final String GET_BY_ID_SQL = """
            SELECT name FROM label 
                WHERE id = ?
            """;


    public static void main(String[] args) throws SQLException {
        try {
            Writer writer = new Writer();
            writer.setId(6L);
            writer.setFirstname("John");
            writer.setLastname("Doe");
            writer.setStatus(Status.ACTIVE);


            Label label1 = new Label();
            label1.setId(1L); // Метка уже есть в БД
            label1.setName("Java");

            Label label2 = new Label();
            label2.setId(2L);
            label2.setName("Spring");

            Post post = new Post();
            post.setContent("Diego");
            post.setCreated(LocalDateTime.now());
            post.setUpdated(LocalDateTime.now());
            post.setStatus(Status.ACTIVE);
            post.setWriter(writer);
            save(post);
        } finally {
            ConnectionPoolManager.closePool();
        }
    }

    private static List<Long> getUsersById(String userId) throws SQLException {
        String sql = """
                select id from users
                where user_number = %s
                """.formatted(userId);
        ArrayList<Long> result = new ArrayList<>();
        try (var connection = ConnectionPoolManager.get();
             var statement = connection.createStatement()){
             var resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                result.add(resultSet.getObject("id", Long.class));
            }
        }
        return result;
    }

    public Label update(Label label) {
        try (var connection = ConnectionPoolManager.get();
             var prepStatement = connection.prepareStatement(UPDATE_SQL)) {
            prepStatement.setString(1, label.getName());
            prepStatement.setLong(2, label.getId());

            int affectedRows = prepStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Label with id " + label.getId() + " not found");
            }
            return label;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteById(Long id) {
        try (var connection = ConnectionPoolManager.get();
             var prepStatement = connection.prepareStatement(DELETE_SQL)) {
            prepStatement.setLong(1, id);
            prepStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Post save(Post post) throws SQLException {
        try (var connection = ConnectionPoolManager.get()) {
            connection.setAutoCommit(false);

            try {
                long postId = savePost(connection, SAVE_POST_SQL, post);
                post.setId(postId);

                if (post.getLabels() != null && !post.getLabels().isEmpty()) {
                    savePostLabels(connection, SAVE_LABELS_SQL, post);
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

    private static long savePost(Connection connection, String sql, Post post) throws SQLException {
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

    private static void savePostLabels(Connection connection, String sql, Post post) throws SQLException {
        try (var labelStatement = connection.prepareStatement(sql)){
            for (Label label: post.getLabels()) {
                labelStatement.setLong(1, post.getId());
                labelStatement.setLong(2, label.getId());
                labelStatement.addBatch();
            }
            labelStatement.executeBatch();
        }
    }

    private static void deleteObjectById(Long id) throws SQLException {
        String sqlQuery1 = """
                DELETE FROM writer WHERE id = ?;
                """;
        String sqlQuery2 = """
                DELETE FROM post WHERE writer_id = ?;
                """;

        Connection connection = null;
        PreparedStatement prepStatement = null;
        PreparedStatement prepStatement2 = null;
        try {
            connection = ConnectionPoolManager.get();
            prepStatement = connection.prepareStatement(sqlQuery1);
            prepStatement2 =  connection.prepareStatement(sqlQuery2);
            connection.setAutoCommit(false);

            prepStatement.setLong(1, id);
            prepStatement2.setLong(1, id);

            prepStatement2.executeUpdate();
//            if (true) {
//                throw new RuntimeException("OOOPS");
//            }
            prepStatement.executeUpdate();
            connection.commit();
        } catch (Exception e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (connection != null) {
                connection.close();
            }
            if (prepStatement2 != null) {
                prepStatement2.close();
            }
            if (prepStatement != null) {
                prepStatement.close();
            }
        }
    }

    private static void deleteObjectByIdWithBatch(Long id) throws SQLException {
        var sqlQuery1 = "DELETE FROM writer WHERE id = " + id;
        var sqlQuery2 = "DELETE FROM post WHERE writer_id = " + id;

        Connection connection = null;
        Statement statement = null;
        try {
            connection = ConnectionPoolManager.get();
            connection.setAutoCommit(false);

            statement = connection.createStatement();
            statement.addBatch(sqlQuery2);
            statement.addBatch(sqlQuery1);

            var ints = statement.executeBatch();
            connection.commit();
        } catch (Exception e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        } finally {
            if (connection != null) {
                connection.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }
}
