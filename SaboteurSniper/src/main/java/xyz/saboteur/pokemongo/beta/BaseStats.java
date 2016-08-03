package xyz.saboteur.pokemongo.beta;

public class BaseStats {
	public int baseAttack, baseDefense, baseStamina;

	public BaseStats(int baseAttack, int baseDefense, int baseStamina) {
		this.baseAttack = baseAttack;
		this.baseDefense = baseDefense;
		this.baseStamina = baseStamina;
	}

	public int getBaseAttack() {
		return baseAttack;
	}

	public int getBaseDefense() {
		return baseDefense;
	}

	public int getBaseStamina() {
		return baseStamina;
	}
}