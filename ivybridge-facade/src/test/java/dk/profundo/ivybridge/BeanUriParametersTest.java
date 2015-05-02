/**
 * Copyright © 2015, QIAGEN Aarhus A/S. All rights reserved.
 */
package dk.profundo.ivybridge;

import static org.junit.Assert.assertEquals;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

/**
 * @author emartino
 *
 */
public class BeanUriParametersTest {
	public static class MyBean {
		private Map<String, Object> map;
		private int integerValue;
		private String stringValue;

		public Map<String, Object> getMap() {
			return map;
		}

		public void setMap(Map<String, Object> map) {
			this.map = map;
		}

		public int getIntegerValue() {
			return integerValue;
		}

		public void setIntegerValue(int integerValue) {
			this.integerValue = integerValue;
		}

		public String getStringValue() {
			return stringValue;
		}

		public void setStringValue(String stringValue) {
			this.stringValue = stringValue;
		}
	}

	@Test
	public void testFromParameters() throws IntrospectionException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		Map<String, String> p = new TreeMap<>();
		p.put("integerValue", "42");
		p.put("stringValue", "hej");
		p.put("map.foo", "bar");

		MyBean bean = new MyBean();
		BeanUriParameters bps = new BeanUriParameters(bean);

		bps.fromParameters(p);

		assertEquals(42, bean.getIntegerValue());
		assertEquals("hej", bean.getStringValue());
		assertEquals("bar", bean.getMap().get("foo"));
	}

	@Test
	public void testToParameters() throws IntrospectionException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		MyBean bean = new MyBean();
		BeanUriParameters bps = new BeanUriParameters(bean);
		bean.setIntegerValue(42);
		bean.setStringValue("hej");
		TreeMap<String, Object> map = new TreeMap<>(
				Collections.<String, Object> singletonMap("foo", "bar"));
		map.put("baz", null);
		bean.setMap(map);
		Map<String, String> parameters = bps.toParameters();
		assertEquals("42", parameters.get("integerValue"));
		assertEquals("hej", parameters.get("stringValue"));
		assertEquals("bar", parameters.get("map.foo"));
		assertEquals(3, parameters.size());
	}

}
