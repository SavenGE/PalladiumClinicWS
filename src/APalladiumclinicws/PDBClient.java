/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package APalladiumclinicws;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author neptune
 */
/*Class to handle Database connection and data fetch utility methods*/
public class PDBClient {

    /**
     * @return the dbConnection
     */
    public Connection getDbConnection() {
        return dbConnection;
    }

    /**
     * @param dbConnection the dbConnection to set
     */
    public void setDbConnection(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }
    private Connection dbConnection = null;
    /*DB connections parameters*/
    String jdbcUrl = "jdbc:oracle:thin:@//localhost:1521/pdbrubikon",
            schemaName = "PALLADIUMCLINIC",
            schemaPassword = "palladiumclinic";

    public PDBClient() {
        connectToDB();
    }

    public void connectToDB() {
        try {
            /*load connection driver and connect to DB*/
            Class.forName("oracle.jdbc.driver.OracleDriver");
            setDbConnection(DriverManager.getConnection(jdbcUrl, schemaName, schemaPassword));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /*Method to fetch data and return in result set*/
    public ResultSet executeQuery(String query) {
        try {
            Logger.doPrint("executeQuery", query);
            if (getDbConnection() == null) {
                connectToDB();
            } else {
                if (getDbConnection().isClosed()) {
                    connectToDB();
                }
            }
            if (getDbConnection() != null) {
                Statement statement = getDbConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                return statement.executeQuery(query);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /*Method to update db data*/
    public boolean executeUpdate(String update) {
        try {
            update = update.replaceAll("'null'", "NULL").replaceAll("'NULL'", "NULL");
            Logger.doPrint("executeUpdate", update);
            if (getDbConnection() == null) {
                connectToDB();
            } else {
                if (getDbConnection().isClosed()) {
                    connectToDB();
                }
            }
            if (getDbConnection() != null) {
                try (Statement statement = getDbConnection().createStatement()) {
                    statement.executeUpdate(update);
                }
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /*Utility method to check if data exists*/
    public boolean checkIfExists(String query) {
        boolean exists = false;
        try {
            try (ResultSet rs = executeQuery(query)) {
                exists = rs.next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return exists;
    }

    /*Close/Destroy db Connection*/
    public void dispose() {
        try {
            if (getDbConnection() != null) {
                getDbConnection().close();
            }
        } catch (Exception ex) {
            ex = null;
        }
        setDbConnection(null);
        Logger.doPrint("dispose", "Dispose DB Connection");
    }
}
