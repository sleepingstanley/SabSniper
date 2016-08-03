package xyz.saboteur.pokemongo.beta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.text.WordUtils;

import com.google.common.base.Joiner;
import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.encounter.NormalEncounterResult;

import POGOProtos.Data.PokemonDataOuterClass.PokemonData;
import POGOProtos.Inventory.Item.ItemIdOuterClass.ItemId;
import xyz.saboteur.pokemongo.StatsUtil;

public class CaptureThread extends Thread {
	
	private Map<CustomPokemon, Integer> queue = new HashMap<>();
	private DecimalFormat ivFormat = new DecimalFormat("0.00");
	
	public void log(Log log, String m) {
		log.log(m);
	}
	
	public void addToQueue(CustomPokemon cp) {
		if(queue.containsKey(cp)) {
			log(Log.WARNING, cp.getType() + " @ " + cp.getLatitude() + ", " + cp.getLongitude() + " is already in capture queue.");
			return;
		}
		queue.put(cp, 0);
		log(Log.INFO, "Added " + cp.getType() + " @ " + cp.getLatitude() + ", " + cp.getLongitude() + " to capture queue. (Position :" + queue.size() + ")");
	}
	
	public void addToCatch(CustomPokemon cp) {
		if(!Boolean.parseBoolean(MainWindow.window.login.p.getProperty("autosnipe", "false"))) return;
		queue/*pokemonToCatch*/.put(cp, 1);
		log(Log.SUPPRESSED, "Added " + cp.getType() + " @ " + cp.getLatitude() + ", " + cp.getLongitude() + " to capture queue. (Position :" + queue.size() + ")");
	}
	
	public void clear() {
		log(Log.WARNING, "Cleared capture queue");
		queue.clear();
	}
	
	public void run() {
		while(true) {
			if(MainWindow.getGo() == null || queue.isEmpty()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			try {
				Thread.sleep(Integer.parseInt(MainWindow.window.login.p.getProperty("queueDelay", "5000")));
			} catch (Exception e1) {
				try {
					Thread.sleep(5000);
				} catch (Exception e2) {
					e1.printStackTrace();
				}
			}
			/*if(queue.isEmpty()) {
				if(!pokemonToCatch.isEmpty() && Boolean.parseBoolean(MainWindow.window.login.p.getProperty("autosnipe", "false")))
					queue.add(pokemonToCatch.remove(0));
				else continue;
			}*/
			CustomPokemon cPokemon = new ArrayList<>(queue.keySet()).get(0);
			int reason = queue.get(cPokemon);
			log(Log.WARNING, "Selecting next queued pokemon: " + cPokemon.getType());
			PokemonGo go = MainWindow.getGo();
			try {
				go.setLocation(cPokemon.getLatitude(), cPokemon.getLongitude(), 1);
				if(reason == 0)
					log(Log.SUPPRESSED, "Teleported to " + cPokemon.getLatitude() + ", " + cPokemon.getLongitude());
				if(go.getMap().getCatchablePokemon().isEmpty()) {
					if(reason == 0)
						log(Log.ERROR, "No nearby pokemon.");
					else
						log(Log.ERROR, cPokemon.getType() + " @ " + cPokemon.getLatitude() + ", " + cPokemon.getLongitude() + " - Fake");
					queue.remove(cPokemon);
					continue;
				}
				Optional<CatchablePokemon> o = go.getMap().getCatchablePokemon().stream().filter(pmon -> pmon.getPokemonId().name().equalsIgnoreCase(cPokemon.getType())).findFirst();
				if(!o.isPresent()) {
					if(reason == 0)
						log(Log.ERROR, "No " + WordUtils.capitalizeFully(cPokemon.getType()) + " nearby.");
					else
						log(Log.ERROR, cPokemon.getType() + " @ " + cPokemon.getLatitude() + ", " + cPokemon.getLongitude() + " - Fake");
					queue.remove(cPokemon);
					continue;
				}
				CatchablePokemon pokemon = o.get();
				
				NormalEncounterResult er = (NormalEncounterResult) pokemon.encounterPokemon();
				PokemonData data = er.getWildPokemon().getPokemonData();
				String name = WordUtils.capitalizeFully(pokemon.getPokemonId().name());
				
				if(data.getCp() == 0) {
					if(reason == 0)
						log(Log.WARNING, name + " has expired");
					queue.remove(cPokemon);
					continue;
				}
				
				log(Log.INFO, "Found " + name + ": ");
				log(Log.INFO, "    CP: " + data.getCp());
				double iv = StatsUtil.calculatePokemonPerfection(data);
				log(iv > 90 ? Log.ERROR : iv > 50 ? Log.WARNING : iv > 25 ? Log.INFO : Log.SUPPRESSED, "    IV: " + ivFormat.format(iv) + "%");
				String move1 = WordUtils.capitalizeFully(er.getWildPokemon().getPokemonData().getMove1().name().replace("_", " "));
				String move2 = WordUtils.capitalizeFully(er.getWildPokemon().getPokemonData().getMove2().name().replace("_", " "));
				log(Log.INFO, "    Move #1: " + move1);
				log(Log.INFO, "    Move #2: " + move2);
				
				if(MainWindow.window.login.chckbxEnabled.isEnabled() && MainWindow.window.login.chckbxEnabled.isSelected()) {
					if(iv < Double.parseDouble(MainWindow.window.login.p.getProperty("autosnipeMinIV", "60"))) {
						log(Log.WARNING, "(IV Too Low @ " + ivFormat.format(iv) + ") Fleeing from " + cPokemon.getType() + " @ " + cPokemon.getLatitude() + ", " + cPokemon.getLongitude());
						queue.remove(cPokemon);
						continue;
					}
				} else if(Boolean.parseBoolean(MainWindow.window.login.p.getProperty("captureConfirmation", "true"))){
					int dialogResult = JOptionPane.showConfirmDialog (null, name + " " + data.getCp() + "CP\nIV: " + ivFormat.format(iv) + "\nMove #1: " + move1 + "\nMove #2: " + move2, "Capture Pokemon?", JOptionPane.YES_NO_OPTION);
					if(dialogResult != JOptionPane.YES_OPTION){
						log(Log.WARNING, "Fleeing from " + cPokemon.getType() + " @ " + cPokemon.getLatitude() + ", " + cPokemon.getLongitude());
						queue.remove(cPokemon);
						continue;
					}
				}				
				
				if(Boolean.parseBoolean(MainWindow.window.login.p.getProperty("unban", "true"))) {
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
									//log(Log.SUPPRESSED, "<font style='font-family: Consolas; font-size: 10px'>Force unban attempt #" + attempts + "</font>");
									if(r.getExperience() != 0) {
										log(Log.SUCCESS, String.format("Fuck yes, you got unbanned after %s attempt(s). (%sXP)", attempts, r.getExperience()));
										break;
									}
								}
							}
						} else {
							log(Log.ERROR, "No usable Pokestops nearby");
							queue.remove(cPokemon);
							continue;
						}
				    } catch (Exception e) {
				    	log(Log.WARNING, "Try signing in and try again");
				    	continue;
				    }
				}
				log(Log.INFO, "Attempting capture of " + cPokemon.getType());
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
				queue.remove(cPokemon);
			} catch(Exception e) {
				log(Log.WARNING, "Try signing in and try again");
			}
		}
	}	
}
