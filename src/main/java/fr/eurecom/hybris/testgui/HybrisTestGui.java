package fr.eurecom.hybris.testgui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStores;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.openstack.swift.v1.blobstore.RegionScopedBlobStoreContext;
import org.jets3t.service.impl.rest.httpclient.GoogleStorageService;
import org.jets3t.service.model.GSObject;
import org.jets3t.service.security.GSCredentials;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.microsoft.windowsazure.services.blob.client.CloudBlob;
import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.blob.client.ListBlobItem;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;

import fr.eurecom.hybris.Hybris;

public class HybrisTestGui implements KeyListener {

    private JFrame frame;
    
    private Hybris hybris;

    private AmazonS3 s3Client;
    private GoogleStorageService gsService;
    private CloudBlobClient azureClient;
    private BlobStore rackspaceBlobStore;
    
    private JList<String> lstRackspace, lstAmazon, lstGoogle, lstAzure, lstHybris;
    DefaultListModel<String> lmRackspace, lmAmazon, lmGoogle, lmAzure, lmHybris;
    
    private final String hybrisPropertiesFile = "hybris.properties";
    private final String hybrisAccountsPropertiesFile = "accounts.properties";
    private final String container = "hybris-guitest";
    
    private CloudBlobContainer containerRef;
    
    public class CustomOutputStream extends OutputStream {
        private JTextArea textArea;
         
        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }
         
        @Override
        public void write(int b) throws IOException {
            textArea.append(String.valueOf((char)b));
            // scrolls the text area to the end of data
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager
                            .getSystemLookAndFeelClassName());
                    new HybrisTestGui();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public HybrisTestGui() {
        
        lmHybris = new DefaultListModel<String>();
        lmAmazon = new DefaultListModel<String>();
        lmAzure = new DefaultListModel<String>();
        lmGoogle = new DefaultListModel<String>();
        lmRackspace = new DefaultListModel<String>();
        
        initializeGUI();
        frame.setVisible(true);
        
        // initialize Hybris and single clouds
        // TODO parallelize
        try {
            System.out.println("Initializing Hybris...");
            hybris = new Hybris(hybrisPropertiesFile);
            
            System.out.println("Initializing single clouds...");
            initSingleCloudClients();
            
            System.out.println("Listing clouds...");
            refreshLists();            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
   private void refreshLists() {
       
       try {
           for (String str: hybris.list())
               lmHybris.addElement(str);
       
           ObjectListing objectListing = s3Client.listObjects(this.container);
           boolean loop = false;
           do {
               for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries())
                   lmAmazon.addElement(objectSummary.getKey());
               if (objectListing.isTruncated()) {
                   objectListing = s3Client.listNextBatchOfObjects(objectListing);
                   loop = true;
               } else loop = false;
           } while (loop);
           
           for (ListBlobItem blobItem : containerRef.listBlobs()) {
               CloudBlob blob = (CloudBlob) blobItem;
               lmAzure.addElement(blob.getName());
           }
           
           GSObject[] objs = this.gsService.listObjects(container);
           for(GSObject obj: objs)
               lmGoogle.addElement(obj.getName());
           
           for (StorageMetadata resourceMd :
               BlobStores.listAll(rackspaceBlobStore,
                       container, ListContainerOptions.NONE))
               lmRackspace.addElement(resourceMd.getName());
           
       } catch (Exception e) {
           e.printStackTrace();
       }

   }
    
    private void initSingleCloudClients() throws Exception {

        String provider = null, identity = null, credential = null;
        Properties accountsProperties = new Properties();
        accountsProperties.load(new FileInputStream(this.hybrisAccountsPropertiesFile));

        // Amazon S3
        identity = accountsProperties.getProperty("hybris.kvs.drivers.amazon.akey");
        credential = accountsProperties.getProperty("hybris.kvs.drivers.amazon.skey");
        BasicAWSCredentials credentials = new BasicAWSCredentials(identity, credential);
        this.s3Client = new AmazonS3Client(credentials);
        if (!this.s3Client.doesBucketExist(this.container))
            this.s3Client.createBucket(this.container, Region.EU_Ireland);

        // Google cloud storage
        identity = accountsProperties.getProperty("hybris.kvs.drivers.google.akey");
        credential = accountsProperties.getProperty("hybris.kvs.drivers.google.skey");
        GSCredentials gsCredentials = new GSCredentials(identity, credential);
        this.gsService = new GoogleStorageService(gsCredentials);
        this.gsService.getOrCreateBucket(this.container);

        // Azure (SDK)
        identity = accountsProperties.getProperty("hybris.kvs.drivers.azure.akey");
        credential = accountsProperties.getProperty("hybris.kvs.drivers.azure.skey");
        String connectionString = "DefaultEndpointsProtocol=http;"
                + "AccountName=" + identity + ";"
                + "AccountKey=" + credential + ";";
        CloudStorageAccount account = CloudStorageAccount.parse(connectionString);
        this.azureClient = account.createCloudBlobClient();
        containerRef = this.azureClient.getContainerReference(this.container);
        containerRef.createIfNotExist();


        // Rackspace (jClouds)
        provider = "rackspace-cloudfiles-us";
        identity = accountsProperties.getProperty("hybris.kvs.drivers.rackspace.akey");
        credential = accountsProperties.getProperty("hybris.kvs.drivers.rackspace.skey");
        this.rackspaceBlobStore = ContextBuilder.newBuilder(provider)
                .credentials(identity, credential)
                .buildView(RegionScopedBlobStoreContext.class)
                .blobStoreInRegion("IAD");
        this.rackspaceBlobStore.createContainerInLocation(null, this.container);
    }
    
    private void initializeGUI() {
        frame = new JFrame();
        frame.setBounds(100, 100, 650, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new GridBagLayout());
        
        JPanel cloudParentPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        JPanel hybrisPanel = new JPanel(new GridBagLayout());
        JPanel cloudsPanel = new JPanel(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.BOTH;
        
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        hybrisPanel.add(new JLabel("Hybris"), gbc);

        gbc.gridwidth = 3;
        gbc.gridheight = 3;
        gbc.gridx = 0;
        gbc.gridy = 1;
        
        lstHybris = new JList<String>(lmHybris);
        lstHybris.setPreferredSize(new java.awt.Dimension(100, 500));
        hybrisPanel.add(lstHybris, gbc);

        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 4;
        JButton b1 = new JButton("Put");
        hybrisPanel.add(b1, gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        JButton b2 = new JButton("Get");
        hybrisPanel.add(b2, gbc);
        gbc.gridx = 2;
        gbc.gridy = 4;
        JButton b3 = new JButton("Delete");
        hybrisPanel.add(b3, gbc);
        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        cloudsPanel.add(new JLabel("Amazon"), gbc);

        gbc.gridheight = 2;
        gbc.gridy = 1;
        lstAmazon = new JList<String>(lmAmazon);
        lstAmazon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstAmazon.setPreferredSize(new java.awt.Dimension(100, 100));
        cloudsPanel.add(lstAmazon, gbc);

        gbc.gridy = 3;
        gbc.gridheight = 1;
        cloudsPanel.add(new JLabel("Azure"), gbc);

        gbc.gridheight = 2;
        gbc.gridy = 4;
        lstAzure = new JList<String>(lmAzure);
        lstAzure.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstAzure.setPreferredSize(new java.awt.Dimension(100, 100));
        cloudsPanel.add(lstAzure, gbc);

        gbc.gridy = 6;
        gbc.gridheight = 1;
        cloudsPanel.add(new JLabel("Rackspace"), gbc);

        gbc.gridheight = 2;
        gbc.gridy = 7;
        lstGoogle = new JList<String>(lmGoogle);
        lstGoogle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstGoogle.setPreferredSize(new java.awt.Dimension(100, 100));
        cloudsPanel.add(lstGoogle, gbc);

        gbc.gridy = 9;
        gbc.gridheight = 1;
        cloudsPanel.add(new JLabel("Google"), gbc);

        gbc.gridheight = 2;
        gbc.gridy = 10;
        lstRackspace = new JList<String>(lmRackspace);
        lstRackspace.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstRackspace.setPreferredSize(new java.awt.Dimension(100, 100));
        cloudsPanel.add(lstRackspace, gbc);
        
        cloudParentPanel.add(hybrisPanel);
        cloudParentPanel.add(cloudsPanel);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        frame.add(cloudParentPanel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        JTextArea jt = new JTextArea(5, 30);      
        JScrollPane scrollPane = new JScrollPane(jt);
        jt.setText("");
        frame.add(scrollPane, gbc);
        
        PrintStream printStream = new PrintStream(new CustomOutputStream(jt));
        System.setOut(printStream);
        System.setErr(printStream);

        frame.pack();
        frame.setSize(550, 700);
        frame.setResizable(false);
        
        lstAmazon.addKeyListener(this);
        lstAzure.addKeyListener(this);
        lstGoogle.addKeyListener(this);
        lstRackspace.addKeyListener(this);
        lstHybris.addKeyListener(this);
    }


    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            JList<String> jlist =  (JList<String>) e.getComponent();
            if (jlist.getSelectedIndex() >= 0) {
                JOptionPane.showMessageDialog(null, "Removed: " + jlist.getSelectedValue());
                ((DefaultListModel<String>) jlist.getModel()).remove(jlist.getSelectedIndex());
            }
        }
    }

    public void keyTyped(KeyEvent e) { }
    public void keyPressed(KeyEvent e) { }
}
