
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import oracle.rdf.kv.client.jena.OracleModelNoSql;
import oracle.rdf.kv.client.jena.OracleNoSqlConnection;

/**
 * Example4b describes how to execute a SPARQL query over the RDF data from a
 * named graph stored in an Oracle NoSQL database.
 * 
 * <p> To manage RDF data stored in an Oracle NoSQL database, it is required to 
 * create an instance of the {@link OracleNoSqlConnection} class, specifying the
 * data to establish a connection with the Oracle NoSQL Database, such as host 
 * name, port name, and store name. This object will manage all the interactions
 * the between RDF Graph and the Oracle NoSQL Database.
 * 
 * <p> To execute a SPARQL query over the RDF data in an Oracle NoSQL database, 
 * an instance of a Jena Model for Oracle NoSQL database should be created, as 
 * this object represents the input for {@link QueryExecution} instance in 
 * charge of the execution of SPARQL queries in Jena.
 * 
 * <p> If the SPARQL query is executed over RDF data (triples) from a named 
 * graph, then create an instance of a {@link OracleModelNoSql} through a 
 * call to createOracleModelNoSql(Node, OracleNoSqlConnection) specifying the 
 * graph node of the named graph.
 * 
 * <p> At the end of all the pertinent operations, it is recommended to dispose
 * the OracleNoSqlConnection instance, as well as to close the OracleModelNoSql 
 * object. Closing a OracleModelNoSql object does not dispose its associated 
 * connection, as it may be used by other objects in the application.
 * 
 */

public class Example4b
{
  
  public static void main(String[] args) throws Exception
  {
    
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];
    String szGraphName  = args[3];
    
    System.out.println("Create OracleNoSQL connection");
    
    OracleNoSqlConnection conn = OracleNoSqlConnection.createInstance(szStoreName,
                                                                      szHostName, 
                                                                      szHostPort);
    
    System.out.println("Create OracleNoSQl model");
    
    Model model = OracleModelNoSql.createOracleModelNoSql(szGraphName, conn);
    
    System.out.println("Clear model");
    
    model.removeAll();
    
    
    System.out.println("Add triples");
    
    model.getGraph().add(
                     Triple.create(Node.createURI("http://example.com/John"),
                                   Node.createURI("http://example.com/fatherOf"),
                                   Node.createURI("http://example.com/Mary")));
    
    
    String queryString  = " select ?father ?kid "                           +
                          " where "                                         +
                          " { ?father <http://example.com/fatherOf> ?kid }";
    
    System.out.println("Execute query " + queryString);
    
    Query query = QueryFactory.create(queryString);
    QueryExecution qexec = QueryExecutionFactory.create(query, model);
    
    try {
      ResultSet results = qexec.execSelect();
      ResultSetFormatter.outputAsJSON(System.out, results);
    }
    
    finally {
      qexec.close();
    }
    
    model.close();
    conn.dispose();
    
  }
}
