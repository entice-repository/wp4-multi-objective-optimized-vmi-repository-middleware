# wp4-multi-objective-optimized-vmi-repository-middleware
This is an Individual work package of Entice Repository Middleware with various functionalities:

1. Uploading Un-optimized and Optimized Images.
2. VMI Location Tracking.
3. Applying Multi-objective optimization
4. Querying the storage node details attached to ENTICE VMI Repository
5. Querying the S3 container/bucket details at a particular Storage Node using its ID
6. Querying the VM Images details stored at storage node inside a particular container/bucket.


Pre-requisites :

1. Java 8.
2. Tomcat 7 
3. Eclipse 
4. S3 compliant storage nodes


Flowchart to deploy wp4-multi-objective-optimized-vmi-repository-middleware :

Step 1. Download the wp4-multi-objective-optimized-vmi-repository-middleware repository from Github and unzip it.

Step 2. cd wp4-multi-objective-optimized-vmi-repository-middleware. Import the project WebServicesRepo in eclipse 
        as a Web project.

Step 3. Add external jar files to this eclipse project from 
        /wp4-multi-objective-optimized-vmi-repository-middleware/referencedLibrary/lib/

Step 4. Export "WebServicesRepo" project as a .war file : WebServicesRepo.war and store it locally for the time. 
        (Include all the necessary libraries and dependencies while exporting war file.) 

Step 5. Copy the Resources folder from /wp4-multi-objective-optimized-vmi-repository-middleware/Resources/ 
        to the folder where Tomcat folder exists.

Step 6. We need to input S3 storage nodes necessary fields within the properties file inside 
        /wp4-multi-objective-optimized-vmi-repository-middleware/Resources/S3Credentials.properties. 

	     (a) Inside /wp4-multi-objective-optimized-vmi-repository-middleware/ , 
	         there is a Credentials.java file. 
	         Input as many as S3 nodes with respective fields in the following manner :
		       credentials.s3Credits(
		       "#NodeId:eg:1", 
		       "#NodeEndpoint:eg:https://s3.amazonaws.com", 
		       "s3", 
		       "#s3Identity", 
		       "#s3Credential");

	     (b) The path of S3Credentials.properties is ../../Resources/Credentials. 
	     	 Make sure you run this pice of Java code such that S3 Credentials 
	     	 are stored in /Resources/S3Credentials.properties.


Step 7. Further, just for authentication purpose of this service, a sample hashed password storage 
        service is provided. Sample java code named : PasswordStorage.java is provided which upon 
        running stores the hashed values in /Resources/Users/Repo-Guard-Users.properties file. A 
        sample properties file is created, where usernames and hashed passwords are stored. In the 
        PasswordStorage.java , the inputs are usernames and actual passwords. Clients accessing this 
        service needs the usernames and actual passwords. For testing purposes , either new hashed 
        passwords can be generated or left same. Make sure the new hashed passwrods are stored in 
	/Resources/Users/Repo-Guard-Users.properties.


Step 8. Just to recap: the Resources folder must be stored in the folder where TOMCAT folder exists.

Step 9. Remember, the war file we generated. Copy this war file WebServicesRepo.war inside webapps folder 
        of TOMCAT(/tomcat/webapps). 

Step 10. Deploy this war file by using start script provided by Tomcat installation. Check the deployment 
         tutorials of Tomcat, to change the server settings for the ports and endpoints at which this service 
         is deployed. 


Step 11. Once deployed, you can go to the browser. 
	 Enter : http://(serverEndpointurl):(PortNumber)/WebServicesRepo/add . 
         The service can be found running.

Step 12. To check the accessibility of this service : Import project RepoGuardClients from 
         /wp4-multi-objective-optimized-vmi-repository-middleware/ folder into eclipse. 
         The client files are stored inside the src folder.

	       (a) To use the client java files for individual services. Initially, go to terminal 
	           and move to the workspace folder where RepoGuardClients project is stored. 

	       (b) cd /RepoGuardClients/src/

	       (c) Now from terminal run : 
	           wsimport -keep http://(serverEndpointurl):(PortNumber)/WebServicesRepo/add?wsdl .

	       (d) This will download all the necessary server modules and apis 
	           into the /RepoGuardClients/src/com/vmrepository/repoguardwebserver.

	       (e) Finally, use the individual RepoGuard Client files from   
	       	   /RepoGuardClients/src/com/vmrepository/RepoGuardClientFuntionalities for individual
	           funtionality testing.

Step 13. Make sure, WebServicesRepo.war is deployed properly and can be accessible through web browser.

Step 14. In order to integrated the whole entice module, specifically with knoweledge base, VMI Optimizer, 
         and User side GUI and backend : Follow the steps presented within entice-repository/wp5-kb-backend-api/
         repository.



Step 15. In case of any issues : mail me at : nishant ( at ) dps.uibk.ac.at
