/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2011
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//	Created By :			Thomas Leonard
//	Created Date :			2011-03-25
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis;

import java.util.HashMap;
import java.util.LinkedList;
import java.io.FileWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.FileReader;
import java.io.IOException;
import org.deri.iris.Configuration;
import org.deri.iris.KnowledgeBaseFactory;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.compiler.Parser;
import org.deri.iris.storage.IRelation;
import static org.deri.iris.factory.Factory.*;

public class Eval {
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			throw new Exception("usage: Eval scenario.dl");
		}
		String filename = args[0];

		Configuration configuration = KnowledgeBaseFactory.getDefaultConfiguration();

		List<IRule> rules = new LinkedList<IRule>();
		Map<IPredicate,IRelation> facts = new HashMap<IPredicate,IRelation>();

		Parser parser = new Parser();
		parse(parser, rules, facts, new File("scenario.dl"));
		List<IQuery> queries = parser.getQueries();
		parse(parser, rules, facts, new File("base.dl"));
		parse(parser, rules, facts, new File("graph.dl"));

		IKnowledgeBase initialKnowledgeBase = KnowledgeBaseFactory.createKnowledgeBase( facts, rules, configuration );
		graph(initialKnowledgeBase, new File("initial.dot"));
		//checkForErrors(initialKnowledgeBase);

		parse(parser, rules, facts, new File("behaviour.dl"));
		parse(parser, rules, facts, new File("system.dl"));

		IKnowledgeBase finalKnowledgeBase = KnowledgeBaseFactory.createKnowledgeBase( facts, rules, configuration );
		graph(finalKnowledgeBase, new File("access.dot"));
		doQueries(finalKnowledgeBase, queries);
		checkForErrors(finalKnowledgeBase);
	}

	static private void doQueries(IKnowledgeBase knowledgeBase, List<IQuery> queries) throws Exception {
		List<IVariable> variableBindings = new ArrayList<IVariable>();

		for (IQuery query : queries) {
			// Execute the query
			IRelation results = knowledgeBase.execute( query, variableBindings );

			System.out.println("\n" +  query );

			if( results.size() == 0 ) {
				System.out.println( "no results" );
			} else {
				boolean first = true;
				for( IVariable variable : variableBindings )
				{
					if( first )
						first = false;
					else
						System.out.print( ", " );
					System.out.print( variable );
				}
				System.out.println( );

				formatResults( results );
			}
		}
	}

	static private void graph(IKnowledgeBase knowledgeBase, File outputDotFile) throws Exception {
		ITuple xAndY = BASIC.createTuple(TERM.createVariable("X"), TERM.createVariable("Y"));

		IPredicate graphNodePredicate = BASIC.createPredicate("graphNode", 2);
		ILiteral graphNodeLiteral = BASIC.createLiteral(true, graphNodePredicate, xAndY);
		IQuery graphNodeQuery = BASIC.createQuery(graphNodeLiteral);
		IRelation graphNodeResults = knowledgeBase.execute(graphNodeQuery);

		ITuple xAndYandAttr = BASIC.createTuple(TERM.createVariable("X"), TERM.createVariable("Y"), TERM.createVariable("Attr"));
		IPredicate graphEdgePredicate = BASIC.createPredicate("graphEdge", 3);
		ILiteral graphEdgeLiteral = BASIC.createLiteral(true, graphEdgePredicate, xAndYandAttr);
		IQuery graphEdgeQuery = BASIC.createQuery(graphEdgeLiteral);
		IRelation graphEdgeResults = knowledgeBase.execute(graphEdgeQuery);

		graph(graphNodeResults, graphEdgeResults, outputDotFile);
	}

	static private void checkForErrors(IKnowledgeBase knowledgeBase) throws Exception {
		List<ITerm> terms = new LinkedList<ITerm>();

		for (int i = 0; i < 5; i++) {
			IPredicate errorPredicate = BASIC.createPredicate("error", i);
			ILiteral errorLiteral = BASIC.createLiteral(true, errorPredicate, BASIC.createTuple(terms));
			IQuery errorQuery = BASIC.createQuery(errorLiteral);
			IRelation errorResults = knowledgeBase.execute(errorQuery);
			if (errorResults.size() != 0) {
				System.out.println("\n=== Errors detected ===\n");
				formatResults(errorResults);
				//System.exit(1);
			}

			ITerm newTerm = TERM.createVariable("t" + i);
			terms.add(newTerm);
		}
	}

	static private void formatResults(IRelation m )
	{
		for(int t = 0; t < m.size(); ++t )
		{
			ITuple tuple = m.get( t );
			System.out.println( tuple.toString() );
		}
	}

	static private String format(ITerm term) {
		return "\"" + term.getValue().toString() + "\"";
	}

	static private void graph(IRelation nodes, IRelation edges, File dotFile) throws Exception {
		FileWriter writer = new FileWriter(dotFile);
		writer.write("digraph a {\n");
		//writer.write("  concentrate=true;\n");
		//writer.write("  rankdir=LR;\n");

		for (int t = 0; t < nodes.size(); t++) {
			ITuple tuple = nodes.get(t);
			ITerm nodeId = tuple.get(0);
			String nodeAttrs = tuple.get(1).getValue().toString();
			writer.write(format(nodeId) + " [" + nodeAttrs + "];\n");
		}

		for (int t = 0; t < edges.size(); t++) {
			ITuple tuple = edges.get(t);
			ITerm a = tuple.get(0);
			ITerm b = tuple.get(1);
			String edgeAttrs = tuple.get(2).getValue().toString();

			writer.write(format(a) + " -> " + format(b) + " [" + edgeAttrs + "];\n");
		}

		writer.write("}\n");
		writer.close();
	}

	/* Extend rules and facts with information from source. */
	static private void parse(Parser parser, List<IRule> rules, Map<IPredicate,IRelation> facts, File source) throws Exception {
		FileReader reader = new FileReader(source);
		try {
			parser.parse(reader);
		} finally {
			reader.close();
		}

		Map<IPredicate,IRelation> newFacts = parser.getFacts();
		List<IRule> newRules = parser.getRules();

		rules.addAll(newRules);

		for (Map.Entry<IPredicate,IRelation> entry : newFacts.entrySet()) {
			IRelation existing = facts.get(entry.getKey());
			if (existing == null) {
				facts.put(entry.getKey(), entry.getValue());
			} else {
				existing.addAll(entry.getValue());
			}
		}
	}
}
