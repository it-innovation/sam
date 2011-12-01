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
//	Created Date :			2011-12-01
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis.sam;

import eu.serscis.sam.parser.ParserException;
import eu.serscis.sam.node.ATermDecl;
import eu.serscis.sam.node.PTermDecl;
import java.util.List;
import java.util.ArrayList;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import static org.deri.iris.factory.Factory.*;

public class TermDefinition {
	public final ATermDecl decl;
	public final String name;

	public TermDefinition(PTermDecl parsed) throws ParserException {
		decl = ((ATermDecl) parsed);
		this.name = decl.getName().getText();
	}

	public static ITuple makeTuple(TermDefinition[] terms) {
		List<ITerm> list = new ArrayList<ITerm>(terms.length);
		for (int i = 0; i < terms.length; i++) {
			list.add(TERM.createVariable(terms[i].name));
		}
		return BASIC.createTuple(list);
	}
}
