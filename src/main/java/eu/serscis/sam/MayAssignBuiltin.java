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

import org.deri.iris.api.terms.concrete.IBooleanTerm;
import org.deri.iris.api.terms.concrete.IIntegerTerm;
import org.deri.iris.api.terms.IStringTerm;
import eu.serscis.sam.RefTerm;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.builtins.BooleanBuiltin;
import static org.deri.iris.factory.Factory.*;

public class MayAssignBuiltin extends BooleanBuiltin {
	private static final String PREDICATE_STRING = "MAY_ASSIGN";
	private static final IPredicate PREDICATE = BASIC.createPredicate(PREDICATE_STRING, -1);

	public MayAssignBuiltin(ITerm... terms) {
		super(BASIC.createPredicate(PREDICATE_STRING, terms.length), terms);
		
		if (terms.length != 2) {
			throw new IllegalArgumentException("Must have exactly two terms");
		}
	}

	protected boolean computeResult(ITerm[] terms) {
		return mayAssign(terms[0], terms[1]);
	}
	
	public static boolean mayAssign(ITerm type, ITerm value) {
		if (type instanceof IStringTerm) {
			String typeName = type.getValue().toString();
			if (typeName.equals("Object")) {
				return true;
			}
			if (typeName.equals("String") || typeName.equals("Identity")) {
				return value instanceof IStringTerm;
			}
			if (typeName.equals("int")) {
				return value instanceof IIntegerTerm;
			}
			if (typeName.equals("boolean")) {
				return value instanceof IBooleanTerm;
			}
			if (typeName.equals("Value")) {
				return !(value instanceof RefTerm);
			}
			// Otherwise, we specified a user-defined type, which
			// must be a reference.
			return value instanceof RefTerm;
		} else {
			return false;
		}
	}
}
