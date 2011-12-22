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
//	Created Date :			2011-12-22
//	Created for Project :		SERSCIS
//
/////////////////////////////////////////////////////////////////////////
//
//  License : GNU Lesser General Public License, version 2.1
//
/////////////////////////////////////////////////////////////////////////

package eu.serscis.sam;

import java.util.Iterator;
import java.util.LinkedList;

public class InvalidModelException extends Exception {
	public final String source;		// File/resource containing the line
	public final String code;
	public final int line;
	public final int col;

	public InvalidModelException(Exception cause, String source, String code, int line, int col) {
		super(cause.getMessage(), cause);
		this.source = source;
		this.code = code;
		this.line = line;
		this.col = col;
	}

	public Iterator<InvalidModelException> getChain() {
		LinkedList<InvalidModelException> chain = new LinkedList<InvalidModelException>();

		Throwable ex = this;
		while (ex != null) {
			chain.add((InvalidModelException) ex);

			Throwable cause = ex.getCause();
			if (cause instanceof InvalidModelException) {
				ex = cause;
			} else {
				break;
			}
		}

		return chain.descendingIterator();
	}
}
