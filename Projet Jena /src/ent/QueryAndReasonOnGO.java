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



public class QueryAndReasonOnGO {

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

        // Declaration des espaces de noms et du chemin vers le fichier contenant l'ontologie
    	
        String mcfURI = "http://www.mycorporisfabrica.org/ontology/mcf.owl#";
        PrintUtil.registerPrefix("mcf", mcfURI);

        String rdfURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        PrintUtil.registerPrefix("rdf", rdfURI);

        String goURI = "http://www.geneontology.org/dtds/go.dtd#";
        PrintUtil.registerPrefix("go", goURI);

        String oboURI = "http://purl.obolibrary.org/obo";
        PrintUtil.registerPrefix("obo", oboURI);
        
        //String pathToOntology = "/path/to/go_daily-termdb.rdf-xml";
        String pathToOntology = "/auto_home/hguenoune/workspace/Reasoning_demo/GO/go_daily-termdb.rdf-xml";
        // Exemples de requêtes pour tester l'ontologie
        
        // Récupération de la liste de toutes le entités crées par l'utilisateur 'midori'
        String queryGO = " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + " PREFIX  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + " PREFIX  owl: <http://www.w3.org/2002/07/owl#>"
                + " PREFIX  go: <http://www.geneontology.org/dtds/go.dtd#>"
                + " PREFIX  obo: <http://purl.obolibrary.org/obo/>" + " SELECT  ?id ?label"
                + " WHERE  {?id <http://www.geneontology.org/formats/oboInOwl#created_by>  \"midori\" ."
                + "         ?id <http://www.w3.org/2000/01/rdf-schema#label> ?label ." + "         } " + " LIMIT 10";

        // 
        String howManyTriples = "select (count(*) as ?total_number_of_triples) where {?s ?p ?o}";


        // 
        StringBuilder rules = new StringBuilder();

        // Définition des régles 
        //rules.append("[rule14: (?a go:negatively_regulates ?c), (?c go:part_of ?b)  -> (?a go:regulates ?b)] ");

        //is_a
        rules.append("[rule1: (?a go:is_a ?b), (?b go:is_a ?c)  -> (?a go:is_a ?d)] ");
        rules.append("[rule2: (?a go:is_a ?b), (?b go:part_of ?d)  -> (?a go:part_of ?d)] ");
        rules.append("[rule3: (?a go:is_a ?b), (?b go:regulates ?d)  -> (?a go:regulates ?d)] ");
        rules.append("[rule4: (?a go:is_a ?b), (?b go:positively_regulates ?d)  -> (?a go:positively_regulates ?d)] ");
        rules.append("[rule5: (?a go:is_a ?b), (?b go:negatively_regulates ?d)  -> (?a go:negatively_regulates ?d)] ");
        
        //part_of
        rules.append("[rule6: (?a go:part_of ?b), (?b go:is_a ?d)  -> (?a go:part_of ?d)] ");
        rules.append("[rule7: (?a go:part_of ?b), (?b go:part_of  ?d)  -> (?a go:part_of ?d)] ");
        
        //regulates
        rules.append("[rule8: (?a go:regulates ?c), (?c go:is_a ?b)  -> (?a go:regulates ?b)] ");
        rules.append("[rule9: (?a go:regulates ?c), (?c go:part_of ?b)  -> (?a go:regulates ?b)] ");

        //positively-regulates
        rules.append("[rule10: (?a go:positively_regulates ?c), (?c go:is_a ?b)  -> (?a go:positively_regulates ?b)] ");
        rules.append("[rule11: (?a go:positively_regulates ?c), (?c go:part_of ?b)  -> (?a go:regulates ?b)] ");

        //negatively-regulates
        rules.append("[rule12: (?a go:negatively_regulates ?c), (?c go:is_a ?b)  -> (?a go:negatively_regulates ?b)] ");
        rules.append("[rule13: (?a go:negatively_regulates ?c), (?c go:part_of ?b)  -> (?a go:regulates ?b)] ");

        //has-part
        rules.append("[rule14: (?a go:has_part ?c), (?c go:is_a ?b)  -> (?a go:has_part ?b)] ");
        rules.append("[rule15: (?a go:has_part ?c), (?c go:has_part ?b)  -> (?a go:has_part ?b)] ");

        // Création d'un modèle RDF et chargement du fichier
        Model model_mcf = ModelFactory.createDefaultModel();

        

        InputStream in_mcf = FileManager.get().open(pathToOntology);

        Long start = System.currentTimeMillis();

        model_mcf.read(in_mcf, null);

        System.out.println("Import time : " + (System.currentTimeMillis() - start));

        // Instantiation du raisonneur
        
        GenericRuleReasoner reasoner_mcf = (GenericRuleReasoner) GenericRuleReasonerFactory.theInstance().create(null);

        reasoner_mcf.setRules(Rule.parseRules(rules.toString()));

        // Changemnt du type du raisonneur
        reasoner_mcf.setMode(GenericRuleReasoner.HYBRID);

        start = System.currentTimeMillis();

        InfModel inf_mcf = ModelFactory.createInfModel(reasoner_mcf, model_mcf);

        System.out.println("Rules pre-processing time : " + (System.currentTimeMillis() - start));

        // Création d'une instance query
        Query query = QueryFactory.create(howManyTriples);

        start = System.currentTimeMillis();

        QueryExecution qexec_mcf = QueryExecutionFactory.create(query, inf_mcf);
        //QueryExecution qexec_mcf = QueryExecutionFactory.create(query, model_mcf);

        System.out.println("Query pre-processing time : " + (System.currentTimeMillis() - start));

        // Execution de la requête et mesure des performances
        start = System.currentTimeMillis();

        try {

            ResultSet rs_mcf = qexec_mcf.execSelect();

            ResultSetFormatter.out(System.out, rs_mcf, query);

        } finally {

            qexec_mcf.close();
        }

        System.out.println("Query + Display time : " + (System.currentTimeMillis() - start));

        //Export de l'ontologie saturée
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