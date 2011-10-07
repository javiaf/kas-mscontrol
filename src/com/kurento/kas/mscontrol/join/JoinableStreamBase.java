/*
 * Kurento Android MSControl: MSControl implementation for Android.
 * Copyright (C) 2011  Tikal Technologies
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kurento.kas.mscontrol.join;

import com.kurento.commons.mscontrol.join.JoinableContainer;
import com.kurento.commons.mscontrol.join.JoinableStream;

public abstract class JoinableStreamBase extends JoinableImpl implements JoinableStream {

	private JoinableContainer container;
	private StreamType type;
	
	protected JoinableStreamBase(JoinableContainer container, StreamType type) {
		this.container = container;
		this.type = type;
	}
	
	@Override
	public JoinableContainer getContainer() {
		return this.container;
	}

	@Override
	public StreamType getType() {
		return this.type;
	}

}
