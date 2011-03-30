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

package eu.serscis;

import java.util.HashMap;
import java.util.LinkedList;
import java.io.FileWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
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
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.compiler.Parser;
import org.deri.iris.storage.IRelation;
import org.deri.iris.rules.IRuleSafetyProcessor;
import org.deri.iris.RuleUnsafeException;
import org.deri.iris.utils.TermMatchingAndSubstitution;
import static org.deri.iris.factory.Factory.*;

public class Debugger {
	private List<IRule> rules = new LinkedList<IRule>();
	private Map<IPredicate,IRelation> facts = new HashMap<IPredicate,IRelation>();
	private Map<ILiteral,ResultTree> seen = new HashMap<ILiteral,ResultTree>();
	private IKnowledgeBase knowledgeBase;

	public Debugger(List<IRule> rules, Map<IPredicate,IRelation> initialFacts, IKnowledgeBase knowledgeBase) throws Exception {
		this.rules = rules;
		this.knowledgeBase = knowledgeBase;
		this.facts = initialFacts;
	}

	/* Why was this true? */
	public void debug(ILiteral problem) throws Exception {
		ResultTree result = findShortestReason(problem);
		result.dump();
	}

	public ResultTree findShortestReason(ILiteral problem) throws Exception {
		ResultTree resultTree = seen.get(problem);
		if (resultTree != null) {
			return resultTree;
		}
		resultTree = new ResultTree(problem);
		seen.put(problem, resultTree);

		//System.out.println(indent + problem);
		
		if (!problem.isPositive()) {
			resultTree.msg = "(stopping at negative)";
			return resultTree;
		}

		IAtom problemAtom = problem.getAtom();

		if (!problemAtom.isGround()) {
			throw new RuntimeException("Not ground!");
		}

		ITuple problemTuple = problemAtom.getTuple();

		IPredicate p = problemAtom.getPredicate();

		IRelation initialFacts = facts.get(p);
		if (initialFacts != null) {
			if (initialFacts.contains(problemTuple)) {
				//resultTree.msg = "(initial fact)";
				resultTree.depth = 0;
				resultTree.complete = true;
				return resultTree;
			}
		}

		// find all rules which could assert this
		for (IRule rule : rules) {
			List<ILiteral> heads = rule.getHead();
			if (heads.size() != 1) {
				throw new RuntimeException("multiple heads in " + rule + "!");
			}
			ILiteral head = heads.get(0);
			if (!head.getAtom().getPredicate().equals(p)) {
				continue;
			}
			//System.out.println(indent + rule);

			Map<IVariable,ITerm> varMap = new HashMap<IVariable,ITerm>();
			if (TermMatchingAndSubstitution.unify(problemTuple, head.getAtom().getTuple(), varMap)) {
				// rule head matches
				List<ILiteral> negatives = new LinkedList<ILiteral>();
				List<ILiteral> newLiterals = new LinkedList<ILiteral>();
				for (ILiteral tail : rule.getBody()) {
					ITuple result = TermMatchingAndSubstitution.substituteVariablesInToTuple(tail.getAtom().getTuple(), varMap);
					IAtom newAtom = BASIC.createAtom(tail.getAtom().getPredicate(), result);
					ILiteral newLiteral = BASIC.createLiteral(tail.isPositive(), newAtom);
					if (tail.isPositive()) {
						newLiterals.add(newLiteral);
					} else {
						//System.out.println(indent + "ignoring negative: " + tail);
						negatives.add(newLiteral);
						continue;
					}
					//System.out.println(indent + "->" + newAtom);
					//debug(tail, indent + "  ");
				}
				IQuery query = BASIC.createQuery(newLiterals);

				debugQuery(query, resultTree);

				resultTree.negatives = negatives;
			} else {
				//System.out.println("can't unify: " + problemTuple + " with " + head);
			}
		}

		if (resultTree.depth == -1) {
			//resultTree.depth = 0;
			//resultTree.msg = "(no depth?)";
			throw new RuntimeException("no depth for " + problem);
		}

		resultTree.complete = true;
		return resultTree;
	}

	/* query should return no results. Otherwise, explore their causes.
	 * If the found solution is better than the one in 'result', update it with the new one.
	 */
	public void debugQuery(IQuery query, ResultTree resultTree) throws Exception {
		List<IVariable> queryVars = new LinkedList<IVariable>();
		IRelation debugResults = knowledgeBase.execute(query, queryVars);
		if (debugResults.size() == 0) {
			//System.out.println(indent + "(no results)");
			return;
		}

		/*
		for (ILiteral literal : query.getLiterals()) {
			Map<IVariable,ITerm> thisVarMap = new HashMap<IVariable,ITerm>();
			if (literal.getAtom().isGround()) {
				findShortestReason(literal, indent);
			}
		}
		*/

		//System.out.println(indent + query);

		for (int i = 0; i < debugResults.size(); i++) {
			ITuple resultTuple = debugResults.get(i);

			//System.out.println(indent + resultTuple.toString());

			List<ILiteral> newLiterals = new LinkedList<ILiteral>();

			for (ILiteral literal : query.getLiterals()) {
				Map<IVariable,ITerm> varMap = getVarMap(queryVars, resultTuple);
				ITuple result = TermMatchingAndSubstitution.substituteVariablesInToTuple(literal.getAtom().getTuple(), varMap);
				IAtom newAtom = BASIC.createAtom(literal.getAtom().getPredicate(), result);
				//System.out.println(indent + newAtom);
				newLiterals.add(BASIC.createLiteral(true, newAtom));
			}

			/*
			if (newLiterals.size() > 1) {
				System.out.println(indent + BASIC.createQuery(newLiterals).toString().substring(3));
			} // else we're going to print the single literal next anyway
			*/

			ResultTree[] subResult = new ResultTree[newLiterals.size()];
			int maxDepth = -1;
			int r = 0;

			for (ILiteral literal : newLiterals) {
				subResult[r] = findShortestReason(literal);
				if (subResult[r].complete && subResult[r].depth > maxDepth) {
					maxDepth = subResult[r].depth;
				}
				r += 1;
			}

			maxDepth += 1;
			if (resultTree.depth == -1 || maxDepth < resultTree.depth) {
				resultTree.setDepth(maxDepth, subResult);
			}
		}
	}

	private static Map<IVariable,ITerm> getVarMap(List<IVariable> names, ITuple values) {
		Map<IVariable,ITerm> map = new HashMap<IVariable,ITerm>();
		for (int i = 0; i < names.size(); i++) {
			map.put(names.get(i), values.get(i));
		}
		return map;
	}

	private static class ResultTree {
		private int depth = -1;
		private ILiteral problem;
		private String msg;
		private ResultTree[] children;
		private List<ILiteral> negatives;
		private boolean complete = false;

		public ResultTree(ILiteral problem) {
			this.problem = problem;
		}

		public void dump() {
			HashSet<ResultTree> seen = new HashSet<ResultTree>();
			dump(seen, "");
		}

		private void dump(HashSet<ResultTree> seen, String indent) {
			System.out.println(indent + problem);

			if (seen.contains(this)) {
				return;
			}
			seen.add(this);

			if (msg != null) {
				System.out.println(indent + msg);
			}
			if (children != null) {
				for (ResultTree child : children) {
					child.dump(seen, indent + "   ");
				}
			}
			if (negatives != null) {
				for (ILiteral lit : negatives) {
					System.out.println(indent + "   " + lit);
				}
			}
		}

		private void setDepth(int newDepth, ResultTree[] subResult) {
			if (depth != -1 && newDepth >= depth) {
				throw new RuntimeException("new depth is greater than current depth!");
			}
			if (newDepth < 0) {
				throw new RuntimeException("new depth < 0!");
			}
			if (complete) {
				throw new RuntimeException("setting new depth after complete!");
			}
			depth = newDepth;
			children = subResult;
		}
	}
}
