
import java.io.PrintStream;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec; 
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.graph.*;
import oracle.rdf.kv.client.jena.*;

/**
 * Example 17 describes how to use OWL_MEM_RULE_INF to build an OntologyModel 
 * on top of an OracleModelNoSql instance. The inference results are added to 
 * an in-memory Jena Model.
 * 
 * @author gmontiel
 *
 */

public class Example17
{

  public static void main(String[] szArgs) throws Exception
  {

    PrintStream psOut = System.out;

    psOut.println("start");
    String szStoreName = szArgs[0];
    String szHostName  = szArgs[1];
    String szHostPort  = szArgs[2];

    // Create a connection to the Oracle NoSQL Database
    OracleNoSqlConnection conn
                            = OracleNoSqlConnection.createInstance(szStoreName, 
                                                                   szHostName, 
                                                                   szHostPort);

    // Create an OracleGraphNoSql object to handle the default graph
    // and use it to create a Jena Model object.
    Node graphNode = Node.createURI("http://example.org/graph1");
    OracleGraphNoSql graph = new OracleNamedGraphNoSql(graphNode, conn);
    Model model = OracleModelNoSql.createOracleModelNoSql(graphNode, conn);

    // Clear model
    model.removeAll();

    Node sub = Node.createURI("http://sub/a");
    Node pred = Node.createURI("http://pred/a");
    Node obj = Node.createURI("http://obj/a");

    // Add few axioms

    Triple triple = Triple.create(sub, pred, obj);
    graph.add(triple);

    graph.add(Triple.create(pred, 
                 Node.createURI("http://www.w3.org/2000/01/rdf-schema#domain"),              
                 Node.createURI("http://C")));

    graph.add(Triple.create(pred, 
             Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), 
             Node.createURI("http://www.w3.org/2002/07/owl#ObjectProperty")));

    {
      // read it out
      ExtendedIterator<Triple> it = GraphUtil.findAll(graph);
      
      while (it.hasNext()) {
        psOut.println("triple " + it.next().toString());
      }

    }

    // Create an OntModel instance
    OntModel om = 
             ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF, 
                                                 model);

    Model modelInMem = ModelFactory.createDefaultModel();
    modelInMem.add(om);

    {
      ExtendedIterator<Triple> it = GraphUtil.findAll(modelInMem.getGraph());
      while (it.hasNext()) {
        psOut.println("triple from OntModel " + it.next().toString());
      }
    }

    model.close();
    conn.close();
  }

}
