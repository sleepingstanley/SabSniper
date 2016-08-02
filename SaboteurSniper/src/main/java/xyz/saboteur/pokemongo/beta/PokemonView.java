package xyz.saboteur.pokemongo.beta;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.WordUtils;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.pokemon.Pokemon;

import xyz.saboteur.pokemongo.StatsUtil;

public class PokemonView extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	public PokemonView() {
		setTitle("Pokemon");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 550, 500);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		DefaultListModel<Pokemon> modal = new DefaultListModel<>();
		JList<Pokemon> list = new JList<>(modal);
		list.setCellRenderer(new PokemonCellRenderer());
		scrollPane.setViewportView(list);
		/*File folder = new File("D:\\Users\\infin\\Desktop\\PokemonView-GO-App-Assets-and-Images-master\\PokemonView");
		for(File f : folder.listFiles())
			f.renameTo(new File(folder, PokemonId.forNumber(Integer.parseInt(f.getName().split("\\.")[0])).name() + ".png"));*/
		this.addWindowListener(new WindowListener() {
			@Override public void windowActivated(WindowEvent e) {
				modal.clear();
				try {
					PokemonGo go = MainWindow.getGo();
					setTitle("Pokemon | " + go.getInventories().getPokebank().getPokemons().size() + "/" + go.getPlayerProfile().getPlayerData().getMaxPokemonStorage());
					go.getInventories().getPokebank().getPokemons().stream().sorted((p1, p2) -> ((Integer)p2.getCp()).compareTo((Integer)p1.getCp())).forEach(pokemon -> {
						modal.addElement(pokemon);
					});
				} catch(Exception ex) {
					Log.WARNING.log("Attempting sign in...");
					MainWindow.window.attemptSignIn(new Thread() {
						public void run() {
							windowActivated(e);
						}
					});
				}
			}
			@Override public void windowClosed(WindowEvent e) { }
			@Override public void windowClosing(WindowEvent e) { }
			@Override public void windowDeactivated(WindowEvent e) { }
			@Override public void windowDeiconified(WindowEvent e) { }
			@Override public void windowIconified(WindowEvent e) { }
			@Override public void windowOpened(WindowEvent e) { }
		});
	}
	
	class PokemonCellRenderer extends JLabel implements ListCellRenderer {

		  public PokemonCellRenderer() {
		    setOpaque(true);
		    setIconTextGap(12);
		  }

		  public Component getListCellRendererComponent(JList list, Object value,
		      int index, boolean isSelected, boolean cellHasFocus) {
		    Pokemon pokemon = (Pokemon) value;
		    setText(WordUtils.capitalizeFully(pokemon.getPokemonId().name().replace("_", " ")) + " | " + pokemon.getCp() + "CP");
		    try {
		    	ImageIcon icon;
		    	if(PokemonView.class.getClassLoader().getResource("assets/pokemon/" + pokemon.getPokemonId().name() + ".png")!=null) {
		    		icon = new ImageIcon(IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("assets/pokemon/" + pokemon.getPokemonId().name() + ".png")), WordUtils.capitalizeFully(pokemon.getPokemonId().name().replace("_", " ")));
		    	} else icon = new ImageIcon(IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("assets/items/ITEM_NONE.png")), WordUtils.capitalizeFully(pokemon.getPokemonId().name().replace("_", " ")));
		    	//getClass().getClassLoader().getResourceAsStream("assets/items/" + item.getItemId().name() + ".png")
		    	
				setIcon(new ImageIcon(icon.getImage().getScaledInstance(32, 32, Image.SCALE_REPLICATE)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		    setBackground(Color.white);
	        setForeground(Color.black);
		    Font labelFont = getFont();
		    setFont(new Font(labelFont.getName(), Font.PLAIN, 24));
		    return this;
		  }
		}
}
