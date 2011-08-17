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

import java.util.LinkedList;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.deri.iris.storage.IRelation;
import org.deri.iris.rules.IRuleSafetyProcessor;
import org.deri.iris.RuleUnsafeException;
import static org.deri.iris.factory.Factory.*;

public class Eval {
	private Model model = new Model(createDefaultConfiguration());

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
		//graph(initialKnowledgeBase, new File("initial.png"));

		boolean expectFailure = expectingFailure(initialKnowledgeBase);
		boolean initialProblem = checkForErrors(initialKnowledgeBase, "in initial configuration");

		if (initialProblem) {
			if (expectFailure) {
				return;
			}
			System.out.println("Unexpected error in " + scenario);
			System.exit(1);
		}

		parseResource("system.dl");

		if (!doSetup()) {
			System.out.println("Unexpected error in " + scenario);
			System.exit(1);
		}

		parseResource("finalChecks.dl");

		String stem = scenario.getName();
		if (stem.endsWith(".sam")) {
			stem = stem.substring(0, stem.length() - 4);
		}

		IRelation phase = model.getRelation(Constants.phaseP);
		phase.add(BASIC.createTuple(new ITerm[] { TERM.createString("test") }));

		IKnowledgeBase finalKnowledgeBase = model.createKnowledgeBase();
		finalKnowledgeBase = doDebugging(finalKnowledgeBase);
		Graph.graph(finalKnowledgeBase, new File(stem + ".png"));
		doQueries(finalKnowledgeBase, queries);
		boolean finalProblem = checkForErrors(finalKnowledgeBase, "after applying propagation rules");

		if (finalProblem != expectFailure) {
			if (expectFailure) {
				System.out.println("Expecting model to fail ('expectFailure' is set), but passed, in " + scenario);
			} else {
				System.out.println("Unexpected error in " + scenario);
			}
			System.exit(1);
		}

		if (expectFailure) {
			System.out.println(scenario + ": OK (failed, as expected)");
		} else {
			System.out.println(scenario + ": OK");
		}
	}

	/* Instantiate the Setup class and run the model. Update the
	 * initialObject and field relations with the results,
	 * throwing everything else away.
	 */
	private boolean doSetup() throws Exception {
		Model savedModel = model;
		try {
			model = new Model(savedModel);

			IRelation phase = model.getRelation(Constants.phaseP);
			phase.add(BASIC.createTuple(new ITerm[] { TERM.createString("setup") }));

			IKnowledgeBase setupKnowledgeBase = model.createKnowledgeBase();
			/*
			boolean setupProblem = checkForErrors(setupKnowledgeBase, "during setup phase");
			if (setupProblem) {
				return false;
			}
			*/

			ITuple xAndY = BASIC.createTuple(TERM.createVariable("X"), TERM.createVariable("Y"));
			ILiteral isAL = BASIC.createLiteral(true, Constants.isAP, xAndY);
			IQuery isAQ = BASIC.createQuery(isAL);
			IRelation isAR = setupKnowledgeBase.execute(isAQ);
			savedModel.getRelation(Constants.initialObjectP).addAll(isAR);

			ITuple triple = BASIC.createTuple(TERM.createVariable("X"), TERM.createVariable("Y"), TERM.createVariable("Z"));
			ILiteral fieldL = BASIC.createLiteral(true, Constants.fieldP, triple);
			IQuery fieldQ = BASIC.createQuery(fieldL);
			IRelation fieldR = setupKnowledgeBase.execute(fieldQ);
			savedModel.getRelation(Constants.fieldP).addAll(fieldR);

			/* Unknown objects may continue running after the setup phase. Find all unknown objects
			 * and add initialInvocation facts for them, preserving the setup context. i.e.
			 *
			 * ?- didCreate(?Caller, ?Invocation, ?CallSite, ?NewChild), isA(?NewChild, "Unknown").
			 */
			IVariable newChildVar = TERM.createVariable("NewChild");
			IVariable invocationVar = TERM.createVariable("Invocation");
			ILiteral didCreate = BASIC.createLiteral(true, BASIC.createAtom(Constants.didCreateP,
						BASIC.createTuple(
							TERM.createVariable("Caller"),
							invocationVar,
							TERM.createVariable("CallSite"),
							newChildVar
							)));
			ILiteral isUnknown = BASIC.createLiteral(true, BASIC.createAtom(Constants.isAP,
						BASIC.createTuple(
							newChildVar,
							TERM.createString("Unknown")
							)));

			List<IVariable> bindings = new LinkedList<IVariable>();
			IRelation unknownsR = setupKnowledgeBase.execute(BASIC.createQuery(didCreate, isUnknown), bindings);
			int newChildIndex = bindings.indexOf(newChildVar);
			int invocationIndex = bindings.indexOf(invocationVar);
			IRelation initialInvocations = savedModel.getRelation(Constants.initialInvocation2P);
			for (int t = unknownsR.size() - 1; t >= 0; t--) {
				ITuple tuple = unknownsR.get(t);
				initialInvocations.add(BASIC.createTuple(
						tuple.get(newChildIndex),
						tuple.get(invocationIndex)));
			}

			return true;
		} finally {
			model = savedModel;
		}
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

	private boolean expectingFailure(IKnowledgeBase knowledgeBase) throws Exception {
		ILiteral expectFailureL = BASIC.createLiteral(true, Constants.expectFailureP, BASIC.createTuple());
		IQuery expectFailureQ = BASIC.createQuery(expectFailureL);
		IRelation expectFailureResults = knowledgeBase.execute(expectFailureQ);
		return (expectFailureResults.size() != 0);
	}

	/* Returns true if an error was detected */
	private boolean checkForErrors(IKnowledgeBase knowledgeBase, String when) throws Exception {
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
				for(int t = 0; t < errorResults.size(); ++t )
				{
					ITuple tuple = errorResults.get( t );
					String msg = tuple.get(0).getValue().toString();
					for (int part = 1; part < tuple.size(); part++) {
						msg += ", " + tuple.get(part).getValue();
					}
					System.out.println(msg);
				}
			}

			ITerm newTerm = TERM.createVariable("t" + i);
			terms.add(newTerm);
		}

		return problem;
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
}
