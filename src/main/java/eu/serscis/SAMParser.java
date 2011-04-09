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

public class SAMParser {
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
			int i;
			if (sam.startsWith("class ")) {
				i = -1;
			} else {
				i = sam.indexOf("\nclass ");
				if (i == -1) {
					datalog += sam;
					break;
				}
			}
			int j = sam.indexOf("\n}", i);
			if (j == -1) {
				throw new RuntimeException("Missing final } for class: " + sam);
			}

			String javaCode = sam.substring(i + 1, j + 2);
			//System.out.println("Java chunk:\n" + javaCode);

			SAMClass klass = new SAMClass(configuration, javaCode);
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
}
