package queuemonitor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class helps with connection to the KACE ticketing system and takes
 * care of executing SQL search queries.
 * @author bshteinfeld
 */
public class DBConnection {
    // Name of JDBC driver (jar file included)
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    // IP of the KACE server
    private static final String DB_URL = "jdbc:mysql://192.168.32.104/ORG1";
    private String USER;
    private String PASS;
    private Connection connection;
    
    public DBConnection() {
        connection = null;
        // Username to KACE is R1, pass is box747 by default
        USER = "R1";
        PASS = "box747";
    }
    
    /**
     * Initialize a connection to the KACE database
     */
    public void setUpDBConnection() {
        try {
            // Register a JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection to database
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch(SQLException ex) {
            System.err.println("Error connecting to DB");
        } catch (ClassNotFoundException ex) {
            System.err.println("Error finding JDBC jar file");
        } 
    }
    
    /**
     * Close a connection to the KACE database.
     */
    public void closeDBConnection() {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e){
            System.err.println("Error closing database file: " + e.getMessage());
        }
    }
    
    /**
     * Execute a search query in SQL.
     * @param qry -- Query in SQL
     * @return -- ResultSet of the query
     */
    public ResultSet executeQuery(String qry) {
        if(connection == null) {
            System.err.println(" Must set up connection to DB before executing query.");
            return null;
        }
        // Create a statment in order to execute the query
        System.out.println("Creating statement...");
        Statement statm = null;
        ResultSet rs = null;
        try {
            statm = connection.createStatement();
            rs = statm.executeQuery(qry);
        } catch (SQLException ex) {
            System.err.println("Error creating statement");
            return null;
        }
        return rs;
    }
    
}
