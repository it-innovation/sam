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
					    p.equals("MAKE_OBJECT") || p.equals("MATCH_TO") || p.equals("ASSIGN")) {
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

	public Results evaluate(File modelFile) {
		Results results = new Results(new Model(createDefaultConfiguration()));

		try {
			evaluate(modelFile, results);
		} catch (Exception ex) {
			results.setException(ex);
		}

		return results;
	}

	private void evaluate(File modelFile, Results results) throws Exception {
		File baseDir = modelFile.getParentFile();

		ClassLoader loader = Eval.class.getClassLoader();

		parseResource(results.model, "base.sam");
		SAMParser parser = new SAMParser(results.model, modelFile);
		parseResource(results.model, "groupBy.sam");
		parseResource(results.model, "checks.sam");
		parseResource(results.model, "graph.sam");
		parseResource(results.model, "system.sam");

		List<IQuery> queries = parser.getQueries();

		for (String scenario : results.model.scenarios) {
			ScenarioResult result = results.createScenarioResult(scenario);

			IPredicate scenarioP = BASIC.createPredicate(scenario, 0);
			IRelation scenarioR = result.model.getRelation(scenarioP);
			scenarioR.add(BASIC.createTuple());

			evaluateScenario(result);
			doQueries(results.scenarios.get(scenario).finalKnowledgeBase, queries);
		}
	}

	private void evaluateScenario(ScenarioResult result) throws Exception {
		result.phase = Phase.Setup;

		boolean setupOK = doSetup(result);

		parseResource(result.model, "finalChecks.sam");
		parseResource(result.model, "gui.sam");

		if (setupOK) {
			result.phase = Phase.Test;

			IRelation phase = result.model.getRelation(Constants.phaseP);
			phase.add(BASIC.createTuple(new ITerm[] { TERM.createString("test") }));
		} // else doSetup set finalKnowledgeBase already

		boolean finalProblem = (!setupOK) || checkForErrors(result, "after applying propagation rules");

		if (!finalProblem) {
			result.phase = Phase.Success;
		}
	}

	/* Instantiate the Setup class and run the model. Update the
	 * initialObject and field relations with the results,
	 * throwing everything else away.
	 * Returns true on success, false for an expected failure (also sets
	 * finalKnowledgeBase), or throws an exception
	 * on unexpected failure.
	 * XXX: should also keep ACL updates.
	 */
	private boolean doSetup(ScenarioResult result) throws Exception {
		Model mainModel = result.model;
		Model tmpModel = new Model(mainModel);

		IRelation phase = tmpModel.getRelation(Constants.phaseP);
		phase.add(BASIC.createTuple(new ITerm[] { TERM.createString("setup") }));

		result.model = tmpModel;
		boolean setupProblem = checkForErrors(result, "during setup phase");
		if (setupProblem) {
			return false;
		}
		result.model = mainModel;

		IKnowledgeBase setupKnowledgeBase = result.finalKnowledgeBase;

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

	/* Returns true if an error was detected, and adds the errors to results.errors.
	 * Also runs the debugger, adding error edges to results.model.
	 * Sets:
	 * - results.expectingFailure
	 * - results.finalKnowledgeBase
	 * - results.errors
	 */
	private boolean checkForErrors(ScenarioResult results, String when) throws Exception {
		Model model = results.model;
		IKnowledgeBase knowledgeBase = model.createKnowledgeBase();

		results.expectingFailure = expectingFailure(knowledgeBase);

		List<ITerm> terms = new LinkedList<ITerm>();
		boolean problem = false;
		boolean ranDebugger = false;

		// If debug is set, run the debugger on that.
		ILiteral debugL = BASIC.createLiteral(true, BASIC.createPredicate("debug", 0), BASIC.createTuple());
		IQuery debugQ = BASIC.createQuery(debugL);
		IRelation debugResults = knowledgeBase.execute(debugQ);
		if (debugResults.size() != 0) {
			doDebugging(model, debugL);
			ranDebugger = true;
		}

		for (int i = 0; i < 7; i++) {
			IPredicate errorPredicate = BASIC.createPredicate("error", i);
			ILiteral errorLiteral = BASIC.createLiteral(true, errorPredicate, BASIC.createTuple(terms));
			IQuery errorQuery = BASIC.createQuery(errorLiteral);
			IRelation errorResults = knowledgeBase.execute(errorQuery);
			if (errorResults.size() != 0) {
				if (!problem) {
					System.out.println("\n=== Errors detected " + when + " ===\n");
					problem = true;

					// Run the debugger on the first error
					//if (!ranDebugger && !results.expectingFailure) {
					if (!ranDebugger) {
						ranDebugger = true;
						doDebugging(model, errorLiteral);
					}
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

		if (ranDebugger) {
			results.finalKnowledgeBase = model.createKnowledgeBase();		// Re-create KB to include debug edges
		} else {
			results.finalKnowledgeBase = knowledgeBase;
		}

		return problem;
	}

	/* Evaluate the model with the debugger turned on. Add the resulting debug edges to model. */
	private void doDebugging(Model model, ILiteral problem) throws Exception {
		IRelation debugEdges = model.getRelation(Constants.debugEdgeP);
		System.out.println("Starting debugger...");
		Debugger debugger = new Debugger(model);
		debugger.debug(problem, debugEdges);
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
