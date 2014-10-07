package fr.eurecom.hybris.testgui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

public class HybrisTestGui implements KeyListener {

    private JFrame frame;

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
        initialize1();
    }

    
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 650, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        frame.add(new JLabel("Hybris"), gbc);

        gbc.gridheight = 10;
        gbc.gridx = 0;
        gbc.gridy = 1;
        String[] data = { "file1", "file2", "file3", "file4" };
        JList<String> myList = new JList<String>(data);
        // myList.setPreferredSize(new Dimension(30, 120));
        frame.add(myList, gbc);

        JPanel jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.X_AXIS));
        
/*        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;*/
        JButton b1 = new JButton("Put");
//        frame.add(b2, gbc);

//        gbc.gridx = 1;
        JButton b2 = new JButton("Get");
//        frame.add(b3, gbc);

//        gbc.gridx = 2;
        JButton b3 = new JButton("Delete");
        jp.add(b1);
        jp.add(b2);
        jp.add(b3);
        
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        frame.add(jp, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        frame.add(new JLabel("Cloud1"), gbc);

        gbc.gridheight = 2;
        gbc.gridy = 1;
        String[] data1 = { "file2_chunk1", "file2_chunk1", "file3_chunk1",
                "file4_chunk1" };
        JList<String> listC1 = new JList<String>(data1);
        frame.add(listC1, gbc);

        gbc.gridy = 3;
        gbc.gridheight = 1;
        frame.add(new JLabel("Cloud2"), gbc);

        gbc.gridheight = 2;
        gbc.gridy = 4;
        JList<String> listC2 = new JList<String>(data1);
        frame.add(listC2, gbc);

        gbc.gridy = 6;
        gbc.gridheight = 1;
        frame.add(new JLabel("Cloud3"), gbc);

        gbc.gridheight = 2;
        gbc.gridy = 7;
        JList<String> listC3 = new JList<String>(data1);
        frame.add(listC3, gbc);

        gbc.gridy = 9;
        gbc.gridheight = 1;
        frame.add(new JLabel("Cloud4"), gbc);

        gbc.gridheight = 2;
        gbc.gridy = 10;
        JList<String> listC4 = new JList<String>(data1);
        frame.add(listC4, gbc);

        gbc.gridx = 0;
        gbc.gridy = 13;
        gbc.gridwidth = 7;
        gbc.gridheight = 5;
        JTextArea jt = new JTextArea(5, 30);
        JScrollPane scrollPane = new JScrollPane(jt);
        jt.setText("> cmd output...");
        frame.add(scrollPane, gbc);

        frame.pack();
    }
    
    /**
     * Initialize the contents of the frame.
     */
    private void initialize1() {
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

        frame.pack();
        frame.setSize(550, 700);
        frame.setResizable(false);
        
        listC1.addKeyListener(this);
        listC2.addKeyListener(this);
        listC3.addKeyListener(this);
        listC4.addKeyListener(this);
        hybrisList.addKeyListener(this);
    }

    public void keyPressed(KeyEvent e) {
        
        
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

    public void keyTyped(KeyEvent e) {
        
        
    }
}
