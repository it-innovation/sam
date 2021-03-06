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

import java.util.Iterator;
import java.util.LinkedList;
import eu.serscis.sam.gui.GUI;
import java.io.File;

public class Main {
	public static void main(String[] args) throws Exception {
		boolean batch = false;
		File resultsDir = null;
		String format = "png";

		int i = 0;
		while (i < args.length && args[i].startsWith("-")) {
			String opt = args[i];
			if (opt.equals("--batch")) {
				batch = true;
			} else if (opt.equals("--results-dir")) {
				resultsDir = new File(args[i + 1]);
				i++;
			} else if (opt.startsWith("-T")) {
				format = opt.substring(2);
			} else {
				usageError("Unknown option '" + args[i] + "'");
			}
			i++;
		}

		if (batch) {
			Eval eval = new Eval();

			if (resultsDir != null && !resultsDir.isDirectory()) {
				resultsDir.mkdir();
			}

			while (i < args.length) {
				String arg = args[i];
				File modelFile = new File(arg);
				System.out.println(modelFile);
				Results results = eval.evaluate(modelFile);
				if (results.exception != null) {
					if (results.exception instanceof RuntimeException) {
						results.exception.printStackTrace();
					} else if (results.exception instanceof InvalidModelException) {
						printParserException((InvalidModelException) results.exception);
					} else {
						//results.exception.printStackTrace();
						System.out.println(results.exception);
					}
					System.exit(1);
				}
				String stem = modelFile.getName();
				if (stem.endsWith(".sam")) {
					stem = stem.substring(0, stem.length() - 4);
				}
				if (resultsDir != null) {
					File resultsFile = new File(resultsDir, stem + ".results");
					results.save(resultsFile);
				}
				for (String scenario : results.model.scenarios) {
					ScenarioResult result = results.scenarios.get(scenario);
					if (result.finalKnowledgeBase != null) {
						Graph.graph(result.finalKnowledgeBase, new File(stem + "-" + scenario + "." + format), format);
					}
					if (result.phase != Phase.Success) {
						if (result.expectingFailure) {
							System.out.println(scenario + ": OK (failed, as expected)");
						} else {
							System.out.println(scenario + ": FAILED");
							System.exit(1);
						}
					} else {
						if (result.expectingFailure) {
							System.out.println(scenario + ": Expecting model to fail ('expectFailure' is set), but passed!");
							System.exit(1);

						}
						System.out.println(scenario + ": OK");
					}
				}

				i++;
			}
		} else {
			if (resultsDir != null) {
				usageError("--results-dir only available with --batch");
			}

			if (args.length == 0) {
				new GUI(null);
			} else if (args.length == i + 1) {
				new GUI(new File(args[i]));
			} else {
				usageError("multiple files only supported in --batch mode");
			}
		}
	}

	private static void usageError(String message) {
		System.out.println(message);
		System.out.println("");
		System.out.println("Usage: sam model.sam");
		System.out.println("       sam --batch [--results-dir DIR] model.sam ...");
		System.exit(1);
	}

	private static void printParserException(InvalidModelException ex) {
		boolean first = true;
		Iterator<InvalidModelException> iter = ex.getChain();
		while (iter.hasNext()) {
			InvalidModelException link = iter.next();

			if (first) {
				System.out.println("\nError: " + link.getMessage());
				first = false;
			} else {
				System.out.println("\nImported from here:\n");
			}

			System.out.println(link.code);
			String spaces = "";
			for (int i = link.col; i > 1; i--) {
				spaces += " ";
			}
			System.out.println(spaces + "^");
			System.out.println("" + link.source + ":" + link.line);
		}
	}
}
