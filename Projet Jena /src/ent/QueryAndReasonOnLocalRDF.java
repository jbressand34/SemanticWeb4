package ent;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.PrintUtil;



public class QueryAndReasonOnLocalRDF {

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

		// Enregistrement des espaces de noms

		String mcfURI = "http://www.mycorporisfabrica.org/ontology/mcf.owl#";
		PrintUtil.registerPrefix("mcf", mcfURI);

		String rdfURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
		PrintUtil.registerPrefix("rdf", rdfURI);

		String goURI = "http://www.geneontology.org/dtds/go.dtd#";
		PrintUtil.registerPrefix("go", goURI);

		String oboURI = "http://purl.obolibrary.org/obo";
		PrintUtil.registerPrefix("obo", oboURI);
		
		// Chemin vers l'ontologie mycf
		String pathToOntology = "/auto_home/hguenoune/workspace/Reasoning_demo/MyCF/MyCF/MyCF2.rdf";

		// Exemples de requêtes pour tester les inférences
		
		// Lister toutes les entités anatomiques qui contribuent dans la flexion de l'articulation du genou
		String queryMyCF = " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ " PREFIX  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ " PREFIX  owl: <http://www.w3.org/2002/07/owl#>"
				+ " PREFIX  mcf: <http://www.mycorporisfabrica.org/ontology/mcf.owl#>"
				+ " SELECT distinct ?y"
				+ " WHERE { ?s mcf:ContributesTo mcf:Flexion_of_knee_joint }";
		
		// Les entités anatomiques sur lesquelles porte la stabilité du genou
		String queryMyCF2 = " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ " PREFIX  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ " PREFIX  owl: <http://www.w3.org/2002/07/owl#>"
				+ " PREFIX  mcf: <http://www.mycorporisfabrica.org/ontology/mcf.owl#>"
				+ " SELECT distinct ?s"
				+ " WHERE { ?s ?o mcf:Body_stability "
				+ "}";
		
		String queryMyCF3 = " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ " PREFIX  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ " PREFIX  owl: <http://www.w3.org/2002/07/owl#>"
				+ " PREFIX  mcf: <http://www.mycorporisfabrica.org/ontology/mcf.owl#>"
				+ " SELECT distinct ?s"
				+ " WHERE { ?s mcf:ContributesTo ?o ."
				+ "         ?o  mcf:IsInvolvedIn mcf:Body_stability"
				+ " }";
		
		// Lister le nom de toutes les entités créées par l'utilisateur 'midori'
		String queryGO = " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ " PREFIX  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ " PREFIX  owl: <http://www.w3.org/2002/07/owl#>"
				+ " PREFIX  go: <http://www.geneontology.org/formats/oboInOwl#>"
				+ " PREFIX  obo: <http://purl.obolibrary.org/obo/>" + " SELECT  ?id ?label"
				+ " WHERE  {?id <http://www.geneontology.org/formats/oboInOwl#created_by>  \"midori\" ."
				+ "         ?id <http://www.w3.org/2000/01/rdf-schema#label> ?label" + "         } " + " LIMIT 10";

		// Le nombre total des triplets du modèle Jena
		
		String howManyTriples = "select (count(*) as ?total_number_of_triples) where {?s ?p ?o}";

		
		// Selection des sous-classes
		
		String someClasses = " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "SELECT DISTINCT ?c where {?c rdfs:subClassOf ?o}";


		StringBuilder rules = new StringBuilder();

		// Définition des règles 
		
		rules.append("[rule1:  (?x mcf:PartOf ?y), (?y mcf:PartOf ?z) -> (?x mcf:PartOf ?z)] ");
		rules.append("[rule2:  (?x rdfs:subClassOf ?y), (?y rdfs:subClassOf ?z) -> (?x rdfs:subClassOf ?z)] ");

		// Création d'un modèle RDF et chargement des triplets à partir d'un fichier
		
		Model model = ModelFactory.createDefaultModel();
		
		// Lecture du fichier 
		InputStream in = FileManager.get().open(pathToOntology);

		Long start = System.currentTimeMillis();
		
		// chargement du fichier dans le modèle
		
		model.read(in, null);

		System.out.println("Import time : " + (System.currentTimeMillis() - start));

		// Instantiation d'un raisonneur de type 'GenericRuleReasoner'

		GenericRuleReasoner reasoner = (GenericRuleReasoner) GenericRuleReasonerFactory.theInstance().create(null);

		reasoner.setRules(Rule.parseRules(rules.toString()));

		// Changement du mode du raisonnement
		
		reasoner.setMode(GenericRuleReasoner.HYBRID);

		start = System.currentTimeMillis();

		InfModel inf = ModelFactory.createInfModel(reasoner, model);

		System.out.println("Rules pre-processing time : " + (System.currentTimeMillis() - start));

		// Création d'une instance de la classe Query

		Query query = QueryFactory.create(queryMyCF2);

		start = System.currentTimeMillis();

		QueryExecution qexec = QueryExecutionFactory.create(query, inf);

		System.out.println("Query pre-processing time : " + (System.currentTimeMillis() - start));

		// Éxécution de la requête et affichage des résultats
		start = System.currentTimeMillis();

		try {

			ResultSet rs = qexec.execSelect();

			ResultSetFormatter.out(System.out, rs, query);

		} finally {

			qexec.close();
		}

		System.out.println("Query + Display time : " + (System.currentTimeMillis() - start));

		// /**
		// *
		// * Export du modèle saturé
		// *
		// */
		//
		// PrintWriter resultWriter;
		// try {
		// resultWriter = new PrintWriter("testmycfsat.rdf");
		//
		// inf.write(resultWriter);
		//
		// resultWriter.close();
		//
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// }

	}
}
