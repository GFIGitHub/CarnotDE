

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import oracle.rdf.kv.client.jena.*;

/**
 * Example7 describes how to execute a SPARQL DESCRIBE query over the RDF data
 * stored in an Oracle NoSQL database.
 * 
 * <p> To manage RDF data stored in an Oracle NoSQL database, it is required to 
 * create an instance of the {@link OracleNoSqlConnection} class, specifying the
 * data to establish a connection with the Oracle NoSQL Database, such as host 
 * name, port name, and store name. This object will manage all the interactions
 * the between RDF Graph and the Oracle NoSQL Database.
 * 
 * <p> To execute an DESCRIBE query over the RDF data in an Oracle NoSQL 
 * database, an instance of a Jena Model/Dataset for Oracle NoSQL database 
 * should be created, as this object represents the input for 
 * {@link QueryExecution} instance in charge of the execution of SPARQL queries 
 * in Jena.
 * 
 * <p> If the DESCRIBE query is executed over RDF data (triples) from a default 
 * graph (no named graph), then create an instance of a {@link OracleModelNoSql} 
 * through a call to createOracleDefaultModelNoSql(OracleNoSqlConnection).
 * 
 * <p> If the DESCRIBE query is executed over RDF data (triples) from a specified
 * named graph, then create an instance of a {@link OracleModelNoSql} through a 
 * call to createOracleModelNoSql(Node, OracleNoSqlConnection) specifying the 
 * graph node of the named graph.
 * 
 * <p> If the DESCRIBE query is executed over RDF data from several  
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

public class Example7
{
  
  public static void main(String[] args) throws Exception 
  {
    
    String szStoreName   = args[0];
    String szHostName    = args[1];
    String szHostPort    = args[2];
    String szModelName   = args[3];
    
    // Create Oracle NoSQL connection
    OracleNoSqlConnection conn = OracleNoSqlConnection.createInstance(szStoreName,
                                                                      szHostName, 
                                                                      szHostPort);
    
    // Create model from named graph
    OracleModelNoSql model = OracleModelNoSql.createOracleModelNoSql(szModelName, 
                                                                     conn);
    
    // Clear model
    model.removeAll();
    
    // Get graph from model
    OracleGraphNoSql graph = model.getGraph();
    
    // Add triples   
    graph.add(Triple.create(Node.createURI("u:John"), 
                            Node.createURI("u:parentOf"),
                            Node.createURI("u:Mary")));
    
    graph.add(Triple.create(Node.createURI("u:John"), 
                            Node.createURI("u:parentOf"),
                            Node.createURI("u:Jack")));
    
    graph.add(Triple.create(Node.createURI("u:Amy"), 
                            Node.createURI("u:parentOf"),
                            Node.createURI("u:Jack")));
    
    String szQuery = "DESCRIBE ?x WHERE {?x <u:parentOf> <u:Jack>}";
    
    System.out.println("Execute describe query " + szQuery);
    
    Query query = QueryFactory.create(szQuery);
    QueryExecution qexec = QueryExecutionFactory.create(query, model);
    
    Model describeModel = qexec.execDescribe();
    
    System.out.println("Describe result = " + describeModel.toString());
    
    qexec.close();
    describeModel.close();
    
    model.close();
    conn.dispose();
    
  }
}
