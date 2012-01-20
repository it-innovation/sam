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

package eu.serscis.sam.gui;

import eu.serscis.sam.ScenarioResult;
import eu.serscis.sam.Results;
import java.util.LinkedList;
import java.util.List;

public class LiveResults {
	private Results myResults;
	private List<Updatable> myListeners;
	private String selectedScenario = "baseline";

	public void update(Results results) {
		myResults = results;

		List<Updatable> oldListeners = myListeners;
		myListeners = new LinkedList<Updatable>();

		if (oldListeners != null) {
			for (Updatable listener : oldListeners) {
				try {
					listener.update();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/* Call listener.update() on the next update.
	 * (if you want to get further updates, subscribe again in update())
	 */
	public void whenUpdated(Updatable listener) {
		myListeners.add(listener);
	}

	public ScenarioResult getResults() throws Exception {
		if (myResults.exception != null) {
			throw myResults.exception;
		}
		return myResults.scenarios.get(selectedScenario);
	}

	public void selectScenario(String scenario) {
		if (selectedScenario.equals(scenario)) {
			return;
		}
		selectedScenario = scenario;
		update(myResults);
	}
}
