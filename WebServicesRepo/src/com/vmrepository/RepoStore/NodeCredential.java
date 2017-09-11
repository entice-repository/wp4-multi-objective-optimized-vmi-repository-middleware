package com.vmrepository.RepoStore;

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


import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobAccess;
import org.jclouds.blobstore.domain.ContainerAccess;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.io.ContentMetadata;
import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import java.net.URI;

import static org.jclouds.blobstore.options.PutOptions.Builder.multipart;



/*
 * 	<<< Implementation of RepoStore S3 based Storage System functionalities. >>>
 * 	<<< RepoGuard web interface calls these functionalities. >>>
 * 	@author : nishant.dps.uibk.ac.at 
 */


public class NodeCredential {
	
	String nodeEndpoint;
	String provider;
	String identity;
	String credential;
	
	private static final int QUERY_RETRY_INTERVAL_MILLIS = 100;
    
    private final BlobStoreContext ctx;
    private final BlobStore store ;
    private static BlobAccess blobAccess;
    private ContainerAccess containerAccess;
    
    
    
    public static Properties props = new Properties();
    
    
    /*
     * <<< S3 Storage system : End-point, Provider, Identity, Credential. >>>
     * <<< Currently 4 S3 storage systems : >>>
     * <<< 1.  >>>
     * <<< 2.  >>>
     * <<< 3.  >>>
     * <<< 4.  >>>
     * @author : nishant.dps.uibk.ac.at
     */
    
    public NodeCredential(String nodeId) throws FileNotFoundException, IOException {
    	
    	
    	props.load(new FileInputStream("../../Resources/Credentials/S3Credentials.properties"));
		
		Map<String,String> db = new HashMap<String,String>();
		
		for (String key : props.stringPropertyNames()) {
			   db.put(key, props.get(key).toString());
			}
		
		String any = db.get(nodeId);
		int anyLength = any.length();
		
		
		
		StringBuilder sb = new StringBuilder(any);
		sb.deleteCharAt(0);
		sb.deleteCharAt(anyLength-2);
		
		
		String[] sbArray = sb.toString().split(",");
		
		nodeEndpoint = sbArray[0];
		provider = sbArray[1].replaceAll("\\s+","");
		identity = sbArray[2].replaceAll("\\s+","");
		credential = sbArray[3].replaceAll("\\s+","");
		
		
    	Properties overrides = new Properties();
    	overrides.setProperty("jclouds.mpu.parallel.degree", "" + Runtime.getRuntime().availableProcessors());
    	overrides.setProperty("jclouds.mpu.parts.size", "15728640");
    	overrides.setProperty(org.jclouds.Constants.PROPERTY_STRIP_EXPECT_HEADER, "true");
        overrides.setProperty(org.jclouds.Constants.PROPERTY_MAX_RETRIES, "100");
    	
    	/*
    	 *  SLF4JLoggingModule logging = new SLF4JLoggingModule();   	
 		   .modules(ImmutableSet.of(new OkHttpCommandExecutorServiceModule()))
		   .modules(ImmutableSet.of(new SLF4JLoggingModule()))
    	 */
    	
        
        ctx = ContextBuilder.newBuilder(provider).endpoint(nodeEndpoint).credentials(identity, credential)
        			.overrides(overrides)
        			.buildView(BlobStoreContext.class);
               
        store = ctx.getBlobStore();
        ctx.unwrap();
        
        
                
    }
    
    
    /*
     * <<<Creating Containers or Buckets at a S3 storage. Input : Container Name. Before creating container, checks if it exists.>>>
     * @author : nishant.dps.uibk.ac.at
     */
   
    public void createContainer(String containerName){ 	
    	
    	if(store.containerExists(containerName)){
    		System.out.println("The container with name " + " " + containerName + " " + "exists");
    	}else{
    		store.createContainerInLocation(null, containerName);   				
    		System.out.println("The container with name " + " " + containerName + " " + "created");
    	}
    	
    }
    
    /*
     * <<<Normal Upload VMI as Object to S3 storage for a file less than 5 GB * Input : VMI Path + Name , Container name>>>
     * @author : nishant.dps.uibk.ac.at
     */
    
    public void uploadVMIAsObject(File file, String containerName) throws IOException, InterruptedException, ExecutionException{
                    
        long fileSize = file.length();
        System.out.format("Starting upload of %d bytes%n", fileSize);
        final String filename = file.getName();
        
        
        createContainer(containerName);
    	store.putBlob(containerName, store.blobBuilder(filename).payload(file).build());
    }
    
    
    /*
     * <<<Multiple-part upload to the repository for any VMI greater than size 5 GB>>>
     * <<<Chunks size is 15 megabytes.>>>
     * @author : nishant.dps.uibk.ac.at
     */
    
    public void multipartUpload(File file, String containerName) throws IOException{
    	
    	
    	try{
    		long fileSize = file.length();
        	System.out.println(fileSize);
        	
        	
        	System.out.format("Starting upload of %d bytes%n", fileSize);
        	final String filename = file.getName();
        	
        	createContainer(containerName);   	
        	
        	ByteSource source = Files.asByteSource(file);
        	
        	
        	Payload payload = Payloads.newByteSourcePayload(source);
        	
        	
        	
        	payload.getContentMetadata().setContentLength(fileSize);
        	
        	Blob blob = store.blobBuilder(filename)
                    .payload(payload).contentDisposition(filename)
                    .build();
        	
        	
        	String eTag = store.putBlob(containerName, blob, multipart());
        	
        	System.out.format("Uploaded %s eTag=%s", filename, eTag);
        	
        	waitUntilVMIExistsTrue(store, containerName, filename);
        	waitUntilVMIContentAvailable(store, containerName, filename);
        	
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	
    }
    /*
     * <<<Get Public URI of VMI. Input : Container name, VMI name. return type : URI.>>>
     * @author : nishant.dps.uibk.ac.at
     */
    
    public URI getFileURI(String containerName, String filename) {
    		return ctx.getSigner().signGetBlob(containerName, filename).getEndpoint();    	
    }
       
    /*
     * <<<Get Meta Data corresponding to a VMI . Input : Container name , filename. return type : Content meta data.>>>
     * @author : nishant.dps.uibk.ac.at
     */
    
    public ContentMetadata getFileMetaData(String containerName, String filename){
    	return store.blobMetadata(containerName, filename).getContentMetadata();
    }
    
    /*
     * <<<Get VMI creation date. Inputs : Container Name, file name. return type : date>>>
     * @author : nishant.dps.uibk.ac.at
     */
    public Date getFileCreationDate(String containerName, String filename){
    	return store.blobMetadata(containerName, filename).getCreationDate();
    }
    
    /*
     * <<<Get VMI last Modification Date. Inputs : Container name, file name. return type : date>>>
     * @author : nishant.dps.uibk.ac.at
     */
    public Date getFileLastModificationDate(String containerName, String filename){
    	return store.blobMetadata(containerName, filename).getLastModified();
   	
    }
    
  
    
    /*
     * <<<Get VMI size . Inputs : Container Name, file name. return type : Long >>>
     * @author : nishant.dps.uibk.ac.at
     */
    
    public Long getFileSize(String containerName, String filename){
    	return store.blobMetadata(containerName, filename).getSize();
    }
        	
        
    /*
     * <<<To check if VMI exists after uploading it.>>>
     * @author : nishant.dps.uibk.ac.at
     */
    
    public void waitUntilVMIExistsTrue(BlobStore store, String containerName, String filename) throws InterruptedException {
  	
    	
    		while(!store.blobExists(containerName, filename)){
    			 TimeUnit.MILLISECONDS.sleep(QUERY_RETRY_INTERVAL_MILLIS);
                 System.out.println("Waiting for VMI to 'exist'");
                 return;
                
    		}
    		System.out.println("VMI exists");
    }
    
    /*
     * <<<To check if VMI is available.>>> 
     * @author : nishant.dps.uibk.ac.at
     */
    
    public void waitUntilVMIContentAvailable(BlobStore store, String containerName, String filename) throws InterruptedException {
    	
    	
    		while (store.blobMetadata(containerName, filename)
                    .getContentMetadata().getContentLength() == null) {
                TimeUnit.MILLISECONDS.sleep(QUERY_RETRY_INTERVAL_MILLIS);
                System.out.println("Waiting for VMI to become available");
                
    	}
        
        System.out.println("VMI available");
    }
    
    public boolean checkifVMIExists(String containerName, String filename){
    	
    	boolean check = false;
    	
    	if(store.blobExists(containerName, filename)){
    		
    		if(store.blobMetadata(containerName, filename) != null){
    			check = true;
    		}
    		else {
    			check = false;
    		}
    		
    	}
    	
    	return check;
    }
    
    /*
     * <<<Delete store VMI upon request>>>
     * @author : nishant.dps.uibk.ac.at
     */
    
    public void deleteVMI(String containerName, String filename){
    	try{
    		store.removeBlob(containerName, filename);
    	}catch (RuntimeException exception) {
            System.err.format("Unable to delete VMI due to: %s%n", exception.getMessage());
        }   	
    	
    }
    
    /*
     * <<<Delete the bucket if it has no store VMI.>>>
     * @author : nishant.dps.uibk.ac.at
     */
    private static void deleteContainerIfEmpty(BlobStore store, String containerName) {
        try {
        		
        		store.deleteContainer(containerName);
        	
            
        } catch (RuntimeException exception) {
            System.err.format("Unable to delete container due to: %s%n", exception.getMessage());
        }
    }
    
 
    /*
     * <<<making buckets and VMI accessible to enable download using URI.>>>
     * @author : nishant.dps.uibk.ac.at
     */
    
    
    public void getVMIPublicAccess(String nodeId, String containerName, String filename) throws FileNotFoundException, IOException{
    	
        	store.setContainerAccess(containerName, ContainerAccess.PUBLIC_READ);
        	store.setBlobAccess(containerName, filename, BlobAccess.PUBLIC_READ);
    
    	
    }
    
    
    
    /*
     * <<<Extracting the Buckets  at a particular storage node>>>
     * @author : nishant.dps.uibk.ac.at
     */
    public JSONArray listBucket(){
    	
    	JSONObject record = new JSONObject();
      	JSONArray arr = new JSONArray();  
      	JSONArray arrFinal = new JSONArray(); 
      	
    	for (StorageMetadata resourceBucket : store.list()){    	    		
    		PageSet<? extends StorageMetadata> objects = store.list(resourceBucket.getName());    		
    		arr.add(resourceBucket.getName());
    		record.put("bucketList", arr);
    	}
    	arrFinal.add(record);
    	return arrFinal;
    }
    
    /*
     * <<<Extracting the stored VMI within each bucket at a particular storage node>>>
     * @author : nishant.dps.uibk.ac.at
     */
    
    public JSONArray listBucketVMI(String containerName){
    	
   	 	JSONObject record = new JSONObject();
   	 	JSONArray arr = new JSONArray();    
   	 	JSONArray arrFinal = new JSONArray(); 
     	
   	 	PageSet<? extends StorageMetadata> objects = store.list(containerName);
   	 	for (StorageMetadata resourceVMI : objects) { 		
   	 		arr.add(resourceVMI.getName()); 
     		record.put("vmiList", arr);
     	}
     	arrFinal.add(record);
     	return arrFinal;
     }

    public void cleanup() {
        ctx.close();
    }
             
		
}