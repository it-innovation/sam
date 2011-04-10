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
import org.deri.iris.api.basics.IAtom;
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

public class SAMParser {
	public Map<IPredicate,IRelation> facts;
	public List<IRule> rules;
	public List<IQuery> queries;
	private Configuration configuration;
	private BuiltinRegister builtinRegister = new BuiltinRegister();

	public SAMParser(Configuration configuration) {
		this.configuration = configuration;
	}

	public void parse(Reader source) throws Exception {
		facts = new HashMap<IPredicate,IRelation>();
		rules = new LinkedList<IRule>();
		queries = new LinkedList<IQuery>();

		Map<String,SAMClass> classDefinitions = new HashMap<String,SAMClass>();

		PushbackReader pbr = new PushbackReader(source);
		Lexer lexer = new Lexer(pbr);
		Parser parser = new Parser(lexer);
		Start start = parser.parse();

		for (PToplevel toplevel : ((AProgram) start.getPProgram()).getToplevel()) {
			//System.out.println("Processing " + toplevel);

			if (toplevel instanceof ABehaviourToplevel) {
				SAMClass klass = new SAMClass(configuration, (ABehaviour) ((ABehaviourToplevel) toplevel).getBehaviour());
				if (classDefinitions.containsKey(klass.name)) {
					throw new RuntimeException("Duplicate class definition: " + klass.name);
				}
				classDefinitions.put(klass.name, klass);
			} else if (toplevel instanceof AFactToplevel) {
				addFact((AFact) ((AFactToplevel) toplevel).getFact());
			} else if (toplevel instanceof ARuleToplevel) {
				addRule((ARule) ((ARuleToplevel) toplevel).getRule());
			} else if (toplevel instanceof AQueryToplevel) {
				addQuery((AQuery) ((AQueryToplevel) toplevel).getQuery());
			} else {
				throw new RuntimeException("UNKNOWN " + toplevel + ", " + toplevel.getClass());
			}
		}

		for (SAMClass klass : classDefinitions.values()) {
			klass.addDatalog(facts, rules);
		}
	}

	public List<IQuery> getQueries() {
		return queries;
	}

	private ITerm parseTerm(PTerm parsed) {
		if (parsed instanceof AStringTerm) {
			String str = ((AStringTerm) parsed).getStringLiteral().getText();
			return TERM.createString(str.substring(1, str.length() - 1));	// TODO: ignores escapes
		} else if (parsed instanceof AVarTerm) {
			String name = ((AVarTerm) parsed).getName().getText();
			return TERM.createVariable(name);
		} else {
			throw new RuntimeException("Unknown term type:" + parsed);
		}
	}

	private ITuple parseTerms(ATerms parsed) {
		List<ITerm> terms = new LinkedList<ITerm>();

		terms.add(parseTerm(parsed.getTerm()));

		for (PTermTail tail : parsed.getTermTail()) {
			terms.add(parseTerm(((ATermTail) tail).getTerm()));
		}
		return BASIC.createTuple(terms);
	}

	private IAtom parseAtom(PAtom parsed) {
		if (parsed instanceof ANormalAtom) {
			ANormalAtom atom = (ANormalAtom) parsed;
			ITuple terms = parseTerms((ATerms) atom.getTerms());

			String name = atom.getName().getText();

			Class<?> builtinClass = builtinRegister.getBuiltinClass(name);
			if (builtinClass == null) {
				IPredicate predicate = BASIC.createPredicate(name, terms.size());

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

			String name = atom.getName().getText();
			IPredicate predicate = BASIC.createPredicate(name, 0);

			return BASIC.createAtom(predicate, BASIC.createTuple());
		} else {
			ABuiltinAtom atom = (ABuiltinAtom) parsed;
			String op = atom.getBinop().getText();

			if ("=".equals(op)) {
				return BUILTIN.createEqual(parseTerm(atom.getLhs()), parseTerm(atom.getRhs()));
			} else if ("!=".equals(op)) {
				return BUILTIN.createUnequal(parseTerm(atom.getLhs()), parseTerm(atom.getRhs()));
			} else {
				throw new RuntimeException("Unknown binary op " + op);
			}
		}
	}

	private void addFact(AFact fact) {
		IAtom atom = parseAtom(fact.getAtom());

		//System.out.println(" --> " + atom);

		IRelation rel = getRelation(atom.getPredicate());
		rel.add(atom.getTuple());
	}

	private ILiteral parseLiteral(PLiteral parsed) {
		if (parsed instanceof APositiveLiteral) {
			return BASIC.createLiteral(true, parseAtom(((APositiveLiteral) parsed).getAtom()));
		} else {
			return BASIC.createLiteral(false, parseAtom(((ANegativeLiteral) parsed).getAtom()));
		}
	}

	private List<ILiteral> parseLiterals(ALiterals parsed) {
		List<ILiteral> literals = new LinkedList<ILiteral>();

		literals.add(parseLiteral(parsed.getLiteral()));

		for (PLiteralTail tail : parsed.getLiteralTail()) {
			literals.add(parseLiteral(((ALiteralTail) tail).getLiteral()));
		}
		return literals;
	}

	private void addRule(ARule rule) {
		ILiteral head = BASIC.createLiteral(true, parseAtom(rule.getHead()));

		List<ILiteral> body = parseLiterals((ALiterals) rule.getBody());

		IRule r = BASIC.createRule(makeList(head), body);
		//System.out.println(" --> " + r);

		rules.add(r);
	}

	private void addQuery(AQuery query) {
		List<ILiteral> literals = parseLiterals((ALiterals) query.getLiterals());

		IQuery q = BASIC.createQuery(literals);

		queries.add(q);
	}

	IRelation getRelation(IPredicate pred) {
		IRelation rel = facts.get(pred);
		if (rel == null) {
			rel = configuration.relationFactory.createRelation();
			facts.put(pred, rel);
		}
		return rel;
	}
}
