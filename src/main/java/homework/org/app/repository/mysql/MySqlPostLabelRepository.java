package homework.org.app.repository.mysql;

import homework.org.app.util.ConnectionPoolManager;

public class MySqlPostLabelRepository {

    private final ConnectionPoolManager connectionPoolManager;

    public MySqlPostLabelRepository(ConnectionPoolManager connectionPoolManager) {
        this.connectionPoolManager = connectionPoolManager;
    }
}
