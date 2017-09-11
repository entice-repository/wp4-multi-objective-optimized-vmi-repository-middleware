package com.vmrepository.RepoGuardClientFunctionalities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.MTOMFeature;

import com.vmrepository.repoguardwebserver.*;

public class VMIRedistribution {
	
	public static final String WS_URL = "http://localhost:6070/?wsdl";
	public static void main(String[] args)
			throws MalformedURLException, IOException, FileNotFoundException, IOException, FileNotFoundException_Exception, IOException_Exception, URISyntaxException, URISyntaxException_Exception, ParseException_Exception{

		AddVMIImplementationService client = new AddVMIImplementationService();
		AddVMIImplementation service = client.getAddVMIImplementationPort(new MTOMFeature(10240));

		BindingProvider provider = (BindingProvider) service;
		Map<String, Object> req_ctx = provider.getRequestContext();

		req_ctx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, WS_URL);

		Map<String, List<String>> headers = new HashMap<String, List<String>>();

		/*
		 * authenticate(user-name,password) checks for authentication on the
		 * client side.
		 */

		if (service.authenticate("FLEXIANT", "cVgZzW9Y&*xk%U=9")) {

			/*
			 * Provide your user name and password as a header to enable the
			 * server to upload the file
			 */
			headers.put("Username", Collections.singletonList("FLEXIANT"));
			headers.put("Password", Collections.singletonList("cVgZzW9Y&*xk%U=9"));

			req_ctx.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
			
			/*
			 * input : redistributionFile
			 */
			
			//File  file = new File("/home/nishant/test.json");
			
			String redistributionFileName = "test.json";
			String redistributionFileSourcePath = " " + redistributionFileName;
			File redistributionFile = new File(redistributionFileSourcePath);
			FileInputStream in = null;
			BufferedInputStream bin = null;
			
			
			System.out.println("The size of file:" + redistributionFile.length());

			try {
				in = new FileInputStream(redistributionFile);
				bin = new BufferedInputStream(in);

				/*
				 * Change the size of byte array as per the settings of JVM heap
				 * size
				 */
				byte[] fileBytes = new byte[1024 * 1024 * 32];
				int bytesRead;
				
				
				while ((bytesRead = bin.read(fileBytes)) != -1) {				
					//List list = service.receiveVMImage(vmImageBytes, vmImage.getName(), bytesRead, vmImage.length(), nodeId);
					List list = service.reDistribution(fileBytes,redistributionFile.getName(), bytesRead, redistributionFile.length());
					
					System.out.println(list);
					
				}

			} catch (IOException e) {
				System.err.println(e);
			} finally {

				if (bin != null) {

					in.close();
				}
				
			}

			
			
			//System.out.println(file);
			//List list = service.reDistribution(file.to);
			
			//List list = service.reDistribution(file);
			//System.out.println(list);
	
		} else {

			System.out.println("Check your authetication credentials");
		}

	}

}
