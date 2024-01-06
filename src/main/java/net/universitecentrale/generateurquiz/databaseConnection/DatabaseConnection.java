package net.universitecentrale.generateurquiz.databaseConnection;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    public static Connection getConnection(){
        Properties prop = new Properties();
        Connection databaseLink = null;

        try {
            prop.load(new FileInputStream("src/config.properties"));
            String databaseName = prop.getProperty("databaseName");
            String databaseUser = prop.getProperty("databaseUser");
            String databasePassword = prop.getProperty("databasePassword");
            String databasePort = prop.getProperty("databasePort");
            String url = "jdbc:mysql://localhost:" + databasePort + "/" + databaseName;

            Class.forName("com.mysql.cj.jdbc.Driver");
            databaseLink = DriverManager.getConnection(url, databaseUser, databasePassword);
        } catch (IOException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return databaseLink;
    }
}