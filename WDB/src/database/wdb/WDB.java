/*
* Created on Feb 3, 2005
*
* TODO To change the template for this generated file go to
* Window - Preferences - Java - Code Style - Code Templates
*/
package wdb;

import wdb.metadata.*;
import wdb.parser.*;

import java.io.*;
import java.util.*;

/**
 * @author Bo Li
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WDB {

	private static QueryParser parser;
	private static Database db;
	private static BufferedReader in;
	private static String dbName = "WDB";

	public static void main(String[] args)
	{
		try
		{
			db = new Database();
			System.out.println("WDB Simantic Database Project");
			System.out.println("Copyright 2015 University of Texas at Austin");
			System.out.println("DB Name: " + dbName + " DB Path: " + db.getStorageDirPath());

			WDB.in = new BufferedReader(new InputStreamReader(System.in));
			WDB.parser = new QueryParser(WDB.in);
			Query q;

			while(true)
			{
				try
				{
					if(!in.ready())
					{
						System.out.print("\nWDB>");
					}

					q = parser.getNextQuery();
					if(q == null)
					{
						break;
					}

					else
					{
						processQuery(q);
					}
				}
				catch(ParseException pe)
				{
					System.out.println("SYNTAX ERROR: " + pe.getMessage());
					QueryParser.ReInit(System.in);

				}
				catch(TokenMgrError tme)
				{
					System.out.println("PARSER ERROR: " + tme.getMessage());
					break;
				}
				catch(IOException ioe)
				{
					System.out.println("STANDARD IN ERROR: " + ioe.getMessage());
					break;
				}
				catch(NumberFormatException nfe)
				{
					System.out.println("PARSE ERROR: Failed to convert to Integer " + nfe.getMessage());
				}
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		finally
		{
			try
			{
				// Cleans eeeeeverything up
				db.ultimateCleanUp("Shutting down store.");
			}
			catch(Exception e)
			{
				System.out.println("DATABASE CLOSE ERROR");
			}
		}
	}


	// Main logic of WDB
	static private void processQuery(Query q)
	{
		if(q.getClass() == SourceQuery.class)
		{
			SourceQuery sq = (SourceQuery)q;
//			db.clearDBs();
			db.dropTable(db.getClassTable());
			db.dropTable(db.getObjectTable());
			db.createTables();

			try
			{
				QueryParser.ReInit(new FileReader(sq.filename));
				Query fq;
				while(true)
				{
					fq = parser.getNextQuery();
					if(fq == null)
					{
						break;
					}
					else
					{
						processQuery(fq);
					}
				}
			}
			catch(FileNotFoundException e)
			{
				System.out.println("FILE OPEN ERROR: " + e.getMessage());
			}
			catch(ParseException pe)
			{
				System.out.println("SYNTAX ERROR: " + pe.getMessage());
			}
			catch(TokenMgrError tme)
			{
				System.out.println("PARSER ERROR: " + tme.getMessage());
			}
			finally
			{
				QueryParser.ReInit(WDB.in);
			}
		}

		if(q.getClass() == ClassDef.class || q.getClass() == SubclassDef.class)
		{
			ClassDef cd = (ClassDef)q;

			try
			{
				Adapter adapter = new Adapter(db);

				try
				{
					try
					{
						adapter.getClass(cd.name);
						//That class already exists;
						throw new Exception("Class \"" + cd.name + "\" already exists");
					}
					catch(ClassNotFoundException cnfe)
					{
						if(cd.getClass() == SubclassDef.class)
						{
							ClassDef baseClass = null;
							for(int i = 0; i < ((SubclassDef)cd).numberOfSuperClasses(); i++)
							{
								//Cycles are implicitly checked since getClass will fail for the current defining class
								ClassDef superClass = adapter.getClass(((SubclassDef)cd).getSuperClass(i));
								if(baseClass == null)
								{
									baseClass = superClass.getBaseClass(adapter);
								}
								else if(!baseClass.name.equals(superClass.getBaseClass(adapter).name))
								{
									throw new Exception("Super classes of class \"" + cd.name + "\" do not share the same base class");
								}
							}
						}

						adapter.putClass(cd);
						adapter.commit();
					}
				}
				catch(Exception e)
				{
					System.out.println(e.toString() + ": " + e.getMessage());
					adapter.abort();
				}
			}
			catch(Exception e)
			{
				System.out.println(e.toString() + ": " + e.getMessage());
			}
		}

		if(q.getClass() == ModifyQuery.class)
		{
			ModifyQuery mq = (ModifyQuery)q;
			try
			{
				Adapter adapter = new Adapter(db);

				try
				{
					ClassDef targetClass = adapter.getClass(mq.className);
					WDBObject[] targetClassObjs = targetClass.search(mq.expression, adapter);
					if(mq.limit > -1 && targetClassObjs.length > mq.limit)
					{
						throw new Exception("Matching entities exceeds limit of " + mq.limit.toString());
					}
					for(int i = 0; i < targetClassObjs.length; i++)
					{
						setValues(mq.assignmentList, targetClassObjs[i], adapter);
					}
					adapter.commit();
				}
				catch(Exception e)
				{
					System.out.println(e.toString());
					adapter.abort();
				}
			}
			catch(Exception e)
			{
				System.out.println(e.toString());
			}
		}

		if(q.getClass() == InsertQuery.class)
		{
			InsertQuery iq = (InsertQuery)q;

			try
			{
				Adapter adapter = new Adapter(db);

				try
				{
					ClassDef targetClass = adapter.getClass(iq.className);
					WDBObject newObject = null;

					if(iq.fromClassName != null)
					{
						//Inserting from an entity of a superclass...
						if(targetClass.getClass() == SubclassDef.class)
						{
							SubclassDef targetSubClass = (SubclassDef)targetClass;
							ClassDef fromClass = adapter.getClass(iq.fromClassName);
							if(targetSubClass.isSubclassOf(fromClass.name, adapter))
							{
								WDBObject[] fromObjects = fromClass.search(iq.expression, adapter);
								if(fromObjects.length <= 0)
								{
									throw new IllegalStateException("Can't find any entities from class \"" + fromClass.name + "\" to extend");
								}
								for(int i = 0; i < fromObjects.length; i++)
								{
									newObject = targetSubClass.newInstance(fromObjects[i].getBaseObject(adapter), adapter);
									setValues(iq.assignmentList, newObject, adapter);
								}
							}
							else
							{
								throw new IllegalStateException("Inserted class \"" + targetClass.name + "\" is not a subclass of the from class \"" + iq.fromClassName);
							}
						}
						else
						{
							throw new IllegalStateException("Can't extend base class \"" + targetClass.name + "\" from class \"" + iq.fromClassName);
						}
					}
					else
					{
						//Just inserting a new entity
						newObject = targetClass.newInstance(null, adapter);
						setDefaultValues(targetClass, newObject, adapter);
						setValues(iq.assignmentList, newObject, adapter);
						checkRequiredValues(targetClass, newObject, adapter);
					}

					if(newObject != null)
					{
						newObject.commit(adapter);
					}
					adapter.commit();
				}
				catch(Exception e)
				{
					System.out.println(e.toString());
					adapter.abort();
				}
			}
			catch(Exception e)
			{
				System.out.println(e.toString());
			}
		}
		if(q.getClass() == IndexDef.class)
		{
			IndexDef indexQ = (IndexDef)q;

			try
			{
				Adapter adapter = new Adapter(db);

				try
				{
					ClassDef classDef = adapter.getClass(indexQ.className);
					classDef.addIndex(indexQ, adapter);

					System.out.println("Would have opened secondary databae");
					// TODO: Find replacement for this? What was it being used for?
//					db.openSecDb(indexQ);

					adapter.commit();
				}
				catch(Exception e)
				{
					System.out.println(e.toString());
					adapter.abort();
				}
			}
			catch(Exception e)
			{
				System.out.println(e.toString());
			}
		}
		if(q.getClass() == RetrieveQuery.class)
		{
			//Ok, its a retrieve...
			RetrieveQuery rq = (RetrieveQuery)q;

			try
			{

				Adapter adapter = new Adapter(db);

				try
				{
					ClassDef targetClass = adapter.getClass(rq.className);
					WDBObject[] targetClassObjs = targetClass.search(rq.expression, adapter);
					int i, j;
					String[][] table;
					String[][] newtable;

					PrintNode node = new PrintNode(0,0);
					for(j = 0; j < rq.numAttributePaths(); j++)
					{
						targetClass.printAttributeName(node, rq.getAttributePath(j), adapter);
					}
					table = node.printRow();
					for(i = 0; i < targetClassObjs.length; i++)
					{
						node = new PrintNode(0,0);
						for(j = 0; j < rq.numAttributePaths(); j++)
						{
							targetClassObjs[i].PrintAttribute(node, rq.getAttributePath(j), adapter);
						}
						newtable = joinRows(table, node.printRow());
						table = newtable;
					}

					adapter.commit();

					Integer[] columnWidths= new Integer[table[0].length];

					for(i = 0; i < columnWidths.length; i++)
					{
						columnWidths[i] = 0;
						for(j = 0; j < table.length; j++)
						{
							if(i < table[j].length && table[j][i] != null && table[j][i].length() > columnWidths[i])
							{
								columnWidths[i] = table[j][i].length();
							}
						}
					}

					for(i = 0; i < table.length; i++)
					{
						for(j = 0; j < table[0].length; j++)
						{
							if(j >= table[i].length || table[i][j] == null)
							{
								System.out.format("| %"+columnWidths[j].toString()+"s ", "");
							}
							else
							{
								System.out.format("| %"+columnWidths[j].toString()+"s ", table[i][j]);
							}
						}
						System.out.format("|%n");
					}
				}
				catch(Exception e)
				{
					System.out.println(e.toString());
					adapter.abort();
				}
			}
			catch(Exception e)
			{
				System.out.println(e.toString());
			}
		}
	}

	private static void setDefaultValues(ClassDef targetClass, WDBObject targetObject, Adapter adapter) throws Exception
	{
		for(int j = 0; j < targetClass.numberOfAttributes(); j++)
		{
			if(targetClass.getAttribute(j) instanceof DVA)
			{
				DVA dva = (DVA)targetClass.getAttribute(j);
				if(dva.initialValue != null)
				{
					targetObject.setDvaValue(dva.name, dva.initialValue, adapter);
				}
			}
		}
	}
	private static void checkRequiredValues(ClassDef targetClass, WDBObject targetObject, Adapter adapter) throws Exception
	{
		for(int j = 0; j < targetClass.numberOfAttributes(); j++)
		{
			Attribute attribute = (Attribute)targetClass.getAttribute(j);
			if(attribute.required != null && attribute.required && targetObject.getDvaValue(attribute.name, adapter) == null)
			{
				throw new Exception("Attribute \"" + targetClass.getAttribute(j).name + "\" is required");
			}
		}
	}
	private static void setValues(ArrayList assignmentList, WDBObject targetObject, Adapter adapter) throws Exception
	{
		for(int j = 0; j < assignmentList.size(); j++)
		{
			if(assignmentList.get(j) instanceof DvaAssignment)
			{
				DvaAssignment dvaAssignment = (DvaAssignment)assignmentList.get(j);
				targetObject.setDvaValue(dvaAssignment.AttributeName, dvaAssignment.Value, adapter);
			}

			else if(assignmentList.get(j) instanceof EvaAssignment)
			{
				EvaAssignment evaAssignment = (EvaAssignment)assignmentList.get(j);
				if(evaAssignment.mode == EvaAssignment.REPLACE_MODE)
				{
					WDBObject[] currentObjects = targetObject.getEvaObjects(evaAssignment.AttributeName, adapter);
					targetObject.removeEvaObjects(evaAssignment.AttributeName, evaAssignment.targetClass, currentObjects, adapter);
					targetObject.addEvaObjects(evaAssignment.AttributeName, evaAssignment.targetClass, evaAssignment.expression, adapter);
				}
				else if(evaAssignment.mode == EvaAssignment.EXCLUDE_MODE)
				{
					targetObject.removeEvaObjects(evaAssignment.AttributeName, evaAssignment.targetClass, evaAssignment.expression, adapter);
				}
				else if(evaAssignment.mode == EvaAssignment.INCLUDE_MODE)
				{
					targetObject.addEvaObjects(evaAssignment.AttributeName, evaAssignment.targetClass, evaAssignment.expression, adapter);
				}
				else
				{
					throw new Exception("Unsupported multivalue EVA insert/modify mode");
				}
			}
		}
	}
	private static String[][] joinRows(String[][] row1, String[][] row2)
	{
		if(row1.length <= 0)
		{
			return row2;
		}
		else
		{
			String[][] newRow = new String[row1.length+row2.length][row1[0].length];
			int i, j;
			for(i = 0; i < row1.length; i++)
			{
				newRow[i] = row1[i];
			}
			for(j = i; j < row2.length + i; j++)
			{
				newRow[j] = row2[j-i];
			}

			return newRow;
		}
	}
}
