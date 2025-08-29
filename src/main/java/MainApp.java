import homework.org.app.util.LiquiBaseUtill;
import homework.org.app.view.MainView;

public class MainApp {
    public static void main(String[] args) {
        LiquiBaseUtill.applyMigrations();
        new MainView().start();
    }

}
