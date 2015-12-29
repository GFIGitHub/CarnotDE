
import com.hp.hpl.jena.query.*;
import org.openjena.riot.Lang;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import oracle.rdf.kv.client.jena.*;


/**
 * Example5d describes how to execute a SPARQL query using parallel execution 
 * in an Oracle NoSQL database.
 * 
 * <p> Query execution in the Jena adapter for Oracle NoSQL database provides 
 * additional performance features such as timeout and graceful timeout policies
 * used in query execution. 
 * 
 * <p> The <em>degree of parallelism</em> allows to execute the SPARQL query using
 * multi-threading to optimize performance. This feature is specified as a hint
 * in the ORACLE_SEM_FS_NS namespace as <em>DOP=N</em>. By default, a DOP of 1
 * is used in query execution.
 * 
 */

public class Example5d
{
  
  public static void main(String[] args) throws Exception
  {
    
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];
   
    System.out.println("create connection");
    OracleNoSqlConnection conn = OracleNoSqlConnection.createInstance(szStoreName,
                                                                      szHostName, 
                                                                      szHostPort);


    // Create datasetgraph   
    OracleGraphNoSql graph = new OracleGraphNoSql(conn);
    DatasetGraphNoSql datasetGraph = DatasetGraphNoSql.createFrom(graph);
   
    // Close graph, as it is no longer needed
    graph.close();
    
    // Clear dataset
    datasetGraph.clearRepository();
    
    Dataset ds = DatasetImpl.wrap(datasetGraph);
    
    // Load data from file into the dataset
    DatasetGraphNoSql.load("example.nt", Lang.NQUADS, conn, "http://example.com");    
   
    String szQuery = 
        " PREFIX ORACLE_SEM_FS_NS: <http://oracle.com/semtech#dop=4>"           +
        " PREFIX foaf: <http://xmlns.com/foaf/0.1/>"                            +
        " SELECT ?name1 ?name2 ?homepage2 "                                     +
        " WHERE { "                                                             +
        "   graph <http://example.org/alice/foaf.rdf> { "                       +
        "     ?person1 foaf:knows ?person2 . "                                  +
        "     ?person1 foaf:name ?name1 . "                                     +
        "     ?person2 foaf:name ?name2 . "                                     +
        "     ?person2 foaf:homepage ?homepage2 . "                             +
        "   } "                                                                 +
        " } ";
    
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
