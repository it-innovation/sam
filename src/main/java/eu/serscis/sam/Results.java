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
//	Created Date :			2011-08-17
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis.sam;

import java.util.HashMap;
import java.util.Map;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Arrays;
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

public class Results {
	public Model model;
	public Exception exception;
	public final Map<String,ScenarioResult> scenarios = new HashMap<String,ScenarioResult>();
	
	public Results(Model model) {
		this.model = model;
	}

	public void setException(Exception ex) {
		if (exception != null) {
			throw new RuntimeException("Already have an exception", ex);
		}
		this.exception = ex;
	}

	public void save(File file) throws Exception {
		Map<IPredicate,IRelation> baseline = null;
		Writer writer = new FileWriter(file);
		try {
			for (String scenario : model.scenarios) {
				writer.write("== " + scenario + " ==\n\n");
				ScenarioResult result = scenarios.get(scenario);
				if (baseline == null) {
					baseline = result.saveBaseline(writer);
				} else {
					result.saveDiff(writer, baseline);
				}
			}
		} finally {
			writer.close();
		}
	}

	public ScenarioResult createScenarioResult(String scenario) {
		if (scenarios.containsKey(scenario)) {
			throw new IllegalArgumentException("Scenario '" + scenario + "' already evaluated");
		}
		ScenarioResult result = new ScenarioResult(this.model);
		scenarios.put(scenario, result);
		return result;
	}
}
