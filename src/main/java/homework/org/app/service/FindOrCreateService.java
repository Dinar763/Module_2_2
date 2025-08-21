package homework.org.app.service;

public interface FindOrCreateService<T> {
    T findOrCreate(String firstParam, String secondParam);
}
