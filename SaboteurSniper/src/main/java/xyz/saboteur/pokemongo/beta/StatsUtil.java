package xyz.saboteur.pokemongo.beta;

import com.pokegoapi.api.pokemon.PokemonMeta;
import com.pokegoapi.api.pokemon.PokemonMetaRegistry;

import POGOProtos.Data.PokemonDataOuterClass.PokemonData;

public class StatsUtil {
	public static double calculateCpMultiplier(PokemonData data) {
		PokemonMeta meta = PokemonMetaRegistry.getMeta(data.getPokemonId());
		return (meta.getBaseAttack() + data.getIndividualAttack())
				* Math.sqrt(meta.getBaseDefense() + data.getIndividualDefense())
				* Math.sqrt(meta.getBaseStamina() + data.getIndividualStamina());
	}

	public static int calculateMaxCp(PokemonData data) {
		return Math.max((int) Math.floor(0.1 * calculateMaxCpMultiplier(data)
				* Math.pow(data.getCpMultiplier() + data.getAdditionalCpMultiplier(), 2)), 10);
	}

	public static double calculateMaxCpMultiplier(PokemonData data) {
		PokemonMeta meta = PokemonMetaRegistry.getMeta(data.getPokemonId());
		return (meta.getBaseAttack() + 15) * Math.sqrt(meta.getBaseDefense() + 15)
				* Math.sqrt(meta.getBaseStamina() + 15);
	}

	public static int calculateMinCp(PokemonData data) {
		return Math.max((int) Math.floor(0.1 * calculateMinCpMultiplier(data)
				* Math.pow(data.getCpMultiplier() + data.getAdditionalCpMultiplier(), 2)), 10);
	}

	public static double calculateMinCpMultiplier(PokemonData data) {
		PokemonMeta meta = PokemonMetaRegistry.getMeta(data.getPokemonId());
		return meta.getBaseAttack() * Math.sqrt(meta.getBaseDefense()) * Math.sqrt(meta.getBaseStamina());
	}

	public static double calculatePokemonPerfection(PokemonData data) {
		// TODO: Confirm if 3.0 is more accurate than 4.0???
		if (Math.abs(data.getCpMultiplier() + data.getAdditionalCpMultiplier()) <= 0)
			return (data.getIndividualAttack() * 2 + data.getIndividualDefense() + data.getIndividualStamina())
					/ (3.0/* 4.0 */ * 15.0) * 100.0;

		double maxCp = calculateMaxCpMultiplier(data);
		double minCp = calculateMinCpMultiplier(data);
		double curCp = calculateCpMultiplier(data);

		return (curCp - minCp) / (maxCp - minCp) * 100.0;
	}

}
