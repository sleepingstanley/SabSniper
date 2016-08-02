package xyz.saboteur.pokemongo.beta;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Strings;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;
import com.pokegoapi.util.Log.Level;

import POGOProtos.Enums.PokemonIdOuterClass.PokemonId;
import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.OkHttpClient;

public class MainWindow {
	public JFrame frame;
	private JTextField textField;
	static JTextPane textPane;
	static JScrollPane scrollPane;
	public JLabel lblStatus;
	private String version;
	private DecimalFormat ivFormat = new DecimalFormat("0.00");
	
	public static MainWindow window;
	
	private static PokemonGo go;
	
	public static PokemonGo getGo() { return go; }
	
	public static CaptureThread captureQueue = new CaptureThread();

	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					com.pokegoapi.util.Log.setLevel(Level.ASSERT);
					window = new MainWindow();
					window.frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		new StatusThread().start();
		captureQueue.start();
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		try {
	        Properties p = new Properties();
	        InputStream is = getClass().getResourceAsStream("/version");
	        if (is != null) {
	            p.load(is);
	            version = p.getProperty("version", "");
	        }
	    } catch (Exception ignored) { }
		getGithubVersion();
		initialize();
		checkForUpdates();
	}
	
	public static String getToken(String authCode) {
		try {
			GoogleUserCredentialProvider provider = new GoogleUserCredentialProvider(new OkHttpClient());
			provider.login(authCode);
			return provider.getRefreshToken();
		} catch(Exception e) {
			return null;
		}
	}
	
	public static boolean isValidLogin(Properties p, String type, String authCodeOrToken) {
		OkHttpClient httpClient = new OkHttpClient();
		if(type.equalsIgnoreCase("Google")) {
			GoogleUserCredentialProvider provider;
			String token = null;
			try {
				provider = new GoogleUserCredentialProvider(new OkHttpClient());
				provider.login(authCodeOrToken);
				token = provider.getRefreshToken();
				if(token != null) {
					p.setProperty("token", token);
					try {
						p.store(new FileOutputStream(new File("settings.txt")), "");
					} catch (Exception e) {
						e.printStackTrace();
					}
					go = new PokemonGo(provider, httpClient);
					return true;
				}
			} catch(Exception e) { }
			try {
				go = new PokemonGo(new GoogleUserCredentialProvider(httpClient, p.getProperty("token")), httpClient);
				return true;
			} catch(Exception e2) {
				e2.printStackTrace();
				return false;
			}
		} else if(type.equalsIgnoreCase("PTC")) {
			try {
				go = new PokemonGo(new PtcCredentialProvider(httpClient, p.getProperty("username"), p.getProperty("password")), httpClient);
				return true;
			} catch(Exception ignored) { }
		}
		return false;
	}
	
	private String generateHeader() {
		List<String> header = Arrays.asList(new String[] {
			"      ,gg,                               ,gg,",
			"      i8\"\"8i               ,dPYb,        i8\"\"8i",
			"      `8,,8'               IP'`Yb        `8,,8'",
			"       `88'                I8  8I         `88'                   gg",
			"       dP\"8,               I8  8'         dP\"8,                  \"\"",
			"      dP' `8a    ,gggg,gg  I8 dP         dP' `8a   ,ggg,,ggg,    gg   gg,gggg,     ,ggg,    ,gggggg,",
			"     dP'   `Yb  dP\"  \"Y8I  I8dP   88gg  dP'   `Yb ,8\" \"8P\" \"8,   88   I8P\"  \"Yb   i8\" \"8i   dP\"\"\"\"8I",
			" _ ,dP'     I8 i8'    ,8I  I8P    8I_ ,dP'     I8 I8   8I   8I   88   I8'    ,8i  I8, ,8I  ,8'    8I",
			" \"888,,____,dP,d8,   ,d8b,,d8b,  ,8I\"888,,____,dP,dP   8I   Yb,_,88,_,I8 _  ,d8'  `YbadP' ,dP     Y8,",
			" a8P\"Y88888P\" P\"Y8888P\"`Y88P'\"Y88P\"'a8P\"Y88888P\" 8P'   8I   `Y88P\"\"Y8PI8 YY88888P888P\"Y8888P      `Y8",
			"                                                                      I8",
			"                                                                      I8",
			"                                                                      I8",
			"                                                                      I8",
			"                                                                      I8",
			"                                                                      I8"
		});
		
		String message = "";
		
		for(String s : header) {
			boolean started = false;
			for(int i = 0; i < s.length(); i++) {
				String cur = s.substring(i, i+1);
				String next = i == (s.length() - 1) ? "" : "" + s.charAt(i + 1);
				if(cur.trim().length() == 0) {
					if(!started) {
						message += "<font!color='black'>";
						started = true;
					}
					message += cur;
					if(next.trim().length() != 0 && started) {
						message += "</font>";
						started = false;
					}
				} else message += cur;
			}
			message += "<br/>";
		}
		message = message.replaceAll("(?<=(^|\\G)\\S{0,100}\\s\\S{0,100})\\s", "#").replace("!", " ");
		return message;
	}
	
	Login login = new Login();
	private Items items = new Items();
	private PokemonView pokemonView = new PokemonView();

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("SabSniper v" + version);
		frame.setBounds(100, 100, 1066, 606);
		
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(57dlu;default)"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("358dlu:grow"),
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
				RowSpec.decode("max(261dlu;default):grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("max(15dlu;default)"),}));
		List<PokemonId> possible = new ArrayList<>(Arrays.asList(PokemonId.values()));
		possible.remove(PokemonId.MISSINGNO);
		possible.remove(PokemonId.UNRECOGNIZED);
		possible.remove(PokemonId.NIDORAN_FEMALE);
		possible.remove(PokemonId.NIDORAN_MALE);
		Collections.sort(possible, (PokemonId p1, PokemonId p2)->p1.name().compareTo(p2.name()));
		
		JLabel lblType = new JLabel("Type:");
		frame.getContentPane().add(lblType, "2, 2, right, default");
		
		JComboBox<String> comboBox = new JComboBox<>();
		comboBox.setMaximumRowCount(25);
		comboBox.setEditable(true);
		frame.getContentPane().add(comboBox, "4, 2, fill, fill");
		for(PokemonId pId : possible)
			comboBox.addItem(WordUtils.capitalizeFully(pId.name().replace("_", " ")));
		
		JLabel lblCoordinates = new JLabel("Coordinates:");
		frame.getContentPane().add(lblCoordinates, "2, 4, right, default");
		
		textField = new JTextField();
		frame.getContentPane().add(textField, "4, 4, fill, default");
		textField.setColumns(10);
		
		JButton btnLoad = new JButton("Search");
		frame.getContentPane().add(btnLoad, "4, 6");
		btnLoad.addActionListener(a -> {
			try {
				double lat = Double.parseDouble(textField.getText().replace(" ", "").split(",")[0]);
				double lon = Double.parseDouble(textField.getText().replace(" ", "").split(",")[1]);
				captureQueue.addToQueue(new CustomPokemon((String)comboBox.getSelectedItem(), lat, lon));
			} catch (Exception e) {
				log(Log.ERROR, "Invalid coordinates. Should be formatted similarly to: x.xxxxx, x.xxxxx");
				return;
			}
		});
		
		scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {  
	        public void adjustmentValueChanged(AdjustmentEvent e) {  
	            e.getAdjustable().setValue(e.getAdjustable().getMaximum());  
	        }
	    });
		frame.getContentPane().add(scrollPane, "2, 8, 3, 3, fill, fill");
		
		textPane = new JTextPane();
		textPane.setBackground(Color.BLACK);
		textPane.setEditable(false);
		textPane.setContentType("text/html");
		textPane.setEditorKit(kit);
	    textPane.setDocument(doc);
	    ((DefaultCaret)textPane.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		textPane.setForeground(Color.white);
	    
	    String[] colors = new String[] {"red", "orange", "yellow", "lime", "aqua", "blue", "purple", "#8D38C9"};
	    
		textPane.setText("<br /><font style='font-family: Consolas; color: " + colors[ThreadLocalRandom.current().nextInt(colors.length)] + ";'>" + generateHeader() + "</font><br /><font color='white'>" + Strings.repeat("-", 180) + "</font><br />");
		scrollPane.setViewportView(textPane);
		
		lblStatus = new JLabel("Status");
		frame.getContentPane().add(lblStatus, "2, 12, 3, 1");
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnTools = new JMenu("Options");
		menuBar.add(mnTools);
		
		JMenu mnView = new JMenu("View");
		menuBar.add(mnView);
		
		JMenuItem mntmInventory = new JMenuItem("Inventory");
		mntmInventory.addActionListener(a -> items.setVisible(true));
		mnView.add(mntmInventory);
		
		JMenuItem mntmPokemonInventory = new JMenuItem("Pokemon");
		mntmPokemonInventory.addActionListener(a -> pokemonView.setVisible(true));
		mnView.add(mntmPokemonInventory);	
		
		JMenuItem mntmSignIn = new JMenuItem("Sign In");
		mntmSignIn.addActionListener(a -> attemptSignIn(null));
		mnTools.add(mntmSignIn);		
		
		JMenuItem mntmForceUnban = new JMenuItem("Force Unban");
		mntmForceUnban.addActionListener(a -> this.forceUnban(null));
		mnTools.add(mntmForceUnban);
		mnTools.addSeparator();
		
		JMenuItem mntmSettings = new JMenuItem("Edit Settings");
		mntmSettings.addActionListener(a -> login.setVisible(true));
		mnTools.add(mntmSettings);
		
		JMenuItem mntmCheckUpdates = new JMenuItem("Check For Updates");
		mntmCheckUpdates.addActionListener(a -> {
			checkForUpdates();
		});
		mnTools.add(mntmCheckUpdates);
		
		log(Log.SUCCESS, "SabSniper v" + version + " | Website: http://saboteur.xyz | Discord: <u>https://discord.gg/KX7EvX2</u>");
		log(Log.WARNING, "GitHub URL: <u>https://github.com/saboteurxyz/SabSniper</u> | Latest version: " + githubVersion);
		
		new Thread() {
			
			public void run() {
				Socket socket;
				try {
					socket = IO.socket("http://spawns.sebastienvercammen.be:49002");
				} catch (URISyntaxException e1) {
					log(Log.ERROR, "Couldn't use socket. Try restarting the program?");
					return;
				}
				socket.on(Socket.EVENT_CONNECT, args -> {
					log(Log.INFO, "Connected to spawns server");
				});
				socket.on(Socket.EVENT_DISCONNECT, args -> {
					log(Log.WARNING, "Disconnected from spawns server");
				});
				if(Boolean.parseBoolean(login.p.getProperty("autosnipeLoadPrev", "false"))) {
					socket.on("helo", args -> {
						JSONArray arr = (JSONArray)args[0];
						for (int i = 0; i < arr.length(); i++) {
							try {
								JSONObject o = arr.getJSONObject(i);
								captureQueue.addToCatch(new CustomPokemon(o.getString("name"), Double.parseDouble(o.getString("lat")), Double.parseDouble(o.getString("lon"))));
							} catch(Exception ignored) {}
						}				
					});
				}
				socket.on("poke", args -> {
					JSONObject o = (JSONObject)args[0];
					try {
						captureQueue.addToCatch(new CustomPokemon(o.getString("name"), Double.parseDouble(o.getString("lat")), Double.parseDouble(o.getString("lon"))));
					} catch(Exception ignored) {}
				});
				while(true) {
					if(!socket.connected()) {
						try {
							log(Log.SUPPRESSED, "Attempting connection to spawn server.");
							socket.connect();
							Thread.sleep(1000);
							if(!socket.connected()) {
								log(Log.WARNING, "Failed to connect to spawn servers. Waiting 5s to retry. (Is it down?)");
								Thread.sleep(5000);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
		}.start();
	}
	
	/*public void tryCapture(String type, String latlong) {
		if(isBusy) {
			log(Log.WARNING, "Wait for last action to finish before trying to capture.");
			while(isBusy) {}
		}
		double lat, lon;
		try {
			lat = Double.parseDouble(latlong.replace(" ", "").split(",")[0]);
			lon = Double.parseDouble(latlong.replace(" ", "").split(",")[1]);
		} catch (Exception e) {
			log(Log.ERROR, "Invalid coordinates");
			return;
		}
		try {
			go.setLocation(lat, lon, 1);
			log(Log.SUPPRESSED, "Teleported to " + lat + ", " + lon);
			if(go.getMap().getCatchablePokemon().isEmpty()) {
				log(Log.ERROR, "No nearby pokemon.");
				return;
			}
			Optional<CatchablePokemon> o = go.getMap().getCatchablePokemon().stream().filter(pmon -> pmon.getPokemonId().name().equalsIgnoreCase(type)).findFirst();
			if(!o.isPresent()) {
				log(Log.ERROR, "No " + WordUtils.capitalizeFully(type) + " nearby.");
				return;
			}
			CatchablePokemon pokemon = o.get();
			
			NormalEncounterResult er = (NormalEncounterResult) pokemon.encounterPokemon();
			PokemonData data = er.getWildPokemon().getPokemonData();
			String name = WordUtils.capitalizeFully(pokemon.getPokemonId().name());
			
			if(data.getCp() == 0) {
				log(Log.WARNING, name + " has expired");
				return;
			}
			
			log(Log.INFO, "Found " + name + ": ");
			log(Log.INFO, "    CP: " + data.getCp());
			double iv = StatsUtil.calculatePokemonPerfection(data);
			log(iv > 90 ? Log.ERROR : iv > 50 ? Log.WARNING : iv > 25 ? Log.INFO : Log.SUPPRESSED, "    IV: " + ivFormat.format(iv) + "%");
			String move1 = WordUtils.capitalizeFully(er.getWildPokemon().getPokemonData().getMove1().name().replace("_", " "));
			String move2 = WordUtils.capitalizeFully(er.getWildPokemon().getPokemonData().getMove2().name().replace("_", " "));
			log(Log.INFO, "    Move #1: " + move1);
			log(Log.INFO, "    Move #2: " + move2);
			
			if(Boolean.parseBoolean(login.p.getProperty("captureConfirmation", "true"))){
				int dialogResult = JOptionPane.showConfirmDialog (null, name + " " + data.getCp() + "CP\nIV: " + ivFormat.format(iv) + "\nMove #1: " + move1 + "\nMove #2: " + move2, "Capture Pokemon?", JOptionPane.YES_NO_OPTION);
				if(dialogResult != JOptionPane.YES_OPTION){
					log(Log.WARNING, "Fleeing.");
					return;
				}
			}
			Thread t = new Thread() {
				public void run() {
					if(unbanQueued) {
						log(Log.SUPPRESSED, "Waiting to be unbanned before capturing.");
						while(true) {if(!unbanQueued) break;}
						log(Log.INFO, "Attempting capture");
					}
					isBusy = true;
					try {
						Map<ItemId, Integer> before = new HashMap<>(), after = new HashMap<>();
						go.getInventories().getItemBag().getItems().forEach(item -> before.put(item.getItemId(), item.getCount()));
						CatchResult result = pokemon.catchPokemon();
						Thread.sleep(300);
						go.getInventories().getItemBag().getItems().forEach(item -> after.put(item.getItemId(), item.getCount()));
						List<String> used = new ArrayList<>();
						after.entrySet().stream().filter(e -> before.get(e.getKey()) > e.getValue()).forEach(e -> used.add((before.get(e.getKey()) - e.getValue()) + "x " + WordUtils.capitalizeFully(e.getKey().name().replace("_", " ").replace("ITEM ", ""))));
						log(Log.SUPPRESSED, "Used: " + Joiner.on(", ").join(used));
						log(result.isFailed() ? Log.ERROR : Log.SUCCESS, "(" + WordUtils.capitalizeFully(result.getStatus().name().replace("_", " ")).replace(" ", "") + ") - " + (result.isFailed() ? "Fuck. It got away." : "Fuck yes, you caught it!"));
					} catch(Exception e) {
						log(Log.ERROR, "An error has occurred. Fleeing.");
					}
					isBusy = false;
				}
			};
			
			if(Boolean.parseBoolean(login.p.getProperty("unban", "true")))
				forceUnban(t);
			else
				t.start();
		} catch(Exception e) {
			log(Log.WARNING, "Attempting sign in...");
			attemptSignIn(new Thread() {
				public void run() {
					tryCapture(type, latlong);
				}
			});
		}
	}*/
	
	private String githubVersion;
	
	public void getGithubVersion() {
		try {
	        Properties p = new Properties();
	        InputStream is = new URL("https://raw.githubusercontent.com/saboteurxyz/SabSniper/master/SabSniper/src/version").openStream();
	        p.load(is);
	        githubVersion = p.getProperty("version", "");
	    } catch (Exception ignored) { }
	}
	
	public void checkForUpdates() {
		SwingUtilities.invokeLater(() -> {
			try {
				getGithubVersion();
				Thread.sleep(100);
		        if(Double.parseDouble(githubVersion) > Double.parseDouble(version)) {
					File updater = new File("Updater.exe");
					if(!updater.exists()) {
						FileUtils.copyURLToFile(new URL("https://github.com/saboteurxyz/SabSniper/releases/download/AutoUpdater-1.0/Updater.exe"), updater);
						Thread.sleep(1000);
					}
					Desktop.getDesktop().browse(updater.toURI());
					Thread.sleep(100);
					System.exit(0);
				} else {
					log(Log.INFO, "Current version is up to date to GitHub version (Version: " + version + " | GitHub: " + githubVersion + ")");
				}
		    } catch (Exception ignored) { }
        });
	}
	
	public void forceUnban(Thread toStart) {
		if(unbanQueued) {
			log(Log.WARNING, "A force unban attempt is already queued.");
			return;
		}
		new Thread() {
			public void run() {
				if(isBusy) {
					log(Log.WARNING, "Waiting last action to finish before unbanning.");
					while(isBusy) {}
				}
				unbanQueued = true;
				try {
					if(!go.getMap().getMapObjects().getPokestops().isEmpty()) {
						Optional<Pokestop> o2 = go.getMap().getMapObjects().getPokestops().stream().filter(pokestop -> pokestop.canLoot(true)).findAny();
						if(o2.isPresent()) {
							Pokestop ps = o2.get();
							int attempts = 0;
							go.setLocation(ps.getLatitude(), ps.getLongitude(), 1);
							log(Log.SUPPRESSED, "Teleported to " + ps.getLatitude() + ", " + ps.getLongitude());
							go.getMap().getMapObjects();
							log(Log.INFO, String.format("Using Pokestop \"%s\" for forceful unban.", ps.getDetails().getName()));
							while(true) {
								PokestopLootResult r = ps.loot();
								attempts++;
								if(r.getExperience() != 0) {
									unbanQueued = false;
									log(Log.SUCCESS, String.format("Fuck yes, you got unbanned after %s attempt(s). (%sXP)", attempts, r.getExperience()));
									if(toStart != null)
										toStart.start();
									break;
								}
							}
						}
					} else {
						log(Log.ERROR, "No usable Pokestops nearby");
					}
			    } catch (Exception e) {
			    	log(Log.WARNING, "Attempting sign in...");
			    	unbanQueued = false;
					attemptSignIn(new Thread() {
						public void run() {
							forceUnban(toStart);
						}
					});
			    }
				unbanQueued = false;
			}
		}.start();
	}
	
	public void attemptSignIn(Thread after) {
		new Thread() {
			public void run() {
				if(isValidLogin(login.p, login.p.getProperty("type", "Google"), login.p.getProperty("token", ""))) {
					try {
						Log.SUCCESS.log("Successfully signed in to " + MainWindow.getGo().getPlayerProfile().getPlayerData().getUsername());
					} catch (Exception e) {
						e.printStackTrace();
					}
					if(after != null)
						after.start();
				} else {
					Log.ERROR.log("Failed to sign in.");
					login.setVisible(true);
					return;
				}
			}
		}.start();
	}
	
	private boolean unbanQueued = false, isBusy = false;
	
	static HTMLEditorKit kit = new HTMLEditorKit();
	static HTMLDocument doc = new HTMLDocument();
	
	
	
	public void log(String message) {
		log(Log.DEFAULT, message);
	}
	
	public void log(Log reason, String message) {
		reason.log(message);
	}
	

}
