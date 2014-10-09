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
package fr.eurecom.hybris.testgui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;

import fr.eurecom.hybris.testgui.CloudManager.ClientType;
import fr.eurecom.hybris.testgui.CloudManager.OperationType;

/**
 * GUI for showing some benefits of using Hybris during demos. 
 * @author P. Viotti
 * 
 * TODO:
 * - embedded Zk server (?)
 * - GET function
 * - tooltip with size (?)
 */
public class HybrisDemoGui implements KeyListener, ActionListener {

    private CloudManager cm;
    
    private JFrame frame;
    private JList<String> lstRackspace, lstAmazon, lstGoogle, lstAzure, lstHybris;
    public DefaultListModel<String> lmRackspace, lmAmazon, lmGoogle, lmAzure, lmHybris;
    private JButton btnGet, btnPut, btnDelete;
    
    
    public class CustomOutputStream extends OutputStream {
        private JTextArea textArea;
         
        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }
         
        @Override
        public void write(int b) throws IOException {
            textArea.append(String.valueOf((char)b));
            textArea.setCaretPosition(textArea.getDocument().getLength()); // scroll to the end
        }
    }

    
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager
                            .getSystemLookAndFeelClassName());
                    new HybrisDemoGui();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    

    public HybrisDemoGui() {
        lmHybris = new DefaultListModel<String>();
        lmAmazon = new DefaultListModel<String>();
        lmAzure = new DefaultListModel<String>();
        lmGoogle = new DefaultListModel<String>();
        lmRackspace = new DefaultListModel<String>();
        
        cm = new CloudManager(this);
        
        initializeGUI();
        frame.setVisible(true);
        
        new Thread(cm.new BackgroundWorker(OperationType.INIT_REFRESH)).start();
    }
    
    
    private void initializeGUI() {
        frame = new JFrame("Hybris Test GUI");
        frame.setIconImage(new ImageIcon(getClass().getResource("/clouds.png")).getImage());
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
        hybrisPanel.add(new JLabel("<html><b>Hybris</b></html>"), gbc);

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
        btnPut = new JButton("Put");
        hybrisPanel.add(btnPut, gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        btnGet = new JButton("Get");
        hybrisPanel.add(btnGet, gbc);
        gbc.gridx = 2;
        gbc.gridy = 4;
        btnDelete = new JButton("Delete");
        hybrisPanel.add(btnDelete, gbc);
        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        cloudsPanel.add(new JLabel("<html><b>Amazon S3</b></html>"), gbc);

        gbc.gridheight = 2;
        gbc.gridy = 1;
        lstAmazon = new JList<String>(lmAmazon);
        lstAmazon.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstAmazon.setPreferredSize(new java.awt.Dimension(100, 100));
        cloudsPanel.add(lstAmazon, gbc);

        gbc.gridy = 3;
        gbc.gridheight = 1;
        cloudsPanel.add(new JLabel("<html><b>Microsoft Azure</b></html>"), gbc);

        gbc.gridheight = 2;
        gbc.gridy = 4;
        lstAzure = new JList<String>(lmAzure);
        lstAzure.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstAzure.setPreferredSize(new java.awt.Dimension(100, 100));
        cloudsPanel.add(lstAzure, gbc);

        gbc.gridy = 6;
        gbc.gridheight = 1;
        cloudsPanel.add(new JLabel("<html><b>Rackspace Cloud Files</b></html>"), gbc);

        gbc.gridheight = 2;
        gbc.gridy = 7;
        lstGoogle = new JList<String>(lmGoogle);
        lstGoogle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstGoogle.setPreferredSize(new java.awt.Dimension(100, 100));
        cloudsPanel.add(lstGoogle, gbc);

        gbc.gridy = 9;
        gbc.gridheight = 1;
        cloudsPanel.add(new JLabel("<html><b>Google Cloud Storage</b></html>"), gbc);

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
        frame.add(scrollPane, gbc);
        
        PrintStream printStream = new PrintStream(new CustomOutputStream(jt));
        System.setOut(printStream);
        System.setErr(printStream);

        frame.pack();
        frame.setSize(550, 700);
        frame.setResizable(false);
        
        lstAmazon.addKeyListener(this); lstAzure.addKeyListener(this);
        lstGoogle.addKeyListener(this); lstRackspace.addKeyListener(this);
        lstHybris.addKeyListener(this);

        btnGet.addActionListener(this);
        btnPut.addActionListener(this);
        btnDelete.addActionListener(this);
    }


    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            
            JList<String> jlist =  (JList<String>) e.getComponent();
            if (jlist.getSelectedIndex() >= 0) {
                
                if (jlist.equals(lstAmazon)) {
                    new Thread(cm.new 
                            BackgroundWorker(OperationType.DELETE, ClientType.AWS, jlist.getSelectedValue(), null)).start();
                    System.out.println("Removed " + jlist.getSelectedValue() + " from Amazon S3.");
                } else if (jlist.equals(lstAzure)) {
                    new Thread(cm.new 
                            BackgroundWorker(OperationType.DELETE, ClientType.AZURE, jlist.getSelectedValue(), null)).start();
                    System.out.println("Removed " + jlist.getSelectedValue() + " from Azure.");
                } else if (jlist.equals(lstGoogle)) {
                    new Thread(cm.new 
                            BackgroundWorker(OperationType.DELETE, ClientType.GOOGLE, jlist.getSelectedValue(), null)).start();
                    System.out.println("Removed " + jlist.getSelectedValue() + " from Google.");
                } else if (jlist.equals(lstRackspace)) {
                    new Thread(cm.new 
                            BackgroundWorker(OperationType.DELETE, ClientType.RACKSPACE, jlist.getSelectedValue(), null)).start();
                    System.out.println("Removed " + jlist.getSelectedValue() + " from Rackspace.");
                } else if (jlist.equals(lstHybris)) {
                    new Thread(cm.new 
                            BackgroundWorker(OperationType.DELETE, ClientType.HYBRIS, jlist.getSelectedValue(), null)).start();
                    System.out.println("Removed " + jlist.getSelectedValue() + " from Hybris.");
                }
            }
        }
    }

    public void keyTyped(KeyEvent e) { }
    public void keyPressed(KeyEvent e) { }

    public void actionPerformed(ActionEvent e) {
        
        String cmd = e.getActionCommand();
        if (cmd.equals("Get")) {    // TODO
            
           System.out.println("Get..");
           
        } if (cmd.equals("Put")) {
            
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                System.out.println("Opening: " + file.getName() + ".");
                byte[] array;
                try {
                    array = FileUtils.readFileToByteArray(file);
                    new Thread(cm.new 
                            BackgroundWorker(OperationType.PUT, ClientType.HYBRIS, file.getName(), array)).start();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                
            }
            
        } if (cmd.equals("Delete")) {
            
            if (lstHybris.getSelectedIndex() >= 0) {
                new Thread(cm.new 
                        BackgroundWorker(OperationType.DELETE, ClientType.HYBRIS, lstHybris.getSelectedValue(), null)).start();
                System.out.println("Removed " + lstHybris.getSelectedValue() + " from Hybris.");
            }
        }
    }
}
