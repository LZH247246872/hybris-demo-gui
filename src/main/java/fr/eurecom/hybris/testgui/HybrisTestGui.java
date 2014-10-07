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
import org.jclouds.openstack.swift.v1.blobstore.RegionScopedBlobStoreContext;
import org.jets3t.service.impl.rest.httpclient.GoogleStorageService;
import org.jets3t.service.security.GSCredentials;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Region;
import com.microsoft.windowsazure.services.blob.client.CloudBlobClient;
import com.microsoft.windowsazure.services.blob.client.CloudBlobContainer;
import com.microsoft.windowsazure.services.core.storage.CloudStorageAccount;

import fr.eurecom.hybris.Hybris;

public class HybrisTestGui implements KeyListener {

    private JFrame frame;
    
    private Hybris hybris;

    private AmazonS3 s3Client;
    private GoogleStorageService gsService;
    private CloudBlobClient azureClient;
    private BlobStore rackspaceBlobStore;
    
    private final String hybrisPropertiesFile = "hybris.properties";
    private final String hybrisAccountsPropertiesFile = "accounts.properties";
    private final String container = "hybris-guitest";
    
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

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager
                            .getSystemLookAndFeelClassName());
                    HybrisTestGui window = new HybrisTestGui();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public HybrisTestGui() {
        
        // initialize Hybris and single clouds
        try {
            hybris = new Hybris(hybrisPropertiesFile);
            initSingleCloudClients();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        initialize();
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
        CloudBlobContainer containerRef = this.azureClient.getContainerReference(this.container);
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
    
    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
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
        DefaultListModel<String> dlmh = new DefaultListModel<String>();
        dlmh.addElement("file1");
        dlmh.addElement("file2");
        dlmh.addElement("file3");
        dlmh.addElement("file4");
        JList<String> hybrisList = new JList<String>(dlmh);
        hybrisList.setPreferredSize(new java.awt.Dimension(100, 500));
        hybrisPanel.add(hybrisList, gbc);

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
        cloudsPanel.add(new JLabel("Cloud1"), gbc);

        gbc.gridheight = 2;
        gbc.gridy = 1;
        DefaultListModel<String> dlm = new DefaultListModel<String>();
        dlm.addElement("file1_chunk1");
        dlm.addElement("file2_chunk1");
        dlm.addElement("file3_chunk1");
        dlm.addElement("file4_chunk1");
        JList<String> listC1 = new JList<String>(dlm);
        listC1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listC1.setPreferredSize(new java.awt.Dimension(100, 100));
        cloudsPanel.add(listC1, gbc);

        gbc.gridy = 3;
        gbc.gridheight = 1;
        cloudsPanel.add(new JLabel("Cloud2"), gbc);

        gbc.gridheight = 2;
        gbc.gridy = 4;
        JList<String> listC2 = new JList<String>(dlm);
        listC2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listC2.setPreferredSize(new java.awt.Dimension(100, 100));
        cloudsPanel.add(listC2, gbc);

        gbc.gridy = 6;
        gbc.gridheight = 1;
        cloudsPanel.add(new JLabel("Cloud3"), gbc);

        gbc.gridheight = 2;
        gbc.gridy = 7;
        JList<String> listC3 = new JList<String>(dlm);
        listC3.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listC3.setPreferredSize(new java.awt.Dimension(100, 100));
        cloudsPanel.add(listC3, gbc);

        gbc.gridy = 9;
        gbc.gridheight = 1;
        cloudsPanel.add(new JLabel("Cloud4"), gbc);

        gbc.gridheight = 2;
        gbc.gridy = 10;
        JList<String> listC4 = new JList<String>(dlm);
        listC4.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listC4.setPreferredSize(new java.awt.Dimension(100, 100));
        cloudsPanel.add(listC4, gbc);
        
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
        jt.setText("> cmd output...");
        frame.add(scrollPane, gbc);
        
        PrintStream printStream = new PrintStream(new CustomOutputStream(jt));
        System.setOut(printStream);
        System.setErr(printStream);

        frame.pack();
        frame.setSize(550, 700);
        frame.setResizable(false);
        
        listC1.addKeyListener(this);
        listC2.addKeyListener(this);
        listC3.addKeyListener(this);
        listC4.addKeyListener(this);
        hybrisList.addKeyListener(this);
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
