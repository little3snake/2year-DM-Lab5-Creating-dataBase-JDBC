import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class CruiseDBManager {
    private static final String POSTGRES_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "password";

    private static String DB_USER_ADMIN = "admin";
    private static String DB_PASSWORD_ADMIN = "admin";
    private static String DB_USER_GUEST = "guest";
    private static String DB_PASSWORD_GUEST = "guest";
    private static String DB_USER_UNKNOWN = "unknown";
    private static String DB_PASSWORD_UNKNOWN = "unknown";

    // Метод для подключения к конкретной базе
    public static Connection getConnection(String dbName, String db_user, String db_password) throws SQLException {
        System.out.println("Connecting to database: " + dbName);
        System.out.println("Using username: " + db_user);
        System.out.println("Using password: " + db_password);
        String dbUrl = "jdbc:postgresql://localhost:5432/" + dbName;
        return DriverManager.getConnection(dbUrl, db_user, db_password);
    }

    public static void init_passwords (String username, String password) {
        if (Objects.equals(username, "admin")){
            DB_PASSWORD_ADMIN = password;
        } else if (Objects.equals(username, "guest")) {
            DB_PASSWORD_GUEST = password;
        } else {
            DB_USER_UNKNOWN = username;
            DB_PASSWORD_UNKNOWN = password;
        }
    }

    public static void verification(String username, String password, ArrayList<Object> privileges) {
        if (privileges == null) {
            try (Connection conn = getConnection("postgres", DB_USER, DB_PASSWORD);
                 Statement stmt = conn.createStatement()) {
                // Проверяем, существует ли роль
                String checkRoleExistsQuery = "SELECT 1 FROM pg_roles WHERE rolname = '" + username + "';";
                ResultSet rs = stmt.executeQuery(checkRoleExistsQuery);
                if (!rs.next()) {
                    // Роль не существует, создаём её
                    String createRole = "CREATE ROLE " + username + " WITH LOGIN PASSWORD '" + password + "';";
                    stmt.executeUpdate(createRole);
                    System.out.println("Роль '" + username + "' успешно создана.");
                } else {
                    System.out.println("Роль '" + username + "' уже существует.");
                }
                init_passwords(username, password);
                // Проверяем, существует ли база данных
                String dbName = "cruise_db";
                String checkDbExistsQuery = "SELECT 1 FROM pg_database WHERE datname = '" + dbName + "';";
                rs = stmt.executeQuery(checkDbExistsQuery);
                if (rs.next()) {
                    // База данных существует, выполняем addPermissions
                    addPermissions(dbName, username, null);
                    System.out.println("База данных '" + dbName + "' существует. Права и пароли инициализированы.");
                } else {
                    System.out.println("База данных '" + dbName + "' не существует.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try (Connection conn = getConnection("postgres", DB_USER, DB_PASSWORD);
                 Statement stmt = conn.createStatement()) {
                // Проверяем, существует ли роль
                String checkRoleExistsQuery = "SELECT 1 FROM pg_roles WHERE rolname = '" + username + "';";
                ResultSet rs = stmt.executeQuery(checkRoleExistsQuery);
                if (!rs.next()) {
                    // Роль не существует, создаём её
                    String createRole = "CREATE ROLE " + username + " WITH LOGIN PASSWORD '" + password + "';";
                    stmt.executeUpdate(createRole);
                    System.out.println("Роль '" + username + "' успешно создана.");
                } else {
                    System.out.println("Роль '" + username + "' уже существует.");
                }
                init_passwords(username, password);
                // Проверяем, существует ли база данных
                String dbName = "cruise_db";
                String checkDbExistsQuery = "SELECT 1 FROM pg_database WHERE datname = '" + dbName + "';";
                rs = stmt.executeQuery(checkDbExistsQuery);
                if (rs.next()) {
                    // База данных существует, выполняем addPermissions
                    //addPermissions(dbName, username);
                    addPermissions (dbName, username, privileges);
                    System.out.println("База данных '" + dbName + "' существует. Права и пароли инициализированы.");
                } else {
                    System.out.println("База данных '" + dbName + "' не существует.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void addPermissions(String dbName, String userRole, ArrayList<Object> privileges) {
        try (Connection conn = getConnection(dbName, DB_USER, DB_PASSWORD);) {
            // Вызов процедуры для очистки таблицы
            if ("admin".equals(userRole) || "guest".equals(userRole)){
                CallableStatement stmt = conn.prepareCall("{call assign_permissions(?, ?)}");
                stmt.setString(1, dbName);
                stmt.setString(2, userRole);
                stmt.execute();
            } else {
                String privilegesString = privileges.stream()
                        .map(Object::toString) // Приводим каждый объект к строке
                        .collect(Collectors.joining(";"));
                CallableStatement stmt = conn.prepareCall("{call assign_permissions_unknown(?, ?, ?)}");
                stmt.setString(1, dbName);
                stmt.setString(2, userRole);
                stmt.setString(3,privilegesString);
                stmt.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Метод для выполнения SQL-файлов (создание процедур)
    public static void executeSQLFile(String filePath, String dbName) {
        try (Connection conn = getConnection(dbName, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            String sql = new String(Files.readAllBytes(Paths.get(filePath))); // Читаем файл
            stmt.execute(sql); // Выполняем SQL-код
            System.out.println("Executed SQL file: " + filePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getPasswordForRole(String userRole) {
        if (Objects.equals(userRole, "admin")) {
            return DB_PASSWORD_ADMIN;
        } else if (Objects.equals(userRole, "guest")) {
            return DB_PASSWORD_GUEST;
        } else {
            return DB_PASSWORD_UNKNOWN;
        }
    }

    public static void createDatabase(String dbName, String userRole, String password) throws SQLException {
        if (!"admin".equalsIgnoreCase(userRole)) {
            throw new SQLException("Недостаточно прав. Базу данных может создать только admin.");
        }
        try (Connection conn = getConnection("postgres", DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement()) {
            // Проверяем, существует ли уже база данных
                String checkDbExistsQuery = "SELECT 1 FROM pg_database WHERE datname = '" + dbName + "';";
                var resultSet = stmt.executeQuery(checkDbExistsQuery);
                if (resultSet.next()) {
                    System.out.println("База данных с именем '" + dbName + "' уже существует.");
                    throw new SQLException("База данных с таким именем уже существует.");
                }
                // Если база данных не существует, создаем ее
                String createDbQuery = "CREATE DATABASE " + dbName;
                stmt.executeUpdate(createDbQuery);
                createTable(dbName);
                System.out.println("База данных '" + dbName + "' успешно создана.");

        } catch (SQLException e) {
            throw new SQLException("Ошибка при создании базы данных: " + e.getMessage(), e);
            //e.printStackTrace();
        }
    }

    public static void deleteDatabase(String dbName, String userRole) throws SQLException {
        String password = getPasswordForRole(userRole);
        if (!"admin".equalsIgnoreCase(userRole)) {
            throw new SQLException("Недостаточно прав. Базу данных может удалить только admin.");
        }
        try (Connection conn = getConnection("postgres", DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            // Удаляем базу данных
            String dropDatabaseQuery = "DROP DATABASE IF EXISTS " + dbName; // нельзя вставить в хранимую функцию, поэтому делаем так
            stmt.executeUpdate(dropDatabaseQuery);
            System.out.println("База данных '" + dbName + "' успешно удалена.");

        } catch (SQLException e) {
            throw new SQLException("Ошибка при создании базы данных: " + e.getMessage(), e);
            //e.printStackTrace();
        }
    }



    public static void addRecord(String userRole, String lastName, String firstName, String cruiseName, String departureDate, String departureCity, String arrivalCity) throws SQLException {
        String dbName = "cruise_db";
        String password = getPasswordForRole(userRole);
        // Проверяем права на уровне базы данных
        boolean hasPrivilege = checkPrivilege(userRole, "INSERT", "cruises");
        if (!hasPrivilege) {
            throw new SQLException("Недостаточно прав для добавления записи.");
        }
        try (Connection conn = getConnection(dbName, userRole, password)) {
            // Вызов процедуры для добавления записи
            CallableStatement stmt = conn.prepareCall("{call add_record(?, ?, ?, ?, ?, ?)}");
            stmt.setString(1, lastName);
            stmt.setString(2, firstName);
            stmt.setString(3, cruiseName);
            stmt.setDate(4, Date.valueOf(departureDate));
            stmt.setString(5, departureCity);
            stmt.setString(6, arrivalCity);
            stmt.execute();
        } catch (SQLException e) {
            // Перехватываем и повторно выбрасываем исключение с подробным сообщением
            throw new SQLException("Ошибка при добавлении данных: " + e.getMessage(), e);
        }
    }

    public static void updateCruise(String userRole, int id, String lastName, String firstName, String cruiseName,
                                    String departureDate, String departureCity, String arrivalCity) throws SQLException {
        String dbName = "cruise_db";
        String password = getPasswordForRole(userRole);
        // Проверяем права на уровне базы данных
        boolean hasPrivilege = checkPrivilege(userRole, "UPDATE", "cruises");
        if (!hasPrivilege) {
            throw new SQLException("Недостаточно прав для обновления записи.");
        }

        try (Connection conn = getConnection(dbName, userRole, password);
             // Вызов процедуры для одновления записи
             CallableStatement stmt = conn.prepareCall("{call update_cruise(?, ?, ?, ?, ?, ?, ?)}")) {
            stmt.setInt(1, id);
            stmt.setString(2, lastName);
            stmt.setString(3, firstName);
            stmt.setString(4, cruiseName);
            stmt.setDate(5, Date.valueOf(departureDate));
            stmt.setString(6, departureCity);
            stmt.setString(7, arrivalCity);
            stmt.execute();
        } catch (SQLException e) {
            throw new SQLException("Ошибка при обновлении данных: " + e.getMessage(), e);
        }
    }


    public static void clearTable(String tableName, String userRole) throws SQLException {
        String dbName = "cruise_db";
        String password = getPasswordForRole(userRole);
        boolean hasPrivilege = checkPrivilege(userRole, "DELETE", "cruises");
        if (!hasPrivilege) {
            throw new SQLException("Недостаточно прав для чистки таблицы.");
        }
        try (Connection conn = getConnection(dbName, userRole, password);) {
            // Вызов процедуры для очистки таблицы
            CallableStatement stmt = conn.prepareCall("{call clear_table(?)}");
            stmt.setString(1, tableName);
            stmt.execute();
        } catch (SQLException e) {
            throw new SQLException("Ошибка при обновлении данных: " + e.getMessage(), e);
        }
    }

    public static ResultSet searchRecord(String element, String userRole) throws SQLException {
        String dbName = "cruise_db";
        String password = getPasswordForRole(userRole);
        boolean hasPrivilege = checkPrivilege(userRole, "SELECT", "cruises");
        if (!hasPrivilege) {
            throw new SQLException("Недостаточно прав для поиска по таблице.");
        }
        try (Connection conn = getConnection(dbName, userRole, password);) {
            // Вызов процедуры для поиска записей
            CallableStatement stmt = conn.prepareCall("{call search_cruise(?)}");
            stmt.setString(1, element);
            return stmt.executeQuery();
        } catch (SQLException e) {
            throw new SQLException("Ошибка при обновлении данных: " + e.getMessage(), e);
        }
    }

    public static void deleteRecord(String element, String userRole) throws SQLException {
        String dbName = "cruise_db";
        String password = getPasswordForRole(userRole);
        boolean hasPrivilege = checkPrivilege(userRole, "DELETE", "cruises");
        if (!hasPrivilege) {
            throw new SQLException("Недостаточно прав для удаления записи.");
        }
        try (Connection conn = getConnection(dbName, userRole, password);) {
            // Вызов процедуры для удаления записи
            CallableStatement stmt = conn.prepareCall("{call delete_record(?)}");
            stmt.setString(1, element);
            stmt.execute();
        } catch (SQLException e) {
            throw new SQLException("Ошибка при удалении данных: " + e.getMessage(), e);
        }
    }

    public static boolean checkPrivilege(String userRole, String operation, String tableName) {
        String dbName = "cruise_db";
        String sql = "{call check_privilege_on_table(?, ?, ?)}";  // Вызов хранимой функции check_privilege_on_table
        try (Connection conn = getConnection(dbName, DB_USER, DB_PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {
            // Устанавливаем параметры
            stmt.setString(1, userRole);
            stmt.setString(2, operation);
            stmt.setString(3, tableName);
            // Выполняем запрос и получаем результат
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);  // Возвращаем результат (true или false)
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при проверке прав пользователя: " + e.getMessage(), e);
        }
        return false;  // Возвращаем false, если не удалось получить результат
    }

    // Метод для создания таблицы в новой БД
    public static void createTable(String dbName) {
        String createTableQuery = """
            CREATE TABLE IF NOT EXISTS public.cruises (
                id SERIAL PRIMARY KEY,
                LastName VARCHAR(255) NOT NULL,
                FirstName VARCHAR(255) NOT NULL,
                CruiseName VARCHAR(255) NOT NULL,
                DepartureDate DATE NOT NULL,
                DepartureCity VARCHAR(255) NOT NULL,
                ArrivalCity VARCHAR(255) NOT NULL
            );
        """;
        //String dbName = "cruises_db";
        try (Connection conn = getConnection(dbName, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableQuery);
            System.out.println("Table 'cruises' created successfully in database " + dbName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


