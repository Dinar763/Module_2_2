import homework.org.app.util.ConnectionManager;
import homework.org.app.view.MainView;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;

public class MainApp {
    public static void main(String[] args) {
        //new MainView().start();
        checkHowWorkLiqiubase();
    }

    public static void checkHowWorkLiqiubase() {
        String changeLogFile = "db/changelog/db.changelog-master.yaml";
        try (Connection connection = ConnectionManager.open()) {
            Database database = DatabaseFactory.getInstance()
                                               .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase(
                    changeLogFile,
                    new ClassLoaderResourceAccessor(),
                    database
            );

            liquibase.update(new Contexts(), new LabelExpression());
            System.out.println("Миграции успешно применены");
        } catch (Exception e) {
            System.out.println("Ошибка в ликвибейс " + e.getMessage());
        }
    }
}
