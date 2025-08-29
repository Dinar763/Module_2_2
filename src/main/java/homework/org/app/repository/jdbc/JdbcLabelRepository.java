package homework.org.app.repository.jdbc;

import homework.org.app.exception.RepositoryException;
import homework.org.app.model.Label;
import homework.org.app.repository.LabelRepository;
import homework.org.app.util.ConnectionManager;
import lombok.AllArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static homework.org.app.util.ConnectionPoolManager.*;

@AllArgsConstructor
public class JdbcLabelRepository implements LabelRepository {

    private final ConnectionManager connectionManager;

    private static final String DELETE_SQL = """
            DELETE FROM label
            WHERE id = ?;
            """;
    private static final String SAVE_LABEL_SQL = """
            INSERT INTO label (name) VALUES (?);
            """;
    private static final String GET_ALL_SQL = """
            SELECT * FROM label;
            """;
    private static final String UPDATE_SQL = """
            UPDATE label SET name = ?
            WHERE id = ?;
            """;
    private static final String GET_BY_ID_SQL = """
            SELECT *  FROM label 
            WHERE id = ?;
            """;

    @Override
    public Label getById(Long id) {
        try (var prepStatement = connectionManager.prepareStatement(GET_BY_ID_SQL);
             var resultSet = setParametersAndExecuteQuery(prepStatement, id)
        ) {
             if (resultSet.next()) {
                 return mapRowToLabel(resultSet);
             }
             return null;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to get label id" + id, e);
        }
    }

    @Override
    public List<Label> getAll() {
        List<Label> result = new ArrayList<>();
        try (var prepStatement = createStatement();
             var resultSet = prepStatement.executeQuery(GET_ALL_SQL);
        ) {
            while (resultSet.next()) {
                result.add(mapRowToLabel(resultSet));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to get all labels " , e);
        }
        return result;
    }

    @Override
    public Label save(Label label) {
        try (var prepStatement = connectionManager.prepareStatement(SAVE_LABEL_SQL,
                     Statement.RETURN_GENERATED_KEYS)) {
            setParameters(prepStatement, label.getName());
            prepStatement.executeUpdate();
            try (var generatedKeys = prepStatement.getGeneratedKeys();) {
                if (generatedKeys.next()) {
                    return new Label(generatedKeys.getLong(1), label.getName());
                }
            }
            throw new RuntimeException("Failed to save label");
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save label " + label);
        }
    }

    @Override
    public Label update(Label label) {
        try (var prepStatement = connectionManager.prepareStatement(UPDATE_SQL)) {
            setParameters(prepStatement, label.getName(), label.getId());;
            int affectedRows = prepStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Label with id " + label.getId() + " not found");
            }
            return label;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to update label " + label, e);
        }
    }

    @Override
    public void deleteById(Long id){
        try (var prepStatement = connectionManager.prepareStatement(DELETE_SQL)) {
            setParameters(prepStatement, id);
            prepStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete label id" + id, e);
        }
    }

    private Label mapRowToLabel(ResultSet resultSet) {
        try {
            Label label = new Label();
            label.setId(resultSet.getLong("id"));
            label.setName(resultSet.getString("name"));
            return label;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to map ResulTset to Label ", e);
        }
    }
}

