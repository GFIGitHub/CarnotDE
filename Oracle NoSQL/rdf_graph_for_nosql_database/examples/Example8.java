
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;

import oracle.rdf.kv.client.jena.DatasetGraphNoSql;
import oracle.rdf.kv.client.jena.OracleGraphNoSql;
import oracle.rdf.kv.client.jena.OracleModelNoSql;
import oracle.rdf.kv.client.jena.OracleNoSqlConnection;

/**
 * Example8 describes how to execute a SPARQL CONSTRUCT query over the RDF data 
 * stored in an Oracle NoSQL database.
 * 
 * <p> To manage RDF data stored in an Oracle NoSQL database, it is required to 
 * create an instance of the {@link OracleNoSqlConnection} class, specifying the
 * data to establish a connection with the Oracle NoSQL Database, such as host 
 * name, port name, and store name. This object will manage all the interactions
 * the between RDF Graph and the Oracle NoSQL Database.
 * 
 * <p> To execute a SPARQL CONSTRUCT query over the RDF data in an Oracle NoSQL 
 * database, an instance of a Jena Model or a Jena Dataset for Oracle NoSQL 
 * database should be created, as this object represents the input for {
 * @link QueryExecution} instance in charge of the execution of SPARQL queries 
 * in Jena.
 * 
 * <p> If the SPARQL CONSTRUCT query is executed over RDF data (triples) from a 
 * graph (no named graph), then create an instance of a {@link OracleModelNoSql} 
 * through a call to createOracleDefaultModelNoSql(OracleNoSqlConnection).
 * 
 * <p> If the SPARQL CONSTRUCT query is executed over RDF data (triples) from a
 * named graph, then create an instance of a {@link OracleModelNoSql} through a 
 * call to createOracleModelNoSql(Node, OracleNoSqlConnection) specifying the 
 * graph node of the named graph.
 * 
 * <p> If the SPARQL CONSTRUCT query is executed over RDF data from several  
 * named graphs (and/or the default graph), then create a 
 * {@link Dataset} object from a {@link DatasetGraphNoSql} object through a call
 * to DatasetImpl.wrap(com.hp.hpl.jena.sparql.core.DatasetGraph). A 
 * {@link DatasetGraphNoSql} object is created through a call to 
 * createFrom(OracleGraphNoSql) method. It is recommended 
 * that the {@link OracleGraphNoSql} object is closed right after the 
 * {@link DatasetGraphNoSql} object is instantiated. 
 * 
 * <p> At the end of all the pertinent operations, it is recommended to dispose
 * the OracleNoSqlConnection instance, as well as to close the OracleGraphNoSql/
 * OracleModelNoSql/DatasetGraphNoSql objects. Closing a OracleGraphNoSql object
 * does not dispose its associated connection, as it may be used by other 
 * objects in the application.
 * 
 * @version 1.0
 * @author Gabriela Montiel, Zhe Wu
 * 
 */
public class Example8
{
  public static void main(String[] args) throws Exception 
  {
    
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];
    String szModelName  = args[3];
    
    System.out.println("Create OracleNoSQL connection");
    
    OracleNoSqlConnection conn = OracleNoSqlConnection.createInstance(szStoreName,
                                                                      szHostName, 
                                                                      szHostPort);
    
    System.out.println("Create OracleNoSQl model");
    
    OracleModelNoSql model = OracleModelNoSql.createOracleModelNoSql(szModelName, 
                                                                     conn);
    
    System.out.println("Clear model");
    model.removeAll();
    
    System.out.println("Get graph from model");
    OracleGraphNoSql graph = model.getGraph();
    
    System.out.println("Add triples");
    
    graph.add(Triple.create(Node.createURI("u:John"), 
                            Node.createURI("u:parentOf"),
                            Node.createURI("u:Mary")));
    
    graph.add(Triple.create(Node.createURI("u:John"), 
                            Node.createURI("u:parentOf"),
                            Node.createURI("u:Jack")));
    
    graph.add(Triple.create(Node.createURI("u:Amy"), 
                            Node.createURI("u:parentOf"),
                            Node.createURI("u:Jack")));
    
    String szQuery = "CONSTRUCT { ?s <u:loves> ?o } " +
                         "WHERE {?s <u:parentOf> ?o }";
    
    System.out.println("Execute construct query " + szQuery);
    
    Query query = QueryFactory.create(szQuery) ;
    QueryExecution qexec = QueryExecutionFactory.create(query, model);
    
    Model constructModel = qexec.execConstruct();
    System.out.println("Construct result = " + constructModel.toString());
    
    qexec.close();
    constructModel.close();
    
    model.close();
    conn.dispose();
    
  }
  
}