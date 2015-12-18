
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import oracle.rdf.kv.client.jena.*;

import com.hp.hpl.jena.update.*;

/**
 * Example13 describes how to execute a SPARQL query with BUILT-IN ARQ functions
 * over the RDF data stored over an Oracle NoSQL database.
 * 
 * <p> To manage RDF data stored in an Oracle NoSQL database, it is required to 
 * create an instance of the {@link OracleNoSqlConnection} class, specifying the
 * data to establish a connection with the Oracle NoSQL Database, such as host 
 * name, port name, and store name. This object will manage all the interactions
 * the between RDF Graph and the Oracle NoSQL Database.
 * 
 * <p> To execute a SPARQL query over an Oracle NoSQL database, an 
 * instance of a Jena Model/Dataset for Oracle NoSQL Database should be created, 
 * as this object represents the input for {@link UpdateAction} in charge of the 
 * execution of SPARQL update instructions in Jena.
 * 
 *<p> If the query is executed over RDF data (triples) from a default 
 * graph (no named graph), then create an instance of a {@link OracleModelNoSql} 
 * through a call to createOracleDefaultModelNoSql(OracleNoSqlConnection).
 * 
 * <p> If the query is executed over RDF data (triples) from a specified
 * named graph, then create an instance of a {@link OracleModelNoSql} through a 
 * call to createOracleModelNoSql(Node, OracleNoSqlConnection) specifying the 
 * graph node of the named graph.
 * 
 * <p> If the query is executed over RDF data from several  
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

public class Example13 
{
  
  public static void main(String[] args) throws Exception 
  {
    
    String szStoreName  = args[0];
    String szHostName   = args[1];
    String szHostPort   = args[2];
    String szGraphName  = args[3];
    
    // Create Oracle NoSQL connection    
    OracleNoSqlConnection conn = OracleNoSqlConnection.createInstance(szStoreName,
                                                                      szHostName, 
                                                                      szHostPort);
    
    // Create model from named graph
    Model model = OracleModelNoSql.createOracleModelNoSql(szGraphName, conn);
    
    // Clear model
    model.removeAll();
    
    String insertString =
            " PREFIX dc: <http://purl.org/dc/elements/1.1/> "               +
            " INSERT DATA "                                                 +
            " { <http://example/book3> dc:title \"A new book\" ; "          +
            "                          dc:creator \"A.N.Other\" . "         +
            "   <http://example/book4> dc:title \"Semantic Web Rocks\" ; "  +
            "                          dc:creator \"TB\" . "                +
            " } ";
    
    System.out.println("Execute insert action " + insertString);
    UpdateAction.parseExecute(insertString, model);
    
    
    String szQuery 
                = "PREFIX dc: <http://purl.org/dc/elements/1.1/> "          +
                  "PREFIX fn: <http://www.w3.org/2005/xpath-functions#> "   +
                  " SELECT ?subject (fn:upper-case(?object) as ?object1) "  +
                  "        (fn:string-length(?object) as ?strlen) "         +
                  " WHERE { ?subject dc:title ?object } ";
    
    System.out.println("Execute query " + szQuery);
    
    Query query = QueryFactory.create(szQuery, Syntax.syntaxARQ);
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
