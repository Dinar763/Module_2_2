package homework.org.app.controller;

import homework.org.app.model.Writer;
import homework.org.app.service.WriterService;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class WriterController {
    private final WriterService writerService;

    public Writer getByID(Long id) {
        return writerService.getByID(id);
    }

    public List<Writer> getAll() {
        return writerService.getAll();
    }

    public Writer save(Writer writer) {
        return writerService.save(writer);
    }

    public Writer update(Writer writer) {
        return writerService.update(writer);
    }

    public void deleteById(Long id) {
        if (id == null) throw new RuntimeException("ID must be not null");
        writerService.deleteById(id);
    }

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
