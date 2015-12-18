
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import org.openjena.riot.Lang;
import oracle.rdf.kv.client.jena.*;

/**
 * 
 * Example2b describes how to load and store a RDF data file into an Oracle NoSQL
 * database using parallel loading.
 * 
 * <p> To manage RDF data stored in an Oracle NoSQL database, it is required to 
 * create an instance of the {@link OracleNoSqlConnection} class, specifying the
 * data to establish a connection with the Oracle NoSQL Database, such as host 
 * name, port name, and store name. This object will manage all the interactions
 * the between RDF Graph and the Oracle NoSQL Database.
 * 
 * <p> Data load is done through a call to the 
 * load(String, Lang, OracleNoSqlConnection, String, int, int)
 * method from the {@link DatasetGraphNoSql} class, specifying the file name and 
 * language in which RDF data is represented i.e. RDF/XML, N-TRIPLES, NQUADS, 
 * TURTLE, etc. To use parallel loading, we can specify the degree of 
 * parallelism and bulk size used to optimize data upload using multiple threads. 
 * Further information on these configurations can be found in 
 * {@link DatasetGraphNoSql} APIs.
 * 
 * <p> To execute a query to validate the RDF data load, created create a 
 * {@link Dataset} object from a {@link DatasetGraphNoSql} object through a call
 * to  DatasetImpl.wrap(com.hp.hpl.jena.sparql.core.DatasetGraph). A 
 * {@link DatasetGraphNoSql} object is created through a call to 
 * createFrom(OracleGraphNoSql) method. It is recommended 
 * that the {@link OracleGraphNoSql} object is closed right after the 
 * {@link DatasetGraphNoSql} object is instantiated. 
 * 
 * <p> This dataset is the input for the {@link QueryExecution} instance in 
 * charge of the execution of SPARQL queries.
 * 
 * <p> At the end of all the pertinent operations, it is recommended to dispose
 * the OracleNoSqlConnection instance, as well as to close the DatasetGraphNoSql
 * object. Closing a DatasetGraphNoSql object does not dispose its associated 
 * connection, as it may be used by other objects in the application.
 * 
 */

public class Example2b
{
  
  public static void main(String[] args) throws Exception
  {
    
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];
    int iBatchSize      = Integer.parseInt(args[3]);
    int iDOP            = Integer.parseInt(args[4]);

    System.out.println("Create OracleNoSQL connection");
    OracleNoSqlConnection conn = OracleNoSqlConnection.createInstance(szStoreName,
                                                                      szHostName, 
                                                                      szHostPort);
     
    System.out.println("Create Oracle NoSQL datasetgraph");
    OracleGraphNoSql graph = new OracleGraphNoSql(conn);
    DatasetGraphNoSql datasetGraph = DatasetGraphNoSql.createFrom(graph);
   
    // Close graph, as it is no longer needed
    graph.close();
    
    // Clear datasetgraph
    datasetGraph.clearRepository();
    
    // Load data from file into the Oracle NoSQL Database
    DatasetGraphNoSql.load("example.nt", Lang.NQUADS, conn, 
                           "http://example.org",
                           iBatchSize, // batch size
                           iDOP); // degree of parallelism
    
    // Create dataset from Oracle NoSQL datasetgraph to execute
    Dataset ds = DatasetImpl.wrap(datasetGraph);
   
    String szQuery = "select * where { graph ?g { ?s ?p ?o }  }"; 
    System.out.println("Execute query " + szQuery);
    
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
