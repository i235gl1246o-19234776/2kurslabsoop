package repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseRepositoryTest {

    private static final String TEST_DB_NAME = "functions_test_db";
    private static final String TEST_URL = "jdbc:postgresql://localhost:5432/" + TEST_DB_NAME;
    private static final String MAIN_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1234";

    @BeforeAll
    void setUp() throws SQLException {
        System.out.println("=== НАСТРОЙКА ТЕСТОВОЙ СРЕДЫ ===");

        createTestDatabaseIfNeeded();
        createTables();
        DatabaseConnection.setTestConnection(TEST_URL, USER, PASSWORD);

        System.out.println("Тестовая база данных готова: " + TEST_URL);
        System.out.println("Режим подключения: " + DatabaseConnection.getConnectionInfo());
    }

    private void createTestDatabaseIfNeeded() throws SQLException {
        try (Connection conn = DriverManager.getConnection(MAIN_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            boolean dbExists = false;
            try {
                stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + TEST_DB_NAME + "'").next();
                dbExists = true;
                System.out.println("Тестовая база данных уже существует: " + TEST_DB_NAME);
            } catch (SQLException e) {
                // База не существует - создаем
                System.out.println("Создание тестовой базы данных: " + TEST_DB_NAME);
                stmt.execute("CREATE DATABASE " + TEST_DB_NAME);
                dbExists = true;
            }

            if (!dbExists) {
                throw new SQLException("Не удалось создать тестовую базу данных: " + TEST_DB_NAME);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при создании тестовой базы данных: " + e.getMessage());
            throw e;
        }
    }

    private void createTables() throws SQLException {
        try (Connection conn = DriverManager.getConnection(TEST_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET session_replication_role = 'replica'");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS functions (
                    id SERIAL PRIMARY KEY,
                    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                    type_function VARCHAR(20) CHECK (type_function IN ('tabular', 'analytic')),
                    function_name VARCHAR(255) NOT NULL,
                    function_expression TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tabulated_functions (
                    id SERIAL PRIMARY KEY,
                    function_id INTEGER NOT NULL REFERENCES functions(id) ON DELETE CASCADE,
                    x_val DOUBLE PRECISION NOT NULL,
                    y_val DOUBLE PRECISION NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS operations (
                    id SERIAL PRIMARY KEY,
                    function_id INTEGER NOT NULL REFERENCES functions(id) ON DELETE CASCADE,
                    operations_type_id INTEGER NOT NULL
                )
            """);

            stmt.execute("SET session_replication_role = 'origin'");

            System.out.println("Все таблицы успешно созданы в тестовой базе данных");

        } catch (SQLException e) {
            System.err.println("Ошибка при создании таблиц: " + e.getMessage());
            throw e;
        }
    }

    @AfterEach
    void cleanUp() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("SET session_replication_role = 'replica'");

            stmt.execute("DELETE FROM operations");
            stmt.execute("DELETE FROM tabulated_functions");
            stmt.execute("DELETE FROM functions");
            stmt.execute("DELETE FROM users");

            stmt.execute("ALTER SEQUENCE IF EXISTS users_id_seq RESTART WITH 1");
            stmt.execute("ALTER SEQUENCE IF EXISTS functions_id_seq RESTART WITH 1");
            stmt.execute("ALTER SEQUENCE IF EXISTS tabulated_functions_id_seq RESTART WITH 1");
            stmt.execute("ALTER SEQUENCE IF EXISTS operations_id_seq RESTART WITH 1");

            stmt.execute("SET session_replication_role = 'origin'");

        } catch (SQLException e) {
            System.err.println("Ошибка при очистке тестовых данных: " + e.getMessage());
            throw e;
        }
    }

    @AfterAll
    void tearDown() {
        DatabaseConnection.resetToDefaultConnection();
        System.out.println("=== ТЕСТОВАЯ СРЕДА ЗАВЕРШЕНА ===");
        System.out.println("Подключение сброшено к рабочим настройкам");
    }

    protected Long getLastInsertedId(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT id FROM " + tableName + " ORDER BY id DESC LIMIT 1")) {
            return rs.next() ? rs.getLong("id") : null;
        }
    }
}