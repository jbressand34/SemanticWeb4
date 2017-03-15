package ent;

import java.io.IOException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

public class QuerySparqlEndpoint {


	public static void main(String[] args) throws IOException {
		
		// Adresse de l'Endpoint
		String dbpediaEndpoint ="http://dbpedia.org/sparql";
		
		// Exemble d'une requête sparql 
		String q1 =" PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
		+ " PREFIX  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
		+ " PREFIX  owl: <http://www.w3.org/2002/07/owl#>"
        + "SELECT DISTINCT ?c ?l where {?c a rdf:Property. ?c rdfs:label ?l} LIMIT 100";
		
        // Création d'un objet Jena Query à partir du texte de la requête
        
        Query requete1 = QueryFactory.create(q1);

        // Création de l'exécution factory pour l'Endpoint spécifié
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
        		dbpediaEndpoint, requete1);
        
        // Exécution de la requête et affichage des résultats
		try {

			ResultSet rs = qexec.execSelect();

			ResultSetFormatter.out(System.out, rs, requete1);

		} finally {
			qexec.close();
		}
	}

}
