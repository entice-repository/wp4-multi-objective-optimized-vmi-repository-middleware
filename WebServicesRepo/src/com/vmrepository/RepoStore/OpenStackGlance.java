package com.vmrepository.RepoStore;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.inject.Module;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import org.jclouds.ContextBuilder;






import org.jclouds.io.Payload;
import org.jclouds.io.Payloads;
import org.jclouds.openstack.glance.v1_0.GlanceApi;
import org.jclouds.openstack.glance.v1_0.domain.ContainerFormat;
import org.jclouds.openstack.glance.v1_0.domain.DiskFormat;
import org.jclouds.openstack.glance.v1_0.domain.ImageDetails;
import org.jclouds.openstack.glance.v1_0.features.ImageApi;
import org.jclouds.openstack.glance.v1_0.options.CreateImageOptions;
import org.jclouds.openstack.glance.v1_0.options.ListImageOptions;
import org.jclouds.openstack.glance.v1_0.options.UpdateImageOptions;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.FluentIterable;
import com.google.inject.Module;


public class OpenStackGlance {
	
	
	String nodeEndpoint;
	String provider;
	String identity;
	String credential;
	String zone;
	
	public OpenStackGlance(){
		
		nodeEndpoint = "http://192.168.64.175:5000/v2.0";
		provider = "openstack-glance";
	        identity = "entice:nishant";
		credential = "jaykrishna";
		zone = "RegionOne";
		
	}
	
	
	public void uploadImageGlanceApi(String nodeId, File file){
		
	 	
	    long fileSize = file.length();
	    String filename = file.getName();
	    
	    ByteSource source = Files.asByteSource(file);
	    Payload payload = Payloads.newByteSourcePayload(source);
	    payload.getContentMetadata().setContentLength(fileSize);
	    
	    final GlanceApi api = ContextBuilder.newBuilder(provider).endpoint(nodeEndpoint).credentials(identity, credential).buildApi(GlanceApi.class);		    
		final ImageApi img = api.getImageApi(zone);
		
	    ImageDetails imgd = img.create(filename, payload, CreateImageOptions.Builder.containerFormat(ContainerFormat.AMI));
	    		//containerFormat(ContainerFormat.));
	    
	    System.out.println(imgd);			
		System.out.println(img.listInDetail());
		
		img.update(imgd.getId(), UpdateImageOptions.Builder.diskFormat(DiskFormat.QCOW2));
		img.update(imgd.getId(), UpdateImageOptions.Builder.isPublic(true));
	}
	    
		



}