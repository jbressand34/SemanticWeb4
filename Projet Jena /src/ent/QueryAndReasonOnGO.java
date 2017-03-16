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

        String prefix = " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + " PREFIX  rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + " PREFIX  owl: <http://www.w3.org/2002/07/owl#>"
                + " PREFIX  go: <http://www.geneontology.org/dtds/go.dtd#>"
                + " PREFIX  obo: <http://purl.obolibrary.org/obo/>" ;
        
        //
        String howManyTriples = prefix+"\n select (count(*) as ?total_number_of_triples) where {?s go:part_of ?o}";


        //
        StringBuilder rules_mcf = new StringBuilder();
    

        StringBuilder rules = new StringBuilder();
        // Définition des régles
        /*rules.append("[rule1:  (?x go:is_a ?y), (?y go:is_a ?z) -> (?x go:is_a ?z)]");
        rules.append("[rule2:  (?x go:is_a ?y), (?y go:part_of ?z) -> (?x go:part_of ?z)]");
        rules.append("[rule3:  (?x go:is_a ?y), (?y go:regulates ?z) -> (?x go:regulates ?z)]");
        rules.append("[rule4:  (?x go:is_a ?y), (?y go:positively_regulates ?z) -> (?x go:positively_regulates ?z)]");
        rules.append("[rule5:  (?x go:is_a ?y), (?y go:negatively_regulates ?z) -> (?x go:negatively_regulates ?z)] ");
        rules.append("[rule6:  (?x go:is_a ?y), (?y go:has_part ?z) -> (?x go:has_part ?z)] ");
        rules.append("[rule7:  (?x go:part_of ?y), (?y go:is_a ?z) -> (?x go:part_of ?z)] ");
        rules.append("[rule8:  (?x go:part_of ?y), (?y go:part_of ?z) -> (?x go:part_of ?z)] ");
        rules.append("[rule9:  (?x go:regulates ?y), (?y go:is_a ?z) -> (?x go:regulates ?z)] ");
        rules.append("[rule10:  (?x go:regulates ?y), (?y go:part_of ?z) -> (?x go:regulates ?z)] ");
        rules.append("[rule11:  (?x go:positively_regulates ?y), (?y go:is_a ?z) -> (?x go:positively_regulates ?z)] ");
        rules.append("[rule12:  (?x go:positively_regulates ?y), (?y go:part_of ?z) -> (?x go:regulates ?z)] ");
        rules.append("[rule13:  (?x go:negatively_regulates ?y), (?y go:is_a ?z) -> (?x go:negatively_regulates ?z)] ");
        rules.append("[rule14:  (?x go:negatively_regulates ?y), (?y go:part_of ?z) -> (?x go:regulates ?z)] ");
        rules.append("[rule15:  (?x go:has_part ?y), (?y go:is_a ?z) -> (?x go:has_part ?z)] ");
        rules.append("[rule16:  (?x go:has_part ?y), (?y go:has_part ?z) -> (?x go:has_part ?z)] ");
        */
        //rules_mcf.append("[rule14: (?a go:negatively_regulates ?c), (?c go:part_of ?b)  -> (?a go:regulates ?b)] ");

        // Création d'un modèle RDF et chargement du fichier
        Model model_mcf = ModelFactory.createDefaultModel();

        

        InputStream in_mcf = FileManager.get().open(pathToOntology);

        Long start = System.currentTimeMillis();

        model_mcf.read(in_mcf, null);

        System.out.println("Import time : " + (System.currentTimeMillis() - start));

        // Instantiation du raisonneur
        
        GenericRuleReasoner reasoner_mcf = (GenericRuleReasoner) GenericRuleReasonerFactory.theInstance().create(null);

        reasoner_mcf.setRules(Rule.parseRules(rules.toString()));
        reasoner_mcf.setDerivationLogging(true);
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
