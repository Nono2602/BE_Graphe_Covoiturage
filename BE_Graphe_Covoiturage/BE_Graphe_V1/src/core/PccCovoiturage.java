package core;

import java.awt.Color;
import java.io.PrintStream;
import java.util.ArrayList;

import base.Readarg;

public class PccCovoiturage extends Algo {
	
	private Noeud pieton;
	private Noeud voiture;
	
	private Noeud destination;

	public PccCovoiturage(Graphe gr, PrintStream sortie, Readarg readarg) {
		super(gr, sortie, readarg) ;

		// Demande les origines pieton voiture.
		this.pieton = gr.getSommet(readarg.lireInt ("Numero du sommet du pieton ? ")) ;
		this.voiture = gr.getSommet(readarg.lireInt ("Numero du sommet de la voiture ? ")) ;

		// Demander la zone et le sommet destination.
		this.destination = gr.getSommet(readarg.lireInt ("Numero du sommet destination ? "));
	}


    public int run() {
		long startTime = System.currentTimeMillis();
		// Nombre de noeuds parcourus par le covoiturage
		int nbNoeudsParcourus = 0;
		
		printOutAndFile("Run Covoiturage de " + pieton + " et " + voiture + " vers " + destination) ;
		ArrayList<Label> noeudsParcourusPieton = new ArrayList<Label>();
		ArrayList<Label> noeudsParcourusVoiture = new ArrayList<Label>();
		ArrayList<Label> noeudsParcourusDestination = new ArrayList<Label>();
		
		Pcc pcc = new Pcc(this.graphe, this.pieton, this.destination, this.sortie);
		
		float distanceArretPieton = (float) Graphe.distance(pieton.getLongitude(), pieton.getLatitude(), voiture.getLongitude(), voiture.getLatitude()) / 7.5f;
		noeudsParcourusPieton = pcc.DijkstraCovoiturage((distanceArretPieton/(4 / 3.6f))/60, true, new ArrayList<Label>());
		nbNoeudsParcourus += noeudsParcourusPieton.size();
		
		pcc.setOrigine(this.voiture);
		float distanceArretVoiture = (float) Graphe.distance(pieton.getLongitude(), pieton.getLatitude(), voiture.getLongitude(), voiture.getLatitude()) + distanceArretPieton*2;
		noeudsParcourusVoiture = pcc.DijkstraCovoiturage(distanceArretVoiture/(130 / 3.6f)/60, false, noeudsParcourusPieton);
		nbNoeudsParcourus += noeudsParcourusVoiture.size();
		
		pcc.setOrigine(this.destination);
		float distanceArretDestination = (float) Graphe.distance(pieton.getLongitude(), pieton.getLatitude(), destination.getLongitude(), destination.getLatitude()) + distanceArretPieton*2;
		Graphe grapheInverse = this.graphe.inverserGraphe();
		Graphe saveGraphe = this.graphe;
		this.graphe = grapheInverse;
		noeudsParcourusDestination = pcc.DijkstraCovoiturage(distanceArretDestination/(130 / 3.6f)/60, false, noeudsParcourusPieton);
		this.graphe = saveGraphe;
		nbNoeudsParcourus += noeudsParcourusDestination.size();
		
		// Garde les noeuds commun dans les 3 listes et calcul leurs cout totaux
		ArrayList<Label> noeudsCommuns = new ArrayList<Label>();
		ArrayList<Float> attentes = new ArrayList<Float>();
		for(Label labelCourant : noeudsParcourusPieton) {
			Label labelVoiture = labelCourant.findIn(noeudsParcourusVoiture);
			Label labelDestination = labelCourant.findIn(noeudsParcourusDestination);
			if(labelVoiture != null && labelDestination != null) {
				float epsilon = 5; // 5 minutes maximum d'attente possible entre le pieton et la voiture au point de rencontre
				if(labelVoiture.getCout() <= labelCourant.getCout() + epsilon && labelVoiture.getCout() >= labelCourant.getCout() - epsilon ) {
					float attente = Math.abs(labelVoiture.getCout() - labelCourant.getCout());
					float cout = attente + labelCourant.getCout() + labelVoiture.getCout() + labelDestination.getCout();
					Label newLabel = new Label(false, cout, labelCourant.getPere(), labelCourant.getSommetCourant());
					noeudsCommuns.add(newLabel);
					attentes.add(attente);
				}
			}
		}
		
		// Compare tous les noeuds avec leur couts sommet et choisis le meilleur pour définir le point de rencontre
		if(noeudsCommuns.size() > 0) {
			Label rencontre = noeudsCommuns.get(0);
			float attente = attentes.get(0);
			for(int i = 1; i < noeudsCommuns.size(); i++) {
				if(rencontre.getCout() > noeudsCommuns.get(i).getCout()) {
					rencontre = noeudsCommuns.get(i);
					attente = attentes.get(i);
				}
			}
			pcc.setDestination(rencontre.getSommetCourant());
			
			printOutAndFile("Attente de " + attente + " minutes au point de rencontre.");
			
			// Tourne le AStar du pieton vers le point de rencontre
			pcc.setOrigine(pieton);
			printOutAndFile("Chemin du pieton vers le point de rencontre :");
			nbNoeudsParcourus += pcc.DijkstraAStarCovoiturage(true).size();
	
			// Tourne le AStar de la voiture vers le point de rencontre
			pcc.setOrigine(voiture);
			printOutAndFile("Chemin de la voiture vers le point de rencontre :");
			nbNoeudsParcourus += pcc.DijkstraAStarCovoiturage(false).size();
	
			// Tourne le AStar du point de rencontre à la destination
			pcc.setDestination(this.destination);
			pcc.setOrigine(rencontre.getSommetCourant());
			printOutAndFile("Chemin du point de rencontre vers la destination :");
			nbNoeudsParcourus += pcc.DijkstraAStarCovoiturage(false).size();
			
			printOutAndFile("Total de " + nbNoeudsParcourus + " noeuds parcourus.");
	
			// dessins
			this.graphe.getDessin().setColor(Color.black);
			this.graphe.getDessin().drawPoint(rencontre.getSommetCourant().getLongitude(), rencontre.getSommetCourant().getLatitude(), 10);
			this.graphe.getDessin().setColor(Color.red);
			this.graphe.getDessin().drawPoint(this.pieton.getLongitude(), this.pieton.getLatitude(), 7);
			this.graphe.getDessin().drawPoint(this.destination.getLongitude(), this.destination.getLatitude(), 7);
			this.graphe.getDessin().drawPoint(this.voiture.getLongitude(), this.voiture.getLatitude(), 7);
		}
		else {
			printOutAndFile("Aucune solution.");
		}
		
		long endTime = System.currentTimeMillis();
		float executionTime = (endTime - startTime) / 1000f;
		printOutAndFile("Temps d'execution : " + executionTime + " secondes.");
		
		return nbNoeudsParcourus;
    }

}
