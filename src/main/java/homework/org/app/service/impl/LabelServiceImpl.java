package homework.org.app.service.impl;

import homework.org.app.exception.NotFoundException;
import homework.org.app.exception.ServiceException;
import homework.org.app.model.Label;
import homework.org.app.repository.LabelRepository;
import homework.org.app.service.LabelService;


import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class LabelServiceImpl implements LabelService {
    private final LabelRepository repository;

    public LabelServiceImpl(LabelRepository repository) {
        this.repository = Objects.requireNonNull(repository, "Repository must not be null");
    }

    @Override
    public Label getByID(Long id) {
        if (id == null) {
            throw new ServiceException("ID must be not null");
        }
        try {
            Label label = repository.getById(id);
            if (label == null) {
                throw new NotFoundException("Label with id " + id + " not found");
            }
            return label;
        } catch (SQLException e) {
            throw new ServiceException("Failed to find label ", e);
        }
    }

    @Override
    public List<Label> getAll() {
        try {
            return repository.getAll();
        } catch (SQLException e) {
            throw new ServiceException("Failed to get all labels", e);
        }
    }

    @Override
    public Label save(Label label) {
        try {
            return repository.save(label);
        } catch (SQLException e) {
            throw new ServiceException("Failed to save label", e);
        }
    }

    @Override
    public Label update(Label label) {
        try {
            return repository.update(label);
        } catch (SQLException e) {
            throw new ServiceException("Failed to update label", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) throw new ServiceException("ID must be not null");
        try {
            repository.deleteById(id);
        } catch (SQLException e) {
            throw new ServiceException("Failed to delete by id label", e);
        }
    }
}
