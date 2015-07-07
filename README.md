# Using a Liberty JVM server to co-ordinate transactions - sample code

Sample code for the blog post [Using a Liberty JVM server to co-ordinate transactions](https://www.ibm.com/developerworks/community/blogs/cicsdev/entry/using_jta_transactions_in_cics_liberty_jvm_server) on CICSdev.

# Dependencies

* CICS TS for z/OS 5.2
* A Liberty JVM server ([Setting up a CICS Libery JVM server server in 4 easy steps](https://www.ibm.com/developerworks/community/blogs/cicsdev/entry/liberty_jvm_servers_a_quickstart_guide))
* The Derby libraries
* *Some form of recoverable CICS resource* (TODO: decide on this)

# Deploying the Sample

1. Start the CICS region then define and install the Liberty JVM server
2. Update the server.xml of the Liberty JVM server using the [example](bin/server.xml) provided
3. Deploy the CICS bundle to USS (either via FTP or CICS Explorer)
4. Install the bundle
5. View the messages.log of the Liberty JVM server to ensure the application has been enabled

# Using the Sample

1. Access the URL specified in the messages.log activation message (http://host:port/cics-liberty-jta/)
2. Enter some data into the field
3. Click run
4. The data will be written to CICS then the DB in turn and the transaction will commit.
5. Enter some different data and check the rollback box
6. Click run
7. The data will be written to CICS then the DB in turn, then the transaction will rollback, recovering the data in CICS and the DB to the original values

# Notice

&copy; Copyright IBM Coporation 2015

# License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
