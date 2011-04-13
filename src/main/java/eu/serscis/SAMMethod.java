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
//	Created Date :			2011-04-04
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis;

import java.util.HashSet;
import java.util.Set;
import eu.serscis.sam.node.*;
import java.io.StringReader;
import java.io.PushbackReader;
import java.util.Arrays;
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
import org.deri.iris.storage.IRelation;
import org.deri.iris.rules.IRuleSafetyProcessor;
import org.deri.iris.RuleUnsafeException;
import org.deri.iris.compiler.BuiltinRegister;
import static org.deri.iris.factory.Factory.*;
import eu.serscis.sam.lexer.Lexer;
import eu.serscis.sam.parser.Parser;
import static eu.serscis.Constants.*;

class SAMMethod {
	private Set<String> locals = new HashSet<String>();
	private SAMClass parent;
	private Map<IPredicate,IRelation> facts;
	private List<IRule> rules;
	private String localPrefix;

	public SAMMethod(SAMClass parent, Map<IPredicate,IRelation> facts, List<IRule> rules) throws Exception {
		this.parent = parent;
		this.facts = facts;
		this.rules = rules;
	}

	private String parsePattern(PPattern parsed) {
		if (parsed instanceof ANamedPattern) {
			return ((ANamedPattern) parsed).getName().getText();
		} else {
			return "*";
		}
	}

	public void addDatalog(AMethod m, ITerm methodNameFull) throws Exception {
		this.localPrefix = methodNameFull.getValue() + ".";

		AMethod method = (AMethod) m;
		ACode code = (ACode) method.getCode();

		// mayAccept(type, param)
		IRelation acceptRel = parent.getRelation(facts, mayAcceptP);
		AParams params = (AParams) method.getParams();
		if (params != null) {
			addParam(methodNameFull, acceptRel, params.getParam());

			for (PParamsTail tail : params.getParamsTail()) {
				AParam param2 = (AParam) ((AParamsTail) tail).getParam();
				addParam(methodNameFull, acceptRel, param2);
			}
		}

		int nextCallSite = 1;

		for (PStatement ps : code.getStatement()) {
			if (ps instanceof AAssignStatement) {
				AAssignStatement s = (AAssignStatement) ps;

				AAssign assign = (AAssign) s.getAssign();
				PExpr expr = (PExpr) s.getExpr();

				String callSite = methodNameFull.getValue() + "-" + nextCallSite;
				nextCallSite++;

				// hasCallSite(methodFull, callSite).
				IRelation rel = parent.getRelation(facts, hasCallSiteP);
				rel.add(BASIC.createTuple(methodNameFull, TERM.createString(callSite)));

				IPredicate valueP = null;

				if (expr instanceof ACallExpr) {
					ACallExpr callExpr = (ACallExpr) expr;

					// mayCall(callSite, var)
					rel = parent.getRelation(facts, mayCallP);
					String targetVar = callExpr.getName().getText();
					rel.add(BASIC.createTuple(TERM.createString(callSite), TERM.createString(targetVar)));

					addArgs(callSite, (AArgs) callExpr.getArgs());

					// callsMethod(callSite, method)
					String targetMethod = parsePattern(callExpr.getMethod());
					if ("*".equals(targetMethod)) {
						rel = parent.getRelation(facts, callsAnyMethodP);
						rel.add(BASIC.createTuple(TERM.createString(callSite)));
					} else {
						rel = parent.getRelation(facts, callsMethodP);
						rel.add(BASIC.createTuple(TERM.createString(callSite), TERM.createString(targetMethod)));
					}

					valueP = didGetP;
				} else if (expr instanceof ANewExpr) {
					ANewExpr newExpr = (ANewExpr) expr;

					String varName = assign.getName().getText();

					// mayCreate(classname, newType, var)
					rel = parent.getRelation(facts, mayCreateP);
					String newType = ((AType) newExpr.getType()).getName().getText();
					rel.add(BASIC.createTuple(TERM.createString(callSite),
								  TERM.createString(newType)));

					addArgs(callSite, (AArgs) newExpr.getArgs());

					valueP = didCreateP;
				} else if (expr instanceof ACopyExpr) {
					// a = b
					ACopyExpr copyExpr = (ACopyExpr) expr;

					if (assign == null) {
						throw new RuntimeException("Pointless var expression");
					}

					ILiteral isA = BASIC.createLiteral(true, BASIC.createAtom(isAP,
								BASIC.createTuple(
									TERM.createVariable("Caller"),
									TERM.createString(parent.name))));

					String sourceVar = copyExpr.getName().getText();
					assignVar(assign, makeList(isA, getValue(sourceVar)));
				} else {
					throw new RuntimeException("Unknown expr type: " + expr);
				}

				if (assign != null && valueP != null) {
					ITuple tuple = BASIC.createTuple(
							TERM.createVariable("Caller"),
							TERM.createVariable("CallerInvocation"),
							TERM.createString(callSite),
							TERM.createVariable("Value"));
					ILiteral value = BASIC.createLiteral(true, BASIC.createAtom(valueP, tuple));

					assignVar(assign, makeList(value));
				}
			} else if (ps instanceof ADeclStatement) {
				ADeclStatement decl = (ADeclStatement) ps;
				declareLocal(decl.getType(), decl.getName());
			} else if (ps instanceof AReturnStatement) {
				AReturnStatement s = (AReturnStatement) ps;

				// mayReturn(?Target, ?TargetInvocation, ?Method, ?Value) :-
				//	isA(?Target, name),
				//	live(?Target, ?TargetInvocation),
				//	(value)
				ITuple tuple = BASIC.createTuple(
						// XXX: badly named: should be Target, but getValue uses "Caller"
						TERM.createVariable("Caller"),
						TERM.createVariable("CallerInvocation"),
						methodNameFull,
						TERM.createVariable("Value"));

				ILiteral head = BASIC.createLiteral(true, BASIC.createAtom(mayReturnP, tuple));

				ILiteral isA = BASIC.createLiteral(true, BASIC.createAtom(isAP, BASIC.createTuple(
							TERM.createVariable("Caller"),
							TERM.createString(this.parent.name))));
				ILiteral live = BASIC.createLiteral(true, BASIC.createAtom(live2P, BASIC.createTuple(
							TERM.createVariable("Caller"),
							TERM.createVariable("CallerInvocation"))));

				String varName = s.getName().getText();

				IRule rule = BASIC.createRule(makeList(head), makeList(isA, live, getValue(varName)));
				//System.out.println(rule);
				rules.add(rule);
			} else {
				throw new RuntimeException("Unknown statement type: " + ps);
			}
		}
	}

	private void declareLocal(AAssign assign) {
		AType type = (AType) assign.getType();
		if (type != null) {
			declareLocal(type, assign.getName());
		}
	}

	private void declareLocal(PType type, TName aName) {
		String name = aName.getText();
		if (locals.contains(name)) {
			throw new RuntimeException("Duplicate definition of local " + name);
		} else if (parent.fields.contains(name)) {
			throw new RuntimeException("Local variable shadows field of same name: " + name);
		} else {
			locals.add(name);
		}
	}

	/* Assign a local or field, as appropriate:
	 *   local(?Caller, ?CallerInvocation, 'var', ?Value) :- body
	 * or
	 *   field(?Caller, 'var', ?Value) :- body
	 */
	private void assignVar(AAssign assign, List<ILiteral> body) {
		ILiteral head;

		declareLocal(assign);

		String varName = assign.getName().getText();
		if (locals.contains(varName)) {
			ITuple tuple = BASIC.createTuple(TERM.createVariable("Caller"),
							 TERM.createVariable("CallerInvocation"),
							 TERM.createString(expandLocal(varName)),
							 TERM.createVariable("Value"));
			head = BASIC.createLiteral(true, BASIC.createAtom(localP, tuple));
		} else if (parent.fields.contains(varName)) {
			ITuple tuple = BASIC.createTuple(TERM.createVariable("Caller"),
							 TERM.createString(varName),
							 TERM.createVariable("Value"));
			head = BASIC.createLiteral(true, BASIC.createAtom(fieldP, tuple));
		} else {
			throw new RuntimeException("Undeclared variable: " + varName);
		}

		IRule rule = BASIC.createRule(makeList(head), body);
		//System.out.println(rule);
		rules.add(rule);
	}

	private void addParam(ITerm method, IRelation acceptRel, PParam param) {
		String name = ((AParam) param).getName().getText();
		acceptRel.add(BASIC.createTuple(method, TERM.createString(expandLocal(name))));

		if (locals.contains(name)) {
			throw new RuntimeException("Duplicate definition of local " + name);
		} else if (parent.fields.contains(name)) {
			throw new RuntimeException("Local variable shadows field of same name: " + name);
		} else {
			locals.add(name);
		}
	}

	/* mayGet(?Target, ?TargetInvocation, ?Method, ?Pos, ?Value) :-
	 * 	didCall(?Caller, ?CallerInvocation, ?CallSite, ?Target, ?TargetInvocation, ?Method),
	 *	local|field,
	 */
	private void addArg(String callSite, int pos, String varName) {
		ILiteral head = BASIC.createLiteral(true, maySendP, BASIC.createTuple(
					TERM.createVariable("Target"),
					TERM.createVariable("TargetInvocation"),
					TERM.createVariable("Method"),
					CONCRETE.createInt(pos),
					TERM.createVariable("Value")
					));
		
		ILiteral didCall = BASIC.createLiteral(true, didCallP, BASIC.createTuple(
					TERM.createVariable("Caller"),
					TERM.createVariable("CallerInvocation"),
					TERM.createString(callSite),
					TERM.createVariable("Target"),
					TERM.createVariable("TargetInvocation"),
					TERM.createVariable("Method")
					));

		IRule rule = BASIC.createRule(makeList(head), makeList(didCall, getValue(varName)));
		rules.add(rule);
		//System.out.println(rule);
	}

	private void addArgs(String callSite, AArgs args) {
		if (args == null) {
			return;
		}
		int pos = 0;
		String arg0 = args.getName().getText();

		addArg(callSite, pos, arg0);

		for (PArgsTail tail : args.getArgsTail()) {
			pos += 1;
			String arg = ((AArgsTail) tail).getName().getText();
			addArg(callSite, pos, arg);
		}
	}

	/* Returns
	 *   local(?Caller, ?CallerInvocation, 'var', ?Value)
	 * or
	 *   field(?Caller, 'var', ?Value)
	 * or
	 *   equals(?Caller, ?Value)  (for "this")
	 * depending on whether varName refers to a local or a field.
	 */
	private ILiteral getValue(String sourceVar) {
		if (locals.contains(sourceVar)) {
			ITuple tuple = BASIC.createTuple(
					TERM.createVariable("Caller"),
					TERM.createVariable("CallerInvocation"),
					TERM.createString(expandLocal(sourceVar)),
					TERM.createVariable("Value"));
			return BASIC.createLiteral(true, BASIC.createAtom(localP, tuple));
		} else if (parent.fields.contains(sourceVar)) {
			ITuple tuple = BASIC.createTuple(
					TERM.createVariable("Caller"),
					TERM.createString(sourceVar),
					TERM.createVariable("Value"));
			return BASIC.createLiteral(true, BASIC.createAtom(fieldP, tuple));
		} else if (sourceVar.equals("this")) {
			return BASIC.createLiteral(true, BUILTIN.createEqual(
					TERM.createVariable("Caller"),
					TERM.createVariable("Value")));
		} else {
			throw new RuntimeException("Unknown variable " + sourceVar);
		}
	}

	private String expandLocal(String local) {
		return localPrefix + local;
	}
}
