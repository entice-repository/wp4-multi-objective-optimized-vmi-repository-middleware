package com.vmrepository.RepoGuardWebServer;

/* 
* ===================================================================================================
* This file is part of: Entice Repository Environment
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


import javax.xml.ws.Endpoint;
import java.io.IOException;

/*
 * <<< RepoGuard Web server Calling VMI Repository Functionalities >>>
 * <<< 1. StorageNodes() >>>
 * <<< 2. getBucketList(String nodeId) >>>
 * <<< 3. getBucketVMIList(String nodeId, String bucketName) >>>
 * <<< 4. receiveVMImage(byte[] vmImage, String vmImageName, integer bytesRead, long vmiLength, String nodeId) >>>
 * <<< 5. trackLocation(String URI, String vmiName) >>>
 * <<< 6. reDistribution(File file) >>>
 * <<< 7. Authenticate(String user-name, String password) >>>
 * @author: nishant.dps.uibk.ac.at
 */

public class RepoGuardServer  {

	
	public static void main(String[] args) throws IOException{
			   		
		String bindingRepoURI = "http://localhost:6070/";
		AddVMI service = new AddVMIImplementation();
		Endpoint.publish(bindingRepoURI, service);
		
		System.out.println("RepoGuard Server started at:" + bindingRepoURI);
		System.out.println("RepoGuard Server ready to Receive VM Image.....");
		
		
	    
      }
	    	
}