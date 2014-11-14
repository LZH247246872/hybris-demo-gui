/**
 * Copyright (C) 2014 EURECOM (www.eurecom.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.eurecom.hybris.demogui;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.Properties;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStores;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.openstack.swift.v1.blobstore.RegionScopedBlobStoreContext;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.GoogleStorageService;
import org.jets3t.service.model.GSObject;
import org.jets3t.service.security.GSCredentials;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;
import com.google.common.io.ByteSource;
import com.microsoft.windowsazure.services.blob.client.CloudBlob;
import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.blob.client.ListBlobItem;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;

import fr.eurecom.hybris.Hybris;

public class CloudManager {
    
    private HybrisDemoGui gui;
    
    public Hybris hybris;

    private AmazonS3 s3Client;
    private GoogleStorageService gsService;
    private CloudBlobClient azureClient;
    private BlobStore rackspaceBlobStore;
    
    private CloudBlobContainer containerRef;
    
    private final String hybrisPropertiesFile = "hybris.properties";
    private final String hybrisAccountsPropertiesFile = "accounts.properties";
    private final String container = "hybris-demo-gui";
    
    enum OperationType {
        INIT_REFRESH, REFRESH, DELETE, PUT 
    };
    
    enum ClientType {
        HYBRIS, AWS, RACKSPACE, AZURE, GOOGLE
    };
    
    public CloudManager(HybrisDemoGui htg) {
        gui = htg;
    }
    
    public void shutdown() {
        hybris.shutdown();
        try {
            gsService.shutdown();
        } catch(ServiceException e) { }
    }
    
    public class BackgroundWorker implements Runnable {    
        
        private OperationType opType;
        private ClientType cType;
        
        private String key;
        private byte[] payload;
        
        public BackgroundWorker(OperationType o) { opType = o; }
        public BackgroundWorker(OperationType o, ClientType c) { 
            opType = o;
            cType = c;
        }
        public BackgroundWorker(OperationType o, ClientType c, String k, byte[] data) { 
            this(o,c);
            payload = data;
            key = k;
        }

        public void run() {
            switch(opType) {
            case REFRESH:
                refreshLists();
                break;
            case INIT_REFRESH:
                initClouds();
                refreshLists();
                System.out.println("Initialized.");
                break;
            case DELETE:
                delete();
                refreshLists();
                break;
            case PUT:
                put();
                refreshLists();
                break;
            }
        }
        
        private void initClouds() {
            try {
                System.out.println("Initializing Hybris...");
                hybris = new Hybris(hybrisPropertiesFile);
                
                System.out.println("Initializing single clouds...");
                initSingleCloudClients();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        private void initSingleCloudClients() throws Exception {

            String provider = null, identity = null, credential = null;
            Properties accountsProperties = new Properties();
            accountsProperties.load(new FileInputStream(hybrisAccountsPropertiesFile));

            // Google cloud storage
            identity = accountsProperties.getProperty("hybris.kvs.drivers.google.akey");
            credential = accountsProperties.getProperty("hybris.kvs.drivers.google.skey");
            GSCredentials gsCredentials = new GSCredentials(identity, credential);
            gsService = new GoogleStorageService(gsCredentials);
            gsService.getOrCreateBucket(container);

            // Azure (SDK)
            identity = accountsProperties.getProperty("hybris.kvs.drivers.azure.akey");
            credential = accountsProperties.getProperty("hybris.kvs.drivers.azure.skey");
            String connectionString = "DefaultEndpointsProtocol=http;"
                    + "AccountName=" + identity + ";"
                    + "AccountKey=" + credential + ";";
            CloudStorageAccount account = CloudStorageAccount.parse(connectionString);
            azureClient = account.createCloudBlobClient();
            containerRef = azureClient.getContainerReference(container);
            containerRef.createIfNotExist();

            // Rackspace (jClouds)
            provider = "rackspace-cloudfiles-us";
            identity = accountsProperties.getProperty("hybris.kvs.drivers.rackspace.akey");
            credential = accountsProperties.getProperty("hybris.kvs.drivers.rackspace.skey");
            rackspaceBlobStore = ContextBuilder.newBuilder(provider)
                    .credentials(identity, credential)
                    .buildView(RegionScopedBlobStoreContext.class)
                    .blobStoreInRegion("IAD");
            rackspaceBlobStore.createContainerInLocation(null, container);
            
            // Amazon S3
            identity = accountsProperties.getProperty("hybris.kvs.drivers.amazon.akey");
            credential = accountsProperties.getProperty("hybris.kvs.drivers.amazon.skey");
            BasicAWSCredentials credentials = new BasicAWSCredentials(identity, credential);
            s3Client = new AmazonS3Client(credentials);
            if (!s3Client.doesBucketExist(container))
                s3Client.createBucket(container, Region.EU_Ireland);
        }
        
        private void refreshLists() {
            
            try {
                System.out.println("Listing clouds...");
                
                gui.lmHybris.clear();
                for (String str: hybris.list())
                    gui.lmHybris.addElement(str);
            
                gui.lmAmazon.clear();
                ObjectListing objectListing = s3Client.listObjects(container);
                boolean loop = false;
                do {
                    for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries())
                        gui.lmAmazon.addElement(objectSummary.getKey());
                    if (objectListing.isTruncated()) {
                        objectListing = s3Client.listNextBatchOfObjects(objectListing);
                        loop = true;
                    } else loop = false;
                } while (loop);
                
                gui.lmAzure.clear();
                for (ListBlobItem blobItem : containerRef.listBlobs()) {
                    CloudBlob blob = (CloudBlob) blobItem;
                    gui.lmAzure.addElement(blob.getName());
                }
                
                gui.lmGoogle.clear();
                GSObject[] objs = gsService.listObjects(container);
                for(GSObject obj: objs)
                    gui.lmGoogle.addElement(obj.getName());
                
                gui.lmRackspace.clear();
                for (StorageMetadata resourceMd :
                    BlobStores.listAll(rackspaceBlobStore,
                            container, ListContainerOptions.NONE)) 
                    gui.lmRackspace.addElement(resourceMd.getName());
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        private void put() {
            try {
                switch(cType) {
                    case HYBRIS:
                        hybris.put(key, payload);
                        break;
                        
                    // For corrupting single clouds' blobs
                    case AWS:
                        ByteArrayInputStream bais = new ByteArrayInputStream(payload);
                        ObjectMetadata om = new ObjectMetadata();
                        om.setContentLength(payload.length);
                        PutObjectRequest request = new PutObjectRequest(container, key, bais, om);
                        request.setStorageClass(StorageClass.ReducedRedundancy);
                        s3Client.putObject(request);
                        break;
                    case GOOGLE:
                        GSObject object = new GSObject(key);
                        ByteArrayInputStream in = new ByteArrayInputStream(payload);
                        object.setDataInputStream(in);
                        object.setContentLength(payload.length);
                        gsService.putObject(container, object);
                        break;
                    case AZURE:
                        CloudBlobContainer containerRef = azureClient.getContainerReference(container);
                        CloudBlob cblob = containerRef.getBlockBlobReference(key);
                        cblob.upload(new ByteArrayInputStream(payload), payload.length);
                        break;
                    case RACKSPACE:
                        Blob blob = rackspaceBlobStore.blobBuilder(key)
                        .payload(ByteSource.wrap(payload)).build();
                        rackspaceBlobStore.putBlob(container, blob);
                        break;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        private void delete() {
            try {
                switch(cType) {
                    case AWS:
                        s3Client.deleteObject(container, key);
                        break;
                    case GOOGLE:
                        gsService.deleteObject(container, key);
                        break;
                    case AZURE:
                        CloudBlockBlob blob = containerRef.getBlockBlobReference(key);
                        blob.delete();
                        break;
                    case HYBRIS:
                        hybris.delete(key);
                        break;
                    case RACKSPACE:
                        rackspaceBlobStore.removeBlob(container, key);
                        break;
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
