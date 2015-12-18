
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.query.*;
import oracle.rdf.kv.client.jena.*;

/**
 * Example15 describes how to create an OracleNoSQLPool managing connections
 * over an Oracle NoSQL database.
 * 
 * <p> To create an instance of this class, use the {@link #createInstance} 
 * method. Here, users can determine the capacity of the pool (number of 
 * available connections), and a lazyInit flag specifying if pool should execute
 * a lazy initialization. A lazy initialization will generate connections as soon
 * as they are requested. If this lazyInit flag is false, connections in the 
 * pool will be generated at construction time.
 * 
 * <p> To get a connection resource from the pool, the method 
 * {@link #getResource()} should be executed. If a wait flag is set to true and
 * there is no connection available, this operation will wait until a resource 
 * becomes available.
 * 
 * <p> To return a connection back to the connection pool, the method
 * {@link #freeResource(OracleNoSqlConnection)} should be called. This method 
 * will release the connection and notify all waiting threads that a connection
 * has become available. 
 * 
 * <p> At the end, the pool must be closed in order to dispose all the connection
 * resources.
 * 
 * @version 1.0
 * @author Gabriela Montiel, Zhe Wu
 * 
 */ 

public class Example15
{
  
  public static void main(String[] args) throws Exception
  {
    
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];
    String szModelName  = args[3];
    int iPoolSize = Integer.parseInt(args[4]);    
    
    // Property of the pool: wait if no connection is available at request.
    boolean bWaitIfBusy = true;
    
    System.out.println("Creating OracleNoSQL pool");    
    OracleNoSqlPool pool = OracleNoSqlPool.createInstance(szStoreName,
                                                          szHostName, 
                                                          szHostPort, iPoolSize, 
                                                          bWaitIfBusy, 
                                                          true); //lazyInit
        
    System.out.println("Done creating OracleNoSql pool"); 
    
    // grab an Oracle NoSQL connection and do something
    System.out.println("Get a connection from the pool");
    OracleNoSqlConnection conn = pool.getResource();
    
    OracleModelNoSql model = OracleModelNoSql.createOracleModelNoSql(szModelName, 
                                                                     conn);
    
    System.out.println("Clear model");
    model.removeAll();
    
    model.getGraph().add(Triple.create(Node.createURI("u:John"),
                                       Node.createURI("u:cousinOf"),
                                       Node.createURI("u:Jackie")));
    
    model.close();
    
    //return connection back to the pool 
    conn.dispose();
    
    // grab another Oracle NoSQL connection and do something
    System.out.println("Get a connection from the pool");
    conn = pool.getResource();
    
    String queryString = "select ?x ?y ?z WHERE {?x ?y ?z}";
    
    System.out.println("Execute query " + queryString);
    
    Query query = QueryFactory.create(queryString) ;
    QueryExecution qexec = QueryExecutionFactory.create(query, model);
    
    try {
      ResultSet results = qexec.execSelect();
      ResultSetFormatter.out(System.out, results, query);
    }
    
    finally {
      qexec.close();
    } 
    
    model.close();
    
    //return connection back to the pool 
    conn.dispose();
    
    // Close pool. This will close all resources even if they have not been freed up
    System.out.println("Close pool, this will close all resources");
    pool.close();
    
  }

}