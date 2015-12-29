

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import oracle.rdf.kv.client.jena.*;

/**
 * Example10 describes how to execute a SPARQL query with a LIMIT and OFFSET  
 * statement over the RDF data stored over a specified NoSQL database.
 * 
 * <p> To manage RDF data stored in an Oracle NoSQL database, it is required to 
 * create an instance of the {@link OracleNoSqlConnection} class, specifying the
 * data to establish a connection with the Oracle NoSQL Database, such as host 
 * name, port name, and store name. This object will manage all the interactions
 * the between RDF Graph and the Oracle NoSQL Database.
 * 
 * <p> To execute a query over the RDF data in an Oracle NoSQL database, an 
 * instance of a Jena Model/Dataset for Oracle NoSQL database should be created, 
 * as this object represents the input for {@link QueryExecution} instance in 
 * charge of the execution of SPARQL queries in Jena.
 * 
 * <p> If the query is executed over RDF data (triples) from a default 
 * graph (no named graph), then create an instance of a {@link OracleModelNoSql} 
 * through a call to createOracleDefaultModelNoSql(OracleNoSqlConnection).
 * 
 * <p> If the query is executed over RDF data (triples) from a specified
 * named graph, then create an instance of a {@link OracleModelNoSql} through a 
 * call to createOracleModelNoSql(Node, OracleNoSqlConnection) specifying the 
 * graph node of the named graph.
 * 
 * <p> If the query is executed over RDF data from several  
 * named graphs (and/or the default graph), then create a 
 * {@link Dataset} object from a {@link DatasetGraphNoSql} object through a call
 * to DatasetImpl.wrap(com.hp.hpl.jena.sparql.core.DatasetGraph). A 
 * {@link DatasetGraphNoSql} object is created through a call to 
 * createFrom(OracleGraphNoSql) method. It is recommended 
 * that the {@link OracleGraphNoSql} object is closed right after the 
 * {@link DatasetGraphNoSql} object is instantiated. 
 * 
 * <p> At the end of all the pertinent operations, it is recommended to dispose
 * the OracleNoSqlConnection instance, as well as to close the OracleModelNoSql 
 * object. Closing a OracleModelNoSql object does not dispose its associated 
 * connection, as it may be used by other objects in the application.
 * 
 */


public class Example10
{
  
  public static void main(String[] args) throws Exception 
  {
    
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];
    String szModelName  = args[3];
    
    // Create OracleNoSQL connection 
    OracleNoSqlConnection conn = OracleNoSqlConnection.createInstance(szStoreName,
                                                                      szHostName, 
                                                                      szHostPort);
    
    // Create model from named graph
    Model model = OracleModelNoSql.createOracleModelNoSql(szModelName, conn);
    OracleGraphNoSql graph = (OracleGraphNoSql) model.getGraph();

    // Clear graph    
    graph.clearRepository();
    
    
    // Add triples 
    graph.add(Triple.create(Node.createURI("u:John"), 
                            Node.createURI("u:parentOf"),
                            Node.createURI("u:Mary")));
    
    graph.add(Triple.create(Node.createURI("u:John"), 
                            Node.createURI("u:parentOf"),
                            Node.createURI("u:Jack")));
    
    graph.add(Triple.create(Node.createURI("u:Mary"), 
                            Node.createURI("u:parentOf"),
                            Node.createURI("u:Jill")));
    
    
    String szQuery = " SELECT ?s ?o ?gkid "                         +
                     " WHERE { ?s <u:parentOf> ?o . "               +
                     " OPTIONAL {?o <u:parentOf> ?gkid }} "         +
                     " LIMIT 1 OFFSET 2";
    
    System.out.println("Execute query " + szQuery);
    
    Query query = QueryFactory.create(szQuery);
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
