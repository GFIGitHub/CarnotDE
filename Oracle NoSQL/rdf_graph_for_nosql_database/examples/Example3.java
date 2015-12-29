
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import oracle.rdf.kv.client.jena.OracleGraphNoSql;
import oracle.rdf.kv.client.jena.OracleModelNoSql;
import oracle.rdf.kv.client.jena.OracleNoSqlConnection;

/**
 * Example3 describes how to export the RDF data contained in a graph 
 * (or named graph) stored in an Oracle NoSQL database into an RDF file.
 * 
 * <p> To manage RDF data stored in an Oracle NoSQL database, it is required to 
 * create an instance of the {@link OracleNoSqlConnection} class, specifying the
 * data to establish a connection with the Oracle NoSQL Database, such as host 
 * name, port name, and store name. This object will manage all the interactions
 * the between RDF Graph and the Oracle NoSQL Database.
 * 
 * <p> RDF data export is done through a call to the write(fileName, 
 * language) from the {@link OracleModelNoSql} class. An instance 
 * of the {@link OracleModelNoSql} class can be created through the
 * methods createOracleDefaultModelNoSql(OracleNoSqlConnection) or 
 * createOracleModelNoSql(Node, OracleNoSqlConnection).
 * 
 * <p> At the end of all the pertinent operations, it is recommended to dispose
 * the OracleNoSqlConnection instance, as well as to close the OracleModelNoSql
 * objects. Closing a OracleModelNoSql object does not dispose its associated 
 * connection, as it may be used by other objects in the application.
 * 
 */

public class Example3
{
  
  public static void main(String[] args) throws Exception 
  {
    
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];
    String szGraphName  = args[3];
    
    System.out.println("Create Oracle NoSQL connection");
    
    OracleNoSqlConnection conn = OracleNoSqlConnection.createInstance(szStoreName,
                                                                      szHostName, 
                                                                      szHostPort);
    
    System.out.println("Create Oracle NoSQL model");
    
    OracleModelNoSql model 
                   = OracleModelNoSql.createOracleModelNoSql(szGraphName, conn);
    
    System.out.println("Clear model");
    model.removeAll();

    System.out.println("Get graph from model");
    OracleGraphNoSql graph = model.getGraph();
    
    System.out.println("Add triples");
    
    graph.add(Triple.create(Node.createURI("u:John"), 
                            Node.createURI("u:parentOf"),
                            Node.createURI("u:Mary")));
    
    graph.add(Triple.create(Node.createURI("u:John"), 
                            Node.createURI("u:parentOf"),
                            Node.createURI("u:Jack")));
    
    graph.add(Triple.create(Node.createURI("u:Amy"), 
                            Node.createURI("u:parentOf"),
                            Node.createURI("u:Jack")));
    
    System.out.println("Create OracleNoSQL connection");
    
    System.out.println("Write model into an RDF file");
    
    
    try {
      
      OutputStream outStream = new FileOutputStream("output.nt");
      model.write(outStream, "N-TRIPLE");
      outStream.close();
      
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    model.close();   
    conn.dispose();
    
  }
}