
import com.hp.hpl.jena.query.*;
import org.openjena.riot.Lang;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import oracle.rdf.kv.client.jena.*;


/**
 * Example5 describes how to execute a SPARQL query using a join method 
 * (NESTED LOOP/HASH) over the RDF data stored in an Oracle NoSQL database.
 * 
 * <p> Query execution in the Jena adapter for Oracle NoSQL database provides 
 * additional performance features such as the join method used in BGP query 
 * execution. By default, the RDF Graph uses a set of optimization strategies
 * to automatically generate a BGP execution plan using nested loop/hash joins. 
 * If desired, we can specify if only a nested loop join or hash join strategy 
 * should be used in query execution.
 * 
 * <p> This feature is specified as a hint in the ORACLE_SEM_FS_NS namespace 
 * as <em>join_method={nl,hash}</em>.
 * 
 */


public class Example5
{
  
  public static void main(String[] args) throws Exception
  {
    
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];
   
    // Create connection    
    OracleNoSqlConnection conn = OracleNoSqlConnection.createInstance(szStoreName,
                                                                      szHostName, 
                                                                      szHostPort);

    // Create the datasetgraph    
    OracleGraphNoSql graph = new OracleGraphNoSql(conn);
    DatasetGraphNoSql datasetGraph = DatasetGraphNoSql.createFrom(graph);
   
    // Close graph, as it is no longer needed
    graph.close();
    
    // Clear dataset
    datasetGraph.clearRepository();
    
    Dataset ds = DatasetImpl.wrap(datasetGraph);
    
    // Load data from file into the dataset    
    DatasetGraphNoSql.load("example.nt", Lang.NQUADS, conn, 
                           "http://example.com"); //base URI

    // change hint to hash to test hash join, or remove to use default join
    // settings
    String szQuery = 
        " PREFIX ORACLE_SEM_FS_NS: <http://oracle.com/semtech#join_method=nl>"  +
        " PREFIX foaf: <http://xmlns.com/foaf/0.1/>"                            +
        " SELECT ?name1 ?name2 "                                                +
        " WHERE { "                                                             +
        "   graph <http://example.org/alice/foaf.rdf> { "                       +
        "     ?person1 foaf:knows ?person2 . "                                  +
        "     ?person1 foaf:name ?name1 . "                                     +
        "     ?person2 foaf:name ?name2 . "                                     +
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
