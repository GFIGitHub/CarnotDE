
import com.hp.hpl.jena.query.*;
import org.openjena.riot.Lang;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import java.net.URLEncoder;
import oracle.rdf.kv.client.jena.*;

/**
 * Example5g describes how to execute a SPARQL query specifying a plan to 
 * execute a BGP as a SPARQL hint.
 * 
 * <p> This feature is specified as a hint in the ORACLE_SEM_HT_NS namespace 
 * as <em>plan=exPlan</em>, where exPlan is an UTF-8 encoded String representing 
 * a BGP plan to execute triple patterns within a SPARQL query using hash join 
 * and/or nested loop join strategies
 * 
 * <p> A plan is written using a post-fix notation, where a join operation 
 * (expressed as HJ or NLJ) is preceded by its operands (another join operation 
 * or a query pattern expressed as "qp" plus its position in the SPARQL query). 
 * Every join operation and its respective operands should be wrapped into 
 * parenthesis.
 * 
 * <p> If a plan does not satisfy this notation, the hint is ignored and query
 * execution will continue normally.
 * 
 */


public class Example5g
{
  
  public static void main(String[] args) throws Exception
  {
    
    String szStoreName = args[0];
    String szHostName = args[1];
    String szHostPort = args[2];
   
    // Create connection
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
    
    // Load data from file into the dataset
    DatasetGraphNoSql.load("example.nt", Lang.NQUADS, conn, "http://example.com"); 
    
    
    Dataset ds = DatasetImpl.wrap(datasetGraph);
    
    String plan = URLEncoder.encode("((qp2 qp3 NLJ) qp1 NLJ)", "UTF-8");

    String queryString = 
        " PREFIX ORACLE_SEM_HT_NS: <http://oracle.com/semtech#plan=" + plan + ">" +
        " PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
        " SELECT ?name1 ?name2 " +
        " WHERE { " +
        " graph <http://example.org/alice/foaf.rdf> { " +
        "   ?person1 foaf:knows ?person2 . " +
        "   ?person1 foaf:name ?name1 . " +
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
