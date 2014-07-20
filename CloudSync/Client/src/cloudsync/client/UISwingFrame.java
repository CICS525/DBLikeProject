package cloudsync.client;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JPasswordField;

import sun.awt.WindowClosingListener;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UISwingFrame extends JFrame implements Runnable{
	public UISwingFrame() {
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static UISwingFrame frame;
	public JPanel contentPane;
	private JTextField usernameTF;
	private JTextField directoryTF;
	private JTextField deviceTF;
	private JPasswordField passwordField;
	private JButton btnRunInBackground;
	private JButton btnNewButton;
	private JButton btnNewButton_1;


	/**
	 * Create the frame.
	 */
	public void startUI() {
		System.out.println("calling constructor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 379, 268);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JLabel lblUsername = new JLabel("UserName");
		JLabel lblPassword = new JLabel("Password");
		JLabel lblDirectory = new JLabel("Directory");
		JLabel lblDevice = new JLabel("Device");
		usernameTF = new JTextField();
		deviceTF = new JTextField();
		directoryTF = new JTextField();
		passwordField = new JPasswordField();

		usernameTF.setColumns(10);
		directoryTF.setColumns(10);
		deviceTF.setColumns(10);
		deviceTF.setEditable(false);
		
		
		btnRunInBackground = new JButton("Run in Background");
		btnRunInBackground.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		
		btnNewButton = new JButton("......");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser();
			    chooser.setCurrentDirectory(new java.io.File("."));
			    chooser.setDialogTitle("choosertitle");
			    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    chooser.setAcceptAllFileFilterUsed(false);

			    if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			      System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
			      System.out.println("getSelectedFile() : " + chooser.getSelectedFile());
			      directoryTF.setText(chooser.getSelectedFile().toString());
			    } else {
			      System.out.println("No Selection ");
			    }
			}
		});
		
		btnNewButton_1 = new JButton("Open");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				Submit();
			}
		});
		
		
		addWindowStateListener(new WindowStateListener() {
			@Override
            public void windowStateChanged(WindowEvent e) {
				System.out.println(e.getNewState());
				System.out.println("Hellooooossosososososos");
				
                if(e.getNewState()==ICONIFIED){
                    setVisible(false);
					System.out.println("added to SystemTray");
                } /*
        if(e.getNewState()==7){
                    try{
            tray.add(trayIcon);
            setVisible(false);
            System.out.println("added to SystemTray");
            }catch(AWTException ex){
            System.out.println("unable to add to system tray");
        }
            }
        if(e.getNewState()==MAXIMIZED_BOTH){
                    tray.remove(trayIcon);
                    setVisible(true);
                    System.out.println("Tray icon removed");
                }
                if(e.getNewState()==NORMAL){
                    tray.remove(trayIcon);
                    setVisible(true);
                    System.out.println("Tray icon removed");
                }
            }*/

			}
			
        });
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(18)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(lblPassword)
						.addComponent(lblUsername)
						.addComponent(lblDevice)
						.addComponent(lblDirectory)
						.addComponent(btnRunInBackground))
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_contentPane.createSequentialGroup()
								.addGap(103)
								.addComponent(btnNewButton_1, GroupLayout.PREFERRED_SIZE, 88, Short.MAX_VALUE))
							.addGroup(gl_contentPane.createSequentialGroup()
								.addComponent(directoryTF, GroupLayout.PREFERRED_SIZE, 144, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)))
						.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false)
							.addComponent(passwordField, Alignment.LEADING)
							.addComponent(usernameTF, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
						.addComponent(deviceTF, GroupLayout.PREFERRED_SIZE, 191, GroupLayout.PREFERRED_SIZE))
					.addGap(23))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(26)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblUsername)
						.addComponent(usernameTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(passwordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblPassword))
					.addGap(26)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblDirectory)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
							.addComponent(directoryTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(btnNewButton)))
					.addGap(28)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblDevice)
						.addComponent(deviceTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnRunInBackground)
						.addComponent(btnNewButton_1))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		contentPane.setLayout(gl_contentPane);
	}

	public void disableAllvalues()
	{
		usernameTF.setEditable(false);
		passwordField.setEditable(false);
		directoryTF.setEditable(false);
		deviceTF.setEditable(false);
	}
	
	public void EnableAllValues()
	{
		usernameTF.setEditable(true);
		passwordField.setEditable(true);
		directoryTF.setEditable(true);
		deviceTF.setEditable(true);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("calling run");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new UISwingFrame();
					frame.startUI();
					frame.initializeFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}
	
	public void makeVisible()
	{
		setVisible(true);
		setState(NORMAL);
	}
	
	public void invalidCredentials()
	{
		usernameTF.setText(Application_Main.settings.getUsername());
		passwordField.setText(Application_Main.settings.getPassword());
		directoryTF.setText(Application_Main.settings.getRootDir());
		deviceTF.setText("herer " + populateDevice());
	}
	
	public void initializeFrame()
	{
		if(Application_Main.settings.loadSettings())
		{
		usernameTF.setText(Application_Main.settings.getUsername());
		passwordField.setText(Application_Main.settings.getPassword());
		directoryTF.setText(Application_Main.settings.getRootDir());
		deviceTF.setText(frame.populateDevice());
		}else
		{
			deviceTF.setText(frame.populateDevice());
		}
	}
	
	public  String  populateDevice()
	{
		String hostname="";
		try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
		System.out.println(hostname + "here");
		return hostname;
	}
	
	
	public boolean Submit()
	{
		Application_Main.settings.setUsername(usernameTF.getText());
		Application_Main.settings.setPassword(passwordField.getPassword().toString());
		Application_Main.settings.setRootDir(directoryTF.getText());
		Application_Main.settings.setDeviceName(populateDevice());
		Application_Main.settings.saveSettings();
		return true;
	}
	
	
	

}
