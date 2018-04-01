package core ;

import java.io.PrintStream;
import java.util.ArrayList;

import base.Readarg;

public class PccStar extends Pcc {

    public PccStar(Graphe gr, PrintStream sortie, Readarg readarg) {
    	super(gr, sortie, readarg) ;
    }
    
	@Override
	public void initLabels(Graphe graphe) {
		labels = new ArrayList<>();
		// On cree un label pour chacun de ces noeuds
		for(Noeud noeud : graphe.getNoeuds()) {
			labels.add(new LabelAStar(false, Float.MAX_VALUE, Float.MAX_VALUE, null, noeud));
		}
	}

    public int run() {
		long startTime = System.currentTimeMillis();
		printOutAndFile("Run PCC-AStar de " + origine + " vers " + destination) ;
		//Dijkstra AStar en distance
		int maxHeapSize = DijkstraAStar(true);
		long endTime = System.currentTimeMillis();
		
		//Calcul le temps d'execution de l'algorithme
		this.affichageTempsExecution(startTime, endTime);
		
		// return de max heap size
		return maxHeapSize;
    }

}
