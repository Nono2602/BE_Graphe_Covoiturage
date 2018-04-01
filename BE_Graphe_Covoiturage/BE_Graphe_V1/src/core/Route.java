package core;

import java.util.ArrayList;

import base.Descripteur;

public class Route {

	private int longueur;
	private Noeud noeudDestination;
	private Segment[] segments;
	private Descripteur descripteur;
	
	public Route(int longueur, Noeud noeudDestination, Segment[] segments,
			Descripteur descripteur) {
		this.longueur = longueur;
		this.noeudDestination = noeudDestination;
		this.segments = segments;
		this.descripteur = descripteur;
	}

	public Route(Route autre) {
		this.longueur = autre.getLongueur();
		this.noeudDestination = new Noeud(autre.getNoeudDestination());
		this.segments = new Segment[autre.getSegments().length];
		for(int i = 0; i < autre.getSegments().length; i++) {
			this.segments[i] = autre.getSegments()[i];
		}
		this.descripteur = new Descripteur(autre.getDescripteur());
	}

	public int getLongueur() {
		return longueur;
	}

	public Noeud getNoeudDestination() {
		return noeudDestination;
	}

	public Segment[] getSegments() {
		return segments;
	}

	public Descripteur getDescripteur() {
		return descripteur;
	}
	
	public float cout(boolean isTemps, boolean pieton) {
		if(isTemps) {
			if(pieton) {
				return (this.longueur / (4 / 3.6f))/60;
			} else {
				return (this.longueur / (this.descripteur.vitesseMax() / 3.6f))/60;
			}
		}
		else {
			return this.longueur;
		}
	}

	public void setDestination(Noeud noeud) {
		this.noeudDestination = noeud;
	}
}
