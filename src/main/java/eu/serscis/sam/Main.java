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
		if (args.length < 1) {
			new GUI(null);
			return;
		}

		if (args[0].equals("--batch")) {
			Eval eval = new Eval();

			for (int i = 1; i < args.length; i++) {
				String arg = args[i];
				File scenario = new File(arg);
				Results results = eval.evaluate(scenario);
				if (results.exception != null) {
					System.out.println(results.exception);
					System.exit(1);
				}
				if (results.finalKnowledgeBase != null) {
					String stem = scenario.getName();
					if (stem.endsWith(".sam")) {
						stem = stem.substring(0, stem.length() - 4);
					}
					Graph.graph(results.finalKnowledgeBase, new File(stem + ".png"));
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
			}
		} else {
			if (args.length != 1) {
				throw new RuntimeException("only --batch mode is currently supported");
			}
			new GUI(new File(args[0]));
		}
	}
}
