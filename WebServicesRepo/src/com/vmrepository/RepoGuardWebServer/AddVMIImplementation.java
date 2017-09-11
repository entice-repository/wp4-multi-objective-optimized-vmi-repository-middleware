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

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.annotation.Resource;
import com.vmrepository.LocationTracking.Location;
import com.vmrepository.LocationTracking.LookupService;
import com.vmrepository.LocationTracking.regionName;
import com.vmrepository.LocationTracking.timeZone;
import com.vmrepository.RepoStore.NodeCredential;


import exec.moExecute; 

import javax.jws.WebMethod;
import javax.jws.WebService;

//import javax.servlet.http.HttpServletResponse;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.MTOM;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceContext;


import java.text.DateFormat;


/*
 * <<< Implementation of RepoGuard end-point web Interface >>>
 * @author : nishant.dps.uibk.ac.at
 */

@WebService
@MTOM(enabled=true, threshold = 10240)
public class AddVMIImplementation implements AddVMI{
	
	public static Properties properties = new Properties();
	public static Map<String, String> DB = new HashMap<String, String>(); 
	public static final String SALT = "se4>48'V.Pz`QH+&";
	
	
	public static  NodeCredential repoS3;
	
	
	static final String STAGEI="stageI";
	static final String STAGEII="stageII";
	
	static final String STAGEIF="fragmentUpload";
	static final String STAGEIIUpdated="redistributeOPT";
	static final String ASSEMBLY="Assembly";
	
	static {PropertyConfigurator.configure("../../Resources/log/log4j.properties"); }
	
	static Logger logger__=Logger.getLogger(AddVMIImplementation.class);
	
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Calendar cal = Calendar.getInstance();
	
	
	
	@Resource
	WebServiceContext wsctx;
	
	
	/*
	 * <<< A Web method to call the multi-objective Optimization framework for redistribution of images within Storage repository>>>
	 * <<< Returns Boolean >>>
	 * @author : dragi.dps.uibk.ac.at
	 */
	
	@Override
	@WebMethod
	public boolean optDistribute(String stage, String fragmentIDRedistribute){
		boolean result = false;
		
		
		moExecute mo = new moExecute();
		
		try{
			logger__.info("Multi-objective Optimization started");
			
			mo.moExecute_runtime(stage, fragmentIDRedistribute);
			
			result = true;
			logger__.info("Multi-objective Optimization finished");
		}catch(Exception e){
			e.printStackTrace();
			logger__.error("Multi-Objective optimization failed", e);
		}
		
		return result;
	}
	
	/*
	 * <<< A Web Method to get the list of available storage nodes.>>>
	 * <<< Returns a list of Storage nodes with their identifier, end-point,>>> 
	 * <<< Type of storage - in this case s3 based object storage and Location of the storage Node.>>> 
	 * @author : nishant.dps.uibk.ac.at
	 */
	
	@Override
	@WebMethod	
	public ArrayList<String> StorageNodes() throws FileNotFoundException, IOException{
				
		MessageContext mctx = wsctx.getMessageContext();		
		Map http_headers = (Map) mctx.get(MessageContext.HTTP_REQUEST_HEADERS);	
			
		List<String> userLists = (List) http_headers.get("Username");
		List<String> passLists = (List) http_headers.get("Password");
		
		Properties props = new Properties();
		
    	   	 
    	JSONArray arr = new JSONArray();
    	
    	ArrayList<String> list = new ArrayList<String>();
		
		String username = "";
		String password = "";
		
		
		
		
		if(userLists != null){
			username = userLists.get(0);
			
		}
		
		if(passLists != null){
			password = passLists.get(0);
			
		}
		
		if(Authenticate(username,password)){
			
			logger__.info(cal.getTime() + " :  " + username + " " + "enquiring information about storage nodes." );
			
			props.load(new FileInputStream("../../Resources/Credentials/S3Credentials.properties"));
			
			Map<String,String> db = new HashMap<String,String>();
			
			for (String key : props.stringPropertyNames()) {
				   db.put(key, props.get(key).toString());
				}
			
			for(int i = 1; i<=db.size();i++){
				JSONObject obj = new JSONObject();
				
				String nodeID = Integer.toString(i);
				
				
				String any = db.get(nodeID);
				int anyLength = any.length();
				
				
				
				StringBuilder sb = new StringBuilder(any);
				sb.deleteCharAt(0);
				sb.deleteCharAt(anyLength-2);
				
				System.out.println(sb);
				String[] sbArray = sb.toString().split(",");
				
				String nodeEndpoint = sbArray[0];
				String provider = sbArray[1].replaceAll("\\s+","");
				String identity = sbArray[2].replaceAll("\\s+","");
				String credential = sbArray[3].replaceAll("\\s+","");
				
				obj.put("StorageNodeId", nodeID);
		    	obj.put("storageEndpoint", nodeEndpoint);
		    	obj.put("storageType", provider);
		    	arr.add(obj);
				
			}
			
	    	
	    	if(arr!=null){
	    		for(int i = 0; i<arr.size();i++){
		  	  		list.add(arr.get(i).toString());
		  	  	}
			}
					
		}
		
		else{
			System.out.println("User Invalid");
		}
		logger__.info(cal.getTime() + " :  " + username + " " + "enquiring information about storage nodes." + "get values" + list);
		return list;
		
	}
	
	
	/*
	 * <<< A Web Method to get the list of buckets at storage nodes.>>>
	 * <<< Arguments : Node Identifier : 1,2,3,4 >>> 
	 * <<< Returns a list of buckets at storage nodes>>> 
	 * @author : nishant.dps.uibk.ac.at
	 */
	
	@Override
	@WebMethod
	public ArrayList<String> getBucketList(String nodeId) throws FileNotFoundException, IOException{
		
		MessageContext mctx = wsctx.getMessageContext();		
		Map http_headers = (Map) mctx.get(MessageContext.HTTP_REQUEST_HEADERS);
			
		List<String> userLists = (List) http_headers.get("Username");
		List<String> passLists = (List) http_headers.get("Password");
		
		String username = "";
		String password = "";
		
		
		if(userLists != null){
			username = userLists.get(0);
			
		}
		
		if(passLists != null){
			password = passLists.get(0);
			
		}
		
	
		repoS3 = new NodeCredential(nodeId);
		ArrayList<String> list = new ArrayList<String>();
		
		if(Authenticate(username,password)){
			
			logger__.info(cal.getTime() + " :  " + username + " " + "enquiring information about buckets at storage node" + " " + nodeId);
			
			try{
				
				JSONArray bucket = repoS3.listBucket();
				if(bucket!=null){
		    		for(int i = 0; i<bucket.size();i++){
			  	  		list.add(bucket.get(i).toString());
			  	  	}
				
				}	
				
			}
			catch(Exception e){
				e.printStackTrace();
			}finally{
				repoS3.cleanup();
			}
			
		}else{
			System.out.println("User Invalid");
		}
		logger__.info(cal.getTime() + " :  " + username + " " + "enquiring information about buckets at storage nodes." + "get values" + list);
		return list;
		
	}
	
	
	/*
	 * <<< A Web Method to get the list of VMI stored in a bucket at a storage node.>>>
	 * <<< Arguments : Node Identifier : 1,2,3,4 >>> 
	 * <<< Returns a list of VMI in a bucket at storage nodes>>> 
	 * @author : nishant.dps.uibk.ac.at
	 */
	
	@Override
	@WebMethod
	public ArrayList<String> getBucketVMIList(String nodeId, String bucketName) throws FileNotFoundException, IOException{
		
		MessageContext mctx = wsctx.getMessageContext();		
		Map http_headers = (Map) mctx.get(MessageContext.HTTP_REQUEST_HEADERS);
			
		List<String> userLists = (List) http_headers.get("Username");
		List<String> passLists = (List) http_headers.get("Password");
		
		String username = "";
		String password = "";
		

		if(userLists != null){
			username = userLists.get(0);
			
		}
		
		if(passLists != null){
			password = passLists.get(0);
			
		}
		
	
		repoS3 = new NodeCredential(nodeId);
		ArrayList<String> list = new ArrayList<String>();
		
		if(Authenticate(username,password)){
			
			logger__.info(cal.getTime() + " :  " + username + " " + "enquiring information about bucket" + " " + bucketName  + " at storage node" + " " + nodeId  );
			
			try{
				
				JSONArray bucket = repoS3.listBucketVMI(bucketName);
				if(bucket!=null){
		    		for(int i = 0; i<bucket.size();i++){
			  	  		list.add(bucket.get(i).toString());
			  	  	}
				
				}	
				
			}
			catch(Exception e){
				e.printStackTrace();
			}finally{
				repoS3.cleanup();
			}
			
		}else{
			System.out.println("User Invalid");
		}
		logger__.info(cal.getTime() + " :  " + username + " " + "enquiring information about bucket" + " " + bucketName  + " at storage node" + " " + nodeId + "" + "get values" + list);
		return list;
		
	}
	
	
	
	/*
	 * <<< A Web-method to facilitate upload of unoptimized images.>>>
	 * <<< Returns a list : with storage node ID, VM-Image name, Bucket name and URI corresponding to uploaded VMI.>>>
	 * @author : nishant.dps.uibk.ac.at
	 */
	
	@Override
	@WebMethod	
	public ArrayList<String> receiveVMImage(byte[] vmImage, String vmImageName, int bytesRead, long vmiLength, String nodeId) throws FileNotFoundException, IOException, URISyntaxException  {
				
		MessageContext mctx = wsctx.getMessageContext();		
		Map http_headers = (Map) mctx.get(MessageContext.HTTP_REQUEST_HEADERS);
			
		List<String> userLists = (List) http_headers.get("Username");
		List<String> passLists = (List) http_headers.get("Password");
		
		String username = "";
		String password = "";
		
		URI vmiURI;
		 
		
		JSONObject obj = new JSONObject();   	 
    	JSONArray arr = new JSONArray();
    	
    	ArrayList<String> list = new ArrayList<String>();
		
		int sizeStatus = bytesRead;
		
		if(userLists != null){
			username = userLists.get(0);
			
		}
		
		if(passLists != null){
			password = passLists.get(0);
			
		}
		
		String vmiBucketName = username.toLowerCase()+ "-entice";
		
		
		if(Authenticate(username,password)){
			
			long startTime = System.currentTimeMillis();
	        long elapsedTime = 0L ;
			
			System.out.println("User Valid");
			long r = upload( vmImage,  vmImageName,  bytesRead);
			System.out.println("The file length is :" + r);
			
			if(r==vmiLength){
				
							
				logger__.info(cal.getTime() + " :  " + username + " " + "initiated uploading Unoptimized Image" +  " " + vmImageName + " " +  " in bucket" + " " + vmiBucketName  + "  at storage node" + " " + nodeId  );
				
				uploadVMIAsObjectStorage(nodeId, vmImageName, vmiBucketName);
				
				elapsedTime = (new Date()).getTime() - startTime;
				long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
				
			    if(repoS3.checkifVMIExists(vmiBucketName, vmImageName)){
			    	
			    	logger__.info(cal.getTime() + " :  " + username + " " + "uploaded Unoptimized Image" + " " + vmImageName + " " +  " successfully, in bucket"  + " " + vmiBucketName  + "  at storage node" + " " + nodeId  );
			    	vmiURI = repoS3.getFileURI(vmiBucketName, vmImageName);
			    	
			
					obj.put("StorageNodeId", nodeId );				
			    	obj.put("vmiName", vmImageName);
			    	obj.put("bucketName", vmiBucketName);
			    	obj.put("vmiURI", vmiURI);
			    	obj.put("UploadUnoptimizedTimeInSeconds", timeSeconds);
			    	
			    	arr.add(obj);
			    	
			    	if(arr!=null){
			    		for(int i = 0; i<arr.size();i++){
				  	  		list.add(arr.get(i).toString());
				  	  	}
					}
			    	
			    	
			    }
			    
			    else{
			    	
			    	logger__.error(cal.getTime() + " : " + username + "  failed uploading Unoptimized Image" + " " + vmImageName + "  at " + "Storage Node" + nodeId);
			    	String response = "Upload of VMI Failed : Please try again ! The problem has been detected and reported.";
			    	obj.put("VMIUploadResponse", response);
			    	arr.add(obj);
			    	
			    	if(arr!=null){
			    		for(int i = 0; i<arr.size();i++){
				  	  		list.add(arr.get(i).toString());
				  	  	}
					}
			    	
			    }
			
		    	cleanVMI(vmImageName);
			}
			
		}
		
		else{
			System.out.println("User Invalid");
		}
				
		logger__.info(cal.getTime() + " :  " + username + " " + "upload response of Unoptimized Image" + " " + vmImageName + " " +  "  at storage node" + " " + nodeId + "" + "with values" + list );		
		return list;
	}
	
	
	
	/*
	 * <<< A Web-method to facilitate upload of unoptimized images via URI.>>>
	 * <<< Returns a list : with new storage node ID, VM-Image name, Bucket name and new URI corresponding to uploaded optimized VMI.>>>
	 * @author : nishant.dps.uibk.ac.at
	 */
	
	
	@Override
	@WebMethod
	public ArrayList<String> receiveOptimizedVMImage(URI uri, String vmImageNameOld, String vmImageNameNew, String nodeIdOld, String nodeIdNew) throws FileNotFoundException, IOException, URISyntaxException{
		
		
		
		MessageContext mctx = wsctx.getMessageContext();		
		Map http_headers = (Map) mctx.get(MessageContext.HTTP_REQUEST_HEADERS);
			
		List<String> userLists = (List) http_headers.get("Username");
		List<String> passLists = (List) http_headers.get("Password");
		
		String username = "";
		String password = "";
		
		URI vmiURI;
		
		JSONObject obj = new JSONObject();   	 
    	JSONArray arr = new JSONArray();
		
		ArrayList<String> list = new ArrayList<String>();
		
			
		if(userLists != null){
			username = userLists.get(0);
			
		}
		
		if(passLists != null){
			password = passLists.get(0);
			
		}
		
		String vmiBucketName = username.toLowerCase() + "-entice";
		

		if(Authenticate(username,password)){
			
			long startTime = System.currentTimeMillis();
	        long elapsedTime = 0L ;
			
			System.out.println("User Valid");	
						
			downloadVMIViaURI(uri, new File("../../Resources/UPLOAD/" + vmImageNameNew));
			
			
							
			logger__.info(cal.getTime() + " :  " + username + " " + "initiated uploading optimized Image  " + vmImageNameNew +"   in bucket" + " " + vmiBucketName  + "  at storage node" + " " + nodeIdNew  );
			uploadVMIAsObjectStorage(nodeIdNew, vmImageNameNew, vmiBucketName);
			
			elapsedTime = (new Date()).getTime() - startTime;
			long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
				
			if(repoS3.checkifVMIExists(vmiBucketName, vmImageNameNew)){
					
					logger__.info(cal.getTime() + " :  " + username + " " + "uploaded optimized Image  " + vmImageNameNew +"   successfully in bucket" + " " + vmiBucketName  + "  at storage node" + " " + nodeIdNew  );
					
					vmiURI = repoS3.getFileURI(vmiBucketName, vmImageNameNew);
					
					
					obj.put("StorageNodeId", nodeIdNew);				
			    	obj.put("vmiName", vmImageNameNew);
			    	obj.put("bucketName", vmiBucketName);
			    	obj.put("vmiURI", vmiURI);
			    	obj.put("UploadOptimizedTimeInSeconds", timeSeconds);
			    	
			    	arr.add(obj);
			    	
			    	if(arr!=null){
			    		for(int i = 0; i<arr.size();i++){
				  	  		list.add(arr.get(i).toString());
				  	  	}
					}
				}
				
				else{
					
					logger__.error(cal.getTime() + " :  " + username + " " + "Failed uploading optimized Image  " + vmImageNameNew +"   in bucket" + " " + vmiBucketName  + "  at storage node" + " " + nodeIdNew );
			    	String response = "Upload of Optimized VMI Failed : Please try again ! The problem has been detected and reported.";
			    	obj.put("VMIUploadResponse", response);
			    	arr.add(obj);
			    	
			    	if(arr!=null){
			    		for(int i = 0; i<arr.size();i++){
				  	  		list.add(arr.get(i).toString());
				  	  	}
					}
			    	
			    }
					    	
		    	cleanVMI(vmImageNameNew);
			
			
		}
		
		else{
			System.out.println("User Invalid");
		}
				
		
		logger__.info(cal.getTime() + " :  " + username + " " + "uploade response of optimized Image  " + vmImageNameNew + "  at storage node" + " " + nodeIdNew + "" + "withi values" + list);
		return list;
		
	}
	
	/*
	 * <<< A Web-method to facilitate upload of optimized images via URI.>>>
	 * <<< Returns a list : with new storage node ID, VM-Image name, Bucket name and new URI corresponding to uploaded optimized VMI.>>>
	 * @author : nishant.dps.uibk.ac.at
	 */
	
	@Override
	@WebMethod
	public ArrayList<String> uploadOptimizedVMImage(URI uri, String vmImageNameOld, String vmImageNameNew, String nodeIdOld, String nodeIdNew) throws FileNotFoundException, IOException, URISyntaxException{
		
		
		
		MessageContext mctx = wsctx.getMessageContext();		
		Map http_headers = (Map) mctx.get(MessageContext.HTTP_REQUEST_HEADERS);
			
		List<String> userLists = (List) http_headers.get("Username");
		List<String> passLists = (List) http_headers.get("Password");
		
		String username = "";
		String password = "";
		
		URI vmiURI;
		
		JSONObject obj = new JSONObject();   	 
    	JSONArray arr = new JSONArray();
		
		ArrayList<String> list = new ArrayList<String>();
		
			
		if(userLists != null){
			username = userLists.get(0);
			
		}
		
		if(passLists != null){
			password = passLists.get(0);
			
		}
		
		String vmiBucketName = username.toLowerCase() + "-entice";
		

		if(Authenticate(username,password)){
			
			long startTime = System.currentTimeMillis();
	        long elapsedTime = 0L ;
			
			System.out.println("User Valid");	
						
			downloadVMIViaURI(uri, new File("../../Resources/UPLOAD/" + vmImageNameNew));
			
			
							
			logger__.info(cal.getTime() + " :  " + username + " " + "initiated uploading optimized Image  " + vmImageNameNew +"   in bucket" + " " + vmiBucketName  + "  at storage node" + " " + nodeIdNew  );
			uploadVMIAsObjectStorage(nodeIdNew, vmImageNameNew, vmiBucketName);
			
			elapsedTime = (new Date()).getTime() - startTime;
			long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
				
			if(repoS3.checkifVMIExists(vmiBucketName, vmImageNameNew)){
					
					logger__.info(cal.getTime() + " :  " + username + " " + "uploaded optimized Image  " + vmImageNameNew +"   successfully in bucket" + " " + vmiBucketName  + "  at storage node" + " " + nodeIdNew  );
					
					vmiURI = repoS3.getFileURI(vmiBucketName, vmImageNameNew);
					
					
					obj.put("StorageNodeId", nodeIdNew);				
			    	obj.put("vmiName", vmImageNameNew);
			    	obj.put("bucketName", vmiBucketName);
			    	obj.put("vmiURI", vmiURI);
			    	obj.put("UploadOptimizedTimeInSeconds", timeSeconds);
			    	
			    	arr.add(obj);
			    	
			    	if(arr!=null){
			    		for(int i = 0; i<arr.size();i++){
				  	  		list.add(arr.get(i).toString());
				  	  	}
					}
				}
				
				else{
					
					logger__.error(cal.getTime() + " :  " + username + " " + "Failed uploading optimized Image  " + vmImageNameNew +"   in bucket" + " " + vmiBucketName  + "  at storage node" + " " + nodeIdNew );
			    	String response = "Upload of Optimized VMI Failed : Please try again ! The problem has been detected and reported.";
			    	obj.put("VMIUploadResponse", response);
			    	arr.add(obj);
			    	
			    	if(arr!=null){
			    		for(int i = 0; i<arr.size();i++){
				  	  		list.add(arr.get(i).toString());
				  	  	}
					}
			    	
			    }
					    	
		    	cleanVMI(vmImageNameNew);
			
			
		}
		
		else{
			System.out.println("User Invalid");
		}
				
		
		logger__.info(cal.getTime() + " :  " + username + " " + "uploade response of optimized Image  " + vmImageNameNew + "  at storage node" + " " + nodeIdNew + "" + "withi values" + list);
		return list;
		
	}
	
	
	/*
	 * <<< A Web-method to facilitate upload of image fragments from local storage.>>>
	 * <<< Returns a list : with new storage node ID, VM-Image name, Bucket name and new URI corresponding to uploaded optimized VMI.>>>
	 * @author : nishant.dps.uibk.ac.at
	 */
	
	
	@Override
	@WebMethod	
	public ArrayList<String> receiveVMImageFragments(byte[] vmImage, String vmImageName, int bytesRead, long vmiLength, String nodeId) throws FileNotFoundException, IOException, URISyntaxException  {
				
		MessageContext mctx = wsctx.getMessageContext();		
		Map http_headers = (Map) mctx.get(MessageContext.HTTP_REQUEST_HEADERS);
			
		List<String> userLists = (List) http_headers.get("Username");
		List<String> passLists = (List) http_headers.get("Password");
		
		String username = "";
		String password = "";
		
		URI vmiURI;
		 
		
		JSONObject obj = new JSONObject();   	 
    	JSONArray arr = new JSONArray();
    	
    	ArrayList<String> list = new ArrayList<String>();
		
		int sizeStatus = bytesRead;
		
		if(userLists != null){
			username = userLists.get(0);
			
		}
		
		if(passLists != null){
			password = passLists.get(0);
			
		}
		
		String vmiBucketName = username.toLowerCase() + "-entice";
		
		
		if(Authenticate(username,password)){
			
			long startTime = System.currentTimeMillis();
	        long elapsedTime = 0L ;
			
			System.out.println("User Valid");
			long r = upload( vmImage,  vmImageName,  bytesRead);
			System.out.println("The file length is :" + r);
			
			if(r==vmiLength){
				
							
				logger__.info(cal.getTime() + " :  " + username + " " + "initiated uploading Unoptimized Image" +  " " + vmImageName + " " +  " in bucket" + " " + vmiBucketName  + "  at storage node" + " " + nodeId  );
				
				uploadVMIAsObjectStorage(nodeId, vmImageName, vmiBucketName);
				
				elapsedTime = (new Date()).getTime() - startTime;
				long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
				
			    if(repoS3.checkifVMIExists(vmiBucketName, vmImageName)){
			    	
			    	logger__.info(cal.getTime() + " :  " + username + " " + "uploaded Unoptimized Image" + " " + vmImageName + " " +  " successfully, in bucket"  + " " + vmiBucketName  + "  at storage node" + " " + nodeId  );
			    	vmiURI = repoS3.getFileURI(vmiBucketName, vmImageName);
			    	
			
					obj.put("StorageNodeId", nodeId );				
			    	obj.put("vmiFragmentName", vmImageName);
			    	obj.put("bucketName", vmiBucketName);
			    	obj.put("vmiFragmentURI", vmiURI);
			    	obj.put("UploadFragmentTimeInSeconds", timeSeconds);
			    	
			    	arr.add(obj);
			    	
			    	if(arr!=null){
			    		for(int i = 0; i<arr.size();i++){
				  	  		list.add(arr.get(i).toString());
				  	  	}
					}
			    	
			    	
			    }
			    
			    else{
			    	
			    	logger__.error(cal.getTime() + " : " + username + "  failed uploading Unoptimized Image" + " " + vmImageName + "  at " + "Storage Node" + nodeId);
			    	String response = "Upload of VMI Failed : Please try again ! The problem has been detected and reported.";
			    	obj.put("VMIUploadResponse", response);
			    	arr.add(obj);
			    	
			    	if(arr!=null){
			    		for(int i = 0; i<arr.size();i++){
				  	  		list.add(arr.get(i).toString());
				  	  	}
					}
			    	
			    }
			
		    	cleanVMI(vmImageName);
			}
			
		}
		
		else{
			System.out.println("User Invalid");
		}
				
		logger__.info(cal.getTime() + " :  " + username + " " + "upload response of Unoptimized Image" + " " + vmImageName + " " +  "  at storage node" + " " + nodeId + "" + "with values" + list );		
		return list;
	}
	
	/*
	 * <<< A Web-method to facilitate upload of image fragments via URI.>>>
	 * <<< Returns a list : with new storage node ID, VM-Image name, Bucket name and new URI corresponding to uploaded optimized VMI.>>>
	 * @author : nishant.dps.uibk.ac.at
	 */
	
	
	@Override
	@WebMethod
	public ArrayList<String> receiveVMImageFragmentsviaURI(URI uri, String vmImageNameNew, String nodeIdOld, String nodeIdNew) throws FileNotFoundException, IOException, URISyntaxException{
		
		
		
		MessageContext mctx = wsctx.getMessageContext();		
		Map http_headers = (Map) mctx.get(MessageContext.HTTP_REQUEST_HEADERS);
			
		List<String> userLists = (List) http_headers.get("Username");
		List<String> passLists = (List) http_headers.get("Password");
		
		String username = "";
		String password = "";
		
		URI vmiURI;
		
		JSONObject obj = new JSONObject();   	 
    	JSONArray arr = new JSONArray();
		
		ArrayList<String> list = new ArrayList<String>();
		
			
		if(userLists != null){
			username = userLists.get(0);
			
		}
		
		if(passLists != null){
			password = passLists.get(0);
			
		}
		
		String vmiBucketName = username.toLowerCase() + "-entice";
		

		if(Authenticate(username,password)){
			
			long startTime = System.currentTimeMillis();
	        long elapsedTime = 0L ;
			
			System.out.println("User Valid");	
						
			downloadVMIViaURI(uri, new File("../../Resources/UPLOAD/" + vmImageNameNew));
			
			
							
			logger__.info(cal.getTime() + " :  " + username + " " + "initiated uploading optimized Image  " + vmImageNameNew +"   in bucket" + " " + vmiBucketName  + "  at storage node" + " " + nodeIdNew  );
			uploadVMIAsObjectStorage(nodeIdNew, vmImageNameNew, vmiBucketName);
			
			elapsedTime = (new Date()).getTime() - startTime;
			long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
				
			if(repoS3.checkifVMIExists(vmiBucketName, vmImageNameNew)){
					
					logger__.info(cal.getTime() + " :  " + username + " " + "uploaded optimized Image  " + vmImageNameNew +"   successfully in bucket" + " " + vmiBucketName  + "  at storage node" + " " + nodeIdNew  );
					
					vmiURI = repoS3.getFileURI(vmiBucketName, vmImageNameNew);
					
					
					obj.put("StorageNodeId", nodeIdNew);				
			    	obj.put("vmiFragmentName", vmImageNameNew);
			    	obj.put("bucketName", vmiBucketName);
			    	obj.put("vmiFragmentURI", vmiURI);
			    	obj.put("UploadFragmentTimeInSeconds", timeSeconds);
			    	
			    	arr.add(obj);
			    	
			    	if(arr!=null){
			    		for(int i = 0; i<arr.size();i++){
				  	  		list.add(arr.get(i).toString());
				  	  	}
					}
				}
				
				else{
					
					logger__.error(cal.getTime() + " :  " + username + " " + "Failed uploading optimized Image  " + vmImageNameNew +"   in bucket" + " " + vmiBucketName  + "  at storage node" + " " + nodeIdNew );
			    	String response = "Upload of Optimized VMI Failed : Please try again ! The problem has been detected and reported.";
			    	obj.put("VMIUploadResponse", response);
			    	arr.add(obj);
			    	
			    	if(arr!=null){
			    		for(int i = 0; i<arr.size();i++){
				  	  		list.add(arr.get(i).toString());
				  	  	}
					}
			    	
			    }
					    	
		    	cleanVMI(vmImageNameNew);
			
			
		}
		
		else{
			System.out.println("User Invalid");
		}
				
		
		logger__.info(cal.getTime() + " :  " + username + " " + "uploade response of optimized Image  " + vmImageNameNew + "  at storage node" + " " + nodeIdNew + "" + "withi values" + list);
		return list;
		
	}
	
	
	/*
	 * Not complete yet
	 * @author : nishant.dps.uibk.ac.at
	 */
	
	@WebMethod
	public ArrayList<String> reDistribution(byte[] reFile, String reFileName, int bytesRead, long reFileLength) throws org.json.simple.parser.ParseException, URISyntaxException, FileNotFoundException, IOException{
	  	
		MessageContext mctx = wsctx.getMessageContext();		
		Map http_headers = (Map) mctx.get(MessageContext.HTTP_REQUEST_HEADERS);
			
		List<String> userLists = (List) http_headers.get("Username");
		List<String> passLists = (List) http_headers.get("Password");
		
		String username = "";
		String password = "";
		
		URI redistributionURI;
		ArrayList<String> redistributionList = new ArrayList<String>();
		
		int sizeStatus = bytesRead;
		   	 
    	JSONArray reArr = new JSONArray();
		
		if(userLists != null){
			username = userLists.get(0);
			
		}
		
		if(passLists != null){
			password = passLists.get(0);
			
		}
		
		if(Authenticate(username,password)){
			
			long startTime = System.currentTimeMillis();
	        long elapsedTime = 0L ;
	        
	        System.out.println("User Valid");
	        
	        
	        long r = uploadRedistributionFile(reFile, reFileName, bytesRead);
	        
			System.out.println("The file length is :" + r);
			
			if(r==reFileLength){
				File file = new File("../../Resources/Redistribution/" + reFileName);
				
				JSONParser parser = new JSONParser();
				
				
				try {
			  		
			  		Object obj = parser.parse(new FileReader(file));
					
					JSONArray arrR = (JSONArray) obj;
					
					
					
					
					for(int i = 0; i<arrR.size();i++){
						
						
						
						JSONObject obj1 = new JSONObject();
						obj1 = (JSONObject) arrR.get(i);
						
			           

			            String vmiName = (String) obj1.get("vmiName");
			           
			            
			            String vmiURI = (String) obj1.get("vmiURI");
			           
			            
			            String sourceId = (String) obj1.get("sourceId");
			           
			            
			            
			            final URI vmiUri = new URI(vmiURI);
				  	
				  		
				  		final String bucketName = username.toLowerCase() + "-entice";
			            
				  		
				  		downloadVMIViaURI(vmiUri, new File("../../Resources/UPLOAD/" + vmiName));
				  		deleteUnoptimizedVMI(sourceId,vmiName,username);
				  		System.out.println("The VMI" + vmiName + " is deleted from old node " + sourceId);
			
			            JSONArray destinationId = (JSONArray) obj1.get("destinationId");
			            Iterator<String> iterator = destinationId.iterator();
			            
			            while (iterator.hasNext()) {
			            	String identifier = iterator.next();
			                System.out.println("Now uploading on node " + identifier);
			                
			                try{
			  	    			
			  	    			uploadVMIAsObjectStorage(identifier, vmiName, bucketName);
			  	    			
			  	    			elapsedTime = (new Date()).getTime() - startTime;
			  	  			    long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
			  	  			    JSONObject reObj = new JSONObject();
			  	    			
			  	    			if(repoS3.checkifVMIExists(bucketName, vmiName)){
			  						
			  						logger__.info(cal.getTime() + " :  " + username + " " + "uploaded optimized Image  " + vmiName +"   successfully in bucket" + " " + bucketName  + "  at storage node" + " " + identifier  );
			  						
			  						redistributionURI = repoS3.getFileURI(bucketName, vmiName);
			  						
			  						
			  						reObj.put("StorageNodeId", identifier);				
			  				    	reObj.put("vmiName", vmiName);
			  				    	reObj.put("bucketName", bucketName);
			  				    	reObj.put("vmiredistributionURI", redistributionURI);
			  				    	
			  				    	
			  				    	reObj.put("redistributionTimeInSeconds", timeSeconds);
			  				    	
			  				    	reArr.add(reObj);
			  				    	
			  				    	
			  				    	/*
			  				    	if(arr!=null){
			  				    		for(int i = 0; i<arr.size();i++){
			  					  	  		list.add(arr.get(i).toString());
			  					  	  	}
			  						}*/
			  					}
			  					
			  					else{
			  						
			  						logger__.error(cal.getTime() + " :  " + username + " " + "Failed uploading optimized Image  " + vmiName +"   in bucket" + " " + bucketName  + "  at storage node" + " " + identifier );
			  				    	
			  						reObj.put("StorageNodeId", identifier);				
			  				    	reObj.put("vmiName", vmiName);
			  				    	reObj.put("bucketName", null);
			  				    	reObj.put("vmiredistributionURI", null);
			  				    	reObj.put("redistributionTimeInSeconds", null);
			  						
			  						
			  						//String response = "Upload of Optimized VMI Failed : Please try again ! The problem has been detected and reported.";
			  				    	//obj.put("VMIUploadResponse", response);
			  				    	
			  				    	reArr.add(reObj);
			  				    	
			  				    	/*
			  				    	if(arr!=null){
			  				    		for(int i = 0; i<arr.size();i++){
			  					  	  		list.add(arr.get(i).toString());
			  					  	  	}
			  						}*/
			  				    	
			  				    }
			  	    			
			      	            
			  	    		}catch(Exception e){
			  	    			e.printStackTrace();
			  	    		}finally{
			  	    			//node2.cleanup();
			  	    		}
			            }
			            
			            cleanVMI(vmiName);
					
					
					} 	
					
					cleanFile(reFileName);
					
					if(reArr!=null){
			    		for(int i = 0; i<reArr.size();i++){
				  	  		redistributionList.add(reArr.get(i).toString());
				  	  	}
					}

			  	} catch (FileNotFoundException e) {
			  		e.printStackTrace();
			  	} catch (IOException e) {
			  		e.printStackTrace();
			  	}
				
			}
			
			

		  	
			
		  	
		  	
		}else{
			System.out.println("User Invalid");
		}
		
		return redistributionList;
		
			  		  	
	  }
	
	
	/*
	 * <<< A Local Upload Method called for redistribution>>>
	 * @author : nishant.dps.uibk.ac.at
	 */
	
	public long uploadRedistributionFile(byte[] reFile, String reFileName, int bytesRead){
		String reFileStoragePath = "../../Resources/Redistribution/" + reFileName;
		FileOutputStream out = null;
		File uploadFile = new File(reFileStoragePath);
			
		try{
			
				out = new FileOutputStream(reFileStoragePath,true);			
				out.write(reFile,0, bytesRead);	
				System.out.println("The number of bytes received: " + bytesRead);
				
				out.flush();
				out.close();
					
			
		} catch(IOException e){
			System.err.println(e);
			throw new WebServiceException(e);
		} 
		System.out.println("The received redistribution File: " + reFileStoragePath);
		System.out.println("The received file current size: " + uploadFile.length());
		return uploadFile.length();
		
		
		
	}
	
	
	
	
	/*
	 * <<< A Local Upload Method called by web-method receiveVMImage()>>>
	 * @author : nishant.dps.uibk.ac.at
	 */
		
	public long upload(byte[] vmImage, String vmImageName, int bytesRead){
		String vmImageStoragePath = "../../Resources/UPLOAD/" + vmImageName;
		FileOutputStream out = null;
		File uploadFile = new File(vmImageStoragePath);
			
		try{
			
				out = new FileOutputStream(vmImageStoragePath,true);			
				out.write(vmImage,0, bytesRead);	
				System.out.println("The number of bytes received: " + bytesRead);
				
				out.flush();
				out.close();
					
			
		} catch(IOException e){
			System.err.println(e);
			throw new WebServiceException(e);
		} 
		System.out.println("The received VM Image: " + vmImageStoragePath);
		System.out.println("The received VM Image current size: " + uploadFile.length());
		return uploadFile.length();
	}
	
	
	/*
	 * <<< A local Upload as Object storage method called by receiveVMImage()>>>
	 * @author : nishant.dps.uibk.ac.at
	 */
	
	public void uploadVMIAsObjectStorage(String nodeId, String vmImageName, String username) throws FileNotFoundException, IOException{
		repoS3 = new NodeCredential(nodeId);
		try {
            repoS3.multipartUpload(new File("../../Resources/UPLOAD/" + vmImageName), username);
            repoS3.getVMIPublicAccess(nodeId, username, vmImageName);
        } catch(Throwable e){
        	
        	logger__.error(cal.getTime() + " :  " + username + " " + "Failed uploading Image  " + vmImageName + " " + "  at storage node" + " " + nodeId, e );
        	e.printStackTrace();
        }finally {
            repoS3.cleanup();
        }
    }
	
	/*
	 * <<< A local method to delete VMI called by receiveOptimizedVMImage()>>>
	 * @author : nishant.dps.uibk.ac.at
	 */
	
	public void deleteUnoptimizedVMI(String nodeId, String vmImageName, String username) throws FileNotFoundException, IOException{
		
		repoS3 = new NodeCredential(nodeId);
		final String bucketName = username.toLowerCase() + "-entice";
		
		try{
			repoS3.deleteVMI(bucketName, vmImageName);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			repoS3.cleanup();
		}
	}
	
	
	/*
	 * <<< A local method to delete the uploaded redistribution file from local folder >>>
	 * @author : nishant.dps.uibk.ac.at 
	 */
	
	public void cleanFile(String fileName){
		
		String vmImageStoragePath = "../../Resources/Redistribution/" + fileName;
		FileOutputStream out = null;
		File previousVMI = new File(vmImageStoragePath);
		
		try{
			previousVMI.delete();		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	/*
	 * <<< A local method to delete the uploaded VMI as object storage from local folder >>>
	 * @author : nishant.dps.uibk.ac.at 
	 */
	
	public void cleanVMI(String vmImageName){
		
		String vmImageStoragePath = "../../Resources/UPLOAD/" + vmImageName;
		FileOutputStream out = null;
		File previousVMI = new File(vmImageStoragePath);
		
		try{
			previousVMI.delete();		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public URI parserURL(URI aURL) throws URISyntaxException, MalformedURLException{
		
		
		String protocol =  "http";
        String authority = aURL.getAuthority();
        String host = aURL.getHost();
        int port = aURL.getPort();
        String path = aURL.getPath();
        String query = aURL.getQuery();
        
        
        String externalAuthority = "";
        
        URI externalURI = new URI(protocol + "://" + externalAuthority + path);
		
		return externalURI;
	}
	
	
	/*
	 * <<< A Web-method to track the location of Stored VMI>>
	 * <<< Arguments : URI of VMI of String type and VMI Image name of String Type >>>
	 * <<< Returns List with VM Image name, URI, Country Code, Country Name, Region, City,>>> 
	 * <<< Postal Code, Longitude, Latitude, Time Zone, Distance from Storage to closest RepoGuard Web server >>> 
	 * @author : nishant.dps.uibk.ac.at
	 */
	
	@Override
	@WebMethod
	public ArrayList<String> trackLocation(String uri, String vmiName) throws FileNotFoundException, IOException, URISyntaxException{
		
		
		MessageContext mctx = wsctx.getMessageContext();		
		Map http_headers = (Map) mctx.get(MessageContext.HTTP_REQUEST_HEADERS);
			
		List<String> userLists = (List) http_headers.get("Username");
		List<String> passLists = (List) http_headers.get("Password");
		
		String username = "";
		String password = "";
		
		
		JSONObject obj = new JSONObject();
    	JSONArray arr = new JSONArray();
    	
    	ArrayList<String> list = new ArrayList<String>();
    	
    	if(userLists != null){
			username = userLists.get(0);
			
		}
		
		if(passLists != null){
			password = passLists.get(0);
			
		}
       	
		
		if(Authenticate(username,password)){
			
			
			try{
	    		
				logger__.info(cal.getTime() + ":" + username + "Tracked Location for VMI" + vmiName + "with URI" + uri);
		    	
	    		System.out.println("The VMI URI" + uri);
	    		 		
	    		String vmiIP = getVMIIP(uri);
	    		
	    		System.out.println("The VMI ip is :" + vmiIP);
	    		   		    	   		
	    		/*
	    		 * <<<Instantiating Look UP Service.>>>
	    		 */
	    		
	        	LookupService cl = new LookupService("../../Resources/geo/GeoLiteCity.dat", LookupService.GEOIP_MEMORY_CACHE );   
	        	
	        	/*
	        	 * <<<l1 denotes storage location of VMI.>>>
	        	 * <<<l2 denotes location of RepoGuard web-server.>>>
	        	 */
	        	
	        	Location l1;
	        	
	        	
	        	
	        	
	        	l1 = cl.getLocation(vmiIP);
	        	
	        	
	        	
	        	
	        	/*
	        	 * <<<Putting different location key-fields and their values as json object.>>>
	        	 */	
	        	
	        	obj.put("vmiName", vmiName);
	        	obj.put("vmiURI", uri);
	        	obj.put("CountryCode", l1.countryCode);
	        	obj.put("CountryName",l1.countryName);
	        	obj.put("Region", l1.region);
	        	obj.put("RegionName", regionName.regionNameByCode(l1.countryCode, l1.region));
	        	obj.put("City", l1.city);
	        	obj.put("PostalCode", l1.postalCode);
	        	obj.put("Latitutde", l1.latitude);
	        	obj.put("Longitude", l1.longitude);
	        	obj.put("TimeZone", timeZone.timeZoneByCountryAndRegion(l1.countryCode, l1.region));
	        	
	      
	        	arr.add(obj);
		    	

		    	if(arr!=null){
		    		for(int i = 0; i<arr.size();i++){
			  	  		list.add(arr.get(i).toString());
			  	  	}
				}
	        	
	                                                                                                                       
	        	cl.close();
	    		
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}
			
		}else{
			System.out.println("User Invalid");
		}
		
    	
    	
    	return list;
	}
	
	
	/*
	 * <<< A local method to convert URL of string type to IP called by web-method trackLocation() >>>
	 * @author : nishant.dps.uibk.ac.at
	 */
	
	public String getVMIIP(String url) throws UnknownHostException, URISyntaxException{
    	
    	String ip;
    	URI uri = new URI(url);
    	
    	System.out.println("The URI inside function " + uri);
    	
    	InetAddress address = InetAddress.getByName(uri.getHost());
    	
    	System.out.println("The address is " + address);
    	
		ip = address.getHostAddress();
		
		System.out.println("The ip is " + ip);
		
		return ip;
		
    }
    
	
	
	
	/*
     * <<< A local method To download a VMI and save a VMI using URI>>>
     * @author : nishant.dps.uibk.ac.at
     */
        
    
    public void downloadVMIViaURI(URI uri, File destination){    	    	 	
    	try(
    		
    		ReadableByteChannel rbc = Channels.newChannel(uri.toURL().openStream());
    		FileOutputStream fos = new FileOutputStream(destination);
    		
    	){
    		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    	} catch(Exception e){
    			e.printStackTrace();   		
    	}
    	
    }
    
	
	/*
	 * <<< A web-method Authenticating the user name and password to access the service.>>>
	 * <<< Arguments : user-name and Password of String type >>>
	 * <<< returns a boolean value true/false >>>
	 * @author : nishant.dps.uibk.ac.at
	 */
	
	@WebMethod
	public boolean Authenticate(String username, String password) throws FileNotFoundException, IOException{
		boolean isAuthenticated = false;
		String saltedPassword = SALT + password;
		String hashedPassword = generateHash(saltedPassword);
		
		properties.load(new FileInputStream("../../Resources/Users/Repo-Guard-Users.properties"));
		
		for (String key : properties.stringPropertyNames()) {
			   DB.put(key, properties.get(key).toString());
			}
		
		String storedPasswordHash = DB.get(username);
		if(hashedPassword.equals(storedPasswordHash)){
			isAuthenticated = true;
		}else{
			
			logger__.error(cal.getTime() + " :  " + username + " : " + "Authentication Error");
			isAuthenticated = false;
		}
		return isAuthenticated;
				
	}
	
	/*
	 * <<< A local method Generating hashed passwords called by Authenticate() web-method >>>
	 * @author : nishant.dps.uibk.ac.at
	 */
	
	public static String generateHash(String input) {
		StringBuilder hash = new StringBuilder();

		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] hashedBytes = md5.digest(input.getBytes());
			char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
					'a', 'b', 'c', 'd', 'e', 'f' };
			for (int idx = 0; idx < hashedBytes.length; ++idx) {
				byte b = hashedBytes[idx];
				hash.append(digits[(b & 0xf0) >> 4]);
				hash.append(digits[b & 0x0f]);
			}
		} catch (NoSuchAlgorithmException e) {
			
		}

		return hash.toString();
	}

	
}