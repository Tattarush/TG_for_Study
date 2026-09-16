import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:h2:./db/botdata";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Драйвер не найден в библиотеках проекта");
            e.printStackTrace();
        }
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static void initDatabase() {

        String createTAbleSQL = "CREATE TABLE IF NOT EXISTS user_notes(" +
                "user_id BIGINT PRIMARY KEY, " +
                "note_text TEXT " +
                ")";
        try (Connection conn = getConnection();
        Statement stmt = conn.createStatement()){
            stmt.execute(createTAbleSQL);
            System.out.println("База данных успешно инициализирована");
        } catch (SQLException e) {
            System.out.println("ошибка при инициализации базы данных!");
            e.printStackTrace();
        }



    }
}
