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


import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;



/*
 * <<<To download a VMI and save a VMI using URI>>>
 * @author : nishant.dps.uibk.ac.at
 */

public class DownloadVMI {
	
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
	
	
	public static void main(String[] args) throws ExecutionException, InterruptedException, URISyntaxException {
		
		DownloadVMI download = new DownloadVMI();
		
		String vmImageName = "#name of VMI#"; 
		
		try{
			long startTime = System.currentTimeMillis();
	        long elapsedTime = 0L ;
	        
			URI uri = new URI("#URl of VMI to be downloaded");			
			download.downloadVMIViaURI(uri, new File("#Path where VM is to be downloaded locally#" + vmImageName));
			System.out.println("The VMI: " + vmImageName + " is downloaded from " + uri);
			
			elapsedTime = (new Date()).getTime() - startTime;
			long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
			
			System.out.println("The download time is : " + timeSeconds + "seconds");
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}

}
