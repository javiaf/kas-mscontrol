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

package com.kurento.kas.mscontrol.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.kurento.commons.mscontrol.Parameter;
import com.kurento.commons.mscontrol.Parameters;

public class ParametersImpl implements Parameters {

	Map<Parameter, Object> parameters;

	public ParametersImpl() {
		parameters = new HashMap<Parameter, Object>();
	}

	@Override
	public void clear() {
		parameters.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return parameters.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return parameters.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<Parameter, Object>> entrySet() {
		return parameters.entrySet();
	}

	@Override
	public Object get(Object key) {
		return parameters.get(key);
	}

	@Override
	public boolean isEmpty() {
		return parameters.isEmpty();
	}

	@Override
	public Set<Parameter> keySet() {
		return parameters.keySet();
	}

	@Override
	public Object put(Parameter key, Object value) {
		return parameters.put(key, value);
	}

	@Override
	public void putAll(Map<? extends Parameter, ? extends Object> t) {
		parameters.putAll(t);
	}

	@Override
	public Object remove(Object key) {
		return parameters.remove(key);
	}

	@Override
	public int size() {
		return parameters.size();
	}

	@Override
	public Collection<Object> values() {
		return parameters.values();
	}

	@Override
	public String toString() {
		return this.parameters.toString();
	}

}
