
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.sparql.core.*;
import com.hp.hpl.jena.query.*;
import oracle.rdf.kv.client.jena.*;

/**
 * Example12 describes how to execute a SPARQL query using NAMED GRAPHS over the 
 * RDF data stored over an Oracle NoSQL database.
 * 
 * <p> To manage RDF data stored in an Oracle NoSQL database, it is required to 
 * create an instance of the {@link OracleNoSqlConnection} class, specifying the
 * data to establish a connection with the Oracle NoSQL Database, such as host 
 * name, port name, and store name. This object will manage all the interactions
 * the between RDF Graph and the Oracle NoSQL Database.
 * 
 * <p> To execute a SPARQL query over the RDF data in an Oracle NoSQL database, 
 * an instance of a Jena Dataset for Oracle NoSQL Database should be 
 * created, as this object represents the input for {@link QueryExecution} 
 * instance in charge of the execution of SPARQL queries in Jena.
 * 
 * <p> To create an instance of {@link DatasetGraphNoSql} through a call to 
 * createFrom(OracleGraphNoSql) method, and build a {@link Dataset} object 
 * from this datasetgraph.
 * 
 * <p> At the end of all the pertinent operations, it is recommended to dispose
 * the OracleNoSqlConnection instance, as well as to close the DatasetGraphNoSql 
 * objects. Closing a DatasetGraphNoSql object does not dispose its associated 
 * connection, as it may be used by other objects in the application.
 * 
 */

public class Example12
{
  
  public static void main(String[] args) throws Exception
  {
  
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];
    
    // Create OracleNoSQL connection 
    OracleNoSqlConnection conn = OracleNoSqlConnection.createInstance(szStoreName,
                                                                      szHostName, 
                                                                      szHostPort);
    
    // Create OracleNoSQl graph and dataset 
    OracleGraphNoSql graph = new OracleGraphNoSql(conn);
    DatasetGraphNoSql datasetGraph = DatasetGraphNoSql.createFrom(graph);

    // Close graph, as it is no longer needed
    graph.close(); 
    
    // Clear dataset
    datasetGraph.clearRepository();
    
    // add data to the default graph
    datasetGraph.add(new Quad(
        Quad.defaultGraphIRI, // specifies default graph
        Node.createURI("http://example.org/bob"),
        Node.createURI("http://purl.org/dc/elements/1.1/publisher"),
        Node.createLiteral("Bob Hacker")));
    
    datasetGraph.add(new Quad(
        Quad.defaultGraphIRI, // specifies default graph
        Node.createURI("http://example.org/alice"),
        Node.createURI("http://purl.org/dc/elements/1.1/publisher"),
        Node.createLiteral("alice Hacker")));
    
    // add data to the bob named graph
    datasetGraph.add(new Quad(
        Node.createURI("http://example.org/bob"), // graph name
        Node.createURI("urn:bob"),
        Node.createURI("http://xmlns.com/foaf/0.1/name"),
        Node.createLiteral("Bob")));
    
    datasetGraph.add(new Quad(
        Node.createURI("http://example.org/bob"), // graph name
        Node.createURI("urn:bob"),
        Node.createURI("http://xmlns.com/foaf/0.1/mbox"),
        Node.createURI("mailto:bob@example")));

    // add data to the alice named graph
    datasetGraph.add(new Quad(
        Node.createURI("http://example.org/alice"), // graph name
        Node.createURI("urn:alice"),
        Node.createURI("http://xmlns.com/foaf/0.1/name"),
        Node.createLiteral("Alice")));
    
    datasetGraph.add(new Quad(
        Node.createURI("http://example.org/alice"), // graph name
        Node.createURI("urn:alice"),
        Node.createURI("http://xmlns.com/foaf/0.1/mbox"),
        Node.createURI("mailto:alice@example")));
    
    Dataset ds = DatasetImpl.wrap(datasetGraph);
    
    String szQuery = " PREFIX foaf: <http://xmlns.com/foaf/0.1/> "        +
                         " PREFIX dc: <http://purl.org/dc/elements/1.1/> "    +
                         " SELECT ?who ?graph ?mbox "                         +
                         " FROM NAMED <http://example.org/alice> "            +
                         " FROM NAMED <http://example.org/bob> "              +
                         " WHERE "                                            +
                         " { "                                                +
                         " ?graph dc:publisher ?who . "                       +
                         " GRAPH ?graph { ?x foaf:mbox ?mbox } "              +
                         " } ";
    
    Query query = QueryFactory.create(szQuery);
    QueryExecution qexec = QueryExecutionFactory.create(query, ds);
    
    try {
      ResultSet results = qexec.execSelect();
      ResultSetFormatter.out(System.out, results, query);
    }
    
    finally {
      qexec.close();
    }
    
    ds.close();
    conn.dispose();
    
  }
}
