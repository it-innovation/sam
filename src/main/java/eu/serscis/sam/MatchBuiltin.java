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

import org.deri.iris.builtins.BooleanBuiltin;
import static org.deri.iris.factory.Factory.BASIC;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.builtins.FunctionalBuiltin;
import org.deri.iris.builtins.datatype.ToStringBuiltin;
import org.deri.iris.factory.Factory;

public class MatchBuiltin extends FunctionalBuiltin {

	private static final String PREDICATE_STRING = "MATCH_TO";
	private static final IPredicate PREDICATE = Constants.MATCH_TOP;

	public MatchBuiltin(ITerm... terms) {
		super(BASIC.createPredicate(PREDICATE_STRING, terms.length), terms);
		
		if (terms.length != 3) {
			throw new IllegalArgumentException("The amount of terms <" + terms.length + "> must be 3");
		}
	}

	protected ITerm computeResult(ITerm[] terms) throws EvaluationException {
		if (terms.length != 3) {
			throw new IllegalArgumentException("The amount of terms <" + terms.length + "> must be 3");
		}
		return MatchBuiltin.computeResult(terms[0], terms[1]);
	}

	private static ITerm computeResult(ITerm first, ITerm second) throws EvaluationException {
		ITerm result;

		/* If both terms are not any(), they match only if they are equal.
		 * Otherwise, check that the types match.
		 */
		if (first instanceof AnyTerm) {
			result = matchAny((AnyTerm) first, second);
		} else if (second instanceof AnyTerm) {
			result = matchAny((AnyTerm) second, first);
		} else {
			if (first.equals(second)) {
				result = first;
			} else {
				result = null;
			}
		}

		return result;
	}

	private static ITerm matchAny(AnyTerm any, ITerm other) {
		if (other instanceof AnyTerm) {
			Type intersection = any.type.intersect(((AnyTerm) other).type);
			if (intersection == null) {
				return null;			// e.g. !MATCH(any(int), any(bool), ?Result)
			} else {
				return new AnyTerm(intersection); // e.g. MATCH(any(int), any(Value), any(Value))
			}
		}

		Type otherType = Type.fromTerm(other);
		if (any.type.intersect(otherType) != null) {	// e.g. MATCH(any(int), 3, 3)
			return other;
		}

		return null;					// e.g. !MATCH(any(int), "hi", ?Result)
	}

	/* Two-argument form that just tests whether a match exists. */
	public static class MatchBuiltinBoolean extends BooleanBuiltin {
		private static final IPredicate PREDICATE = Constants.MATCH2P;

		public MatchBuiltinBoolean(final ITerm... t) {
			super(PREDICATE, t);
		}

		public boolean computeResult(ITerm[] terms) {
			try {
				return MatchBuiltin.computeResult(terms[0], terms[1]) != null;
			} catch (EvaluationException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
}
