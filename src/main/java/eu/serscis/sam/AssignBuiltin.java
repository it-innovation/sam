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

import org.deri.iris.builtins.FunctionalBuiltin;
import org.deri.iris.api.terms.concrete.IBooleanTerm;
import org.deri.iris.api.terms.concrete.IIntegerTerm;
import org.deri.iris.api.terms.IStringTerm;
import eu.serscis.sam.RefTerm;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.terms.ITerm;
import static org.deri.iris.factory.Factory.*;

public class AssignBuiltin extends FunctionalBuiltin {
	private static final String PREDICATE_STRING = "ASSIGN";
	private static final IPredicate PREDICATE = BASIC.createPredicate(PREDICATE_STRING, -1);

	public AssignBuiltin(ITerm... terms) {
		super(BASIC.createPredicate(PREDICATE_STRING, terms.length), terms);
		
		if (terms.length != 3) {
			throw new IllegalArgumentException("Must have exactly three terms");
		}
	}

	protected ITerm computeResult(ITerm[] terms) {
		return mayAssign(terms[0], terms[1]);
	}
	
	public static ITerm mayAssign(ITerm type, ITerm value) {
		if (type instanceof IStringTerm) {
			String typeName = type.getValue().toString();
			Type requiredType = Type.fromJavaName(typeName);
			Type valueType = Type.fromTerm(value);
			Type intersection = requiredType.intersect(valueType);

			//System.out.println("Assign " + type + " = " + value + " -> " + intersection);
			//System.out.println("" + requiredType + ".intersect(" + valueType + ") = " + intersection);

			if (intersection == null) {
				return null;			// e.g. !ASSIGN("String", 3, ?Result)
			}

			if (value instanceof AnyTerm) {
				return new AnyTerm(intersection);	// e.g. ASSIGN("String", any(Value), any(String))
			}

			return value;				// e.g. ASSIGN("String", "hi", "hi").
		} else {
			return null;
		}
	}
}
