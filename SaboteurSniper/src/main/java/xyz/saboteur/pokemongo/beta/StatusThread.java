package xyz.saboteur.pokemongo.beta;

import java.text.DecimalFormat;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.inventory.Stats;
import com.pokegoapi.api.player.PlayerProfile;
import com.pokegoapi.api.player.PlayerProfile.Currency;

import POGOProtos.Data.PlayerDataOuterClass.PlayerData;

public class StatusThread extends Thread {

	DecimalFormat commaFormat = new DecimalFormat("#,###");
	
	private long startTime = System.currentTimeMillis();
	
	public void run() {
		while(true) {
			try {
				if(MainWindow.getGo() == null) {
					Thread.sleep(10);
					continue;
				}
				PokemonGo go = MainWindow.getGo();
			
				PlayerProfile profile = go.getPlayerProfile();
				PlayerData data = profile.getPlayerData();
				Stats stats = profile.getStats();
				int totalSecs = (int)(System.currentTimeMillis() - startTime)/1000;
				int hours = totalSecs / 3600;
				int minutes = (totalSecs % 3600) / 60;
				int seconds = totalSecs % 60;

				String runTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
				MainWindow.window.lblStatus.setText(data.getUsername() + " - Lv." + stats.getLevel() + " | Stardust: " + commaFormat.format(go.getPlayerProfile().getCurrency(Currency.STARDUST)) + " | Pokemon: " + go.getInventories().getPokebank().getPokemons().size() + "/" + data.getMaxPokemonStorage() + " | Items: " + go.getInventories().getItemBag().getItemsCount() + "/" + data.getMaxItemStorage() + " | Program Runtime: " + runTime);
				Thread.sleep(1000);
			} catch(Exception ignored) { }
		}
	}
	
}
