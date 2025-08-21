package homework.org.app.controller.impl;

import homework.org.app.controller.LabelController;
import homework.org.app.model.Label;
import homework.org.app.service.LabelService;

import java.sql.SQLException;
import java.util.List;

public class LabelControllerImpl implements LabelController {
    private final LabelService labelService;

    public LabelControllerImpl(LabelService labelService) {
        this.labelService = labelService;
    }

    @Override
    public Label getByID(Long id) {
        return labelService.getByID(id);
    }

    @Override
    public List<Label> getAll() {
        return labelService.getAll();
    }

    @Override
    public Label save(Label label) {
        return labelService.save(label);
    }

    @Override
    public Label update(Label label) {
        return labelService.update(label);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) throw new RuntimeException("ID must be not null");
        labelService.deleteById(id);
    }
}
