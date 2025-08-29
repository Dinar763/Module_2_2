package homework.org.app.repository.jdbc;

import homework.org.app.exception.NotFoundException;
import homework.org.app.exception.RepositoryException;
import homework.org.app.model.Label;
import homework.org.app.model.Post;
import homework.org.app.model.Status;
import homework.org.app.model.Writer;
import homework.org.app.repository.WriterRepository;
import homework.org.app.util.ConnectionManager;
import lombok.AllArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static homework.org.app.util.ConnectionPoolManager.setParameters;

@AllArgsConstructor
public class JdbcWriterRepository implements WriterRepository {

    private final ConnectionManager connectionManager;

    private static final String DELETE_SQL = """
            DELETE FROM writer 
            WHERE id = ?;
            """;
    private static final String SAVE_WRITER_SQL = """
            INSERT INTO writer (firstname, lastname) 
            VALUES (?, ?);
            """;
    private static final String SAVE_POST_SQL = """
            INSERT INTO post (content, writer_id) 
            VALUES (?, ?);
            """;
    private static final String SAVE_POST_LABEL_SQL = """
            INSERT INTO post_label(post_id, label_id)
            values (?, ?);
            """;
    private static final String GET_ALL_SQL = """
            SELECT w.id as writer_id, w.firstname, w.lastname, 
                   p.id as post_id, 
                   p.content,
                   p.created,
                   p.updated,
                   p.status 
            FROM writer w
            LEFT JOIN post p ON w.id = p.writer_id
            """;
    private static final String UPDATE_SQL = """
            UPDATE writer SET firstname = ?, lastname = ?
            WHERE id = ?;
            """;
    private static final String GET_BY_ID_SQL = GET_ALL_SQL + """       
            WHERE w.id = ?;
            """;
    private static final String FIND_BY_NAME_SQL = """
            SELECT * FROM writer 
            WHERE firstname = ? AND lastname = ? 
            LIMIT 1;
            """;


    @Override
    public Writer getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        Writer writer = null;
        List<Post> posts = new ArrayList<>();

        try (var prepStatement = connectionManager.prepareStatement(GET_BY_ID_SQL)) {
            prepStatement.setLong(1, id);
            var resultSet = prepStatement.executeQuery();

            while (resultSet.next()) {
                if (writer == null) {
                    writer = mapRowToWriter(resultSet);
                }
                Long postId = resultSet.getLong("post_id");

                if (postId != 0) {
                    Post post = new Post(
                            postId,
                            resultSet.getString("content"),
                            resultSet.getTimestamp("created").toLocalDateTime(),
                            resultSet.getTimestamp("updated").toLocalDateTime(),
                            writer,
                            new ArrayList<>(),
                            Status.valueOf(resultSet.getString("status"))
                    );
                    posts.add(post);
                }
            }

            if (writer != null) {
                writer.setPosts(posts);
            }
            return writer;

        } catch (SQLException e) {
            throw new RepositoryException("Failed to get writer id" + id, e);
        }
    }

    @Override
    public List<Writer> getAll() {
        List<Writer> result = new ArrayList<>();
        try(var prepStatement = connectionManager.prepareStatement(GET_ALL_SQL);
            var resultSet = prepStatement.executeQuery();
        ) {

            while (resultSet.next()) {
                result.add(mapRowToWriter(resultSet));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to get all writers " , e);
        }
        return result;
    }

    @Override
    public Writer save(Writer writer) {
        if (writer == null) {
            throw new IllegalArgumentException("Writer must not be null");
        }
        try (var preparedStatement = connectionManager.prepareStatement(SAVE_WRITER_SQL,
                Statement.RETURN_GENERATED_KEYS)) {
            setParameters(preparedStatement,
                    writer.getFirstname(),
                    writer.getLastname());
            preparedStatement.executeUpdate();
            try (var keys = preparedStatement.getGeneratedKeys()){
                if (keys.next()) {
                    writer.setId(keys.getLong(1));
                    if (writer.getPosts() != null && !writer.getPosts().isEmpty()) {
                        saveWriterPosts(writer);
                    }
                    return writer;
                }
            }
            throw  new SQLException("Failed to get generate Id");
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save writer " + writer);
        }
    }


    private void saveWriterPosts (Writer writer){
        if (writer.getId() == null) {
            throw new IllegalStateException("Writer must have ID before saving posts");
        }

        for (Post post : writer.getPosts()) {
            if (post == null || post.getContent() == null || post.getStatus() == null) {
                continue;
            }
            try (PreparedStatement statement = connectionManager.prepareStatement(SAVE_POST_SQL,
                    Statement.RETURN_GENERATED_KEYS)){
                setParameters(statement,post.getContent(),
                        post.getStatus().name(),
                        writer.getId());
                statement.executeUpdate();

                try (ResultSet resultSet = statement.getGeneratedKeys()){
                    if (resultSet.next()) {
                        post.setId(resultSet.getLong(1));
                    }
                }

                if (post.getId() != null && post.getLabels() != null && !post.getLabels().isEmpty()) {
                    savePostLabels(post);
                }
            } catch (SQLException e) {
                throw new RepositoryException("Failed to save Writers post" , e);
            }
        }
    }

    private void savePostLabels(Post post){
        try (var labelStatement = connectionManager.prepareStatement(SAVE_POST_LABEL_SQL)){
            List<Label> labels = post.getLabels();
            if (labels == null || labels.isEmpty()) return;
            for (Label label: post.getLabels()) {
                setParameters(labelStatement, post.getId(),
                        label.getId());
                labelStatement.addBatch();
            }
            labelStatement.executeBatch();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save postLabels " , e);
        }
    }

    private void updateWriter(Writer writer) {
        try (var prepStatement = connectionManager.prepareStatement(UPDATE_SQL)) {
            prepStatement.setString(1, writer.getFirstname());
            prepStatement.setString(2, writer.getLastname());
            prepStatement.setLong(4, writer.getId());
            if (prepStatement.executeUpdate() == 0) {
                throw new RuntimeException("Writer not found");
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to update writer to id" , e);
        }
    }

    @Override
    public Writer update(Writer writer) {
        if (writer == null || writer.getId() == null) {
            throw new IllegalArgumentException("Writer and writer ID must not be null");
        }

        try (var connection = connectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (var prepStatement = connection.prepareStatement(UPDATE_SQL)){
                    prepStatement.setString(1, writer.getFirstname());
                    prepStatement.setString(2, writer.getLastname());
                    prepStatement.setLong(4,writer.getId());

                    if (prepStatement.executeUpdate() == 0) {
                        throw new RuntimeException("Writer not found");
                    }
                }

                if (writer.getPosts() != null) {
                    saveWriterPosts(writer);
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
            throw new RepositoryException("Failed to update writer to id" , e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try (var prepStatement = connectionManager.prepareStatement(DELETE_SQL)) {
            setParameters(prepStatement, id);
            int affectedRows = prepStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new NotFoundException("Writer with id " + id + " not found");
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete writer to id" + id, e);
        }
    }

    @Override
    public Writer findByName(String firstname, String lastname)  {
        try (PreparedStatement stmt = connectionManager.prepareStatement(FIND_BY_NAME_SQL)) {
            setParameters(stmt, firstname, lastname);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? mapRowToWriter(rs) : null;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find by name to Writer ", e);
        }
    }

    private Writer mapRowToWriter(ResultSet resultSet)  {
        try {
            Writer writer = new Writer();
            writer.setId(resultSet.getLong("writer_id"));
            writer.setFirstname(resultSet.getString("firstname"));
            writer.setLastname(resultSet.getString("lastname"));
            writer.setPosts(new ArrayList<>());
            return writer;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to map ResulTset to Writer ", e);
        }
    }
}
