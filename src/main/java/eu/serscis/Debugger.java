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
	private Set<ILiteral> seen = new HashSet<ILiteral>();
	private IKnowledgeBase knowledgeBase;

	public Debugger(List<IRule> rules, IKnowledgeBase knowledgeBase) throws Exception {
		this.rules = rules;
		this.knowledgeBase = knowledgeBase;
	}

	/* Why was this true? */
	public void debug(ILiteral problem) throws Exception {
		debug(problem, "");
	}

	public void debug(ILiteral problem, String indent) throws Exception {
		if (seen.contains(problem)) {
			return;
		}
		seen.add(problem);
		System.out.println(indent + problem);
		
		if (!problem.isPositive()) {
			System.out.println(indent + "(stopping at negative)");
			return;
		}

		IAtom problemAtom = problem.getAtom();

		if (!problemAtom.isGround()) {
			throw new RuntimeException("Not ground!");
		}

		ITuple problemTuple = problemAtom.getTuple();

		IPredicate p = problemAtom.getPredicate();

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
				List<ILiteral> newLiterals = new LinkedList<ILiteral>();
				for (ILiteral tail : rule.getBody()) {
					if (!tail.isPositive()) {
						System.out.println(indent + "ignoring negative: " + tail);
					}
					ITuple result = TermMatchingAndSubstitution.substituteVariablesInToTuple(tail.getAtom().getTuple(), varMap);
					IAtom newAtom = BASIC.createAtom(tail.getAtom().getPredicate(), result);
					newLiterals.add(BASIC.createLiteral(true, newAtom));
					//System.out.println(indent + "->" + newAtom);
					//debug(tail, indent + "  ");

				}
				IQuery query = BASIC.createQuery(newLiterals);
				if (debugQuery(query, indent + "   ")) {
					break;	// no need to find any further reasons for this to be true
				}

			} else {
				//System.out.println(indent + "(can't unify)");
			}
		}
	}

	/* query should return no results. Otherwise, explore their causes.
	 * True iff query is true.
	 */
	public boolean debugQuery(IQuery query, String indent) throws Exception {
		List<IVariable> queryVars = new LinkedList<IVariable>();
		IRelation debugResults = knowledgeBase.execute(query, queryVars);
		if (debugResults.size() == 0) {
			//System.out.println(indent + "(no results)");
			return false;
		}

		for (ILiteral literal : query.getLiterals()) {
			Map<IVariable,ITerm> thisVarMap = new HashMap<IVariable,ITerm>();
			if (literal.getAtom().isGround()) {
				debug(literal, indent);
			}
		}

		//System.out.println(indent + query);

		for (int i = 0; i < debugResults.size(); i++) {
			ITuple resultTuple = debugResults.get(i);
			if (resultTuple.size() == 0) {
				break;
			}

			//System.out.println(indent + resultTuple.toString());

			List<ILiteral> newLiterals = new LinkedList<ILiteral>();

			for (ILiteral literal : query.getLiterals()) {
				Map<IVariable,ITerm> thisVarMap = new HashMap<IVariable,ITerm>();
				if (literal.getAtom().isGround()) {
					// handled above
				} else {
					Map<IVariable,ITerm> varMap = getVarMap(queryVars, resultTuple);
					ITuple result = TermMatchingAndSubstitution.substituteVariablesInToTuple(literal.getAtom().getTuple(), varMap);
					IAtom newAtom = BASIC.createAtom(literal.getAtom().getPredicate(), result);
					//System.out.println(indent + newAtom);
					newLiterals.add(BASIC.createLiteral(true, newAtom));
				}
			}

			/*
			if (newLiterals.size() > 1) {
				System.out.println(indent + BASIC.createQuery(newLiterals).toString().substring(3));
			} // else we're going to print the single literal next anyway
			*/

			for (ILiteral literal : newLiterals) {
				debug(literal, indent);
			}

			break;		// (we only need one example)
		}

		return true;
	}

	private static Map<IVariable,ITerm> getVarMap(List<IVariable> names, ITuple values) {
		Map<IVariable,ITerm> map = new HashMap<IVariable,ITerm>();
		for (int i = 0; i < names.size(); i++) {
			map.put(names.get(i), values.get(i));
		}
		return map;
	}
}
