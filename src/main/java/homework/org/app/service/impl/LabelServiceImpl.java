package homework.org.app.service.impl;

import homework.org.app.exception.NotFoundException;
import homework.org.app.exception.ServiceException;
import homework.org.app.model.Label;
import homework.org.app.repository.LabelRepository;
import homework.org.app.service.LabelService;

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
        Label label = repository.getById(id);
        if (label == null) {
            throw new NotFoundException("Label with id " + id + " not found");
        }
        return label;
    }

    @Override
    public List<Label> getAll() {
        return repository.getAll();
    }

    @Override
    public Label save(Label label) {
        return repository.save(label);
    }

    @Override
    public Label update(Label label) {
        return repository.update(label);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) throw new ServiceException("ID must be not null");
        repository.deleteById(id);
    }
}
