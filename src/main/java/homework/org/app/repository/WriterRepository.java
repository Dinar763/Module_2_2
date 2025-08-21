package homework.org.app.repository;

import homework.org.app.model.Writer;

import java.sql.SQLException;

public interface WriterRepository extends GenericRepository<Writer, Long> {
    Writer findByName(String firstName, String lastName) throws SQLException;
}
