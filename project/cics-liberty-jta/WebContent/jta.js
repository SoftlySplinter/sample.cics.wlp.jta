/**
 * Manages calls to the JTA servlet
 */

var status = "";
var running = false;

function update_status() {
	if (this.readyState = this.DONE) {
		if (this.status == 200) {
			if (this.responseXML != null
					&& this.responseXML.getElementsByTagName('response') != null
					&& this.responseXML.getElementsByTagName('response')[0] != null
					&& this.responseXML.getElementsByTagName('response')[0].textContent) {
				// Update the current status.
				status = this.responseXML.getElementsByTagName('response')[0].textContent
			} else {
				status = "Unknown";
			}

			document.getElementById('status').innerHTML = status;
		}
	}
}
function update_db2() {
	if (this.readyState = this.DONE) {
		if (this.status == 200) {
			if (this.responseXML != null
					&& this.responseXML.getElementsByTagName('response') != null
					&& this.responseXML.getElementsByTagName('response')[0] != null
					&& this.responseXML.getElementsByTagName('response')[0].textContent) {
				// Update the current status.
				document.getElementById('db2').innerHTML = this.responseXML
						.getElementsByTagName('response')[0].textContent;
			} else {
				document.getElementById('db2').innerHTML = '<span class="error">Unknown</span>';
			}
		}
	}
}
function update_cics() {
	if (this.readyState = this.DONE) {
		if (this.status == 200) {
			if (this.responseXML != null
					&& this.responseXML.getElementsByTagName('response') != null
					&& this.responseXML.getElementsByTagName('response')[0] != null
					&& this.responseXML.getElementsByTagName('response')[0].textContent) {
				// Update the current status.
				document.getElementById('cics').innerHTML = this.responseXML
						.getElementsByTagName('response')[0].textContent;
			} else {
				document.getElementById('cics').innerHTML = '<span class="error">Unknown</span>';
			}
		}
	}
}

function get_status() {
	if (!running) {
		return;
	}

	var url = '/cics-liberty-jta/jta?type=status';
	var client = new XMLHttpRequest();
	client.onreadystatechange = update_status;
	client.open('GET', url);
	client.send();

	if (status === 'Writing to DB2' || status === 'Committing'
			|| status === 'Rolling back') {
		url = '/cics-liberty-jta/jta?type=db2';
		var db2_client = new XMLHttpRequest();
		db2_client.onreadystatechange = update_db2;
		db2_client.open('GET', url);
		db2_client.send();
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