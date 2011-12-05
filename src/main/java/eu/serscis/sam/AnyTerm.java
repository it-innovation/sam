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
//	Created Date :			2011-12-02
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis.sam;

import eu.serscis.sam.parser.ParserException;
import eu.serscis.sam.node.TName;
import java.net.URI;

import org.deri.iris.api.terms.IConcreteTerm;
import org.deri.iris.api.terms.ITerm;

public class AnyTerm implements ITerm {
	public static final AnyTerm ANY_INT = new AnyTerm(Type.intT);

	public final Type type;

	public AnyTerm(Type type) {
		this.type = type;
	}

	public String getValue() {
		String s = type.toString();
		s = s.substring(0, s.length() - 1);
		return "any("  + s + ")";
	}

	public boolean isGround() {
		return true;
	}

	public int compareTo(ITerm o) {
		if (o instanceof AnyTerm) {
			return type.compareTo(((AnyTerm) o).type);
		}
		return 0;
	}

	public int hashCode() {
		return type.hashCode();
	}

	public boolean equals(final Object o) {
		return (o instanceof AnyTerm) && compareTo((ITerm) o) == 0;
	}

	public String toString() {
		return getValue();
	}

	public static AnyTerm valueOf(TName typeStr) throws ParserException {
		try {
			return new AnyTerm(Type.fromJavaName(typeStr.getText()));
		} catch (IllegalArgumentException ex) {
			throw new ParserException(typeStr, "Invalid type '" + typeStr + "'");
		}
	}
}
