

import org.openjena.riot.Lang;
import oracle.rdf.kv.client.jena.*;

/**
 * 
 * Example16 describes how to generate sampling data from a dataset. This will 
 * generate a set of sampling records representing a percent of the total 
 * triples/quads included in this dataset. Data sampling is generated with a 
 * specified sampling rate triples/quads.
 * 
 * <p> To manage RDF data stored in an Oracle NoSQL database, it is required to 
 * create an instance of the {@link OracleNoSqlConnection} class, specifying the
 * data to establish a connection with the Oracle NoSQL Database, such as host 
 * name, port name, and store name. This object will manage all the interactions
 * the between RDF Graph and the Oracle NoSQL Database.
 * 
 * <p> Sampling generation is done through a call to the analyze method from the 
 * {@link DatasetGraphNoSql} class. Additionally, users can specify the bulk size
 * and degree of parallelism to use in order to optimize the sampling generation 
 * process.
 * 
 * <p> At the end of all the pertinent operations, it is recommended to dispose
 * the OracleNoSqlConnection instance, as well as to close the DatasetGraphNoSql
 * object. Closing a DatasetGraphNoSql object does not dispose its associated 
 * connection, as it may be used by other objects in the application.
 * 
 * @version 1.0
 * @author Gabriela Montiel, Zhe Wu
 * 
 */

public class Example16
{
  
  public static void main(String[] args) throws Exception
  {
    
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];
    double iSampRate = Double.parseDouble(args[3]); 
    
    // Create OracleNoSQL connection  
    OracleNoSqlConnection conn = OracleNoSqlConnection.createInstance(szStoreName,
                                                                      szHostName, 
                                                                      szHostPort);
    
    
    // Create an Oracle DatasetGraphNoSql object to manage the dataset in the
    // Oracle NoSQL Database
    OracleGraphNoSql graph = new OracleGraphNoSql(conn);
    DatasetGraphNoSql datasetGraph = DatasetGraphNoSql.createFrom(graph);
   
    // Clear dataset and close it as it is needed just to clear the dataset
    datasetGraph.clearRepository();
    datasetGraph.close();
    

    
    // Load data from file into the Oracle NoSQL database
    DatasetGraphNoSql.load("family.rdf", Lang.RDFXML, conn, 
                           "http://example.com");     
  
    // Analyze the default graph and gnerate sampling data
    long sizeSamp = graph.analyze(iSampRate);
    
    System.out.println("sampling size is " + sizeSamp);
    
    graph.close();
    conn.dispose();
    
  }
  
}
