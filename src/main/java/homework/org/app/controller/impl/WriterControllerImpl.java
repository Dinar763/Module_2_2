package homework.org.app.controller.impl;

import homework.org.app.controller.WriterController;
import homework.org.app.model.Writer;
import homework.org.app.service.WriterService;

import java.util.List;

public class WriterControllerImpl implements WriterController {
    private final WriterService writerService;

    public WriterControllerImpl(WriterService writerService) {
        this.writerService = writerService;
    }

    @Override
    public Writer getByID(Long id) {
        return writerService.getByID(id);
    }

    @Override
    public List<Writer> getAll() {
        return writerService.getAll();
    }

    @Override
    public Writer save(Writer writer) {
        return writerService.save(writer);
    }

    @Override
    public Writer update(Writer writer) {
        return writerService.update(writer);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) throw new RuntimeException("ID must be not null");
        writerService.deleteById(id);
    }

    @Override
    public Writer findOrCreate (String firstName, String lastName) {
        if (firstName == null && lastName == null) throw new RuntimeException("First name and last name must be not null");
        Writer existing = writerService.findOrCreate(firstName, lastName);
        if (existing != null) {
            return existing;
        }

        Writer newWriter = new Writer();
        newWriter.setFirstname(firstName);
        newWriter.setLastname(lastName);

        return writerService.save(newWriter);
    }
}
