package homework.org.app.controller;

import java.sql.SQLException;
import java.util.List;

public interface GenericController<T, ID> {
    T getByID(ID id) throws SQLException;
    List<T> getAll();
    T save(T entity);
    T update(T entity);
    void deleteById(ID id);
}
