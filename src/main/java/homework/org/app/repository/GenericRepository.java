package homework.org.app.repository;

import java.sql.SQLException;
import java.util.List;

public interface GenericRepository<T, ID> {
    T getById(ID id) throws SQLException;
    List<T> getAll() throws SQLException;
    T save(T entity) throws SQLException;
    T update(T entity) throws SQLException;
    void deleteById(ID id) throws SQLException;
}
