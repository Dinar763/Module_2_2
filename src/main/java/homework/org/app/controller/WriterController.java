package homework.org.app.controller;

import homework.org.app.model.Writer;

public interface WriterController extends GenericController<Writer, Long> {
    Writer findOrCreate(String firstName, String lastName);
}
