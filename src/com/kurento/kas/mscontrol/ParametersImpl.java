package com.kurento.kas.mscontrol;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.kurento.commons.mscontrol.Parameter;
import com.kurento.commons.mscontrol.Parameters;

/**
 * 
 * @author mparis
 *
 */
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
