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
//	Created Date :			2011-03-30
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis.sam;

import org.deri.iris.rules.compiler.RuleCompiler;
import org.deri.iris.evaluation.stratifiedbottomup.EvaluationUtilities;
import org.deri.iris.facts.FiniteUniverseFacts;
import org.deri.iris.rules.safety.AugmentingRuleSafetyProcessor;
import org.deri.iris.utils.equivalence.IEquivalentTerms;
import org.deri.iris.evaluation.IEvaluationStrategy;
import org.deri.iris.evaluation.IEvaluationStrategyFactory;
import org.deri.iris.EvaluationException;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rules.compiler.ICompiledRule;
import org.deri.iris.evaluation.stratifiedbottomup.IRuleEvaluator;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import org.deri.iris.Configuration;
import org.deri.iris.KnowledgeBaseFactory;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.storage.IRelation;
import org.deri.iris.utils.TermMatchingAndSubstitution;
import org.deri.iris.api.builtins.IBuiltinAtom;
import static org.deri.iris.factory.Factory.*;
import static eu.serscis.sam.Constants.*;

public class Debugger {
	private List<IRule> rules = new LinkedList<IRule>();
	private Map<IPredicate,IRelation> facts;
	private IKnowledgeBase knowledgeBase;
	private Map<IPredicate,DebugRelation> debugRelations = new HashMap<IPredicate,DebugRelation>();
	private Map<ICompiledRule,IRule> sourceRules = new HashMap<ICompiledRule,IRule>();
	private int counter = 0;
	static private IPredicate mayPass = BASIC.createPredicate("mayPass", 2);
	static private IPredicate mayStore = BASIC.createPredicate("mayStore", 2);
	static private IPredicate didCall = BASIC.createPredicate("didCall", 6);
	static private IPredicate didGet = BASIC.createPredicate("didGet", 4);
	static private IPredicate didCreate = BASIC.createPredicate("didCreate", 4);
	static private IPredicate getsAccess = BASIC.createPredicate("getsAccess", 2);

	public Debugger(Model model) throws Exception {
		Configuration configuration = Eval.createDefaultConfiguration();

		this.rules = model.getRules();

		/* Make a copy of the initial facts (creating a knowledgeBase modifies them, affecting
		 * other calls to the debugger).
		 */
		this.facts = new HashMap<IPredicate,IRelation>();
		for (Map.Entry<IPredicate,IRelation> entry: model.facts.entrySet()) {
			IRelation copy = configuration.relationFactory.createRelation();
			copy.addAll(entry.getValue());
			facts.put(entry.getKey(), copy);
		}

		configuration.evaluationStrategyFactory = new IEvaluationStrategyFactory() {
			public IEvaluationStrategy createEvaluator(IFacts facts, List<IRule> rules, Configuration configuration) throws EvaluationException {
				return new DebugStragegy(facts, rules, configuration);
			}
		};

		for (Map.Entry<IPredicate,IRelation> entry : facts.entrySet()) {
			getDebugRelation(entry.getKey()).addInitialFacts(entry.getValue());
		}

		knowledgeBase = KnowledgeBaseFactory.createKnowledgeBase(facts, rules, configuration);
	}

	private static String getInvocation(ITuple tuple, int i) {
		String object = tuple.get(i).getValue().toString();
		if ("_testDriver".equals(object)) {
			return "config";
		}
		return object;
		/*
		String context = tuple.get(i + 1).getValue().toString();
		return object + ":" + context;
		*/
	}

	/* Why was this true? Find a simple example which would cause it and
	 * print the explanation to the console. Also, add graph edges to 'edges' to
	 * explain it visually.
	 */
	public void debug(final ILiteral problem, final IRelation edges) throws Exception {
		IQuery query = BASIC.createQuery(problem);
		Recorder recorder = new Recorder(problem);
		debug(query, recorder);
		recorder.showImportantSteps(edges);
	}

	/* Why was this true? Find a simple example which would cause it and
	 * tell reporter.
	 */
	public void debug(final IQuery problem, Reporter reporter) throws Exception {
		IRelation results = knowledgeBase.execute(problem);
		if (results.size() == 0) {
			reporter.noteQuery(problem);
			for (ILiteral lit : problem.getLiterals()) {
				explainNegative(lit, reporter);
			}
			return;
		}
		exploreGraph(problem, new HashSet<ILiteral>(), reporter);
	}

	/* Replace the variables in rule, where the variables in the head have the values in problem.
	 * Returns the transformed body of the rule if possible.
	 * If not possible (e.g. there are literals in the head that don't match) then
	 * throw an exception, or return null (if not strict).
	 */
	private IQuery unify(IRule rule, ILiteral problem, boolean strict) throws Exception {
		Map<IVariable,ITerm> varMap = new HashMap<IVariable,ITerm>();

		List<ILiteral> heads = rule.getHead();
		if (heads.size() != 1) {
			throw new RuntimeException("Multiple heads!");
		}
		ILiteral head = heads.get(0);

		if (!TermMatchingAndSubstitution.unify(problem.getAtom().getTuple(), head.getAtom().getTuple(), varMap)) {
			if (strict) {
				throw new RuntimeException("Failed to unify: " + problem);
			} else {
				return null;
			}
		}

		List<ILiteral> newLiterals = new LinkedList<ILiteral>();
		for (ILiteral tail : rule.getBody()) {
			IAtom oldAtom = tail.getAtom();

			ITuple result = TermMatchingAndSubstitution.substituteVariablesInToTuple(oldAtom.getTuple(), varMap);

			IAtom newAtom = updateAtom(oldAtom, result);
			ILiteral newLiteral = BASIC.createLiteral(tail.isPositive(), newAtom);

			newLiterals.add(newLiteral);
		}
		return BASIC.createQuery(newLiterals);
	}

	private void exploreGraph(IQuery ruleQ, Set<ILiteral> seen, Reporter reporter) throws Exception {
		reporter.noteQuery(ruleQ);

		/* Check internal variable assignments that make this rule true, and select the
		 * one that was true first.
		 */

		List<IVariable> queryVars = new LinkedList<IVariable>();
		IRelation internalAssignments = knowledgeBase.execute(ruleQ, queryVars);

		long bestTrue = -1;
		List<ILiteral> bestLiterals = null;
		List<ILiteral> bestNegatives = null;

		for (int t = 0; t < internalAssignments.size(); ++t ) {
			ITuple resultTuple = internalAssignments.get(t);

			List<ILiteral> newLiterals = new LinkedList<ILiteral>();
			List<ILiteral> negatives = new LinkedList<ILiteral>();

			long lastTrue = -1;

			for (ILiteral literal : ruleQ.getLiterals()) {
				Map<IVariable,ITerm> varMap = getVarMap(queryVars, resultTuple);
				ITuple result = TermMatchingAndSubstitution.substituteVariablesInToTuple(literal.getAtom().getTuple(), varMap);
				IAtom newAtom = updateAtom(literal.getAtom(), result);

				ILiteral newLiteral = BASIC.createLiteral(literal.isPositive(), newAtom);

				if (newLiteral.isPositive()) {
					long whenTrue;
					if (newLiteral.getAtom().isBuiltin()) {
						whenTrue = 0;
					} else {
						DebugRelation rel = debugRelations.get(literal.getAtom().getPredicate());
						whenTrue = rel.getFirstTrue(newLiteral.getAtom().getTuple());
					}
					if (whenTrue > lastTrue) {
						lastTrue = whenTrue;
					}
					if (whenTrue != 0) {
						newLiterals.add(newLiteral);
					}
				} else {
					negatives.add(newLiteral);
				}
			}

			//System.out.println(indent + newLiterals + " first true at " + lastTrue);

			// this result was first true at "lastTrue"

			if (bestTrue == -1 || lastTrue < bestTrue) {
				bestTrue = lastTrue;
				bestLiterals = newLiterals;
				bestNegatives = negatives;
			}
		}

		//System.out.println(indent + "best true: " + bestLiterals + " at " + bestTrue);

		for (ILiteral lit : bestLiterals) {
			explainPositive(lit, seen, reporter);
		}
		for (ILiteral lit : bestNegatives) {
			explainNegative(lit, reporter);
		}
	}

	private void explainPositive(ILiteral problem, Set<ILiteral> seen, Reporter reporter) throws Exception {
		reporter.enter(problem);

		try {
			if (seen.contains(problem)) {
				return;
			}
			seen.add(problem);
			reporter.noteNewProblem(problem);

			DebugRelation debugRelation = debugRelations.get(problem.getAtom().getPredicate());
			IRule rule = debugRelation.getReason(problem.getAtom().getTuple());

			if (rule == null) {
				//reporter.noteStep(problem);
				return;		// initial fact
			}

			IQuery ruleQ = unify(rule, problem, true);

			exploreGraph(ruleQ, seen, reporter);
		} finally {
			reporter.leave(problem);
		}
	}

	/* Why was this false? */
	private void explainNegative(ILiteral lit, Reporter reporter) throws Exception {
		reporter.enterNegative(lit);
		try {
			boolean needHeader = true;

			IPredicate targetPred = lit.getAtom().getPredicate();

			for (IRule rule : rules) {
				List<ILiteral> heads = rule.getHead();
				if (heads.size() != 1) {
					throw new RuntimeException("Multiple heads!");
				}
				ILiteral head = heads.get(0);
				IPredicate pred = head.getAtom().getPredicate();
				if (pred.equals(targetPred)) {
					IQuery unified = unify(rule, lit, false);

					if (unified != null) {
						reporter.noteNegative(rule, unified);
					}
				}
			}
		} finally {
			reporter.leaveNegative();
		}
	}

	private DebugRelation getDebugRelation(IPredicate predicate) {
		DebugRelation relation = debugRelations.get(predicate);
		if (relation == null) {
			relation = new DebugRelation();
			debugRelations.put(predicate, relation);
		}
		return relation;
	}

	/* Based on StratifiedBottomUpEvaluationStrategy; we just want access to optimisedRules! */
	private class DebugStragegy implements IEvaluationStrategy {
		DebugStragegy(IFacts facts, List<IRule> rules,
				Configuration configuration) throws EvaluationException {
			mConfiguration = configuration;
			mFacts = facts;
			mEquivalentTerms = mConfiguration.equivalentTermsFactory
				.createEquivalentTerms();

			List<IRule> allRules = mConfiguration.ruleHeadEqualityPreProcessor
				.process(rules, facts);

			if (mConfiguration.ruleSafetyProcessor instanceof AugmentingRuleSafetyProcessor)
				facts = new FiniteUniverseFacts(facts, allRules);

			EvaluationUtilities utils = new EvaluationUtilities(mConfiguration);

			// Rule safety processing
			List<IRule> safeRules = utils.applyRuleSafetyProcessor(allRules);

			// Stratify
			List<List<IRule>> stratifiedRules = utils.stratify(safeRules);

			RuleCompiler rc = new RuleCompiler(facts, mEquivalentTerms,
					mConfiguration);

			int stratumNumber = 0;
			for (List<IRule> stratum : stratifiedRules) {
				// Re-order stratum
				List<IRule> reorderedRules = utils.reOrderRules(stratum);

				// Rule optimisation
				List<IRule> optimisedRules = utils
					.applyRuleOptimisers(reorderedRules);

				List<ICompiledRule> compiledRules = new ArrayList<ICompiledRule>();

				for (IRule rule : optimisedRules) {
					ICompiledRule cRule = rc.compile(rule);
					compiledRules.add(cRule);
					sourceRules.put(cRule, rule);
				}

				// TODO Enable rule head equality support for semi-naive evaluation.
				// Choose the correct evaluation technique for the specified rules and stratum.
				IRuleEvaluator evaluator = new DebugEvaluator();

				evaluator.evaluateRules(compiledRules, facts, configuration);

				stratumNumber++;
			}
		}

		public IRelation evaluateQuery(IQuery query, List<IVariable> outputVariables)
			throws EvaluationException {
			if (query == null)
				throw new IllegalArgumentException(
						"StratifiedBottomUpEvaluationStrategy.evaluateQuery() - query must not be null.");

			if (outputVariables == null)
				throw new IllegalArgumentException(
						"StratifiedBottomUpEvaluationStrategy.evaluateQuery() - outputVariables must not be null.");

			RuleCompiler compiler = new RuleCompiler(mFacts, mEquivalentTerms,
					mConfiguration);

			ICompiledRule compiledQuery = compiler.compile(query);

			IRelation result = compiledQuery.evaluate();

			outputVariables.clear();
			outputVariables.addAll(compiledQuery.getVariablesBindings());

			return result;
		}

		protected IEquivalentTerms mEquivalentTerms;

		protected final Configuration mConfiguration;

		protected final IFacts mFacts;
	}

	// (based on NaiveEvaluator example)
	private class DebugEvaluator implements IRuleEvaluator {
		public void evaluateRules(List<ICompiledRule> rules, IFacts facts, Configuration configuration) throws EvaluationException {
			boolean cont = true;
			while (cont) {
				cont = false;
				
				// For each rule in the collection (stratum)
				for (final ICompiledRule rule : rules ) {
					IRelation delta = rule.evaluate();

					if (delta != null && delta.size() > 0) {
						IPredicate predicate = rule.headPredicate();

						if (facts.get(predicate).addAll(delta)) {
							getDebugRelation(predicate).addAll(delta, rule);
							cont = true;
						}
					}
				}
			}
		}
	}

	private class DebugRelation {
		/* The first rule that caused this tuple to be asserted. */
		private Map<ITuple,IRule> reason = new HashMap<ITuple,IRule>();
		private Map<ITuple,Long> firstTrue = new HashMap<ITuple,Long>();

		public void addAll(IRelation tuples, ICompiledRule cRule) {
			IRule rule = cRule == null ? null : sourceRules.get(cRule);
			for (int i = tuples.size() - 1; i >= 0; i--) {
				ITuple tuple = tuples.get(i);
				if (!reason.containsKey(tuple)) {
					//System.out.println("" + rule + ", " + tuple + " at " + counter);
					reason.put(tuple, rule);
					firstTrue.put(tuple, Long.valueOf(counter));
				}
			}
			counter += 1;
		}

		public IRule getReason(ITuple tuple) {
			return reason.get(tuple);
		}

		public void addInitialFacts(IRelation tuples) {
			addAll(tuples, null);
		}

		public long getFirstTrue(ITuple tuple) {
			Long when = firstTrue.get(tuple);
			if (when == null) {
				throw new RuntimeException("fact " + tuple + " was never true");
			}
			return when;
		}
	}

	private static Map<IVariable,ITerm> getVarMap(List<IVariable> names, ITuple values) {
		Map<IVariable,ITerm> map = new HashMap<IVariable,ITerm>();
		for (int i = 0; i < names.size(); i++) {
			map.put(names.get(i), values.get(i));
		}
		return map;
	}

	/* Create a new atom "predicate(tuple)" with the same predicate as oldAtom. */
	private static IAtom updateAtom(IAtom oldAtom, ITuple tuple) throws Exception {
		if (oldAtom.isBuiltin()) {
			ITerm[] newTerms = tuple.toArray(new ITerm[tuple.size()]);
			Constructor<IBuiltinAtom> constructor = (Constructor<IBuiltinAtom>) oldAtom.getClass().getConstructor(newTerms.getClass());
			IAtom newAtom = (IAtom) constructor.newInstance((Object) newTerms);
			//System.out.println("created " + newAtom + ", " + newAtom.isBuiltin());
			return newAtom;
		} else {
			return BASIC.createAtom(oldAtom.getPredicate(), tuple);
 		}
 	}

	private static class Step {
		ILiteral problem;
		Step parent;
		List<Step> children;

		private Step(Step parent, ILiteral problem) {
			this.parent = parent;
			this.problem = problem;
			this.children = new LinkedList<Step>();
		}

		public void show(String indent, IRelation debugEdges) {
			String msg = null;

			ITuple tuple = problem.getAtom().getTuple();
			IPredicate p = problem.getAtom().getPredicate();
			if (p.getPredicateSymbol().equals("didReceive")) {
				String target = tuple.get(0).getValue().toString();
				String method = tuple.get(2).getValue().toString();
				String arg;
				if (tuple.size() == 4) {
					arg = tuple.get(3).getValue().toString();
				} else {
					arg = tuple.get(4).getValue().toString();
				}
				msg = target + ": received " + arg;
				if (!method.equals("Unknown.invoke")) {
					msg += " (arg to " + method + ")";
				} else {
					msg += " (as an argument)";
				}
			} else if (p.equals(mayStore)) {
				String callSite = tuple.get(0).getValue().toString();
				String target = tuple.get(1).getValue().toString();
				//steps.add("   (" + callSite + " may store result in " + target + ")");
			} else if (p.equals(didCall)) {
				String caller = getInvocation(tuple, 0);
				String callSite = tuple.get(2).getValue().toString();
				String target = getInvocation(tuple, 3);
				String method = tuple.get(5).getValue().toString();
				//String method = tuple.get(4).getValue().toString();
				//String arg = tuple.get(5).getValue().toString();
				//String result = tuple.get(6).getValue().toString();
				//msg = caller + "@" + callSite + " calls " + target + "." + method;
				int i = method.indexOf('.');
				method = method.substring(i + 1);
				msg = caller + ": " + target + "." + method + "()";
				debugEdges.add(BASIC.createTuple(tuple.get(0),
							    tuple.get(1),
							    tuple.get(2),
							    tuple.get(3),
							    tuple.get(4)));
			} else if (p.equals(didGet)) {
				String caller = getInvocation(tuple, 0);
				String callSite = tuple.get(2).getValue().toString();
				String result = tuple.get(3).getValue().toString();
				msg = "" + caller + ": got " + result;
			} else if (p.equals(didGetExceptionP)) {
				String caller = getInvocation(tuple, 0);
				String callSite = tuple.get(2).getValue().toString();
				String result = tuple.get(3).getValue().toString();
				msg = "" + caller + ": got exception " + result;
			} else if (p.equals(didCreate)) {
				String actor = getInvocation(tuple, 0);
				//String resultVar = tuple.get(2).getValue().toString();
				String type = tuple.get(3).getValue().toString();
				msg = actor + ": new " + type + "()";
				debugEdges.add(BASIC.createTuple(tuple.get(0),
							    tuple.get(1),
							    tuple.get(2),
							    tuple.get(3),
							    tuple.get(1)));
			}

			if (msg == null) {
				msg = problem.toString();
			}

			if (children.size() != 0) {
				//msg += " because:";
			}
			if (parent != null) {
				msg = "<= " + msg;
			}
			System.out.println(indent + msg);

			Set<ILiteral> seen = new HashSet<ILiteral>();	// avoid duplicates

			indent += "   ";
			for (Step child : children) {
				if (!seen.contains(child.problem)) {
					seen.add(child.problem);
					child.show(indent, debugEdges);
				}
			}
		}
	}

	private static class Recorder implements Reporter {
		private int indent = 0;
		private Step myStep;
		private ILiteral optNegativeNeedHeader = null;
		
		public Recorder(ILiteral problem) {
			myStep = new Step(null, problem);
		}

		public void enter(ILiteral literal) {
			IPredicate p = literal.getAtom().getPredicate();

			if (p.equals(didCall) ||
			    p.equals(didGet) ||
			    p.equals(didGetExceptionP) ||
			    p.equals(didCreate) ||
			    p.equals(getsAccess) ||
			    p.getPredicateSymbol().equals("error") ||
			    p.getPredicateSymbol().equals("didReceive")) {
				Step child = new Step(myStep, literal);
				myStep.children.add(child);
				myStep = child;
			}

			indent += 1;
		}

		public void leave(ILiteral literal) {
			if (myStep.problem == literal && myStep.parent != null) {
				myStep = myStep.parent;
			}
			indent -= 1;
		}

		public void printIndented(Object item) {
			String msg = "";
			for (int i = 0; i < indent; i++) {
				msg += "   ";
			}
			System.out.println(msg + item);
		}

		public void noteNewProblem(ILiteral problem) {
			printIndented(problem);
		}

		public void noteQuery(IQuery ruleQ) {
			printIndented(ruleQ);
		}

		public void showImportantSteps(IRelation debugEdges) {
			System.out.println("\nSimplified debug graph:\n");
			myStep.show("", debugEdges);
		}

		/* We are about to explain why literal is false. */
		public void enterNegative(ILiteral literal) {
			optNegativeNeedHeader = literal;
			indent += 1;
		}

		public void leaveNegative() {
			if (optNegativeNeedHeader != null) {
				printIndented("" + optNegativeNeedHeader + "; no rules for this predicate");
			}
			optNegativeNeedHeader = null;
			indent -= 1;
		}

		/* This rule might have been intended to fire, but there was no match. */
		public void noteNegative(IRule rule, IQuery unified) {
			if (optNegativeNeedHeader != null) {
				printIndented("" + optNegativeNeedHeader + "; none of these was true:");
				optNegativeNeedHeader = null;
			}

			indent += 1;
			printIndented(rule);
			printIndented(unified);
			indent -= 1;
		}

	}
}
