
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import oracle.rdf.kv.client.jena.*;

/**
 * Example5e describes how to execute a SPARQL query using an inference only 
 * policy.
 * 
 * <p> Query execution in the Jena adapter for Oracle NoSQL database provides 
 * additional performance features such as an a only policy
 * used in query execution. 
 * 
 * <p> This feature is specified as a hint in the ORACLE_SEM_FS_NS namespace 
 * as <em>inf_only</em>, and establishes that the SPARQL query should retrieve
 * only inferred triple/quads.
 * 
 */

public class Example5e
{
  
  public static void main(String[] args) throws Exception 
  {
    
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];
    int iRuleBaseId     = Integer.parseInt(args[3]);
    
    // Create Oracle NoSQL connection 
    OracleNoSqlConnection conn = OracleNoSqlConnection.createInstance(szStoreName,
                                                                      szHostName, 
                                                                      szHostPort);
    
    // Create model from default graph 
    Model model = OracleModelNoSql.createOracleDefaultModelNoSql(conn);
    OracleGraphNoSql graph = (OracleGraphNoSql) model.getGraph();
    
    // Clear model
    model.removeAll();

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
    
    // Create Oracle NoSQL inferred graph  
    InferredGraphNoSql inferredGraph = new InferredGraphNoSql(conn,
                                                              iRuleBaseId);

    // Add inferred triples    
    inferredGraph.add(Triple.create(Node.createURI("u:Jack"), 
                                    Node.createURI("u:siblingOf"),
                                    Node.createURI("u:Mary")));
    
    // Close inferred graph;
    inferredGraph.close();
    
    String prefix = " PREFIX ORACLE_SEM_FS_NS: <http://oracle.com/semtech#" +
                    "include_rulebase_id=" + iRuleBaseId + 
                    ",inf_only>";

    String szQuery = prefix + " select ?x ?y ?z WHERE {?x ?y ?z} ";
    
    System.out.println("Execute query " + szQuery);
    
    Query query = QueryFactory.create(szQuery) ;
    QueryExecution qexec = QueryExecutionFactory.create(query, model);
    
    try {
      ResultSet results = qexec.execSelect();
      ResultSetFormatter.out(System.out, results, query);
    }
    
    finally {
      qexec.close();
    }
    

    model.close();
    conn.dispose();
    
  }
}