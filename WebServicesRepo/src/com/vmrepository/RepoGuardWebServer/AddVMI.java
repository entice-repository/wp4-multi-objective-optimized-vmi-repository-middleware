package com.vmrepository.RepoGuardWebServer;


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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.ws.soap.MTOM;




@WebService
@SOAPBinding(style = Style.RPC)
@MTOM(enabled=true, threshold = 10240)
public interface AddVMI {

	@WebMethod
	public ArrayList<String> receiveVMImage( byte[] vmImage, String vmImageName, int bytesRead, long vmiLength, String nodeId) throws FileNotFoundException, IOException, URISyntaxException;
	
	@WebMethod
	public ArrayList<String> StorageNodes() throws FileNotFoundException, IOException;
	
	@WebMethod
	public ArrayList<String> trackLocation(String vmiURI, String vmiName) throws FileNotFoundException, IOException, URISyntaxException;
	
	@WebMethod
	public ArrayList<String> getBucketList(String nodeId) throws FileNotFoundException, IOException;
	
	@WebMethod
	public ArrayList<String> getBucketVMIList(String nodeId, String bucketName) throws FileNotFoundException, IOException;
	
	@WebMethod
	public boolean optDistribute(String stage);
	
	@WebMethod
	public ArrayList<String> receiveOptimizedVMImage(URI uri, String vmImageNameOld, String vmImageNameNew, String nodeIdOld, String nodeIdNew) throws FileNotFoundException, IOException, URISyntaxException;
	
	
}
