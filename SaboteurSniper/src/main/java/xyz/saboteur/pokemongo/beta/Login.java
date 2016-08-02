package xyz.saboteur.pokemongo.beta;

import java.awt.Desktop;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URI;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import com.pokegoapi.auth.GoogleUserCredentialProvider;

public class Login extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField usernameField;
	private JTextField passwordField;
	private JTextField tokenField;
	
	public Properties p;
	private JTextField minIVToSnipe;
	private JTextField queueDelay;
	
	public JCheckBox chckbxForceUnban, chckbxAutosnipe, chckbxLoadPreviousAutosniping, chckbxCaptureConfirmation, chckbxEnabled;
	

	/**
	 * Create the frame.
	 */
	public Login() {
		setTitle("Settings");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 544, 324);
		setLocationRelativeTo(null);
		setResizable(false);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		File settingsFile = new File("settings.txt");
		p = new Properties();
		if(settingsFile.exists()) {
			try {
				p.load(new FileReader(settingsFile));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		JLabel lblAccountType = new JLabel("Account Type:");
		contentPane.add(lblAccountType, "2, 2, right, default");
		
		JLabel lblUsername = new JLabel("Username:");
		contentPane.add(lblUsername, "2, 4, right, default");
		
		usernameField = new JTextField();
		contentPane.add(usernameField, "4, 4, 3, 1, fill, default");
		usernameField.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password:");
		contentPane.add(lblPassword, "2, 6, right, default");
		
		passwordField = new JTextField();
		contentPane.add(passwordField, "4, 6, 3, 1, fill, default");
		passwordField.setColumns(10);
		
		JLabel lblToken = new JLabel("Token:");
		contentPane.add(lblToken, "2, 8, right, default");
		
		tokenField = new JTextField();
		contentPane.add(tokenField, "4, 8, fill, default");
		tokenField.setColumns(10);
		
		JButton btnGetToken = new JButton("Get Token");
		btnGetToken.addActionListener(a -> {
			try {
	            Desktop.getDesktop().browse(new URI(GoogleUserCredentialProvider.LOGIN_URL));
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		});
		contentPane.add(btnGetToken, "6, 8");
		
		chckbxForceUnban = new JCheckBox("Force Unban Before Capture");
		contentPane.add(chckbxForceUnban, "4, 10");
		chckbxForceUnban.setSelected(Boolean.parseBoolean(p.getProperty("unban", "true")));
		
		JComboBox<String> typeBox = new JComboBox<>();
		typeBox.setModel(new DefaultComboBoxModel<>(new String[] {"Google", "PTC"}));
		contentPane.add(typeBox, "4, 2, 3, 1, fill, default");
		typeBox.addItemListener(i -> {
			if(i.getItem().equals("PTC")) {
				lblUsername.setText("Username:");
				btnGetToken.setVisible(false);
				tokenField.setVisible(false);
				lblToken.setVisible(false);
				setBounds(100, 100, 544, 300);
				setLocationRelativeTo(null);
			} else {
				lblUsername.setText("Email:");
				btnGetToken.setVisible(true);
				tokenField.setVisible(true);
				lblToken.setVisible(true);
				setBounds(100, 100, 544, 324);
				setLocationRelativeTo(null);
			}
		});
		
		this.addWindowListener(new WindowListener() {
			@Override public void windowClosing(WindowEvent arg0) {
				MainWindow.window.frame.requestFocus();
			}
			@Override public void windowClosed(WindowEvent arg0) { }
			@Override public void windowActivated(WindowEvent arg0) { }
			@Override public void windowDeactivated(WindowEvent arg0) { }
			@Override public void windowDeiconified(WindowEvent arg0) { }
			@Override public void windowIconified(WindowEvent arg0) { }
			@Override public void windowOpened(WindowEvent arg0) {
				Login.this.requestFocusInWindow();
			}
		});
		
		chckbxAutosnipe = new JCheckBox("Autosnipe");
		contentPane.add(chckbxAutosnipe, "4, 16");
		
		chckbxAutosnipe.setSelected(Boolean.parseBoolean(p.getProperty("autosnipe", "false")));
		
		chckbxLoadPreviousAutosniping = new JCheckBox("Load previous autosniping data");
		contentPane.add(chckbxLoadPreviousAutosniping, "6, 16");
		chckbxLoadPreviousAutosniping.setSelected(Boolean.parseBoolean(p.getProperty("autosnipeLoadPrev", "false")));
		
		chckbxCaptureConfirmation = new JCheckBox("Capture Confirmation");
		contentPane.add(chckbxCaptureConfirmation, "4, 12");
		chckbxCaptureConfirmation.setSelected(Boolean.parseBoolean(p.getProperty("captureConfirmation", "true")));
		
		tokenField.setText(p.getProperty("token", ""));
		typeBox.setSelectedIndex(p.getProperty("type", "Google").equalsIgnoreCase("Google") ? 0 : 1);
		usernameField.setText(p.getProperty("username", "Username"));
		passwordField.setText(p.getProperty("password", "Password"));
		
		JLabel lblQueueDelay = new JLabel("Queue Delay:");
		contentPane.add(lblQueueDelay, "2, 14, right, default");
		
		queueDelay = new JTextField();
		contentPane.add(queueDelay, "4, 14, 3, 1, fill, default");
		queueDelay.setColumns(10);
		
		JLabel lblMinIv = new JLabel("Min IV to Snipe:");
		contentPane.add(lblMinIv, "2, 18, right, default");
		
		minIVToSnipe = new JTextField();
		contentPane.add(minIVToSnipe, "4, 18, 2, 1, fill, default");
		minIVToSnipe.setColumns(10);
		
		chckbxEnabled = new JCheckBox("Enabled");
		contentPane.add(chckbxEnabled, "6, 18");
		
		chckbxAutosnipe.addActionListener(a -> {
			if(chckbxEnabled.isSelected())
				chckbxCaptureConfirmation.setEnabled(!chckbxAutosnipe.isSelected());
			chckbxLoadPreviousAutosniping.setEnabled(chckbxAutosnipe.isSelected());
			minIVToSnipe.setEnabled(chckbxAutosnipe.isSelected() & chckbxEnabled.isSelected());
			lblMinIv.setEnabled(chckbxAutosnipe.isSelected() & chckbxEnabled.isSelected());
			chckbxEnabled.setEnabled(chckbxAutosnipe.isSelected());
		});
		
		chckbxEnabled.addActionListener(a -> {
			if(chckbxAutosnipe.isSelected())
				chckbxCaptureConfirmation.setEnabled(!chckbxEnabled.isSelected());
			minIVToSnipe.setEnabled(chckbxAutosnipe.isSelected() & chckbxEnabled.isSelected());
			lblMinIv.setEnabled(chckbxAutosnipe.isSelected() & chckbxEnabled.isSelected());
		});
		
		queueDelay.setText(p.getProperty("queueDelay", "5000"));
		minIVToSnipe.setText(p.getProperty("autosnipeMinIV", "70"));
		chckbxEnabled.setSelected(Boolean.parseBoolean(p.getProperty("useAutosnipeMinIv", "false")));
		
		
		JButton btnSave = new JButton("Save & Sign In");
		contentPane.add(btnSave, "2, 20, 5, 1");
		btnSave.addActionListener(a -> {
			p.setProperty("type", String.valueOf(typeBox.getSelectedItem()));
			p.setProperty("username", usernameField.getText());
			p.setProperty("password", passwordField.getText());
			p.setProperty("unban", String.valueOf(chckbxForceUnban.isSelected()));
			p.setProperty("captureConfirmation", String.valueOf(chckbxForceUnban.isSelected()));
			p.setProperty("autosnipeLoadPrev", String.valueOf(chckbxLoadPreviousAutosniping.isSelected()));
			p.setProperty("autosnipe", String.valueOf(chckbxAutosnipe.isSelected()));
			p.setProperty("queueDelay", queueDelay.getText());
			p.setProperty("autosnipeMinIV", minIVToSnipe.getText());
			p.setProperty("useAutosnipeMinIv", String.valueOf(chckbxAutosnipe.isSelected()));
			try {
				p.store(new FileOutputStream(settingsFile), "");
			} catch (Exception e) {
				e.printStackTrace();
			}
			btnSave.setEnabled(false);
			btnSave.setText("Attempting Sign In...");
			SwingUtilities.invokeLater(() -> {
				if(MainWindow.isValidLogin(p, p.getProperty("type", "Google"), tokenField.getText())) {
					try {
						Log.SUCCESS.log("Successfully signed in as " + MainWindow.getGo().getPlayerProfile().getPlayerData().getUsername());
						setVisible(false);
						MainWindow.window.frame.requestFocus();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else Log.ERROR.log("Failed to sign in.");
				btnSave.setText("Save & Sign In");
				btnSave.setEnabled(true);
	        });
		});
		
		if(chckbxEnabled.isSelected())
			chckbxCaptureConfirmation.setEnabled(!chckbxAutosnipe.isSelected());
		if(chckbxAutosnipe.isSelected())
			chckbxCaptureConfirmation.setEnabled(!chckbxEnabled.isSelected());
		chckbxLoadPreviousAutosniping.setEnabled(chckbxAutosnipe.isSelected());
		minIVToSnipe.setEnabled(chckbxAutosnipe.isSelected() & chckbxEnabled.isSelected());
		lblMinIv.setEnabled(chckbxAutosnipe.isSelected() & chckbxEnabled.isSelected());
		chckbxEnabled.setEnabled(chckbxAutosnipe.isSelected());
	}

}
