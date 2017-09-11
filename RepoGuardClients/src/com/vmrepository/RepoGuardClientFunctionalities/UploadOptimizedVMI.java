package com.vmrepository.RepoGuardClientFunctionalities;

/* 
* ===================================================================================================
* This file is part of:  Entice Repository Environment
* Release version: 0.2
* ===========================================================================================================
* Developer: Nishant Saurabh, University of Innsbruck, DIstributed and Parallel Systems,  Innsbruck, Austria.
* @author : nishant.dps.uibk.ac.at
* 
* The project leading to this application has received funding
* from the European Union's Horizon 2020 research and innovation
* programme under grant agreement No 644179.
*
* Copyright 2016 
* Contact: Vlado Stankovski (vlado.stankovski@fgg.uni-lj.si)
* =================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you must not use this file except in compliance with the License.
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*
* For details see the terms of the License (see attached file: README).
* The License is also available at http://www.apache.org/licenses/LICENSE-2.0.txt.
* ================================================================================
*/


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.MTOMFeature;

import com.vmrepository.repoguardwebserver.*;


/*
 * <<< A Web-method to facilitate upload of optimized images into S3 repository by removing the unoptimized image from S3 repository>>>
 * <<< Returns a list : with storage node ID, VM-Image name, Bucket name and URI corresponding to uploaded VMI.>>>
 * @author : nishant.dps.uibk.ac.at
 */

public class UploadOptimizedVMI {
	
	
	/*
	 * Enter the service end-point wsdl url
	 */
	
	public static final String WS_URL = "http://localhost:6070/?wsdl";

	public static void main(String[] args)
			throws MalformedURLException, IOException, FileNotFoundException, IOException, FileNotFoundException_Exception, IOException_Exception, URISyntaxException_Exception{

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
			 * vmImagename : name of VMI to be uploaded - Should be same name as unoptimized version.
			 * vmImageSourcePath : Path to VMI
			 */
			String vmImageNameOld = "Wordpress.qcow2";
			String vmImageNameNew = "WordpressNew.qcow2";
			
			String  url = " ";	
			
			/*
			 * nodeIdOld - the storage where the Unoptimized VMI was stored : for the deletion of Unoptimized VMI
			 * nodeIdNew - the storage location at which optimized VMI has to be stored.
			 */
				
			String nodeIdOld = "4";
			String nodeIdNew = "2";
				
			List list = service.receiveOptimizedVMImage(url, vmImageNameOld, vmImageNameNew, nodeIdOld, nodeIdNew);
								
					
			System.out.println(list);
					
				
			System.out.println("The VM Image: " + vmImageNameNew
					+ " has finished uploading. Thanks for being PATIENT! We hope to see you again for future UPLOAD.");

		} else {

			System.out.println("Check your authetication credentials");
		}

	}

}
