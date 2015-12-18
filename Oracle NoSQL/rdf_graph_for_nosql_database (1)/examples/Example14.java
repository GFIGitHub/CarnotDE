
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.Model;
import oracle.rdf.kv.client.jena.*;

import com.hp.hpl.jena.update.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Example14 describes how to execute a SPARQL update instruction over 
 * the RDF data stored over an Oracle NoSQL database.
 * 
 * <p> To manage RDF data stored in an Oracle NoSQL database, it is required to 
 * create an instance of the {@link OracleNoSqlConnection} class, specifying the
 * data to establish a connection with the Oracle NoSQL Database, such as host 
 * name, port name, and store name. This object will manage all the interactions
 * the between RDF Graph and the Oracle NoSQL Database.
 * 
 * <p> To execute a SPARQL update instruction over an Oracle NoSQL database, an 
 * instance of a Jena Model/Dataset for Oracle NoSQL Database should be created, 
 * as this object represents the input for {@link UpdateAction} in charge of the 
 * execution of SPARQL update instructions in Jena.
 * 
 * <p> If the update instruction is going to be executed over a default graph 
 * (no named graph), then create an instance of a {@link OracleModelNoSql} 
 * through a call to createOracleDefaultModelNoSql(OracleNoSqlConnection).
 * 
 * <p> If the update instruction is going to be executed over a named graph, 
 * then create an instance of a {@link OracleModelNoSql} through a 
 * call to createOracleModelNoSql(Node, OracleNoSqlConnection) specifying the 
 * graph node of the named graph.
 * 
 * <p> If the update instruction is executed over RDF data from several  
 * named graphs (and/or the default graph), then create a 
 * {@link Dataset} object from a {@link DatasetGraphNoSql} object through a call
 * to DatasetImpl.wrap(com.hp.hpl.jena.sparql.core.DatasetGraph). A 
 * {@link DatasetGraphNoSql} object is created through a call to 
 * createFrom(OracleGraphNoSql) method. It is recommended 
 * that the {@link OracleGraphNoSql} object is closed right after the 
 * {@link DatasetGraphNoSql} object is instantiated. 
 * 
 * <p> At the end of all the pertinent operations, it is recommended to dispose
 * the OracleNoSqlConnection instance, as well as to close the 
 * OracleModelNoSql/DatasetGraphNoSql object. Closing a 
 * OracleModelNoSql/DatasetGraphNoSql object does not dispose its associated 
 * connection, as it may be used by other objects in the application.
 * 
 */


public class Example14 
{
  public static void main(String[] args) throws Exception 
  {
    
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];
    String szGraphName  = args[3];
    
    // Create OracleNoSQL connection 
    OracleNoSqlConnection conn = OracleNoSqlConnection.createInstance(szStoreName,
                                                                      szHostName, 
                                                                      szHostPort);
    
    // Create model for default graph
    Model model = OracleModelNoSql.createOracleModelNoSql(szGraphName, conn);
    
    // Clear model
    model.removeAll();
    
    String insertString = "PREFIX dc: <http://purl.org/dc/elements/1.1/> "       +
                          "INSERT DATA "                                         +
                          "{ <http://example/book3> dc:title \"A new book\" ; "  +
                          "                         dc:creator \"A.N.Other\" . " +
                          " } ";
    
    
    System.out.println("Execute insert action " + insertString);
    UpdateAction.parseExecute(insertString, model);
    
    OracleGraphNoSql graph = (OracleGraphNoSql) model.getGraph();
    
    // Find all triples in the default graph
    ExtendedIterator<Triple> ei = GraphUtil.findAll(graph);
    
    while (ei.hasNext()) {
      System.out.println("Triple " + ei.next().toString());
    }
    
    ei.close();
    model.close();
    conn.dispose();

  }
}
