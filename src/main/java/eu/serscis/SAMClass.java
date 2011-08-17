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
import org.deri.iris.storage.IRelation;
import static org.deri.iris.factory.Factory.*;
import static eu.serscis.Constants.*;

abstract class SAMClass {
	public String name;
	Set<String> fields = new HashSet<String>();
	Model model;

	public SAMClass(Model model, String name) {
		this.model = model;
		this.name = name;
	}

	public void addDatalog() throws Exception {
		IRelation definedType = model.getRelation(definedTypeP);
		definedType.add(BASIC.createTuple(TERM.createString(name)));
	}

	public void declareField(TName aName) {
		String fieldName = aName.getText();
		if (fields.contains(fieldName)) {
			throw new RuntimeException("Duplicate definition of field " + fieldName);
		} else {
			fields.add(fieldName);
			IRelation rel = model.getRelation(hasFieldP);
			rel.add(BASIC.createTuple(TERM.createString(this.name), TERM.createString(fieldName)));
		}
	}
}
