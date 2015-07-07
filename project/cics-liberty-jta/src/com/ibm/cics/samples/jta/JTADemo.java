package com.ibm.cics.samples.jta;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import com.ibm.cics.server.CicsException;
import com.ibm.cics.server.ItemHolder;
import com.ibm.cics.server.TSQ;

/**
 * Using a Liberty JVM server to co-ordinate transactions - JTA servlet.
 * <p>
 * This servlet demonstrates the use of JTA to co-ordinate transactions between
 * CICS and Database.
 */
@WebServlet("/jta")
public class JTADemo extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/** The current status */
	private String currentStatus = "Waiting for input";
	
	/** The JTA transaction co-ordinator */
	private UserTransaction ut;
	
	/** The Database data source */
	private DataSource dbSource;
	
	/** The CICS TSQ */
	private TSQ cicsTSQ;

	/**
	 * Sets up the user transaction.
	 * 
	 * @throws NamingException
	 * @throws SQLException 
	 */
	public JTADemo() throws NamingException, SQLException {
		InitialContext init = new InitialContext();
		
		// Look up the JTA user transaction instance
		ut = (UserTransaction) init.lookup("java:comp/UserTransaction");
		
		// Create the TSQ
		cicsTSQ = new TSQ();
		cicsTSQ.setName("CICSDEV");
		
		// Look up the data source
		dbSource = (DataSource) init.lookup("jdbc/jta-db");
		
		// Set up the database
		setupDB();
	}
	
	/**
	 * Create the Derby database.
	 * 
	 * @throws SQLException
	 */
	private void setupDB() throws SQLException {
		// Get the connection
		try(Connection conn = dbSource.getConnection()) {
			// Create the table JTA.
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("CREATE TABLE JTA(DATA VARCHAR(32), PRIMARY KEY(DATA))");

			// Insert a default value
			Statement insert = conn.createStatement();
			insert.executeUpdate("INSERT INTO JTA(DATA) VALUES('N/A')");
		} catch(SQLException e) {
			String state = e.getSQLState();
			
			// Throw an exception on anything but a create fail.
			if(!state.equals("X0Y32")) {
				throw e;
			}
		}
	}
       
	/**
	 * Gets the state of the current system. Gets either:
	 * 
	 * <ol>
	 * <li>The current status of the system</li>
	 * <li>The current data in CICS</li>
	 * <li>The current data in Database</li>
	 * </ol>
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Set the content type to XML
		response.setContentType("text/xml");
		response.getWriter().println("<?xml version=\"1.0\"?>");
		
		// Start the response element.
		response.getWriter().print("<response>");
		
		// Switch on the type
		switch(request.getParameter("type")) {
		// Get the current status
		case "status":
			response.getWriter().print(currentStatus);
			break;
		// Get the CICS data
		case "cics":
			try {
				response.getWriter().print(getCICS());
			} catch (CicsException e) {
				e.printStackTrace();
				response.getWriter().print("<span class=\"error\">Unknown</span>");
			}
			break;
		// Get the Database data
		case "db":
			try {
				response.getWriter().print(getDatabase());
			} catch (SQLException e) {
				e.printStackTrace();
				response.getWriter().print("<span class=\"error\">Unknown</span>");
			}
			break;
		default:
			response.getWriter().print("<span class=\"error\">Unknown</span>");
		}
		
		// Finish the element.
		response.getWriter().print("</response>");
	}
	
	/**
	 * @return The data from Database
	 * @throws SQLException 
	 */
	private String getDatabase() throws SQLException {
		try(Connection conn = dbSource.getConnection()) {
			PreparedStatement stmt = conn.prepareStatement("SELECT * FROM JTA");
			ResultSet results = stmt.executeQuery();
			if(results.next()) {
				return results.getString(1);
			} else {
				return "...";
			}
		}
	}

	/**
	 * @return The data from CICS
	 * @throws CicsException 
	 */
	private String getCICS() throws CicsException {
		ItemHolder holder = new ItemHolder();
		int len = cicsTSQ.readItem(1, holder);
		cicsTSQ.readItem(len, holder);
		return holder.getStringValue();
	}

	/**
	 * Writes data to CICS and Database in a JTA transaction
	 * <p>
	 * Expects POST data in the form <code>data,rollback</code>, where data is
	 * any string not containing a comma and rollback is a boolean to specify
	 * whether the JTA transaction should force a rollback.
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Read the incoming data.
		BufferedReader r = request.getReader();
		String postData = r.readLine();

		// Format the data.
		String data = postData.split(",")[0];
		String rollbackStr = postData.split(",")[1];
		boolean rollback = Boolean.parseBoolean(rollbackStr);
		
		try {
			// Start the transaction
			ut.begin();
			currentStatus = "Starting transaction";
			wait5();
			
			// Write to CICS
			currentStatus = "Writing to CICS";
			writeCICS(data);
			wait5();
			
			// Write to Database
			currentStatus = "Writing to Database";
			writeDatabase(data);
			wait5();
			
			if(rollback) {
				// Rollback
				currentStatus = "Rolling back";
				ut.rollback();
				wait5();
			} else {
				// Commit the transaction
				currentStatus = "Committing";
				wait5();
				ut.commit();
			}
			
			// Reset the status
			currentStatus = "Waiting for input";
		} catch(NotSupportedException | SystemException | IllegalStateException | SecurityException | HeuristicMixedException | HeuristicRollbackException | RollbackException | SQLException | CicsException e) {
			currentStatus = "<span class=\"error\">Error: " + e.getLocalizedMessage() + "</span>";
			throw new ServletException(e);
		}
	}
	
	private void writeCICS(String data) throws CicsException {
		cicsTSQ.writeString(data);
	}
	
	private void writeDatabase(String data) throws SQLException {
		PreparedStatement stmt = dbSource.getConnection().prepareStatement("UPDATE JTA SET DATA = ?");
		stmt.setString(1, data);
		stmt.execute();
	}
	
	/**
	 * Method to make waiting 5s easy.
	 */
	private void wait5() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
