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

import eu.serscis.sam.node.AType;
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
	public final Type type;

	public TermDefinition(Type type) {
		this.type = type;
		decl = null;
		name = null;
	}

	public TermDefinition(PTermDecl parsed) throws ParserException {
		decl = ((ATermDecl) parsed);
		this.name = decl.getName().getText();

		AType type = (AType) decl.getType();
		if (type == null) {
			this.type = Type.ObjectT;
		} else {
			String typeName = type.getName().getText();
			try {
				this.type = Type.fromJavaName(typeName);
			} catch (IllegalArgumentException ex) {
				throw new ParserException(type.getName(), "Bad type: " + typeName);
			}
		}
	}

	public static ITuple makeTuple(TermDefinition[] terms) {
		List<ITerm> list = new ArrayList<ITerm>(terms.length);
		for (int i = 0; i < terms.length; i++) {
			list.add(TERM.createVariable(terms[i].name));
		}
		return BASIC.createTuple(list);
	}

	public Type checkType(Type type, boolean head) throws ParserException {
		if (!type.equals(this.type)) {
			if (head) {
				if (this.type.equals(Type.ObjectT)) {
					return type;
				}
			} else {
				/* The possible types are the intersection of the two. If the intersection is empty,
				 * it's probably an error.
				 */
				Type intersection = type.intersect(this.type);
				if (intersection != null) {
					return intersection;
				}
			}

			AType aType = decl == null ? null : (AType) decl.getType();
			throw new ParserException(aType == null ? null : aType.getName(),
					"Wrong type:\n" +
					"declared=" + this.type.toJavaName() + "\n" +
					"actual=" + type.toJavaName());
		}
		return type;
	}
}
