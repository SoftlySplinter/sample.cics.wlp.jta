package com.ibm.cics.samples.jta;

import java.io.BufferedReader;
import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * Servlet implementation class JTADemo
 */
@WebServlet("/jta")
public class JTADemo extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private String currentStatus = "Waiting for input";
	
	private UserTransaction ut;
	
	String cics = null;
	String db2 = null;
	
	public JTADemo() throws NamingException {
		InitialContext init = new InitialContext();
		ut = (UserTransaction) init.lookup("java:comp/UserTransaction");
	}
       
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/xml");
		response.getWriter().println("<?xml version=\"1.0\"?>");
		
		response.getWriter().print("<response>");
		
		switch(request.getParameter("type")) {
		case "status":
			response.getWriter().print(currentStatus);
			break;
		case "cics":
			response.getWriter().print(getCICS());
			break;
		case "db2":
			response.getWriter().print(getDB2());
			break;
		default:
			response.getWriter().print("Unknown");
		}
		response.getWriter().print("</response>");
	}
	
	private String getDB2() {
		return db2;
	}

	private String getCICS() {
		return cics;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BufferedReader r = request.getReader();
		
		String postData = r.readLine();

		String data = postData.split(",")[0];
		String rollbackStr = postData.split(",")[1];
		boolean rollback = Boolean.parseBoolean(rollbackStr);

		try {
			ut.begin();
			currentStatus = "Starting transaction";
			wait5();
			
			currentStatus = "Writing to CICS";
			cics = data;
			wait5();
			
			currentStatus = "Writing to DB2";
			db2 = data;
			wait5();
			
			if(rollback) {
				currentStatus = "Rolling back";
				ut.rollback();
				wait5();
			} else {
				currentStatus = "Committing";
				wait5();
				ut.commit();
			}
			
			currentStatus = "Waiting for input";
		} catch(NotSupportedException | SystemException | IllegalStateException | SecurityException | HeuristicMixedException | HeuristicRollbackException | RollbackException e) {
			throw new ServletException(e);
		}
	}
	
	private void wait5() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
