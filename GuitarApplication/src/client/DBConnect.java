package client;

import java.sql.*;
import userInterface.LauncherUI;

/**
 * This class acts to handle all database related queries, actions, and executions
 * that may need to occur. Uses standard JDBC Driver and imports the LauncherUI class from
 * the userInterface package.
 * @author Jonathan Novins
 * @version 1.1
 */
	public class DBConnect {

/**
 * Global Connection variable; Only necessary Connection throughout the application
 */
	private Connection ghCon;
/**
 * Global Statement Object used to send queries/insert/update etc through the Connection Driver
 */
	private Statement ghStmt;
/**
 * Global ResultSet Object used to retrieve the required information from the JDBC Statement
 */
	private ResultSet ghRs;

/**
 * This constructs a DBConnect Object that will act to execute any database related methods.
 */
	public DBConnect() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("***DBCONNECT SUCCESSFULLY OPENED***");
			
			ghCon = DriverManager.getConnection("jdbc:mysql://localhost:3306/guitar_helper", "admin", "password");
			ghStmt = ghCon.createStatement();
			
		}
		catch(Exception dbEx){
			System.out.println("***FAILED TO CONNECT TO DB***");
			System.out.println("[PLEASE MAKE SURE ALL REQUIRED PORTS ARE OPEN (3306 BY DEFUALT)]");
			System.out.println(dbEx + "\n\n\n\n\n");
		}
	}
	
/**
 * Database related method for DBConnect that will check if a given username is already occipied in the
 * connected database.
 * @param username A String for the username to be tested in the currently active Connection to a DB
 * @return A boolean value relating to the status of isTaken(); Returns true if username is already taken
 * and false if username is not already taken
 * @throws SQLException thrown if query execute causes a database error
 */
	public boolean isTaken(String username) throws SQLException {
		boolean taken = false;
		
		ghRs = ghStmt.executeQuery("SELECT COUNT(user_name) " +
									"FROM user_accounts " +
									"WHERE user_name = '" + username + "'");
		
		ghRs.first();
		int usernameCount = ghRs.getInt(1);
		
		if(usernameCount > 0) {
			taken = true;
		}
		
		return taken;
	}
	
/**
 * Database related method that will attempt to create an account using a received username and password
 * @param username - the requested username that the user would like to use for their account. Will throw an
 * error if username is not checked for isTaken()
 * @param password - the desired password that the user would like associated with their account.
 * @throws SQLException thrown if INSERT statement causes a database error (such as a taken username being sent)
 */
	public void createAccount(String username, String password) throws SQLException {
		String statement = "INSERT INTO user_accounts " +
				"VALUES (DEFAULT, '" + username + "', '" + password + "', DEFAULT) ";

		ghStmt.execute(statement);
	}

/**
 * A database related method that attempts to login to an existing account using the received credentials
 * @param username - the name of the account of which the login attempt will be acting upon
 * @param password - the believed associated password to the account with name username
 * @return Returns boolean value in regards to the success of the login attempt; Returns true if login
 * is successful, returns false otherwise
 * @throws SQLException thrown if an error occurs from the database's end of the query execution
 */
	public boolean login(String username, String password) throws SQLException {
		String requiredPassword = "";
		ghRs = ghStmt.executeQuery("SELECT user_password " + 
							"FROM user_accounts " +
							"WHERE user_name = '" + username + "'");
		ghRs.first();
		if(!ghRs.wasNull()) {
			requiredPassword = ghRs.getString(1);
		}
		if(requiredPassword.equals(password)) {
			LauncherUI.loggedName = username;
			return true;
		}
		return false;
	}
}
