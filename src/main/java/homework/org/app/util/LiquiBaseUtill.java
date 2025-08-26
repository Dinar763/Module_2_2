package homework.org.app.util;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;

public class LiquiBaseUtill {

    public static void applyMigrations() {
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
