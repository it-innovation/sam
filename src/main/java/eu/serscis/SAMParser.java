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

public class SAMParser {
	static private IPredicate isA = BASIC.createPredicate("isA", 2);
	static private IPredicate hasCallSiteP = BASIC.createPredicate("hasCallSite", 2);
	static private IPredicate didGetP = BASIC.createPredicate("didGet", 4);
	static private IPredicate didCreateP = BASIC.createPredicate("didCreate", 4);
	static private IPredicate mayCallP = BASIC.createPredicate("mayCall", 2);
	static private IPredicate mayPassP = BASIC.createPredicate("mayPass", 2);
	static private IPredicate mayCreateP = BASIC.createPredicate("mayCreate", 2);
	static private IPredicate mayAcceptP = BASIC.createPredicate("mayAccept", 2);
	static private IPredicate mayReturnP = BASIC.createPredicate("mayReturn", 2);
	static private IPredicate hasFieldP = BASIC.createPredicate("hasField", 2);
	static private IPredicate localP = BASIC.createPredicate("local", 4);
	static private IPredicate fieldP = BASIC.createPredicate("field", 3);
	private org.deri.iris.compiler.Parser parser = new org.deri.iris.compiler.Parser();
	public Map<IPredicate,IRelation> facts;
	public List<IRule> rules;
	private Configuration configuration;

	public SAMParser(Configuration configuration) {
		this.configuration = configuration;
	}

	private String readAll(Reader source) throws IOException {
		char[] buffer = new char[256];
		String result = "";
		while (true) {
			int got = source.read(buffer);
			if (got == -1) {
				return result;
			}
			result += new String(buffer, 0, got);
		}
	}

	public void parse(Reader source) throws Exception {
		Map<String,SAMClass> classDefinitions = new HashMap<String,SAMClass>();

		String sam = readAll(source);

		String datalog = "";

		while (true) {
			int i = sam.indexOf("\nclass ");
			if (i == -1) {
				datalog += sam;
				break;
			}
			int j = sam.indexOf("\n}", i);
			if (j == -1) {
				throw new RuntimeException("Missing final } for class: " + sam);
			}

			String javaCode = sam.substring(i + 1, j + 2);
			//System.out.println("Java chunk:\n" + javaCode);

			SAMClass klass = new SAMClass(javaCode);
			if (classDefinitions.containsKey(klass.name)) {
				throw new RuntimeException("Duplicate class definition: " + klass.name);
			}
			classDefinitions.put(klass.name, klass);

			// Insert the right number of blank lines into the datalog so errors give the
			// right line number...
			String[] javaLines = javaCode.split("\n");
			for (int k = 0; k < javaLines.length; k++) {
				datalog += "\n";
			}

			datalog += sam.substring(0, i + 1);
			sam = sam.substring(j + 2);
		}

		//System.out.println("Code:\n" + datalog);

		parser.parse(datalog);

		facts = parser.getFacts();
		rules = parser.getRules();

		for (SAMClass klass : classDefinitions.values()) {
			klass.addDatalog(facts, rules);
		}
	}

	public List<IQuery> getQueries() {
		return parser.getQueries();
	}

	private static int nextCallSite = 1;

	private IRelation getRelation(Map<IPredicate,IRelation> facts, IPredicate pred) {
		IRelation rel = facts.get(pred);
		if (rel == null) {
			rel = configuration.relationFactory.createRelation();
			facts.put(pred, rel);
		}
		return rel;
	}

	private static <X> List<X> makeList(X... items) {
		return Arrays.asList(items);
	}

	private class SAMClass {
		public String name;
		private ABehaviour behaviour;
		private Set<String> fields = new HashSet<String>();

		public SAMClass(String javaCode) throws Exception {
			PushbackReader pbr = new PushbackReader(new StringReader(javaCode));
			Lexer lexer = new Lexer(pbr);
			Parser parser = new Parser(lexer);

			System.out.println("Parsing " + javaCode);
			Start start = parser.parse();
			System.out.println("Parsed: " + start);

			this.behaviour = (ABehaviour) start.getPBehaviour();
			System.out.println("Class: " + behaviour);

			this.name = behaviour.getName().getText();
		}

		private void declareLocal(Set<String> locals, AAssign assign) {
			AType type = (AType) assign.getType();
			if (type != null) {
				String name = assign.getName().getText();
				if (locals.contains(name)) {
					throw new RuntimeException("Duplicate definition of local " + name);
				} else if (fields.contains(name)) {
					throw new RuntimeException("Local variable shadows field of same name: " + name);
				} else {
					locals.add(name);
				}
			}
		}

		/* Assign a local or field, as appropriate:
		 *   local(?Caller, ?CallerInvocation, 'var', ?Value) :- body
		 * or
		 *   field(?Caller, 'var', ?Value) :- body
		 */
		private void assignVar(AAssign assign, Set<String> locals, List<IRule> rules, List<ILiteral> body) {
			ILiteral head;

			declareLocal(locals, assign);

			String varName = assign.getName().getText();
			if (locals.contains(varName)) {
				ITuple tuple = BASIC.createTuple(TERM.createVariable("Caller"),
								 TERM.createVariable("CallerInvocation"),
								 TERM.createString(varName),
								 TERM.createVariable("Value"));
				head = BASIC.createLiteral(true, BASIC.createAtom(localP, tuple));
			} else if (fields.contains(varName)) {
				ITuple tuple = BASIC.createTuple(TERM.createVariable("Caller"),
								 TERM.createString(varName),
								 TERM.createVariable("Value"));
				head = BASIC.createLiteral(true, BASIC.createAtom(fieldP, tuple));
			} else {
				throw new RuntimeException("Undeclared variable: " + varName);
			}

			IRule rule = BASIC.createRule(makeList(head), body);
			System.out.println(rule);
			rules.add(rule);
		}

		private void addParam(IRelation acceptRel, Set<String> locals, PParam param) {
			String name = ((AParam) param).getName().getText();
			acceptRel.add(BASIC.createTuple(TERM.createString(this.name), TERM.createString(name)));

			if (locals.contains(name)) {
				throw new RuntimeException("Duplicate definition of local " + name);
			} else if (fields.contains(name)) {
				throw new RuntimeException("Local variable shadows field of same name: " + name);
			} else {
				locals.add(name);
			}
		}

		private void addArgs(IRelation rel, String callSite, AArgs args) {
			if (args == null) {
				return;
			}

			String arg0 = args.getName().getText();
			rel.add(BASIC.createTuple(TERM.createString(callSite), TERM.createString(arg0)));

			for (PArgsTail tail : args.getArgsTail()) {
				String arg = ((AArgsTail) tail).getName().getText();
				rel.add(BASIC.createTuple(TERM.createString(callSite), TERM.createString(arg)));
			}
		}

		public void addDatalog(Map<IPredicate,IRelation> facts, List<IRule> rules) {
			AExtends extend = (AExtends) behaviour.getExtends();
			if (extend != null) {
				String superclass = extend.getName().getText();
				ITuple objAndSuper = BASIC.createTuple(TERM.createVariable("X"), TERM.createString(superclass));
				ITuple objAndName = BASIC.createTuple(TERM.createVariable("X"), TERM.createString(name));
				ILiteral head = BASIC.createLiteral(true, isA, objAndSuper);
				ILiteral body = BASIC.createLiteral(true, isA, objAndName);
				IRule rule = BASIC.createRule(makeList(head), makeList(body));
				//System.out.println(rule);
				rules.add(rule);
			}

			AClassBody body = (AClassBody) behaviour.getClassBody();

			for (PField f : body.getField()) {
				AField field = (AField) f;

				// hasField(type, name)
				IRelation rel = getRelation(facts, hasFieldP);
				String fieldName = field.getName().getText();
				fields.add(fieldName);
				rel.add(BASIC.createTuple(TERM.createString(this.name), TERM.createString(fieldName)));
			}

			List<PMethod> methods = body.getMethod();
			for (PMethod m : methods) {
				AMethod method = (AMethod) m;
				ACode code = (ACode) method.getCode();
				Set<String> locals = new HashSet<String>();

				String methodName = method.getName().getText();

				if (method.getType() == null) {
					if (!methodName.equals(this.name)) {
						throw new RuntimeException("Constructor must be named after class (or missing return type): " + methodName + " in " + this.name);
					}
				}

				// mayAccept(type, param)
				IRelation acceptRel = getRelation(facts, mayAcceptP);
				AParams params = (AParams) method.getParams();
				if (params != null) {
					addParam(acceptRel, locals, params.getParam());

					for (PParamsTail tail : params.getParamsTail()) {
						AParam param2 = (AParam) ((AParamsTail) tail).getParam();
						addParam(acceptRel, locals, param2);
					}
				}

				for (PStatement ps : code.getStatement()) {
					if (ps instanceof AAssignStatement) {
						AAssignStatement s = (AAssignStatement) ps;

						AAssign assign = (AAssign) s.getAssign();
						PExpr expr = (PExpr) s.getExpr();

						String callSite = "cs" + nextCallSite;
						nextCallSite++;

						// hasCallSite(name, callSite).
						IRelation rel = getRelation(facts, hasCallSiteP);
						rel.add(BASIC.createTuple(TERM.createString(name), TERM.createString(callSite)));

						IPredicate valueP;

						if (expr instanceof ACallExpr) {
							ACallExpr callExpr = (ACallExpr) expr;

							// mayCall(callSite, var)
							rel = getRelation(facts, mayCallP);
							String targetVar = callExpr.getName().getText();
							rel.add(BASIC.createTuple(TERM.createString(callSite), TERM.createString(targetVar)));

							// mayPass(callSite, var)
							rel = getRelation(facts, mayPassP);
							addArgs(rel, callSite, (AArgs) callExpr.getArgs());

							valueP = didGetP;
						} else if (expr instanceof ANewExpr) {
							ANewExpr newExpr = (ANewExpr) expr;

							String varName = assign.getName().getText();

							// mayCreate(classname, newType, var)
							rel = getRelation(facts, mayCreateP);
							String newType = ((AType) newExpr.getType()).getName().getText();
							rel.add(BASIC.createTuple(TERM.createString(callSite),
										  TERM.createString(newType)));

							// mayPass(callSite, var)
							rel = getRelation(facts, mayPassP);
							addArgs(rel, callSite, (AArgs) newExpr.getArgs());

							valueP = didCreateP;
						} else {
							throw new RuntimeException("Unknown expr type: " + expr);
						}

						if (assign != null) {
							ITuple tuple = BASIC.createTuple(
									TERM.createVariable("Caller"),
									TERM.createVariable("CallerInvocation"),
									TERM.createString(callSite),
									TERM.createVariable("Value"));
							ILiteral value = BASIC.createLiteral(true, BASIC.createAtom(valueP, tuple));

							assignVar(assign, locals, rules, makeList(value));
						}

					} else if (ps instanceof AReturnStatement) {
						AReturnStatement s = (AReturnStatement) ps;

						// mayReturn(type, var)
						IRelation rel = getRelation(facts, mayReturnP);
						String varName = s.getName().getText();
						rel.add(BASIC.createTuple(TERM.createString(this.name),
									  TERM.createString(varName)));
					} else {
						throw new RuntimeException("Unknown statement type: " + ps);
					}
				}
			}
		}
	}
}
