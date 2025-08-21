package homework.org.app.service.impl;

import homework.org.app.exception.ServiceException;
import homework.org.app.model.Status;
import homework.org.app.model.Writer;
import homework.org.app.repository.WriterRepository;
import homework.org.app.service.WriterService;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class WriterServiceImpl implements WriterService  {
    private final WriterRepository repository;

    public WriterServiceImpl(WriterRepository repository) {
        this.repository = Objects.requireNonNull(repository, "Repository must not be null");
    }

    @Override
    public Writer getByID(Long id) {
        if (id == null) throw new RuntimeException("ID must be not null");
        try {
            return repository.getById(id);
        } catch (SQLException e) {
            throw new ServiceException("Failed to find writer ", e);

        }
    }

    @Override
    public List<Writer> getAll() {
        try {
            return repository.getAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to get all writers", e);
        }
    }

    @Override
    public Writer save(Writer writer) {
        try {
            return repository.save(writer);
        } catch (SQLException e) {
            throw new ServiceException("Failed to save writer", e);
        }
    }

    @Override
    public Writer update(Writer writer) {
        try {
            return repository.update(writer);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update writer", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) throw new RuntimeException("ID must be not null");
        try {
            repository.deleteById(id);
        } catch (SQLException e) {
            throw new ServiceException("Failed to delete by id writer", e);
        }
    }

    @Override
    public Writer findOrCreate (String firstName, String lastName) {
        if (firstName == null && lastName == null) throw new RuntimeException("First name and last name must be not null");
        try {
            Writer existing = repository.findByName(firstName, lastName);
            if (existing != null) {
                return existing;
            }

            Writer newWriter = new Writer();
            newWriter.setFirstname(firstName);
            newWriter.setLastname(lastName);
            newWriter.setStatus(Status.ACTIVE);

            return repository.save(newWriter);
        } catch (SQLException e) {
            throw new ServiceException("Failed to find or create writer", e);
        }
    }
}
