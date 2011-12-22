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
//	Created Date :			2011-04-14
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis.sam;

import eu.serscis.sam.node.TNumber;
import eu.serscis.sam.node.TBool;
import eu.serscis.sam.node.TName;
import eu.serscis.sam.node.TRefLiteral;
import eu.serscis.sam.node.TStringLiteral;
import java.util.Iterator;
import org.deri.iris.api.basics.IQuery;
import eu.serscis.sam.node.AAnyTerm;
import org.deri.iris.api.terms.concrete.IBooleanTerm;
import org.deri.iris.api.terms.concrete.IIntegerTerm;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.IVariable;
import eu.serscis.sam.node.ARefTerm;
import eu.serscis.sam.node.AJavavarTerm;
import eu.serscis.sam.node.ANeqBinop;
import eu.serscis.sam.node.AEqBinop;
import eu.serscis.sam.node.PBinop;
import eu.serscis.sam.node.ANegativeLiteral;
import eu.serscis.sam.node.APositiveLiteral;
import eu.serscis.sam.node.PLiteral;
import eu.serscis.sam.node.ALiteralTail;
import eu.serscis.sam.node.PLiteralTail;
import eu.serscis.sam.node.ALiterals;
import eu.serscis.sam.node.ATermTail;
import eu.serscis.sam.node.PTermTail;
import eu.serscis.sam.node.ABuiltinAtom;
import eu.serscis.sam.node.ANullaryAtom;
import eu.serscis.sam.node.ANormalAtom;
import eu.serscis.sam.node.AIntTerm;
import eu.serscis.sam.node.ABoolTerm;
import eu.serscis.sam.node.AVarTerm;
import eu.serscis.sam.node.AStringTerm;
import org.deri.iris.api.basics.IAtom;
import eu.serscis.sam.node.PAtom;
import eu.serscis.sam.node.ATerms;
import eu.serscis.sam.node.PTerm;
import eu.serscis.sam.parser.ParserException;
import eu.serscis.sam.node.Token;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.deri.iris.Configuration;
import org.deri.iris.KnowledgeBaseFactory;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.storage.IRelation;
import org.deri.iris.compiler.BuiltinRegister;
import static org.deri.iris.factory.Factory.*;

public class Model {
	final Configuration configuration;
	private final List<IRule> rules = new LinkedList<IRule>();
	private final Map<IPredicate,IRelation> facts = new HashMap<IPredicate,IRelation>();
	public final Map<IPredicate,TermDefinition[]> declared = new HashMap<IPredicate,TermDefinition[]>();
	private final static BuiltinRegister builtinRegister = new BuiltinRegister();
	private int assertions = 0;

	static {
		ITerm t1 = TERM.createVariable("a");
		ITerm t2 = TERM.createVariable("b");
		ITerm t3 = TERM.createVariable("c");

		builtinRegister.registerBuiltin(new MakeRefBuiltin(t1, t2, t3));

		builtinRegister.registerBuiltin(new IsRefBuiltin(t1));

		builtinRegister.registerBuiltin(new AssignBuiltin(t1, t2, t3));

		builtinRegister.registerBuiltin(new MatchBuiltin(t1, t2, t3));
		builtinRegister.registerBuiltin(new MatchBuiltin.MatchBuiltinBoolean(t1, t2));

		builtinRegister.registerBuiltin(new StringConcatBuiltin(t1, t2, t3));
	}

	public Model(Configuration configuration) {
		this.configuration = configuration;
	}

	public Model(Model source) {
		this.configuration = source.configuration;
		this.rules.addAll(source.rules);
		this.facts.putAll(source.facts);
		this.declared.putAll(source.declared);
	}

	public IRelation getRelation(IPredicate pred) {
		IRelation rel = facts.get(pred);
		if (rel == null) {
			rel = configuration.relationFactory.createRelation();
			facts.put(pred, rel);
		}
		return rel;
	}
	
	public IKnowledgeBase createKnowledgeBase() throws Exception {
		Map<IPredicate,IRelation> workingFacts = new HashMap<IPredicate,IRelation>();

		/* Make a copy of the initial facts (otherwise the debugger doesn't work). */
		for (Map.Entry<IPredicate,IRelation> entry: facts.entrySet()) {
			IRelation copy = configuration.relationFactory.createRelation();
			copy.addAll(entry.getValue());
			workingFacts.put(entry.getKey(), copy);
		}

		return KnowledgeBaseFactory.createKnowledgeBase(workingFacts, rules, configuration);
	}

	public void requireDeclared(Token tok, IPredicate pred) throws ParserException {
		if ("error".equals(pred.getPredicateSymbol())) {
			return;
		}

		if (!declared.containsKey(pred)) {
			throw new ParserException(tok, "Predicate not declared: " + pred + "/" + pred.getArity());
		}
	}

	public TermDefinition[] getDefinition(IPredicate pred) {
		return declared.get(pred);
	}

	public void declare(Token tok, IPredicate pred, TermDefinition[] terms) throws ParserException {
		if (declared.containsKey(pred)) {
			throw new ParserException(tok, "Predicate already declared: " + pred);
		}

		//System.out.println("Declare " + pred + "/" + pred.getArity());
		declared.put(pred, terms);
	}

	public ITerm parseTerm(PTerm parsed, TermProcessor termFn, List<Token> tokensOut) throws ParserException {
		if (termFn != null) {
			ITerm term = termFn.process(parsed, tokensOut);
			if (term != null) {
				return term;
			}
		}

		if (parsed instanceof AStringTerm) {
			TStringLiteral lit = ((AStringTerm) parsed).getStringLiteral();
			String str = Constants.getString(lit);
			tokensOut.add(lit);
			return TERM.createString(str);
		} else if (parsed instanceof ARefTerm) {
			TRefLiteral lit = ((ARefTerm) parsed).getRefLiteral();
			String str = Constants.getRef(lit);
			tokensOut.add(lit);
			return new RefTerm(str);
		} else if (parsed instanceof AVarTerm) {
			TName name = ((AVarTerm) parsed).getName();
			tokensOut.add(name);
			return TERM.createVariable(name.getText());
		} else if (parsed instanceof ABoolTerm) {
			TBool bool =((ABoolTerm) parsed).getBool();
			tokensOut.add(bool);
			boolean val = Boolean.valueOf(bool.getText());
			return CONCRETE.createBoolean(val);
		} else if (parsed instanceof AAnyTerm) {
			AAnyTerm term = ((AAnyTerm) parsed);
			tokensOut.add(term.getName());
			return AnyTerm.valueOf(term.getName());
		} else if (parsed instanceof AIntTerm) {
			TNumber tok = ((AIntTerm) parsed).getNumber();
			tokensOut.add(tok);
			int val = Integer.valueOf(tok.getText());
			return CONCRETE.createInt(val);
		} else if (parsed instanceof AJavavarTerm) {
			AJavavarTerm var = (AJavavarTerm) parsed;
			throw new ParserException(var.getName(), "Can't use a Java variable here (or missing '?')");
		} else {
			throw new RuntimeException("Unknown term type:" + parsed + " : " + parsed.getClass());
		}
	}

	public ITuple parseTerms(ATerms parsed, TermProcessor termFn, List<Token> tokensOut) throws ParserException {
		List<ITerm> terms = new LinkedList<ITerm>();

		terms.add(parseTerm(parsed.getTerm(), termFn, tokensOut));

		for (PTermTail tail : parsed.getTermTail()) {
			terms.add(parseTerm(((ATermTail) tail).getTerm(), termFn, tokensOut));
		}
		return BASIC.createTuple(terms);
	}

	public IAtom parseAtom(PAtom parsed) throws ParserException {
		return parseAtom(parsed, null, new LinkedList<Token>());
	}

	public IAtom parseAtom(PAtom parsed, TermProcessor termFn, List<Token> tokensOut) throws ParserException {
		if (parsed instanceof ANormalAtom) {
			ANormalAtom atom = (ANormalAtom) parsed;

			String name = atom.getName().getText();
			tokensOut.add(atom.getName());

			ITuple terms = parseTerms((ATerms) atom.getTerms(), termFn, tokensOut);

			Class<?> builtinClass = builtinRegister.getBuiltinClass(name);
			if (builtinClass == null) {
				IPredicate predicate = BASIC.createPredicate(name, terms.size());

				requireDeclared(atom.getName(), predicate);

				return BASIC.createAtom(predicate, terms);
			} else {
				try {
					return (IAtom) builtinClass.getConstructor(ITerm[].class).
						newInstance(new Object[] {terms.toArray(new ITerm[terms.size()])});
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		} else if (parsed instanceof ANullaryAtom) {
			ANullaryAtom atom = (ANullaryAtom) parsed;

			tokensOut.add(atom.getName());

			String name = atom.getName().getText();
			IPredicate predicate = BASIC.createPredicate(name, 0);

			requireDeclared(atom.getName(), predicate);

			return BASIC.createAtom(predicate, BASIC.createTuple());
		} else {
			ABuiltinAtom atom = (ABuiltinAtom) parsed;
			PBinop op = atom.getBinop();

			if (op instanceof AEqBinop) {
				tokensOut.add(((AEqBinop) op).getEq());
				ITerm lhs = parseTerm(atom.getLhs(), termFn, tokensOut);
				ITerm rhs = parseTerm(atom.getRhs(), termFn, tokensOut);
				return BUILTIN.createEqual(lhs, rhs);
			} else if (op instanceof ANeqBinop) {
				tokensOut.add(((ANeqBinop) op).getNeq());
				ITerm lhs = parseTerm(atom.getLhs(), termFn, tokensOut);
				ITerm rhs = parseTerm(atom.getRhs(), termFn, tokensOut);
				return BUILTIN.createUnequal(lhs, rhs);
			} else {
				throw new RuntimeException("Unknown binary op " + op);
			}
		}
	}

	private ILiteral parseLiteral(PLiteral parsed, TermProcessor termFn, List<Token> tokensOut) throws ParserException {
		if (parsed instanceof APositiveLiteral) {
			return BASIC.createLiteral(true, parseAtom(((APositiveLiteral) parsed).getAtom(), termFn, tokensOut));
		} else {
			return BASIC.createLiteral(false, parseAtom(((ANegativeLiteral) parsed).getAtom(), termFn, tokensOut));
		}
	}

	/* Stores a suitable List<Token> for each parsed literal in tokensOut. */
	public List<ILiteral> parseLiterals(ALiterals parsed, TermProcessor termFn, List<List<Token>> tokensOut) throws ParserException {
		List<ILiteral> literals = new LinkedList<ILiteral>();

		List<Token> tokens = new LinkedList<Token>();
		tokensOut.add(tokens);
		literals.add(parseLiteral(parsed.getLiteral(), termFn, tokens));

		for (PLiteralTail tail : parsed.getLiteralTail()) {
			tokens = new LinkedList<Token>();
			tokensOut.add(tokens);
			literals.add(parseLiteral(((ALiteralTail) tail).getLiteral(), termFn, tokens));
		}
		return literals;
	}

	public void addRule(IRule rule) throws ParserException {
		addRule(rule, null);
	}

	public void addRule(IRule rule, List<List<Token>> tokens) throws ParserException {
		TypeChecker checker = new TypeChecker(this);
		try {
			List<ILiteral> head = rule.getHead();
			List<ILiteral> body = rule.getBody();

			if (tokens != null && tokens.size() != head.size() + body.size()) {
				throw new RuntimeException("Wrong number of tokens:\n" + tokens + "\n" + head + " :- " + body);
			}

			int i = head.size();

			for (ILiteral lit : body) {
				checker.check(lit, false, tokens == null ? null : tokens.get(i));
				i++;
			}

			i = 0;
			for (ILiteral lit : head) {
				checker.check(lit, true, tokens == null ? null : tokens.get(i));
				i++;
			}

			rules.add(rule);
		} catch (ParserException ex) {
			if (ex.getToken() == null) {
				throw new ParserException(null, ex.getMessage() + " in " + rule);
			} else {
				throw ex;
			}
		}
	}

	public void validateQuery(IQuery query, List<List<Token>> tokens) throws ParserException {
		List<ILiteral> body = query.getLiterals();
		Iterator<List<Token>> tokit = tokens.iterator();
		TypeChecker checker = new TypeChecker(this);

		for (ILiteral lit : body) {
			checker.check(lit, false, tokit.next());
		}
	}

	public void addFact(IPredicate pred, ITuple tuple) throws ParserException {
		addFact(pred, tuple, null);
	}

	public void addFact(IPredicate pred, ITuple tuple, List<Token> tokens) throws ParserException {
		ILiteral lit = BASIC.createLiteral(true, pred, tuple);
		TypeChecker checker = new TypeChecker(this);
		checker.check(lit, true, tokens);

		IRelation rel = getRelation(pred);
		rel.add(tuple);
	}

	public List<IRule> getRules() {
		return rules;
	}

	public Map<IPredicate,IRelation> copyFacts() {
		Map<IPredicate,IRelation> facts = new HashMap<IPredicate,IRelation>();
		for (Map.Entry<IPredicate,IRelation> entry: this.facts.entrySet()) {
			IRelation copy = configuration.relationFactory.createRelation();
			copy.addAll(entry.getValue());
			facts.put(entry.getKey(), copy);
		}
		return facts;
	}

	public int nextAssertion() {
		assertions += 1;
		return assertions;
	}
}
