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

import eu.serscis.sam.node.Token;
import java.io.BufferedReader;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.io.FileWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.Reader;
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
import org.deri.iris.rules.IRuleSafetyProcessor;
import org.deri.iris.RuleUnsafeException;
import org.deri.iris.compiler.BuiltinRegister;
import static org.deri.iris.factory.Factory.*;
import eu.serscis.Constants;

public class Eval {
	private Model model = new Model(createDefaultConfiguration());

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			throw new Exception("usage: sam scenario.sam");
		}
		new Eval(new File(args[0]));
	}

	public static Configuration createDefaultConfiguration() {
		Configuration config = KnowledgeBaseFactory.getDefaultConfiguration();

		/* IRIS bug? It thinks that STRING_CONCAT is unsafe.
		 * Workaround this by skipping the rules that need it.
		 */
		final IRuleSafetyProcessor oldProcessor = config.ruleSafetyProcessor;
		config.ruleSafetyProcessor = new IRuleSafetyProcessor() {
			public IRule process(IRule rule) throws RuleUnsafeException {
				String p = rule.getHead().get(0).getAtom().getPredicate().getPredicateSymbol();
				if (p.equals("realNewObject") || p.equals("graphInvocation")) {
					return rule;
				}

				return oldProcessor.process(rule);
			}
		};

		return config;
	}

	public Eval(File scenario) throws Exception {
		File baseDir = scenario.getParentFile();

		ClassLoader loader = Eval.class.getClassLoader();

		parseResource("base.dl");
		parseResource("checks.dl");
		parseResource("graph.dl");

		SAMParser parser = new SAMParser(model, scenario);
		List<IQuery> queries = parser.getQueries();

		IKnowledgeBase initialKnowledgeBase = model.createKnowledgeBase();
		//graph(initialKnowledgeBase, new File("initial.dot"));

		checkForErrors(initialKnowledgeBase, "in initial configuration");
		parseResource("system.dl");

		IKnowledgeBase finalKnowledgeBase = model.createKnowledgeBase();
		finalKnowledgeBase = doDebugging(finalKnowledgeBase);
		graph(finalKnowledgeBase, new File("access.dot"));
		doQueries(finalKnowledgeBase, queries);
		checkForErrors(finalKnowledgeBase, "after applying propagation rules");
	}

	private void parseResource(String resource) throws Exception {
		InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
		try {
			SAMParser parser = new SAMParser(model, new InputStreamReader(is));
		} finally {
			is.close();
		}
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

		ITuple xAndYandAttrAndLabel = BASIC.createTuple(TERM.createVariable("X"), TERM.createVariable("Y"),
								TERM.createVariable("Attr"), TERM.createVariable("Label"));
		IPredicate graphEdgeLabelPredicate = BASIC.createPredicate("graphEdge", 4);
		ILiteral graphEdgeLabelLiteral = BASIC.createLiteral(true, graphEdgeLabelPredicate, xAndYandAttrAndLabel);
		IQuery graphEdgeLabelQuery = BASIC.createQuery(graphEdgeLabelLiteral);
		IRelation graphEdgeLabelResults = knowledgeBase.execute(graphEdgeLabelQuery);

		graph(graphNodeResults, graphEdgeResults, graphEdgeLabelResults, outputDotFile);
	}

	private void checkForErrors(IKnowledgeBase knowledgeBase, String when) throws Exception {
		List<ITerm> terms = new LinkedList<ITerm>();
		boolean problem = false;

		for (int i = 0; i < 7; i++) {
			IPredicate errorPredicate = BASIC.createPredicate("error", i);
			ILiteral errorLiteral = BASIC.createLiteral(true, errorPredicate, BASIC.createTuple(terms));
			IQuery errorQuery = BASIC.createQuery(errorLiteral);
			IRelation errorResults = knowledgeBase.execute(errorQuery);
			if (errorResults.size() != 0) {
				if (!problem) {
					System.out.println("\n=== Errors detected " + when + " ===\n");
					problem = true;
				}
				formatResults(errorResults);
			}

			ITerm newTerm = TERM.createVariable("t" + i);
			terms.add(newTerm);
		}

		if (problem) {
			System.exit(1);
		}
	}

	private IKnowledgeBase doDebugging(IKnowledgeBase knowledgeBase) throws Exception {
		ILiteral debugL = BASIC.createLiteral(true, BASIC.createPredicate("debug", 0), BASIC.createTuple());
		IQuery debugQ = BASIC.createQuery(debugL);
		IRelation debugResults = knowledgeBase.execute(debugQ);
		if (debugResults.size() == 0) {
			return knowledgeBase;
		}

		IRelation debugEdges = model.getRelation(Constants.debugEdgeP);
		System.out.println("Starting debugger...");
		Debugger debugger = new Debugger(model);
		debugger.debug(debugL, debugEdges);

		return model.createKnowledgeBase();
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

	static private void graph(IRelation nodes, IRelation edges, IRelation labelledEdges, File dotFile) throws Exception {
		FileWriter writer = new FileWriter(dotFile);
		writer.write("digraph a {\n");
		//writer.write("  concentrate=true;\n");
		//writer.write("  rankdir=LR;\n");
		writer.write("  node[shape=plaintext];\n");

		for (int t = 0; t < nodes.size(); t++) {
			ITuple tuple = nodes.get(t);
			ITerm nodeId = tuple.get(0);
			String nodeAttrs = tuple.get(1).getValue().toString();
			writer.write(format(nodeId) + " [" + nodeAttrs + "];\n");
		}

		Set<String> dotEdges = new HashSet<String>();
		Set<String> doubles = new HashSet<String>();

		for (int t = 0; t < edges.size(); t++) {
			ITuple tuple = edges.get(t);
			ITerm a = tuple.get(0);
			ITerm b = tuple.get(1);
			String edgeAttrs = tuple.get(2).getValue().toString();

			String reverse = format(b) + " -> " + format(a) + " [" + edgeAttrs;

			if (dotEdges.contains(reverse)) {
				doubles.add(reverse);
			} else {
				String line = format(a) + " -> " + format(b) + " [" + edgeAttrs;
				dotEdges.add(line);
			}
		}

		for (String edge : dotEdges) {
			if (doubles.contains(edge)) {
				if (!edge.endsWith("[")) {
					edge += ",";
				}
				edge += "dir=both";
			}
			writer.write(edge + "];\n");
		}

		for (int t = 0; t < labelledEdges.size(); t++) {
			ITuple tuple = labelledEdges.get(t);
			ITerm a = tuple.get(0);
			ITerm b = tuple.get(1);
			String edgeAttrs = tuple.get(2).getValue().toString();
			String labelAttr = "label=" + format(tuple.get(3));
			if (edgeAttrs.equals("")) {
				edgeAttrs = labelAttr;
			} else {
				edgeAttrs += "," + labelAttr;
			}

			writer.write(format(a) + " -> " + format(b) + " [" + edgeAttrs + "];\n");
		}

		writer.write("}\n");
		writer.close();

		String dotBinary = "dot";
		String graphvizHome = System.getenv("GRAPHVIZ_HOME");
		if (graphvizHome != null) {
			File bin = new File(graphvizHome, "bin");
			File dotBinaryFile = new File(bin, "dot.exe");
			if (bin.exists()) {
				dotBinary = dotBinaryFile.toString();
			} else {
				dotBinaryFile = new File(bin, "dot");
				dotBinary = dotBinaryFile.toString();
			}
		}

		Process proc = Runtime.getRuntime().exec(new String[] {dotBinary, "-O", "-Tpng", dotFile.getAbsolutePath()});
		int result = proc.waitFor();
		if (result != 0) {
			throw new RuntimeException("dot failed to run: exit status = " + result);
		}
	}
}
