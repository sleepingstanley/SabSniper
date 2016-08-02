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
import com.pokegoapi.api.inventory.Item;

public class Items extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	public Items() {
		setTitle("Items");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 550, 500);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		DefaultListModel<Item> modal = new DefaultListModel<>();
		JList<Item> list = new JList<>(modal);
		list.setCellRenderer(new ItemCellRenderer());
		scrollPane.setViewportView(list);
		/*File folder = new File("D:\\Users\\infin\\Desktop\\PokemonView-GO-App-Assets-and-Images-master\\PokemonView");
		for(File f : folder.listFiles())
			f.renameTo(new File(folder, PokemonId.forNumber(Integer.parseInt(f.getName().split("\\.")[0])).name() + ".png"));*/
		this.addWindowListener(new WindowListener() {
			@Override public void windowActivated(WindowEvent e) {
				modal.clear();
				try {
					PokemonGo go = MainWindow.getGo();
					setTitle("Items | " + go.getInventories().getItemBag().getItemsCount() + "/" + go.getPlayerProfile().getPlayerData().getMaxItemStorage());
					go.getInventories().getItemBag().getItems().stream().sorted((i1, i2) -> ((Integer)i2.getCount()).compareTo((Integer)i1.getCount())).forEach(item -> {
						/*JLabel label = new JLabel(item.getCount() + "x " + WordUtils.capitalizeFully(item.getItemId().name().replace("ITEM_", "").replace("_", " ")));
						try {
							label.setIcon(new ImageIcon(IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("/assets/items/" + item.getItemId().name() + ".png")), WordUtils.capitalizeFully(item.getItemId().name().replace("ITEM_", "").replace("_", " "))));
						} catch (Exception e1) {
							MainWindow.Log.ERROR.log("Error while loading inventory (Check that you're signed in first)");
						}*/
						modal.addElement(item);
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
	
	class ItemCellRenderer extends JLabel implements ListCellRenderer {

		  public ItemCellRenderer() {
		    setOpaque(true);
		    setIconTextGap(12);
		  }

		  public Component getListCellRendererComponent(JList list, Object value,
		      int index, boolean isSelected, boolean cellHasFocus) {
		    Item item = (Item) value;
		    setText(item.getCount() + "x " + WordUtils.capitalizeFully(item.getItemId().name().replace("ITEM_", "").replace("_", " ")));
		    try {
		    	ImageIcon icon;
		    	if(Items.class.getClassLoader().getResource("assets/items/" + item.getItemId().name() + ".png")!=null) {
		    		icon = new ImageIcon(IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("assets/items/" + item.getItemId().name() + ".png")), WordUtils.capitalizeFully(item.getItemId().name().replace("ITEM_", "").replace("_", " ")));
		    	} else icon = new ImageIcon(IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("assets/items/ITEM_NONE.png")), WordUtils.capitalizeFully(item.getItemId().name().replace("ITEM_", "").replace("_", " ")));
		    	//getClass().getClassLoader().getResourceAsStream("assets/items/" + item.getItemId().name() + ".png")
		    	
				setIcon(new ImageIcon(icon.getImage().getScaledInstance(48, 48, Image.SCALE_FAST)));
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
