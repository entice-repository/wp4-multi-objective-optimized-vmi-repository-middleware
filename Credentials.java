import java.util.*;
import java.io.*;


/*
 * Sample Add your S3 credentials here
 * and store it in a properties file ./Resources/Credentials/S3Credentials.properties
 */


public class Credentials {
	
	public static Properties properties = new Properties();
	
	public static Map<String,String> credentialDB = new HashMap<String,String>();
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
		
		Credentials credentials = new Credentials();
		String basePath = new File("").getAbsolutePath();
		
		/*
		 * Inputs : NodeId, NodeEndpoint, Provider: always s3 , S3Identity, S3Credentials
		 */
		
		credentials.s3Credits("#NodeId:eg:1", "#NodeEndpoint:eg:https://s3.amazonaws.com", "s3", "#s3Identity", "#s3Credential");
		credentials.s3Credits("#NodeId:eg:2", "#NodeEndpoint:eg:https://s3.amazonaws.com", "s3", "#s3Identity", "#s3Credential");
		credentials.s3Credits("#NodeId:eg:3", "#NodeEndpoint:eg:https://s3.amazonaws.com", "s3", "#s3Identity", "#s3Credential");
		credentials.s3Credits("#NodeId:eg:4", "#NodeEndpoint:eg:https://s3.amazonaws.com", "s3", "#s3Identity", "#s3Credential");
		
		
		Properties properties = new Properties();
		properties.putAll(credentialDB);
				
		properties.store(new FileOutputStream("./Resources/Credentials/S3Credentials.properties"), null);
						
		
	}
	
	public void s3Credits(String nodeId, String nodeEndpoint, String provider, String identity, String credential){
		
		ArrayList<String> info = new ArrayList<String>();
		
		
		info.add(nodeEndpoint);
		info.add(provider);
		info.add(identity);
		info.add(credential);
		
		credentialDB.put(nodeId, info.toString());
		
	}
	
	

}
