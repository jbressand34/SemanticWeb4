package ent;

import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.NsIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.PrintUtil;
import org.apache.jena.vocabulary.RDF;


public class QueryModel {
	public static void main(String[] args){
		
		/**
		 * 
		 * Cet exemple illustre l'utilisation des méthodes Jena pour interroger un modèle RDF 
		 * 
		 */
		
		// Spécification des chemins des fichiers et d'autres arguments
		
		String inputFileName = "SW-FAQ-feed.rdf"; 
		
		
		// Déclaration des espaces de noms
		
		String mcfURI = "http://www.mycorporisfabrica.org/ontology/mcf.owl#";
		PrintUtil.registerPrefix("mcf", mcfURI);

		String rdfURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
		PrintUtil.registerPrefix("rdf", rdfURI);
		
		// Création d'un modèle vide
		
		 Model model = ModelFactory.createDefaultModel();

		 // Utilisation de la classe FileManager pour retrouver un fichier à partir de son chemin
		 InputStream in = FileManager.get().open( inputFileName );
		if (in == null) {
		    throw new IllegalArgumentException(
		                                 "File: " + inputFileName + " not found");
		}

		// Lecture du fichier RDF
		model.read(in, null);
		// Nombre des triplets du modèle
		System.out.println("Taille du modèle : "+ model.size());
		
		// Les espaces de noms
		NsIterator lesNS = model.listNameSpaces();
		System.out.println("Les espaces de noms utilisés dans le modèle : ");
		while (lesNS.hasNext()) {
			System.out.println((String) lesNS.next());
		}
		
		// Les sujets du modèle
		ResIterator listeSub = model.listSubjects();
		System.out.println("Ressources déclarées comme sujets : ");
		while (listeSub.hasNext()) {
			System.out.println((Resource) listeSub.next());
		}
		
		// 
		
		// les entités utilisées pour typer des ressources
		System.out.println("Les ressources servant à typer d'autres ressources : \n");
		NodeIterator entities = model.listObjectsOfProperty(RDF.type);
		while (entities.hasNext()) {
			RDFNode rdfNode = (RDFNode) entities.next();
			System.out.println(rdfNode);
		}
		
		System.out.println("les ressources liées à la proprété rdf:type : \n");
		ResIterator sujets = model.listResourcesWithProperty(RDF.type);	
		while (sujets.hasNext()) {
			System.out.println((Resource) sujets.next());
		}
		
		// Affichage de tout le modèle chargé
		//System.out.println("Les triplets du modèle : ");
		//model.write(System.out);
		

	}
}
