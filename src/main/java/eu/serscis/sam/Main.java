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

import eu.serscis.sam.gui.GUI;
import java.io.File;

public class Main {
	public static void main(String[] args) throws Exception {
		boolean batch = false;
		File resultsDir = null;

		int i = 0;
		while (i < args.length && args[i].startsWith("--")) {
			String opt = args[i].substring(2);
			if (opt.equals("batch")) {
				batch = true;
			} else if (opt.equals("results-dir")) {
				resultsDir = new File(args[i + 1]);
				i++;
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
				File scenario = new File(arg);
				Results results = eval.evaluate(scenario);
				if (results.exception != null) {
					if (results.exception instanceof RuntimeException) {
						results.exception.printStackTrace();
					} else {
						//results.exception.printStackTrace();
						System.out.println(results.exception);
					}
					System.exit(1);
				}
				if (results.finalKnowledgeBase != null) {
					String stem = scenario.getName();
					if (stem.endsWith(".sam")) {
						stem = stem.substring(0, stem.length() - 4);
					}
					Graph.graph(results.finalKnowledgeBase, new File(stem + ".png"));

					if (resultsDir != null) {
						File resultsFile = new File(resultsDir, stem + ".results");
						results.save(resultsFile);
					}
				}
				if (results.phase != Results.Phase.Success) {
					if (results.expectingFailure) {
						System.out.println(scenario + ": OK (failed, as expected)");
					} else {
						System.out.println(scenario + ": FAILED");
						System.exit(1);
					}
				} else {
					if (results.expectingFailure) {
						System.out.println(scenario + ": Expecting model to fail ('expectFailure' is set), but passed!");
						System.exit(1);

					}
					System.out.println(scenario + ": OK");
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
}
