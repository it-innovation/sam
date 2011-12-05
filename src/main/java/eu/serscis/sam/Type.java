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
import eu.serscis.sam.node.TName;
import eu.serscis.sam.AnyTerm;
import org.deri.iris.api.terms.concrete.IBooleanTerm;
import org.deri.iris.api.terms.concrete.IIntegerTerm;
import eu.serscis.sam.RefTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IStringTerm;
import eu.serscis.sam.node.ATermDecl;
import eu.serscis.sam.node.PTermDecl;
import java.util.List;
import java.util.ArrayList;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.ITerm;
import static org.deri.iris.factory.Factory.*;

public enum Type {
	ObjectT, RefT, StringT, intT, booleanT, ValueT;

	static public Type fromJavaName(String javaName) {
		return valueOf(javaName + "T");
	}

	static public Type fromTerm(ITerm term) {
		if (term instanceof IStringTerm) {
			return StringT;
		} else if (term instanceof RefTerm) {
			return RefT;
		} else if (term instanceof IIntegerTerm) {
			return intT;
		} else if (term instanceof IBooleanTerm) {
			return booleanT;
		} else if (term instanceof AnyTerm) {
			return ((AnyTerm) term).type;
		} else {
			throw new RuntimeException("Unknown term type: " + term);
		}
	}

	public String toJavaName() {
		String s = this.toString();
		return s.substring(0, s.length() - 1);
	}

	public Type intersect(Type other) {
		if (other == null) {
			throw new IllegalArgumentException("other is null");
		}
		if (other == this) {
			return this;
		}
		if (this == ObjectT) {
			return other;
		}
		if (this == ValueT) {
			if (other == ObjectT) {
				return ValueT;
			}
			if (other == RefT) {
				return null;
			}
			return other;
		}
		if (this == RefT) {
			if (other == ObjectT) {
				return RefT;
			}
			// (Ref*Ref handled above)
			return null;
		}
		if (other == ObjectT || other == ValueT || other == RefT) {
			return other.intersect(this);
		}
		return null;
	}

	public static void validateJavaName(TName type) throws ParserException {
		try {
			fromJavaName(type.getText());
		} catch (IllegalArgumentException ex) {
			throw new ParserException(type, "Unknown SAM type '" + type + "' (hint: use Object/Value/Ref)");
		}
	}
}
