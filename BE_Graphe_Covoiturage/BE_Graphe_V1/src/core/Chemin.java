package core;

import java.util.ArrayList;

public class Chemin {

	private Graphe carte;
	private Noeud[] noeuds;
	
	public Chemin(Graphe carte, Noeud[] noeuds) {
		this.carte = carte;
		this.noeuds = noeuds;
	}

	public Graphe getCarte() {
		return carte;
	}

	public Noeud[] getNoeuds() {
		return noeuds;
	}

	public float coutChemin(boolean isTemps) {
		float cout = 0;
		for(int i = 0; i < noeuds.length-1; i++) {
			ArrayList<Route> routes = noeuds[i].getRoutesSortantes();
			// On cherche le chemin au cout minimal car il peut exister plusieurs route pour aller d'un point à un autre
			float min = Float.MAX_VALUE;
			for(int j = 0; j < routes.size(); j++) {
				Route route = routes.get(j);
				if(route.getNoeudDestination().equals(noeuds[i+1])) {
					float val = 0;
					if(isTemps) {
						val = route.getLongueur() / (route.getDescripteur().vitesseMax() / 3.6f);
					}
					else {
						val = route.getLongueur();
					}
					if(min > val) {
						min = val;
					}
				}
			}
			cout += min;
		}
		if(isTemps) {
			cout = cout/60;
		}
		return cout;
	}
}
