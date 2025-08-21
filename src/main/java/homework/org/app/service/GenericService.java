package homework.org.app.service;

import java.util.List;

public interface GenericService<T> {
    T getByID(Long id) ;
    List<T> getAll();
    T save(T entity);
    T update(T entity);
    void deleteById(Long id);
}
