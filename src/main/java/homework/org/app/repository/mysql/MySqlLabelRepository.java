package homework.org.app.repository.mysql;

import homework.org.app.model.Label;
import homework.org.app.model.Status;
import homework.org.app.repository.LabelRepository;
import homework.org.app.util.ConnectionPoolManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MySqlLabelRepository implements LabelRepository {

    private final ConnectionPoolManager connectionPoolManager;

    public MySqlLabelRepository(ConnectionPoolManager connectionPoolManager) {
        this.connectionPoolManager = connectionPoolManager;
    }

    private static final String DELETE_SQL = """
            UPDATE label SET status = 'DELETED' 
            WHERE id = ? AND status != 'DELETED';
            """;
    private static final String SAVE_LABEL_SQL = """
            INSERT INTO label (name, status) VALUES (?, ?);
            """;
    private static final String GET_ALL_SQL = """
            SELECT * FROM label
            WHERE status != 'DELETED';
            """;
    private static final String UPDATE_SQL = """
            UPDATE label SET name = ?, status = ?
            WHERE id = ?;
            """;
    private static final String GET_BY_ID_SQL = """
            SELECT *  FROM label 
            WHERE id = ? AND status != 'DELETED';
            """;

    @Override
    public Label getById(Long id) throws SQLException {
        try (var connection = connectionPoolManager.get();
             var prepStatement = connection.prepareStatement(GET_BY_ID_SQL)) {
             prepStatement.setLong(1, id);

             var resultSet = prepStatement.executeQuery();
             if (resultSet.next()) {
                 return mapRowToLabel(resultSet);
             }
             return null;
        }
    }

    @Override
    public List<Label> getAll() throws SQLException {
        List<Label> result = new ArrayList<>();
        try (var connection = connectionPoolManager.get();
            var prepStatement = connection.createStatement()) {
            var resultSet = prepStatement.executeQuery(GET_ALL_SQL);
            while (resultSet.next()) {
                result.add(mapRowToLabel(resultSet));
            }
        }
        return result;
    }

    @Override
    public Label save(Label label) throws SQLException {
        try (var connection = connectionPoolManager.get();
             var prepStatement = connection.prepareStatement(SAVE_LABEL_SQL,
                     Statement.RETURN_GENERATED_KEYS)) {
            prepStatement.setString(1, label.getName());
            prepStatement.setString(2, label.getStatus().name());
            prepStatement.executeUpdate();
            try (var generatedKeys = prepStatement.getGeneratedKeys();) {
                if (generatedKeys.next()) {
                    return new Label(generatedKeys.getLong(1),
                            label.getName(), label.getStatus());
                }
            }
            throw new RuntimeException("Failed to save label");
        }
    }

    @Override
    public Label update(Label label) throws SQLException {
        try (var connection = connectionPoolManager.get();
             var prepStatement = connection.prepareStatement(UPDATE_SQL)) {
            prepStatement.setString(1, label.getName());
            prepStatement.setString(2, label.getStatus().name());
            prepStatement.setLong(3, label.getId());

            int affectedRows = prepStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Label with id " + label.getId() + " not found");
            }
            return label;
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

    private Label mapRowToLabel(ResultSet resultSet) throws SQLException {
        Label label = new Label();
        label.setId(resultSet.getLong("id"));
        label.setName(resultSet.getString("name"));

        try {
            String statusValue = resultSet.getString("status");
            label.setStatus(statusValue != null
                    ? Status.valueOf(statusValue)
                    : Status.ACTIVE);
        } catch (IllegalArgumentException e) {
            label.setStatus(Status.ACTIVE);
        }
        return label;
    }
}

