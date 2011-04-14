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

import eu.serscis.sam.parser.ParserException;
import java.io.BufferedReader;
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
	public List<IQuery> queries = new LinkedList<IQuery>();
	private Model model;
	private BuiltinRegister builtinRegister = new BuiltinRegister();
	private File dir;

	public SAMParser(Model model, File path) throws Exception {
		this.model = model;
		dir = path.getParentFile();

		FileReader reader = new FileReader(path);
		try {
			parse(reader);
		} catch (eu.serscis.sam.parser.ParserException ex) {
			Token t = ex.getToken();
			System.out.println("\nParsing error: " + ex.getMessage());
			System.out.println(getLine(path, t.getLine()));
			String spaces = "";
			for (int i = t.getPos(); i > 1; i--) {
				spaces += " ";
			}
			System.out.println(spaces + "^");
			System.out.println(path + ":" + t.getLine());
			System.exit(1);
		} finally {
			reader.close();
		}
	}

	public SAMParser(Model model, Reader source) throws Exception {
		this.model = model;
		parse(source);
	}

	private void parse(Reader source) throws Exception {
		Map<String,SAMClass> classDefinitions = new HashMap<String,SAMClass>();

		PushbackReader pbr = new PushbackReader(source);
		Lexer lexer = new Lexer(pbr);
		Parser parser = new Parser(lexer);
		Start start = parser.parse();

		for (PToplevel toplevel : ((AProgram) start.getPProgram()).getToplevel()) {
			//System.out.println("Processing " + toplevel);

			if (toplevel instanceof ABehaviourToplevel) {
				SAMClass klass = new SAMClass(model, (ABehaviour) ((ABehaviourToplevel) toplevel).getBehaviour());
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
			} else if (toplevel instanceof ADeclareToplevel) {
				addDeclare((ADeclare) ((ADeclareToplevel) toplevel).getDeclare());
			} else if (toplevel instanceof AImportToplevel) {
				addImport((AImport) ((AImportToplevel) toplevel).getImport());
			} else {
				throw new RuntimeException("UNKNOWN " + toplevel + ", " + toplevel.getClass());
			}
		}

		for (SAMClass klass : classDefinitions.values()) {
			klass.addDatalog();
		}
	}

	public List<IQuery> getQueries() {
		return queries;
	}

	private String getString(TStringLiteral literal) {
		String str = literal.getText();
		return str.substring(1, str.length() - 1);	// TODO: ignores escapes
	}

	private ITerm parseTerm(PTerm parsed) {
		if (parsed instanceof AStringTerm) {
			String str = getString(((AStringTerm) parsed).getStringLiteral());
			return TERM.createString(str);
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

	private IAtom parseAtom(PAtom parsed) throws ParserException {
		return parseAtom(parsed, false);
	}

	private IAtom parseAtom(PAtom parsed, boolean declaration) throws ParserException {
		if (parsed instanceof ANormalAtom) {
			ANormalAtom atom = (ANormalAtom) parsed;
			ITuple terms = parseTerms((ATerms) atom.getTerms());

			String name = atom.getName().getText();

			Class<?> builtinClass = builtinRegister.getBuiltinClass(name);
			if (builtinClass == null) {
				IPredicate predicate = BASIC.createPredicate(name, terms.size());

				if (declaration) {
					declare(predicate);
				} else {
					requireDeclared(atom.getName(), predicate);
				}

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

			if (declaration) {
				declare(predicate);
			} else {
				requireDeclared(atom.getName(), predicate);
			}

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

	private void addFact(AFact fact) throws ParserException {
		IAtom atom = parseAtom(fact.getAtom());

		//System.out.println(" --> " + atom);

		IRelation rel = model.getRelation(atom.getPredicate());
		rel.add(atom.getTuple());
	}

	private ILiteral parseLiteral(PLiteral parsed) throws ParserException {
		if (parsed instanceof APositiveLiteral) {
			return BASIC.createLiteral(true, parseAtom(((APositiveLiteral) parsed).getAtom()));
		} else {
			return BASIC.createLiteral(false, parseAtom(((ANegativeLiteral) parsed).getAtom()));
		}
	}

	private List<ILiteral> parseLiterals(ALiterals parsed) throws ParserException {
		List<ILiteral> literals = new LinkedList<ILiteral>();

		literals.add(parseLiteral(parsed.getLiteral()));

		for (PLiteralTail tail : parsed.getLiteralTail()) {
			literals.add(parseLiteral(((ALiteralTail) tail).getLiteral()));
		}
		return literals;
	}

	private void addRule(ARule rule) throws ParserException {
		ILiteral head = BASIC.createLiteral(true, parseAtom(rule.getHead()));

		List<ILiteral> body = parseLiterals((ALiterals) rule.getBody());

		IRule r = BASIC.createRule(makeList(head), body);
		//System.out.println(" --> " + r);

		model.rules.add(r);
	}

	private void addQuery(AQuery query) throws ParserException {
		List<ILiteral> literals = parseLiterals((ALiterals) query.getLiterals());

		IQuery q = BASIC.createQuery(literals);

		queries.add(q);
	}

	private void addDeclare(ADeclare declare) throws ParserException {
		parseAtom(declare.getAtom(), true);
	}

	private void declare(IPredicate pred) {
		if (model.declared.contains(pred)) {
			throw new RuntimeException("Predicate already declared: " + pred);
		}

		//System.out.println("Declare " + pred + "/" + pred.getArity());
		model.declared.add(pred);
	}

	private void requireDeclared(Token tok, IPredicate pred) throws ParserException {
		if ("error".equals(pred.getPredicateSymbol())) {
			return;
		}

		if (!model.declared.contains(pred)) {
			throw new ParserException(tok, "Predicate not declared: " + pred + "/" + pred.getArity());
		}
	}

	private void addImport(AImport aImport) throws Exception {
		String path = getString(aImport.getStringLiteral());

		new SAMParser(model, new File(dir, path));
	}

	/* Get the nth line of a file. */
	private String getLine(File source, int line) throws Exception {
		 BufferedReader in = new BufferedReader(new FileReader(source));
		 for (int i = 1; i < line; i++) {
			  in.readLine();
		 }
		 return in.readLine();
	}
}
