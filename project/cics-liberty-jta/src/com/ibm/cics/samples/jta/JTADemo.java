package com.ibm.cics.samples.jta;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

/**
 * Using a Liberty JVM server to co-ordinate transactions - JTA servlet.
 * <p>
 * This servlet demonstrates the use of JTA to co-ordinate transactions between
 * CICS and DB2.
 */
@WebServlet("/jta")
public class JTADemo extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/** The current status */
	private String currentStatus = "Waiting for input";
	
	/** The JTA transaction co-ordinator */
	private UserTransaction ut;
	
	/** The DB2 data source */
	private DataSource db2Source;
	
	// TODO - Remove these
	/** Temporary */
	String cics = null;

	// TODO - Remove these
	/** Temporary */
	String db2 = null;
	
	/**
	 * Sets up the user transaction.
	 * 
	 * @throws NamingException
	 */
	public JTADemo() throws NamingException {
		InitialContext init = new InitialContext();
		ut = (UserTransaction) init.lookup("java:comp/UserTransaction");
		
		// TODO Uncomment this once DB2 is set up in server.xml
//		db2Source = (DataSource) init.lookup("jdbc/db2");
	}
       
	/**
	 * Gets the state of the current system. Gets either:
	 * 
	 * <ol>
	 * <li>The current status of the system</li>
	 * <li>The current data in CICS</li>
	 * <li>The current data in DB2</li>
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
			response.getWriter().print(getCICS());
			break;
		// Get the DB2 data
		case "db2":
			response.getWriter().print(getDB2());
			break;
		default:
			response.getWriter().print("Unknown");
		}
		
		// Finish the element.
		response.getWriter().print("</response>");
	}
	
	/**
	 * @return The data from DB2
	 */
	private String getDB2() {
		return db2;
	}

	/**
	 * @return The data in CICS
	 */
	private String getCICS() {
		return cics;
	}

	/**
	 * Writes data to CICS and DB2 in a JTA transaction
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
			cics = data;
			wait5();
			
			// Write to DB2
			currentStatus = "Writing to DB2";
			writeDB2(data);
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
		} catch(NotSupportedException | SystemException | IllegalStateException | SecurityException | HeuristicMixedException | HeuristicRollbackException | RollbackException | SQLException e) {
			throw new ServletException(e);
		}
	}
	
	private void writeDB2(String data) throws SQLException {
		db2 = data;
		
//		PreparedStatement stmt = db2Source.getConnection().prepareStatement("UPDATE jta SET DATA = ?");
//		stmt.setString(1, data);
//		stmt.execute();
	}
	
	/**
	 * Method to make waiting 5s easy.
	 */
	private void wait5() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
