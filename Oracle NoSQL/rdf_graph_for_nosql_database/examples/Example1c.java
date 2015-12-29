
import oracle.rdf.kv.client.jena.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Example1c describes how to save inferred RDF data from a default graph
 * into an Oracle NoSQL database.
 * 
 * <p> To manage RDF data stored in an Oracle NoSQL database, it is required to 
 * create an instance of the {@link OracleNoSqlConnection} class, specifying the
 * data to establish a connection with the Oracle NoSQL Database, such as host 
 * name, port name, and store name. This object will manage all the interactions
 * the between RDF Graph and the Oracle NoSQL Database.
 * 
 * <p> Add/removal of inferred triples is done through calls to the add(Triple) 
 * and remove(Triple) methods from the {@link InferredGraphNoSql} class. An 
 * instance of the {@link InferredGraphNoSql} class can be created through the 
 * constructor {@link oracle.rdf.kv.client.jena.InferredGraphNoSql
 * #InferredGraphNoSql(OracleNoSqlConnection, int)}, specifying the
 * ID of the rule base used to generate the inferred triple.
 *  
 * <p> At the end of all the pertinent operations, it is recommended to dispose
 * the OracleNoSqlConnection instance, as well as to close the InferredGraphNoSql
 * object. Closing a InferredGraphNoSql object does not dispose its associated 
 * connection, as it may be used by other objects in the application.
 * 
 */

public class Example1c {
  
  public static void main(String[] args) throws Exception 
  {
    
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];

    // the rule base id used for inferred triples 
    int iRuleBaseId     = Integer.parseInt(args[4]); 
    
    OracleNoSqlConnection conn = OracleNoSqlConnection.createInstance(szStoreName,
                                                                      szHostName, 
                                                                      szHostPort);
    
    // This object handle a model associated to a default graph
    Model model = OracleModelNoSql.createOracleDefaultModelNoSql(conn);
    OracleGraphNoSql graph = (OracleGraphNoSql) model.getGraph();
    
    model.removeAll(); // removes all the triples from the associated model, 
                       // this will remove all asserted and inferred triples

    graph.add(Triple.create(Node.createURI("u:John"), 
                            Node.createURI("u:parentOf"),
                            Node.createURI("u:Mary")));
    
    graph.add(Triple.create(Node.createURI("u:John"), 
                            Node.createURI("u:parentOf"),
                            Node.createURI("u:Jack")));
    
    graph.add(Triple.create(Node.createURI("u:Amy"), 
                            Node.createURI("u:parentOf"),
                            Node.createURI("u:Jack")));   

    // This object handles all the inferred triples of the default graph 
    // produced with rule base ID    
    InferredGraphNoSql inferredGraph = new InferredGraphNoSql(conn,
                                                              iRuleBaseId);
    
    inferredGraph.add(Triple.create(Node.createURI("u:Jack"), 
                                    Node.createURI("u:siblingOf"),
                                    Node.createURI("u:Mary")));
    
    inferredGraph.close();
      
    String prefix = " PREFIX ORACLE_SEM_FS_NS: <http://oracle.com/semtech#" +
                    "include_rulebase_id=" + iRuleBaseId + ">";

    String szQuery = prefix + " select ?x ?y ?z WHERE {?x ?y ?z} ";

    
    System.out.println("Execute query " + szQuery);
    
    Query query = QueryFactory.create(szQuery) ;
    QueryExecution qexec = QueryExecutionFactory.create(query, model);
    
    try {
      ResultSet results = qexec.execSelect();
      ResultSetFormatter.out(System.out, results, query);
    }
    
    finally {
      qexec.close();
    }
    

    model.close();
    conn.dispose();
    
  }
}
