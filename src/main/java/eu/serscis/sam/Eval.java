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

package eu.serscis.sam;

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
	public static Configuration createDefaultConfiguration() {
		Configuration config = KnowledgeBaseFactory.getDefaultConfiguration();

		/* IRIS bug? It thinks that STRING_CONCAT is unsafe.
		 * Workaround this by skipping the rules that need it.
		 */
		final IRuleSafetyProcessor oldProcessor = config.ruleSafetyProcessor;
		config.ruleSafetyProcessor = new IRuleSafetyProcessor() {
			public IRule process(IRule rule) throws RuleUnsafeException {
				for (ILiteral lit : rule.getBody()) {
					String p = lit.getAtom().getPredicate().getPredicateSymbol();
					if (p.equals("TO_STRING") || p.equals("STRING_CONCAT") ||
					    p.equals("MAKE_OBJECT") || p.equals("MATCH") || p.equals("ASSIGN")) {
						return rule;
					}
				}

				return oldProcessor.process(rule);
			}
		};

		return config;
	}

	public Eval() {
	}

	public Results evaluate(File scenario) {
		Results results = new Results(new Model(createDefaultConfiguration()));

		try {
			evaluate(scenario, results);
		} catch (Exception ex) {
			results.setException(ex);
		}

		return results;
	}

	private void evaluate(File scenario, Results results) throws Exception {
		File baseDir = scenario.getParentFile();

		ClassLoader loader = Eval.class.getClassLoader();

		parseResource(results.model, "base.sam");
		parseResource(results.model, "groupBy.sam");
		parseResource(results.model, "checks.sam");
		parseResource(results.model, "graph.sam");

		SAMParser parser = new SAMParser(results.model, scenario);
		List<IQuery> queries = parser.getQueries();

		IKnowledgeBase initialKnowledgeBase = results.model.createKnowledgeBase();
		//graph(initialKnowledgeBase, new File("initial.png"));

		results.expectingFailure = expectingFailure(initialKnowledgeBase);
		boolean initialProblem = checkForErrors(results, initialKnowledgeBase, "in initial configuration");

		if (initialProblem) {
			return;
		}

		parseResource(results.model, "system.sam");

		results.phase = Results.Phase.Setup;

		if (!doSetup(results.model)) {
			throw new ModelFailureException("Unexpected error in " + scenario);
		}

		results.phase = Results.Phase.Test;

		parseResource(results.model, "finalChecks.sam");
		parseResource(results.model, "gui.sam");

		IRelation phase = results.model.getRelation(Constants.phaseP);
		phase.add(BASIC.createTuple(new ITerm[] { TERM.createString("test") }));

		IKnowledgeBase finalKnowledgeBase = results.model.createKnowledgeBase();
		results.finalKnowledgeBase = doDebugging(results.model, finalKnowledgeBase);
		doQueries(results.finalKnowledgeBase, queries);
		boolean finalProblem = checkForErrors(results, results.finalKnowledgeBase, "after applying propagation rules");

		if (!finalProblem) {
			results.phase = Results.Phase.Success;
		}
	}

	/* Instantiate the Setup class and run the model. Update the
	 * initialObject and field relations with the results,
	 * throwing everything else away.
	 */
	private boolean doSetup(Model mainModel) throws Exception {
		Model tmpModel = new Model(mainModel);

		IRelation phase = tmpModel.getRelation(Constants.phaseP);
		phase.add(BASIC.createTuple(new ITerm[] { TERM.createString("setup") }));

		IKnowledgeBase setupKnowledgeBase = tmpModel.createKnowledgeBase();
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
		mainModel.getRelation(Constants.initialObjectP).addAll(isAR);

		ITuple triple = BASIC.createTuple(TERM.createVariable("X"), TERM.createVariable("Y"), TERM.createVariable("Z"));
		ILiteral fieldL = BASIC.createLiteral(true, Constants.fieldP, triple);
		IQuery fieldQ = BASIC.createQuery(fieldL);
		IRelation fieldR = setupKnowledgeBase.execute(fieldQ);
		mainModel.getRelation(Constants.fieldP).addAll(fieldR);

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
		IRelation initialInvocations = mainModel.getRelation(Constants.initialInvocation2P);
		for (int t = unknownsR.size() - 1; t >= 0; t--) {
			ITuple tuple = unknownsR.get(t);
			initialInvocations.add(BASIC.createTuple(
					tuple.get(newChildIndex),
					tuple.get(invocationIndex)));
		}

		return true;
	}

	private void parseResource(Model model, String resource) throws Exception {
		new SAMParser(model, resource);
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
	private boolean checkForErrors(Results results, IKnowledgeBase knowledgeBase, String when) throws Exception {
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

					ILiteral thisError = BASIC.createLiteral(true,
						errorPredicate,
						tuple);
					results.errors.add(thisError);
				}
			}

			ITerm newTerm = TERM.createVariable("t" + i);
			terms.add(newTerm);
		}

		return problem;
	}

	private IKnowledgeBase doDebugging(Model model, IKnowledgeBase knowledgeBase) throws Exception {
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
