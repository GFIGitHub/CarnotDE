
import com.hp.hpl.jena.query.*;
import org.openjena.riot.Lang;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import oracle.rdf.kv.client.jena.*;

/**
 * Example5c describes how to execute a SPARQL query using timeout and graceful 
 * timeout policies in an Oracle NoSQL database.
 * 
 * <p> Query execution in the Jena adapter for Oracle NoSQL database provides 
 * additional performance features such as timeout and graceful timeout policies 
 * used in query execution. 
 * 
 *  <p> The <em>timeout</em> policy allows to execute the SPARQL query before the 
 * time out (in seconds) is reached, else it stop its executions and generates an
 * error. This feature is specified as as a hint in the ORACLE_SEM_FS_NS 
 * namespace as <em>timeout=N</em>. By default, a timeout of 0 is used, and query 
 * execution must wait until it is completed. 
 * 
 * <p> An additional feature called <em>best effort</em> policy can be set 
 * to retrieve partial results if the timeout is reached. This feature is 
 * specified as as a hint in the ORACLE_SEM_FS_NS namespace as 
 * <em>best_effort_query=t</em>. By default, the best effort policy is set to 
 * false. 
 * 
 */

public class Example5c
{
  
  public static void main(String[] args) throws Exception
  {
    
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];
   
    // create connection
    OracleNoSqlConnection conn = OracleNoSqlConnection.createInstance(szStoreName,
                                                                      szHostName, 
                                                                      szHostPort);

    // Create datasetgraph  
    OracleGraphNoSql graph = new OracleGraphNoSql(conn);
    DatasetGraphNoSql datasetGraph = DatasetGraphNoSql.createFrom(graph);
   
    // Close graph, as it is no longer needed
    graph.close();
    
    // Clear the dataset
    datasetGraph.clearRepository();
    
    Dataset ds = DatasetImpl.wrap(datasetGraph);
    
    // Load data from file into the dataset
    DatasetGraphNoSql.load("example.nt", Lang.NQUADS, conn, "http://example.com");    
   
    // Add a hint best_effort_query=t to use a graceful timeout policy   
    String szQuery = 
        " PREFIX ORACLE_SEM_FS_NS: <http://oracle.com/semtech#timeout=1>"       +
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
