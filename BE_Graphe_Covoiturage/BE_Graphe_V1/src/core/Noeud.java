package core;

import java.util.ArrayList;

public class Noeud {
	
	private float longitude;
	private float latitude;
	private ArrayList<Route> routesSortantes;
	private int numero;
	
	public Noeud(float longitude, float latitude, int numero) {
		this.longitude = longitude;
		this.latitude = latitude;
		this.numero = numero;
		this.routesSortantes = new ArrayList<>();
	}
	
	public Noeud(Noeud autre) {
		this.longitude = autre.getLongitude();
		this.latitude = autre.getLatitude();
		this.numero = autre.getNumero();
		this.routesSortantes = new ArrayList<>();
	}

	public float getLongitude() {
		return longitude;
	}

	public float getLatitude() {
		return latitude;
	}

	public ArrayList<Route> getRoutesSortantes() {
		return routesSortantes;
	}
	
	public void addRouteSortante(Route routeSortante) {
		this.routesSortantes.add(routeSortante);
	}
	
	public void addRoutesSortante(ArrayList<Route> routeSortante) {
		this.routesSortantes.addAll(routeSortante);
	}
	
	public int getNumero() {
		return this.numero;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Noeud) {
			Noeud noeud = (Noeud)other;
			return this.longitude == noeud.longitude && this.latitude == noeud.latitude;
		}
		return false;
	}
}
