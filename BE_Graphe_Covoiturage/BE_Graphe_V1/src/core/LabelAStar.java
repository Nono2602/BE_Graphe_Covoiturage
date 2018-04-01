package core;


public class LabelAStar extends Label {

	private float coutDest;
	
	public LabelAStar(boolean marquage, float cout, float coutDest, Noeud pere,
			Noeud sommetCourant) {
		super(marquage, cout, pere, sommetCourant);
		this.coutDest = coutDest;
	}

	public float getCoutDest() {
		return coutDest;
	}
	
	public void setCoutDest(float coutDest) {
		this.coutDest = coutDest;
	}
	
	@Override
	public int compareTo(Label autre) {
		if(autre.getCout() + autre.getCoutDest() > this.getCout() + this.getCoutDest()) {
			return -1;
		}
		else if(autre.getCout() + autre.getCoutDest() < this.getCout() + this.getCoutDest()) {
			return 1;
		}
		else {
			if(autre.getCoutDest() > this.getCoutDest()) {
				return -1;
			}
			else if(autre.getCoutDest() < this.getCoutDest()) {
				return 1;
			}
			else {
				return 0;
			}
		}
	}

}
