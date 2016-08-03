package xyz.saboteur.pokemongo.beta;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public enum Log {
	DEFAULT("white"), SUCCESS("green"), ERROR("red"), INFO("aqua"), WARNING("yellow"), SUPPRESSED("gray");
	private String color;
	private static final DateFormat dateFormat = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss] ");
	
	Log(String color) {
		this.color = color;
	}
	
	public void log(String message) {
		try {
			MainWindow.kit.insertHTML(MainWindow.doc, MainWindow.doc.getLength(), String.format("<font style='font-family: Comic Sans MS; font-size: 11px; color: %s;'>%s</font><br />", color, dateFormat.format(Calendar.getInstance().getTime()) + message), 0, 0, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		MainWindow.textPane.repaint();
	}
}