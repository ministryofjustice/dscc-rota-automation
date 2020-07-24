package net.atos.laa.rotas.algorithm.dataset;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Manages access to a single source of rota data in SQLite3 format. Enables
 * access to the list of schemes and the list of solicitors for each scheme,
 * that are stored in the dataset.
 */
public class SQLiteDataset implements Dataset {

    /**
     * Stores the connection to the database
     */
    private Connection connection;


    /**
     * Creates and opens a connection to a dataset for accessing data in the
     * SQlite3 format
     * @param sourceFilePath The path to the dataset file
     * @throws FileNotFoundException
     * @throws SQLException
     */
    public SQLiteDataset(String sourceFilePath) throws FileNotFoundException, SQLException {

        // Check if the specified dataset file exists
        if (!new File(sourceFilePath).exists()) {
            throw new FileNotFoundException("Dataset file does not exist");
        }

        connection = DriverManager.getConnection("jdbc:sqlite:" + sourceFilePath);
    }

    @Override
    public ArrayList<Scheme> getSchemes() {
        ArrayList<Scheme> schemes = new ArrayList<>();

        try {
            String query = "SELECT * FROM schemes";
            ResultSet result = connection.createStatement().executeQuery(query);

            // Add each returned scheme to the list
            while (result.next()) {
                schemes.add(new Scheme(
                        result.getInt("scheme_id"),
                        result.getString("name")
                ));
            }

        } catch (Exception e) {
            return null;
        }

        return schemes;
    }

    @Override
    public ArrayList<Solicitor> getSolicitorsForScheme(int schemeId) {
        ArrayList<Solicitor> solicitors = new ArrayList<>();

        try {
            // Query the database to get the solicitors
            String query = "SELECT * FROM solicitors WHERE scheme_id = " + schemeId;
            ResultSet result = connection.createStatement().executeQuery(query);

            // Add each returned scheme to the list
            while (result.next()) {
                solicitors.add(new Solicitor(result.getInt("solicitor_id"),
                        result.getString("name"), result.getInt("firm_id"), result.getInt("scheme_id")
                ));
            }

        } catch (SQLException e) {
            return null;
        }

        return solicitors;
    }

    @Override
    public boolean disconnect() {
        try {
            if (connection != null) {
                connection.close();
                return true;
            }

        } catch (Exception ignored) {
        }

        return false;
    }
}
