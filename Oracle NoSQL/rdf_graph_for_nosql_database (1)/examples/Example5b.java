
import com.hp.hpl.jena.query.*;
import org.openjena.riot.Lang;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import oracle.rdf.kv.client.jena.*;

/**
 * Example5b describes how to execute a SPARQL query using an ordered policy.
 * 
 * <p> This feature is specified as a hint in the ORACLE_SEM_FS_NS namespace 
 * as <em>ordered=t</em>, and establishes that triple patterns in a BGP should
 * be executed in the order specified in the SPARQL query.
 * 
 */

public class Example5b
{
  
  public static void main(String[] args) throws Exception
  {
    
    String szStoreName = args[0];
    String szHostName = args[1];
    String szHostPort = args[2];
   
    System.out.println("create connection");  
    OracleNoSqlConnection conn = OracleNoSqlConnection.createInstance(szStoreName,
                                                                      szHostName, 
                                                                      szHostPort);

    System.out.println("Create datasetgraph");   
    OracleGraphNoSql graph = new OracleGraphNoSql(conn);
    DatasetGraphNoSql datasetGraph = DatasetGraphNoSql.createFrom(graph);
   
    // Close graph, as it is no longer needed
    graph.close();
    
    System.out.println("Clear dataset");
    datasetGraph.clearRepository();
    
    
    System.out.println("Load data from file into a DatasetGraphNoSql");   
    DatasetGraphNoSql.load("example.nt", Lang.NQUADS, conn, 
                           "http://example.com"); 
    
    
    Dataset ds = DatasetImpl.wrap(datasetGraph);
   
    String queryString = 
        " PREFIX ORACLE_SEM_FS_NS: <http://oracle.com/semtech#ordered>" +
        " PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
        " SELECT ?name1 ?name2 " +
        " WHERE { " +
        " graph <http://example.org/alice/foaf.rdf> { " +
        "   ?person1 foaf:name ?name1 . " +
        "   ?person1 foaf:knows ?person2 . " +
        "   ?person2 foaf:name ?name2 . " +
        " } } ";
    
    System.out.println("Execute query " + queryString);
    
    Query query = QueryFactory.create(queryString);
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
