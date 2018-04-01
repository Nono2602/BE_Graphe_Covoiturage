package core ;

/**
 *   Classe representant un graphe.
 *   A vous de completer selon vos choix de conception.
 */

import java.io.* ;
import java.util.ArrayList;

import base.* ;

public class Graphe {

	// Nom de la carte utilisee pour construire ce graphe
	private final String nomCarte ;

	// Fenetre graphique
	private final Dessin dessin ;

	// Version du format MAP utilise'.
	private static final int version_map = 4 ;
	private static final int magic_number_map = 0xbacaff ;

	// Version du format PATH.
	private static final int version_path = 1 ;
	private static final int magic_number_path = 0xdecafe ;

	// Identifiant de la carte
	private int idcarte ;

	// Numero de zone de la carte
	private int numzone ;

	/*
	 * Ces attributs constituent une structure ad-hoc pour stocker les informations du graphe.
	 * Vous devez modifier et ameliorer ce choix de conception simpliste.
	 */
	private Noeud[] noeuds;


	// Deux malheureux getters.
	public Dessin getDessin() { return dessin ; }
	public int getZone() { return numzone ; }

	// Le constructeur cree le graphe en lisant les donnees depuis le DataInputStream
	public Graphe (String nomCarte, DataInputStream dis, Dessin dessin) {

		this.nomCarte = nomCarte ;
		this.dessin = dessin ;
		Utils.calibrer(nomCarte, dessin) ;

		// Lecture du fichier MAP. 
		// Voir le fichier "FORMAT" pour le detail du format binaire.
		try {

			// Nombre d'aretes
			int edges = 0 ;

			// Verification du magic number et de la version du format du fichier .map
			int magic = dis.readInt () ;
			int version = dis.readInt () ;
			Utils.checkVersion(magic, magic_number_map, version, version_map, nomCarte, ".map") ;

			// Lecture de l'identifiant de carte et du numero de zone, 
			this.idcarte = dis.readInt () ;
			this.numzone = dis.readInt () ;

			// Lecture du nombre de descripteurs, nombre de noeuds.
			int nb_descripteurs = dis.readInt () ;
			int nb_nodes = dis.readInt () ;

			// Nombre de successeurs enregistrés dans le fichier.
			int[] nsuccesseurs_a_lire = new int[nb_nodes] ;

			// En fonction de vos choix de conception, vous devrez certainement adapter la suite.
			this.noeuds = new Noeud[nb_nodes] ;

			// Lecture des noeuds
			for (int num_node = 0 ; num_node < nb_nodes ; num_node++) {
				// Lecture du noeud numero num_node
				float longitude = ((float)dis.readInt ()) / 1E6f;
				float latitude = ((float)dis.readInt ()) / 1E6f;
				noeuds[num_node] = new Noeud(longitude, latitude, num_node);
				nsuccesseurs_a_lire[num_node] = dis.readUnsignedByte() ;
			}

			Utils.checkByte(255, dis) ;

			// Lecture des descripteurs
			Descripteur[] descripteurs = new Descripteur[nb_descripteurs];
			for (int num_descr = 0 ; num_descr < nb_descripteurs ; num_descr++) {
				// Lecture du descripteur numero num_descr
				descripteurs[num_descr] = new Descripteur(dis) ;

				// On affiche quelques descripteurs parmi tous.
				if (0 == num_descr % (1 + nb_descripteurs / 400))
					System.out.println("Descripteur " + num_descr + " = " + descripteurs[num_descr]) ;
			}

			Utils.checkByte(254, dis) ;

			
			// Lecture des successeurs
			for (int num_node = 0 ; num_node < nb_nodes ; num_node++) {
				// Lecture de tous les successeurs du noeud num_node
				int nb_succs = nsuccesseurs_a_lire[num_node];
				ArrayList<Route> routes = new ArrayList<>();
				for (int num_succ = 0 ; num_succ < nsuccesseurs_a_lire[num_node] ; num_succ++) {
					// zone du successeur
					int succ_zone = dis.readUnsignedByte() ;

					// numero de noeud du successeur
					int dest_node = Utils.read24bits(dis) ;

					// descripteur de l'arete
					int descr_num = Utils.read24bits(dis) ;

					// longueur de l'arete en metres
					int longueur  = dis.readUnsignedShort() ;

					// Nombre de segments constituant l'arete
					int nb_segm   = dis.readUnsignedShort() ;

					edges++ ;

					Couleur.set(dessin, descripteurs[descr_num].getType()) ;

					float current_long = noeuds[num_node].getLongitude() ;
					float current_lat  = noeuds[num_node].getLatitude() ;

					// Chaque segment est dessiné
					Segment segms[] = new Segment[nb_segm];
					for (int i = 0 ; i < nb_segm ; i++) {
						float delta_lon = (dis.readShort()) / 2.0E5f ;
						float delta_lat = (dis.readShort()) / 2.0E5f ;
						segms[i] = new Segment(delta_lon, delta_lat);
						dessin.drawLine(current_long, current_lat, (current_long + delta_lon), (current_lat + delta_lat)) ;
						current_long += delta_lon ;
						current_lat  += delta_lat ;
					}

					// Le dernier trait rejoint le sommet destination.
					// On le dessine si le noeud destination est dans la zone du graphe courant.
					if (succ_zone == numzone) {
						// Affectation des routes au noeud
						routes.add(new Route(longueur, noeuds[dest_node], segms, descripteurs[descr_num]));
						// Si la route n'est pas en sens unique
						if(! descripteurs[descr_num].isSensUnique()) {
							Segment segms_inverse[] = new Segment[nb_segm];
							for(int i = 0; i < segms.length; i++) {
								Segment current = segms[segms.length - i - 1];
								segms_inverse[i] = new Segment(current.getdLongitude()*-1, current.getdLatitude()*-1);
							}
							Route inverse_route = new Route(longueur, noeuds[num_node], segms_inverse, descripteurs[descr_num]);
							noeuds[dest_node].addRouteSortante(inverse_route);
						}
						dessin.drawLine(current_long, current_lat, noeuds[dest_node].getLongitude(), noeuds[dest_node].getLatitude()) ;
					}
				}
				// Affectation des routes aux noeuds
				noeuds[num_node].addRoutesSortante(routes);
			}

			Utils.checkByte(253, dis) ;

			System.out.println("Fichier lu : " + nb_nodes + " sommets, " + edges + " aretes, " 
					+ nb_descripteurs + " descripteurs.") ;

		} catch (IOException e) {
			e.printStackTrace() ;
			System.exit(1) ;
		}

	}

	public Graphe(Graphe autre) {
		this.dessin = autre.getDessin();
		this.idcarte = autre.idcarte;
		this.nomCarte = autre.nomCarte;
		this.numzone = autre.numzone;
	}

	// Rayon de la terre en metres
	private static final double rayon_terre = 6378137.0 ;

	/**
	 *  Calcule de la distance orthodromique - plus court chemin entre deux points à la surface d'une sphère
	 *  @param long1 longitude du premier point.
	 *  @param lat1 latitude du premier point.
	 *  @param long2 longitude du second point.
	 *  @param lat2 latitude du second point.
	 *  @return la distance entre les deux points en metres.
	 *  Methode Ã©crite par Thomas Thiebaud, mai 2013
	 */
	public static double distance(double long1, double lat1, double long2, double lat2) {
		double sinLat = Math.sin(Math.toRadians(lat1))*Math.sin(Math.toRadians(lat2));
		double cosLat = Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2));
		double cosLong = Math.cos(Math.toRadians(long2-long1));
		return rayon_terre*Math.acos(sinLat+cosLat*cosLong);
	}

	/**
	 *  Attend un clic sur la carte et affiche le numero de sommet le plus proche du clic.
	 *  A n'utiliser que pour faire du debug ou des tests ponctuels.
	 *  Ne pas utiliser automatiquement a chaque invocation des algorithmes.
	 */
	public void situerClick() {

		System.out.println("Allez-y, cliquez donc.") ;

		if (dessin.waitClick()) {
			float lon = dessin.getClickLon() ;
			float lat = dessin.getClickLat() ;

			System.out.println("Clic aux coordonnees lon = " + lon + "  lat = " + lat) ;

			// On cherche le noeud le plus proche. O(n)
			float minDist = Float.MAX_VALUE ;
			int   noeud   = 0 ;

			for (int num_node = 0 ; num_node < noeuds.length ; num_node++) {
				float londiff = (noeuds[num_node].getLongitude() - lon) ;
				float latdiff = (noeuds[num_node].getLatitude() - lat) ;
				float dist = londiff*londiff + latdiff*latdiff ;
				if (dist < minDist) {
					noeud = num_node ;
					minDist = dist ;
				}
			}

			System.out.println("Noeud le plus proche : " + noeud) ;
			System.out.println() ;
			dessin.setColor(java.awt.Color.red) ;
			dessin.drawPoint(noeuds[noeud].getLongitude(), noeuds[noeud].getLatitude(), 5) ;
		}
	}

	/**
	 *  Charge un chemin depuis un fichier .path (voir le fichier FORMAT_PATH qui decrit le format)
	 *  Verifie que le chemin est empruntable et calcule le temps de trajet.
	 */
	public void verifierChemin(DataInputStream dis, String nom_chemin) {
		
		try {

			// Verification du magic number et de la version du format du fichier .path
			int magic = dis.readInt () ;
			int version = dis.readInt () ;
			Utils.checkVersion(magic, magic_number_path, version, version_path, nom_chemin, ".path") ;

			// Lecture de l'identifiant de carte
			int path_carte = dis.readInt () ;

			if (path_carte != this.idcarte) {
				System.out.println("Le chemin du fichier " + nom_chemin + " n'appartient pas a la carte actuellement chargee." ) ;
				System.exit(1) ;
			}

			int nb_noeuds = dis.readInt () ;

			// Origine du chemin
			int first_zone = dis.readUnsignedByte() ;
			int first_node = Utils.read24bits(dis) ;

			// Destination du chemin
			int last_zone  = dis.readUnsignedByte() ;
			int last_node = Utils.read24bits(dis) ;

			System.out.println("Chemin de " + first_zone + ":" + first_node + " vers " + last_zone + ":" + last_node) ;

			int current_zone = 0 ;
			int current_node = 0 ;

			Noeud noeuds_chemin[] = new Noeud[nb_noeuds];
			// Tous les noeuds du chemin
			
			for (int i = 0 ; i < nb_noeuds ; i++) {
				current_zone = dis.readUnsignedByte() ;
				current_node = Utils.read24bits(dis) ;
				noeuds_chemin[i] = this.noeuds[current_node];
				System.out.println(" --> " + current_zone + ":" + current_node) ;
			}

			if ((current_zone != last_zone) || (current_node != last_node)) {
				System.out.println("Le chemin " + nom_chemin + " ne termine pas sur le bon noeud.") ;
				System.exit(1) ;
			}
			
			// Instanciation du chemin
			Chemin chemin = new Chemin(this, noeuds_chemin);
			this.dessinerChemin(chemin);
			
			System.out.println("Coût du chemin : " + chemin.coutChemin(true) + " minutes");

		} catch (IOException e) {
			e.printStackTrace() ;
			System.exit(1) ;
		}
	}
	
	public void dessinerChemin(Chemin chemin) {
		dessin.setColor(java.awt.Color.cyan) ;
		dessin.setWidth(3);
		
		Noeud[] noeuds_chemin = chemin.getNoeuds();
		for(int i = 1; i < noeuds_chemin.length; i++) {
			dessin.drawLine(noeuds_chemin[i-1].getLongitude(), noeuds_chemin[i-1].getLatitude(), noeuds_chemin[i].getLongitude(), noeuds_chemin[i].getLatitude());
		}
	}
	
	public Graphe inverserGraphe() {
		Graphe grapheInverse = new Graphe(this);
		// Copie des noeuds
		Noeud nouveauxNoeuds[] = new Noeud[this.noeuds.length];
		for(int i = 0; i < this.noeuds.length; i++) {
			nouveauxNoeuds[i] = new Noeud(this.noeuds[i]);
		}
		grapheInverse.setNoeuds(nouveauxNoeuds);
		
		// Inversion des routes
		for(Noeud noeudCourant : this.noeuds) {
			for(Route routeCourante : noeudCourant.getRoutesSortantes()) {
				Route nouvelleRoute = new Route(routeCourante);
				// Inversion de la route
				Noeud noeudOrigine = grapheInverse.getSommet(nouvelleRoute.getNoeudDestination().getNumero());
				nouvelleRoute.setDestination(grapheInverse.getSommet(noeudCourant.getNumero()));
				noeudOrigine.addRouteSortante(nouvelleRoute);
			}
		}
		return grapheInverse;
	}
	
	public Noeud getSommet(int numSommet) {
		return this.noeuds[numSommet];
	}
	
	public int getNbSommets() {
		return this.noeuds.length;
	}
	
	public Noeud[] getNoeuds() {
		return this.noeuds;
	}

	private void setNoeuds(Noeud[] nouveauxNoeuds) {
		this.noeuds = nouveauxNoeuds;
	}
	
}