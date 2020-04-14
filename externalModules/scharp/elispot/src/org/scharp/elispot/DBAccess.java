package org.scharp.elispot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Can be used to get a direct connection to the DB. Note: the DEFAULT values hsould be overridden.
 * This should not be used when running inside the Atlas Portal, since that uses a Connection Pool.
 * @author chuck
 * @version $Id: DBAccess.java 11382 2007-05-24 22:38:39Z chuck $
 *
 */
public class DBAccess {

    private static final String DEFAULT_DRIVER = "org.postgresql.Driver";
    private static final String DEFAULT_USER = "cpas";
    private static final String DEFAULT_PASSWORD = "cpas";
    private static final String DEFAULT_CONNECTION_URL = "jdbc:postgresql://sqltest.pc.scharp.org/elispot";

    private String driver = DEFAULT_DRIVER;
    private String user = DEFAULT_USER;
    private String password = DEFAULT_PASSWORD;
    private String connectionUrl = DEFAULT_CONNECTION_URL;

    private Connection conn;

    /**
     * Define the Connection URL to use. Supports Postgresql only.
     * @param driver
     * @param user
     * @param password
     * @param connectionUrl
     */
    public DBAccess(String driver, String user, String password,
            String connectionUrl) {
        this.driver = driver;
        this.user = user;
        this.password = password;
        this.connectionUrl = connectionUrl;
    }

    /**
     * uses defaults. Should not be used except for testing and probably not even then.
     */
    public DBAccess() {
        //To change body of created methods use File | Settings | File Templates.
    }

    /**
     * This object holds a reference to the Connection in order to close it later. Calling getConnection()
     * twice on the same object will return the existing connection if still open or close the first connection
     * and instantiate a new one.
     * @return a Connection to the DB
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public Connection getConnection() throws SQLException,
            ClassNotFoundException {

        if (conn != null && !conn.isClosed()) {
            return conn;
        }
        Class.forName(driver);
        conn = DriverManager.getConnection(connectionUrl, user,
                password);
        return conn;

    }

    /**
     * Should be called when the connection recieved from calling getConnection() is done being used.
     *
     */
    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     *
     * Make sure the connection is closed.
     *
     */
    protected void finalize() throws Throwable {
        if (conn != null) {
            this.closeConnection();
        }
    }

}
