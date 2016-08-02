package xyz.saboteur.pokemongo.beta;

public class CustomPokemon {
	private String type;
	private double latitude, longitude;
	
	public CustomPokemon(String type, double latitude, double longitude) {
		super();
		this.type = type;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getType() {
		return type;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}	
}
