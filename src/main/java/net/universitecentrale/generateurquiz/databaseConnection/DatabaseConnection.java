package net.universitecentrale.generateurquiz.databaseConnection;

import net.universitecentrale.generateurquiz.controller.EditQuestionController;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cette classe est utilisée pour établir une connexion à la base de données.
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(EditQuestionController.class.getName()); // Logger pour afficher les messages de débogage
    /**
     * Cette méthode est utilisée pour obtenir une connexion à la base de données.
     * Elle charge les propriétés de la base de données à partir d'un fichier de configuration.
     * Ensuite, elle utilise ces propriétés pour établir une connexion à la base de données.
     * Si une erreur se produit lors de l'établissement de la connexion, elle imprime la trace de la pile de l'erreur.
     *
     * @return Une connexion à la base de données, ou null si une erreur se produit.
     */
    public static Connection getConnection() {
        Properties prop = new Properties(); // Crée un nouvel objet Properties pour stocker les propriétés de la base de données
        Connection databaseLink = null; // Initialise la connexion à la base de données à null

        try {
            prop.load(new FileInputStream("src/config.properties")); // Charge les propriétés de la base de données à partir du fichier de configuration
            String databaseName = prop.getProperty("databaseName"); // Obtient le nom de la base de données à partir des propriétés
            String databaseUser = prop.getProperty("databaseUser"); // Obtient le nom d'utilisateur de la base de données à partir des propriétés
            String databasePassword = prop.getProperty("databasePassword"); // Obtient le mot de passe de la base de données à partir des propriétés
            String databasePort = prop.getProperty("databasePort"); // Obtient le port de la base de données à partir des propriétés
            String url = "jdbc:mysql://localhost:" + databasePort + "/" + databaseName; // Construit l'URL de la base de données

            Class.forName("com.mysql.cj.jdbc.Driver"); // Charge le pilote JDBC pour MySQL
            databaseLink = DriverManager.getConnection(url, databaseUser, databasePassword); // Établit une connexion à la base de données
        } catch (IOException | ClassNotFoundException |
                 SQLException e) { // Attrape et gère les exceptions d'entrée/sortie, de classe non trouvée et SQL
            LOGGER.log(Level.SEVERE, "Erreur lors de l'établissement de la connexion à la base de données", e); // Imprime la trace de la pile de l'erreur
        }

        return databaseLink; // Renvoie la connexion à la base de données
    }
}