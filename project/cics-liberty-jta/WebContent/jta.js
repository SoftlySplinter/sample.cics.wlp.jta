/**
 * Manages calls to the JTA servlet
 */

/** The current status */
var status = "";

/** If the transaction is running */
var running = false;

/**
 * Update the status.
 * 
 * A callback from a XMLHttpRequest.
 */
function update_status() {
	// Ensure the request is done
	if (this.readyState = this.DONE) {
		// Ensure the status was 200 OK
		if (this.status == 200) {
			// Ensure the response XML is present and contains the correct elements
			if (this.responseXML != null
					&& this.responseXML.getElementsByTagName('response') != null
					&& this.responseXML.getElementsByTagName('response')[0] != null
					&& this.responseXML.getElementsByTagName('response')[0].textContent) {
				// Update the current status.
				status = this.responseXML.getElementsByTagName('response')[0].textContent
			} else {
				status = "Unknown";
			}

			// Update the status in the web page.
			document.getElementById('status').innerHTML = status;
		}
	}
}

/**
 * Updates the web page with the response to update the database.
 * 
 * A callback from a XMLHttpRequest
 */
function update_db() {
	if (this.readyState = this.DONE) {
		if (this.status == 200) {
			if (this.responseXML != null
					&& this.responseXML.getElementsByTagName('response') != null
					&& this.responseXML.getElementsByTagName('response')[0] != null
					&& this.responseXML.getElementsByTagName('response')[0].textContent) {
				// Update the current status.
				document.getElementById('db').innerHTML = this.responseXML.getElementsByTagName('response')[0].textContent;
			} else {
				document.getElementById('db').innerHTML = '<span class="error">Unknown</span>';
			}
		}
	}
}

/**
 * Updates the web page with the response to update CICS.
 * 
 * A callback from a XMLHttpRequest
 */
function update_cics() {
	if (this.readyState = this.DONE) {
		if (this.status == 200) {
			if (this.responseXML != null
					&& this.responseXML.getElementsByTagName('response') != null
					&& this.responseXML.getElementsByTagName('response')[0] != null
					&& this.responseXML.getElementsByTagName('response')[0].textContent) {
				// Update the current status.
				document.getElementById('cics').innerHTML = this.responseXML.getElementsByTagName('response')[0].textContent;
			} else {
				document.getElementById('cics').innerHTML = '<span class="error">Unknown</span>';
			}
		}
	}
}

/**
 * Get the status from the JTA servlet.
 */
function get_status() {
	if (!running) {
		return;
	}

	var url = '/cics-liberty-jta/jta?type=status';
	var client = new XMLHttpRequest();
	client.onreadystatechange = update_status;
	client.open('GET', url);
	client.send();

	if (status === 'Writing to Database' 
	     || status === 'Committing'
	     || status === 'Rolling back') {
		url = '/cics-liberty-jta/jta?type=db';
		var db_client = new XMLHttpRequest();
		db_client.onreadystatechange = update_db;
		db_client.open('GET', url);
		db_client.send();
	}

	if (status === 'Writing to CICS' || status === 'Committing'
			|| status === 'Rolling back') {
		url = '/cics-liberty-jta/jta?type=cics';
		cics_client = new XMLHttpRequest();
		cics_client.onreadystatechange = update_cics;
		cics_client.open('GET', url);
		cics_client.send();
	}
}

function finish_request() {
	if (this.readyState = this.DONE) {
		if (this.status == 200) {
			if (this.responseText != null) {
				running = false;
				document.getElementById('status').innerHTML = "Waiting for input";
				document.getElementById("run").disabled = false;
			}
		}
	}
}

function send_request() {
	var url = '/cics-liberty-jta/jta'
	var client = new XMLHttpRequest();
	client.onreadystatechange = finish_request;
	client.open('POST', url);

	var data = {
		data : document.forms["run"]["data"].value,
		rollback : document.forms["run"]["rollback"].checked
	}
	running = true;
	document.getElementById("run").disabled = true;
	client.send(data.data + "," + data.rollback);

	return false;
}

setInterval(get_status, 1000);
get_status();