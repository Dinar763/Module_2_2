package homework.org.app.view;

public interface EntityView<T> {
    void showMenu();
    void create();
    void edit();
    void delete();
    void showAll();
    void showById();
}
