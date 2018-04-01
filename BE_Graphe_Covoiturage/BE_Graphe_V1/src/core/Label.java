package core;

import java.util.ArrayList;

public class Label implements Comparable<Label> {
	private boolean marquage;
	private float cout;
	private Noeud pere;
	private Noeud sommetCourant;
	
	public Label(boolean marquage, float cout, Noeud pere, Noeud sommetCourant) {
		this.marquage = marquage;
		this.cout = cout;
		this.pere = pere;
		this.sommetCourant = sommetCourant;
	}
	
	public boolean isMarque() {
		return marquage;
	}
	public void marquer(boolean marquage) {
		this.marquage = marquage;
	}
	public float getCout() {
		return cout;
	}
	public void setCout(float cout) {
		this.cout = cout;
	}
	public Noeud getPere() {
		return pere;
	}
	public void setPere(Noeud pere) {
		this.pere = pere;
	}
	public Noeud getSommetCourant() {
		return sommetCourant;
	}
	public void setSommetCourant(Noeud sommetCourant) {
		this.sommetCourant = sommetCourant;
	}
	
	public float getCoutDest() {
		return 0;
	}
	
	public void setCoutDest(float coutDest) {
		//rien
	}

	@Override
	public int compareTo(Label autre) {
		if(autre.getCout() > this.getCout()) {
			return -1;
		}
		else if(autre.getCout() < this.getCout()) {
			return 1;
		}
		else {
			return 0;
		}
	}

	public Label findIn(ArrayList<Label> listeNoeuds) {
		Label labelFound = null;
		for(Label labelCourant : listeNoeuds) {
			if(this.sommetCourant.getNumero() == labelCourant.getSommetCourant().getNumero()) {
				labelFound = labelCourant;
			}
		}
		return labelFound;
	}
}
