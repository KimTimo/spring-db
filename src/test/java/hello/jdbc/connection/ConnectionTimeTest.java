package hello.jdbc.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

public class ConnectionTimeTest {
    public static void main(String[] args) {
        String[] dbUrls = {
//            "jdbc:mysql://localhost:3306/test_db",
//            "jdbc:postgresql://localhost:5432/test_db",
//            "jdbc:oracle:thin:@localhost:1521:xe",
//            "jdbc:sqlserver://localhost:1433;databaseName=test_db",
//            "jdbc:sqlite:test.db"
           "jdbc:mariadb://localhost:3306/spring_db"
        };

        for (String url : dbUrls) {
            long startTime = System.currentTimeMillis();
            try (Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
                long endTime = System.currentTimeMillis();
                System.out.println("Connected to " + url + " in " + (endTime - startTime) + "ms");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}