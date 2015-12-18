
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import oracle.rdf.kv.client.jena.*;

/**
 * Example1 describes how to add/remove a set of triples over a default graph 
 * stored over an Oracle NoSQL database.
 * 
 * <p> To manage RDF data stored in an Oracle NoSQL database, it is required to 
 * create an instance of the {@link OracleNoSqlConnection} class, specifying the
 * data to establish a connection with the Oracle NoSQL Database, such as host 
 * name, port name, and store name. This object will manage all the interactions
 * the between RDF Graph and the Oracle NoSQL Database.
 * 
 * <p> Add/removal of triples is done through calls to the add(Triple) and 
 * remove(Triple) methods from the {@link OracleGraphNoSql} class. An instance 
 * of the {@link OracleGraphNoSql} class can be created through 
 * the constructor {@link oracle.rdf.kv.client.jena.OracleGraphNoSql
 * #OracleGraphNoSql(OracleNoSqlConnection)}.
 * 
 * <p> Additional settings can be defined when we create a
 * {@link OracleGraphNoSql} to optimize performance, such as clear repository,
 * bulk size used when great amounts of data is uploaded, and pool size for a
 * Oracle NoSQL pool used in query answering and data upload. For details on 
 * these configurations, see {@link OracleGraphNoSql} APIs.
 * 
 * <p> To execute a query to validate the RDF data manipulation, create an 
 * instance of {@link OracleModelNoSql} from the default graph. This model is
 * the input for {@link QueryExecution} instance in charge of the execution of 
 * SPARQL queries.
 * 
 * <p> At the end of all the pertinent operations, it is recommended to dispose
 * the OracleNoSqlConnection instance, as well as to close the OracleGraphNoSql 
 * object. Closing a OracleGraphNoSql object does not dispose its associated 
 * connection, as it may be used by other objects in the application.
 * 
 */

public class Example1 {
  
  public static void main(String[] args) throws Exception 
  {
    
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];

    OracleNoSqlConnection conn = OracleNoSqlConnection.createInstance(szStoreName,
                                                                      szHostName, 
                                                                      szHostPort);
    
    // This object will handle operations over the default graph 
    OracleGraphNoSql graph = new OracleGraphNoSql(conn);
    graph.clearRepository(); // Clear the graph including inferred triples
    
    graph.add(Triple.create(Node.createURI("u:John"), 
                            Node.createURI("u:parentOf"),
                            Node.createURI("u:Mary")));
        
    graph.add(Triple.create(Node.createURI("u:Mary"), 
                            Node.createURI("u:parentOf"),
                            Node.createURI("u:Jack")));
       
    String queryString = " select ?x ?y WHERE {?x <u:parentOf> ?y}";
    
    System.out.println("Execute query " + queryString);

    Model model = new OracleModelNoSql(graph);
    Query query = QueryFactory.create(queryString) ;
    QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
    
    try {
      ResultSet results = qexec.execSelect();
      ResultSetFormatter.out(System.out, results, query);
    }
    
    finally {
      qexec.close();
    }

    graph.delete(Triple.create(Node.createURI("u:John"), 
                               Node.createURI("u:parentOf"),
                               Node.createURI("u:Mary")));

    queryString = "select ?x ?y ?z WHERE {?x ?y ?z}";
    
    System.out.println("Execute query " + queryString);

    query = QueryFactory.create(queryString) ;
    qexec = QueryExecutionFactory.create(query, model);
    
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
