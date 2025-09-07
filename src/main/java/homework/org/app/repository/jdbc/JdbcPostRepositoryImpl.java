package homework.org.app.repository.jdbc;

import homework.org.app.exception.RepositoryException;
import homework.org.app.model.Label;
import homework.org.app.model.Post;
import homework.org.app.model.Status;
import homework.org.app.model.Writer;
import homework.org.app.repository.PostRepository;
import homework.org.app.util.ConnectionManager;
import lombok.AllArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static homework.org.app.util.ConnectionPoolManager.prepareStatement;
import static homework.org.app.util.ConnectionPoolManager.setParameters;

@AllArgsConstructor
public class JdbcPostRepositoryImpl implements PostRepository {

    private final ConnectionManager connectionManager;

    private static final String DELETE_SQL = """
            UPDATE post SET status = 'DELETED' 
            WHERE id = ? AND status != 'DELETED';
            """;
    private static final String DELETE_POST_LABEL = """
            DELETE FROM post_label WHERE post_id = ?
            """;
    private static final String INSERT_POST_LABEL = """
            INSERT INTO post_label (post_id, label_id) VALUES (?, ?)
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
            SELECT p.id, p.content, p.created, p.updated, p.status, p.writer_id, l.id as label_id, l.name as label_name 
            FROM post p
            LEFT JOIN post_label pl ON p.id = pl.post_id
            LEFT JOIN label l ON  pl.label_id = l.id
            """;
    private static final String UPDATE_SQL = """
            UPDATE post SET content = ?, updated = CURRENT_TIMESTAMP, status = ?
            WHERE id = ?;
            """;
    private static final String GET_BY_ID_SQL = GET_ALL_SQL + """
            WHERE p.id = ?;
            """;
    private static final String SELECT_ID_FROM_LABEL_ID = """
            SELECT l.id, l.name FROM label l
            JOIN post_label pl ON l.id = pl.label_id
            WHERE pl.post_id = ?;
            """;
    private static final String SELECT_LABELS_BY_POST_ID = """
            SELECT l.id, l.name FROM label l
            JOIN post_label pl ON l.id = pl.label_id
            WHERE pl.post_id = ?
            """;


    @Override
    public Post getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        try (var prepStatement = connectionManager.prepareStatement(GET_BY_ID_SQL)) {
            setParameters(prepStatement, id);
            var resultSet = prepStatement.executeQuery();
            if (!resultSet.isBeforeFirst()) {
                return null;
            }
            return mapRowToPost(resultSet);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to get post id" + id, e);
        }
    }

    @Override
    public List<Post> getAll() {
        try(var prepStatement = connectionManager.prepareStatement(GET_ALL_SQL);
            var resultSet = prepStatement.executeQuery()
        ) {
            return mapResultSetToPosts(resultSet);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to get all posts " , e);
        }
    }

    @Override
    public Post save(Post post) {
        try (var preparedStatement = connectionManager.prepareStatement(SAVE_POST_SQL,
                     Statement.RETURN_GENERATED_KEYS)) {
            setParameters(preparedStatement, post.getContent(),
                    post.getStatus().name(),
                    post.getWriter().getId());
            preparedStatement.executeUpdate();
            try (var keys = preparedStatement.getGeneratedKeys()){
                if (keys.next()) {
                    post.setId(keys.getLong(1));
                    savePostLabels(post);
                    return post;
                }
            }
            throw  new SQLException("Failed to get generate Id");
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save post " + post);
        }
    }

    private void savePostLabels(Post post) {
        if (post.getLabels() != null && !post.getLabels().isEmpty()) {
            try (var prepStatement = connectionManager.prepareStatement(SAVE_POST_LABEL_SQL)){
                for (Label label: post.getLabels()) {
                    if (label != null && label.getId() != null) {
                        setParameters(prepStatement, post.getId(), label.getId());
                        prepStatement.addBatch();
                    }
                }
                prepStatement.executeBatch();
            } catch (SQLException e) {
                throw new RepositoryException("Failed to save post_label " + post);
            }
        }
    }

    @Override
    public void deleteById(Long id) {
        try (var prepStatement = connectionManager.prepareStatement(DELETE_SQL)) {
            setParameters(prepStatement, id);
            prepStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete post to id" + id, e);
        }
    }

    public Post updateWithLabels(Post post) {
        if (post == null || post.getId() == null) {
            throw new IllegalArgumentException("Post and ID must not be null");
        }
        update(post);
        updatePostLabels(post);
        return post;
    }

    @Override
    public Post update(Post post) {
        try (var prepStatement = connectionManager.prepareStatement(UPDATE_SQL)){
            setParameters(prepStatement,
                    post.getContent(),
                    post.getStatus().name(),
                    post.getId());
            if (prepStatement.executeUpdate() == 0) {
                throw new RuntimeException("Post not found");
            }

            return post;

        } catch (SQLException e) {
            throw new RepositoryException("Failed to update post " + post);
        }
    }

    private void updatePostLabels(Post post) {
        List<Label> currentLabelsId = getCurrentLabelsId(post.getId());

        List<Long> newLabelsId = post.getLabels().stream()
                .filter(Objects::nonNull)
                .map(Label::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (currentLabelsId.equals(newLabelsId)) {
            return;
        }
        try (var deleteStatement = connectionManager.prepareStatement(
                DELETE_POST_LABEL)) {
            setParameters(deleteStatement, post.getId());
            deleteStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to update post_label " + post);
        }

        if (!newLabelsId.isEmpty()) {
            try (var insertStatement = prepareStatement(
                    INSERT_POST_LABEL)) {
                for (Long labelId : newLabelsId) {
                    setParameters(insertStatement, post.getId(), labelId);
                    insertStatement.addBatch();
                }
                insertStatement.executeBatch();
            } catch (SQLException e) {
                throw new RepositoryException("Failed to insert post_label " + post);
            }
        }
    }

    private List<Label> getCurrentLabelsId(Long postId) {
        List<Label> labels = new ArrayList<>();
        try (var prepStatement = connectionManager.prepareStatement(SELECT_ID_FROM_LABEL_ID)){
            setParameters(prepStatement, postId);
            ResultSet rs = prepStatement.executeQuery();
            while (rs.next()) {
                labels.add(new Label(rs.getLong("id"), rs.getString("name")));
            }
            return labels;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to get label id" + postId, e);
        }
    }

    private List<Post> mapResultSetToPosts(ResultSet resultSet) throws SQLException {
        Map<Long, Post> postMap = new LinkedHashMap<>();

        while (resultSet.next()) {
            Long postId = resultSet.getLong("id");

            Post post = postMap.get(postId);
            if (post == null) {
                post = new Post();
                post.setId(postId);
                post.setContent(resultSet.getString("content"));
                post.setCreated(resultSet.getTimestamp("created")
                                         .toLocalDateTime());

                Timestamp updated = resultSet.getTimestamp("updated");
                if (updated != null) {
                    post.setUpdated(updated.toLocalDateTime());
                }

                post.setStatus(Status.valueOf(resultSet.getString("status")));

                Writer writer = new Writer();
                writer.setId(resultSet.getLong("writer_id"));
                post.setWriter(writer);

                post.setLabels(new ArrayList<>());
                postMap.put(postId, post);
            }
            long labelId = resultSet.getLong("label_id");
            if (!resultSet.wasNull()) {
                Label label = new Label();
                label.setId(labelId);
                label.setName(resultSet.getString("label_name"));
                post.getLabels()
                    .add(label);
            }
        }

        return new ArrayList<>(postMap.values());
    }

    private Post mapRowToPost(ResultSet resultSet) {
        try {
            if (!resultSet.next()) {
                return null;
            }
            Post post = new Post();
            post.setId(resultSet.getLong("id"));
            post.setContent(resultSet.getString("content"));
            post.setCreated(resultSet.getTimestamp("created").toLocalDateTime());
            Timestamp updated = resultSet.getTimestamp("updated");
            if (updated != null) {
                post.setUpdated(updated.toLocalDateTime());
            }
            post.setStatus(Status.valueOf(resultSet.getString("status")));
            Writer writer = new Writer();
            writer.setId(resultSet.getLong("writer_id"));
            post.setWriter(writer);
            List<Label> labels = new ArrayList<>();

            long labelId = resultSet.getLong("label_id");
            if (!resultSet.wasNull()) {
                Label label = new Label();
                label.setId(labelId);
                label.setName(resultSet.getString("label_name"));
                labels.add(label);
            }
            while (resultSet.next()) {
                labelId = resultSet.getLong("label_id");
                if (!resultSet.wasNull()) {
                    Label label = new Label();
                    label.setId(labelId);
                    label.setName(resultSet.getString("label_name"));
                    labels.add(label);
                }
            }
            post.setLabels(labels);
            return post;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to map ResulTset to Post ", e);
        }
    }
}
