/**
 * Manages calls to the JTA servlet
 */

function update_status() {
	console.log("update status");
	if(this.readyState = this.DONE) {
		if (this.status == 200) {
			if (this.responseXML != null
					&& this.responseXML.getElementsByTagName('response') != null
					&& this.responseXML.getElementsByTagName('response')[0] != null
					&& this.responseXML.getElementsByTagName('response')[0].textContent) {
				// Update the current status.
				document.getElementById('status').innerHTML = this.responseXML.getElementsByTagName('response')[0].textContent;
			} else {
				document.getElementById('status').innerHTML = '<span class="error">Unknown</span>';
			}
		}
	}
}
function update_db2() {
	console.log("update db2");
	if(this.readyState = this.DONE) {
		if (this.status == 200) {
			if (this.responseXML != null
					&& this.responseXML.getElementsByTagName('response') != null
					&& this.responseXML.getElementsByTagName('response')[0] != null
					&& this.responseXML.getElementsByTagName('response')[0].textContent) {
				// Update the current status.
				document.getElementById('db2').innerHTML = this.responseXML.getElementsByTagName('response')[0].textContent;
			} else {
				document.getElementById('db2').innerHTML = '<span class="error">Unknown</span>';
			}
		}
	}
}
function update_cics() {
	console.log("update cics");
	if(this.readyState = this.DONE) {
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

function get_status() {
	console.log("get status");
	var url = '/cics-liberty-jta/jta?type=status';
	var client = new XMLHttpRequest();
	client.onreadystatechange = update_status;
	client.open('GET', url);
	client.send();
	

	url = '/cics-liberty-jta/jta?type=db2';
	var db2_client = new XMLHttpRequest();
	db2_client.onreadystatechange = update_db2;
	db2_client.open('GET', url);
	db2_client.send();
	

	url = '/cics-liberty-jta/jta?type=cics';
	cics_client = new XMLHttpRequest();
	cics_client.onreadystatechange = update_cics;
	cics_client.open('GET', url);
	cics_client.send();
}

function send_request() {
	console.log("send request");
	
	
	var url = '/cics-liberty-jta/jta'
	var client = new XMLHttpRequest();
	client.onreadystatechange = update_status;
	client.open('POST', url);
	
	var data = {
		data: document.forms["run"]["data"].value,
		rollback: document.forms["run"]["rollback"].checked
	}
	console.log(data);
	client.send(data.data + "," + data.rollback);
	
	return false;
}

setInterval(get_status, 100);
get_status();