package core ;

import java.awt.Color;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Stack;

import base.Readarg;

public class Pcc extends Algo {

	// Numero des sommets origine et destination
	//protected int zoneOrigine ;
	protected Noeud origine ;

	//protected int zoneDestination ;
	protected Noeud destination ;
	protected Label labelDestination;

	protected int nbSommetsExplores;
	protected int nbSommetsMarques;
	protected int maxHeapSize;
	
	protected ArrayList<Label> labels; //liste comprenant tous les labels du graphe
	protected BinaryHeap<Label> tasLabels;
	//Pour covoiturage
	protected ArrayList<Label> labelsParcourus; 

	public Pcc(Graphe gr, PrintStream sortie, Readarg readarg) {
		super(gr, sortie, readarg) ;

		//this.zoneOrigine = gr.getZone () ;
		this.origine = gr.getSommet(readarg.lireInt ("Numero du sommet d'origine ? ")) ;

		// Demander la zone et le sommet destination.
		this.destination = gr.getSommet(readarg.lireInt ("Numero du sommet destination ? "));
	}
	
	/**
	 * Constructeur pour le covoiturage
	 * @param gr
	 * @param origine
	 * @param destination
	 * @param sortie
	 */
	public Pcc(Graphe gr, Noeud origine, Noeud destination, PrintStream sortie) {
		super(gr, sortie, null) ;
		this.origine = origine;
		this.destination = destination;
	}
	
	public int run() {
		long startTime = System.currentTimeMillis();
		printOutAndFile("Run PCC de " + origine + " vers " + destination) ;
		//Dijsktra (plus court chemin en distance)
		int maxHeapSize = Dijkstra(true);
		long endTime = System.currentTimeMillis();
		
		//Calcul le temps d'execution de l'algorithme
		affichageTempsExecution(startTime,endTime);
		
		// retour du nombre de noeuds parcourus
		return maxHeapSize;
	}
	
	/**
	 * Initialise tous les labels de type Label
	 * @param graphe
	 */
	public void initLabels(Graphe graphe) {
		labels = new ArrayList<>();
		// On cree un label pour chacun de ces noeuds
		for(Noeud noeud : graphe.getNoeuds()) {
			labels.add(new Label(false, Float.MAX_VALUE, null, noeud));
		}
	}
	
	/**
	 * Initialise les labels du graphe et quelques parametres
	 */
	public void initialiseDijkstra() {
		maxHeapSize = 0;
		nbSommetsExplores = 0;
		nbSommetsMarques = 0;
		//Creation des labels de tous les noeuds d'un graphe
		initLabels(this.graphe);
	}
	
	/**
	 * Initialise le label d'origine pour un Dijkstra basique
	 * @return le label d'origine
	 */
	public Label initialiseLabelOrigine() {
		Label labelOrigine = labels.get(this.origine.getNumero());
		labelOrigine.setCout(0);
		return labelOrigine;
	}
	
	/**
	 * Initialise le label d'origine et son cout a vol d'oiseau pour un AStar
	 * @return le label d'origine
	 */
	public Label initialiseLabelOrigineAStar(boolean isTemps) {
		Label labelOrigine = labels.get(this.origine.getNumero());
		labelOrigine.setCout(0);
		//calcul le cout a vol d'oiseau
		float coutDest = (float) Graphe.distance(destination.getLongitude(), destination.getLatitude(), origine.getLongitude(), origine.getLatitude());
		if(isTemps) {
			labelOrigine.setCoutDest((coutDest / (130 / 3.6f))/60);
		} else {
			labelOrigine.setCoutDest(coutDest);
		}
		return labelOrigine;
	}
	
	/**
	 * Algorithme de Dijkstra
	 * @param isTemps boolean qui indique si on calcule le plus court chemin en temps ou en distance
	 * @return le nombre de noeuds parcourus
	 */
	public int Dijkstra(boolean isTemps) {
		
		this.initialiseDijkstra();
		
		//initialise le label origine
		Label labelOrigine = this.initialiseLabelOrigine();

		tasLabels = new BinaryHeap<Label>();
		//Ajout du label origine dans le tas
		tasLabels.insert(labelOrigine);
		
		//Initialise le label de destination
		labelDestination = labels.get(this.destination.getNumero());
		
		//Initialise le label courant pour ensuite parcourir le graphe
		Label labelCourant = new Label(false, 0, null, null); // Pour la premiere fois qu'on teste la condition d'arret

		//Parcours les noeuds et effectue le dijkstra
		this.parcoursDijkstra(labelCourant, isTemps);
		
		// Creation du chemin
		this.creationCheminDijkstra(isTemps);

		//dessine un point de départ et d'arriver
		this.dessinerDepartArrive();

		return maxHeapSize;
	}
	
	/**
	 * Algorithme de Dijkstra AStar
	 * @param isTemps boolean qui indique si on calcule le plus court chemin en temps ou en distance
	 * @return le nombre de noeuds parcourus
	 */
	public int DijkstraAStar(boolean isTemps) {
		
		this.initialiseDijkstra();
		
		//initialise le label origine
		Label labelOrigine = this.initialiseLabelOrigineAStar(isTemps);

		tasLabels = new BinaryHeap<Label>();
		//Ajout du label origine dans le tas
		tasLabels.insert(labelOrigine);
		
		//Initialise le label de destination
		labelDestination = labels.get(this.destination.getNumero());
		
		//Initialise le label courant pour ensuite parcourir le graphe
		Label labelCourant = new Label(false, 0, null, null); // Pour la premiere fois qu'on teste la condition d'arret

		//Parcours les noeuds et effectue le dijkstra
		this.parcoursDijkstra(labelCourant, isTemps);
		
		//Creation du chemin
		this.creationCheminDijkstra(isTemps);

		//Dessine un point de départ et d'arriver
		this.dessinerDepartArrive();

		return maxHeapSize;
	}
	
	public ArrayList<Label> DijkstraCovoiturage(float distanceArret, boolean pieton, ArrayList<Label> labelsToCover) {
		this.labelsParcourus = new ArrayList<Label>();
		this.initialiseDijkstra();
		
		//initialise le label origine
		Label labelOrigine = this.initialiseLabelOrigine();

		tasLabels = new BinaryHeap<Label>();
		//Ajout du label origine dans le tas
		tasLabels.insert(labelOrigine);
		
		//Initialise le label de destination
		labelDestination = labels.get(this.destination.getNumero());
		
		//Initialise le label courant pour ensuite parcourir le graphe
		Label labelCourant = new Label(false, 0, null, null); // Pour la premiere fois qu'on teste la condition d'arret

		//Parcours les noeuds et effectue le dijkstra
		this.parcoursDijkstraCovoiturage(labelCourant, distanceArret, pieton, labelsToCover);

		return labelsParcourus;
	}
	
	public ArrayList<Label> DijkstraAStarCovoiturage(boolean pieton) {
		this.labelsParcourus = new ArrayList<Label>();
		this.initialiseDijkstra();
		
		//initialise le label origine
		Label labelOrigine = this.initialiseLabelOrigineAStar(true);

		tasLabels = new BinaryHeap<Label>();
		//Ajout du label origine dans le tas
		tasLabels.insert(labelOrigine);
		
		//Initialise le label de destination
		labelDestination = labels.get(this.destination.getNumero());
		
		//Initialise le label courant pour ensuite parcourir le graphe
		Label labelCourant = new Label(false, 0, null, null); // Pour la premiere fois qu'on teste la condition d'arret

		//Parcours les noeuds et effectue le dijkstra
		this.parcoursDijkstraAStarCovoiturage(labelCourant, pieton);
		
		//Creation du chemin
		this.creationCheminDijkstra(true);

		//Dessine un point de départ et d'arriver
		this.dessinerDepartArrive();

		return this.labelsParcourus;
	}
	
	/**
	 * Version Dijkstra et AStar, parcours les noeuds du graphe pour trouver le plus court chemin
	 * @param labelCourant
	 * @param isTemps
	 */
	public void parcoursDijkstra(Label labelCourant, boolean isTemps) {
		while(! labelDestination.isMarque() && ! tasLabels.isEmpty()) {
			//met a jour le nombre de sommets parcourus
			if(maxHeapSize < tasLabels.size()) {
				maxHeapSize = tasLabels.size();
			}
			//traite le label courant
			labelCourant = tasLabels.deleteMin();
			labelCourant.marquer(true);
			nbSommetsMarques++;
			
			//Colore le label sur la carte
			this.coloreNoeud(labelCourant);
			
			// Parcours des routes sortantes
			this.parcoursRoutesSortantesDijkstra(labelCourant, isTemps);
		}
	}
	
	/**
	 * Version Covoiturage, parcours les noeuds du graphe pour trouver le plus court chemin
	 * @param labelCourant
	 * @param isTemps
	 */
	public void parcoursDijkstraCovoiturage(Label labelCourant, float distanceArret, boolean pieton, ArrayList<Label> labelsToCover) {
		int nbLabelsCover = 0;
		// Condition d'arret : si on à attend la distance d'arret et qu'on est pieton ou si on à couvert tous les labels du pieton à couvrir
		while(((labelCourant.getCout() < distanceArret && pieton) || (nbLabelsCover < labelsToCover.size() && ! pieton)) && ! tasLabels.isEmpty()) {
			//met a jour le nombre de sommets parcourus
			if(maxHeapSize < tasLabels.size()) {
				maxHeapSize = tasLabels.size();
			}
			//traite le label courant
			labelCourant = tasLabels.deleteMin();
			labelCourant.marquer(true);
			if(labelCourant.findIn(labelsToCover) != null) {
				nbLabelsCover++;
			}
			labelsParcourus.add(labelCourant);
			nbSommetsMarques++;
			
			//Colore le label sur la carte
			this.coloreNoeud(labelCourant);
			
			// Parcours des routes sortantes
			this.parcoursRoutesSortantesCovoiturage(labelCourant, pieton);
		}
	}
	
	public void parcoursDijkstraAStarCovoiturage(Label labelCourant, boolean pieton) {
		while(! labelDestination.isMarque() && ! tasLabels.isEmpty()) {
			//met a jour le nombre de sommets parcourus
			if(maxHeapSize < tasLabels.size()) {
				maxHeapSize = tasLabels.size();
			}
			//traite le label courant
			labelCourant = tasLabels.deleteMin();
			labelCourant.marquer(true);
			labelsParcourus.add(labelCourant);
			nbSommetsMarques++;
			
			//Colore le label sur la carte
			//this.coloreNoeud(labelCourant);
			
			// Parcours des routes sortantes
			this.parcoursRoutesSortantesCovoiturage(labelCourant, pieton);
		}
	}
	
	/**
	 * Colore un noeud sur la carte
	 * @param label
	 */
	public void coloreNoeud(Label label) {
		//colore le noeud du graphe parcouru
		this.graphe.getDessin().setColor(Color.green);
		this.graphe.getDessin().setWidth(2);
		this.graphe.getDessin().drawPoint(label.getSommetCourant().getLongitude(), label.getSommetCourant().getLatitude(), 2);
		
	}
	
	/**
	 * Version Dijkstra et ASTar, parcours les routes sortantes de la route selectionnee et actualise leur cout
	 * @param labelCourant
	 * @param isTemps
	 */
	public void parcoursRoutesSortantesDijkstra(Label labelCourant, boolean isTemps) {
		for(Route routeCourante : labelCourant.getSommetCourant().getRoutesSortantes()) {
			Label labelDest = labels.get(routeCourante.getNoeudDestination().getNumero());
			//Si le label sortant est deja marque, on continue
			if(labelDest.isMarque()) {
				continue;
			}
			
			float cout = routeCourante.cout(isTemps, false) + labelCourant.getCout();

			// Si le label a un cout plus grand et qu'il n'est pas marque on modifie son cout
			if(labelDest.getCout() > cout) {
				labelDest.setCout(cout);
				float coutDest = (float) Graphe.distance(destination.getLongitude(), destination.getLatitude(), routeCourante.getNoeudDestination().getLongitude(), routeCourante.getNoeudDestination().getLatitude());
				if(isTemps) {
					labelDest.setCoutDest((coutDest / (130 / 3.6f))/60);
				}else {
					labelDest.setCoutDest(coutDest);
				}

				labelDest.setPere(labelCourant.getSommetCourant());
				if(tasLabels.contains(labelDest)) {
					tasLabels.update(labelDest);
				}
				else {
					tasLabels.insert(labelDest);
					nbSommetsExplores++;
				}
			}
		}
	}
	
	/**
	 * Version Covoiturage, parcours les routes sortantes de la route selectionnee et actualise leur cout
	 * @param labelCourant
	 * @param isTemps
	 * @param pieton
	 */
	public void parcoursRoutesSortantesCovoiturage(Label labelCourant, boolean pieton) {
		for(Route routeCourante : labelCourant.getSommetCourant().getRoutesSortantes()) {
			Label labelDest = labels.get(routeCourante.getNoeudDestination().getNumero());
			//Si le label sortant est deja marque, on continue
			if(labelDest.isMarque()) {
				continue;
			}
			
			float cout = routeCourante.cout(true, pieton) + labelCourant.getCout();

			// Si le label a un cout plus grand et qu'il n'est pas marque on modifie son cout
			if(labelDest.getCout() > cout) {
				labelDest.setCout(cout);
				labelDest.setPere(labelCourant.getSommetCourant());
				if(tasLabels.contains(labelDest)) {
					tasLabels.update(labelDest);
				}
				else {
					tasLabels.insert(labelDest);
					nbSommetsExplores++;
				}
			}
		}
	}
	
	/**
	 * Creation du chemin le plus court entre l'origine et la destination
	 * @param labels
	 * @param labelDestination
	 * @param isTemps
	 */
	public void creationCheminDijkstra(boolean isTemps) {
		//Part de la destination et remonte jusqu'a l'origine
		Stack<Noeud> noeuds = new Stack<Noeud>();
		Noeud noeudCourant = this.destination;
		while(noeudCourant != null) {
			noeuds.push(noeudCourant);
			noeudCourant = labels.get(noeudCourant.getNumero()).getPere();
		}
		Noeud[] paths = new Noeud[noeuds.size()];
		int index = 0;
		while (!noeuds.empty()) {
			paths[index++] = noeuds.pop();
		}
		Chemin chemin = new Chemin(this.graphe,paths);
		this.graphe.dessinerChemin(chemin);

		if(labelDestination.isMarque()) {
			if(isTemps) {
				printOutAndFile("Cout calcule par l'algo : " + labelDestination.getCout() + " minutes.");
				printOutAndFile("Cout calcule par la classe Chemin  : " + chemin.coutChemin(isTemps) + " minutes.");
			}
			else {
				printOutAndFile("Cout calcule par l'algo : " + labelDestination.getCout() + " metres.");
				printOutAndFile("Cout calcule par la classe Chemin  : " + chemin.coutChemin(isTemps) + " metres.");
			}
		}
		else {
			printOutAndFile("Aucune solution.");
		}
	}
	
	/**
	 * Affichage point de depart et arrive
	 */
	public void dessinerDepartArrive() {
		this.graphe.getDessin().setColor(Color.red);
		this.graphe.getDessin().setWidth(7);
		this.graphe.getDessin().drawPoint(this.origine.getLongitude(), this.origine.getLatitude(), 7);
		this.graphe.getDessin().drawPoint(this.destination.getLongitude(), this.destination.getLatitude(), 7);
	}
	
	/**
	 * Calcul le temps d'execution de l'algorithme
	 * @param startTime
	 * @param endTime
	 */
	public void affichageTempsExecution(long startTime, long endTime) {
		//Calcul le temps d'execution de l'algorithme
		float executionTime = (endTime - startTime) / 1000f;
		printOutAndFile("Temps d'execution : " + executionTime + " secondes.");
		printOutAndFile(nbSommetsExplores + " sommets explores.");
		printOutAndFile(nbSommetsMarques + " sommets marques.");
	}

	public void setOrigine(Noeud origine) {
		this.origine = origine;
	}

	public void setDestination(Noeud destination) {
		this.destination = destination;
	}
}
