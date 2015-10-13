package com.stripe.net;

import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.net.LiveStripeResponseGetter;
import com.stripe.net.RequestOptions;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class LiveStripeResponseGetterTest {
	LiveStripeResponseGetter srg;

	@Before
	public void before() {
		srg = new LiveStripeResponseGetter();
	}

	/* Kind of hacky, but makes tests readable */
	public String encode(String s) throws UnsupportedEncodingException {
		return s.replace("[", "%5B").replace("]", "%5D");
	}

	@Test
	public void testCreateQuery() throws StripeException, UnsupportedEncodingException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("a", "b");
		assertEquals("a=b", srg.createQuery(params));
	}

	@Test
	public void testCreateQueryWithNestedParams() throws StripeException, UnsupportedEncodingException {
		/* Use LinkedHashMap because it preserves iteration order */
		Map<String, Object> params = new LinkedHashMap<String, Object>();
		Map<String, Object> nested = new LinkedHashMap<String, Object>();
		nested.put("A", "B");
		nested.put("C", "D");
		params.put("nested", nested);
		params.put("c", "d");
		params.put("e", "f");
		assertEquals(encode("nested[A]=B&nested[C]=D&c=d&e=f"), srg.createQuery(params));
	}

	@Test
	public void testCreateQueryWithListParams() throws StripeException, UnsupportedEncodingException {

		List<String> nested = new LinkedList<String>();
		nested.add("A");
		nested.add("B");
		nested.add("C");

		/* Use LinkedHashMap because it preserves iteration order */
		Map<String, Object> params = new LinkedHashMap<String, Object>();
		params.put("nested", nested);
		params.put("a", "b");
		params.put("c", "d");

		assertEquals(encode("nested[]=A&nested[]=B&nested[]=C&a=b&c=d"), srg.createQuery(params));
	}

    // It's really arrays of hashes where things in
    // application/x-www-urlencoded-form start to get *really* hairy, so let's
    // have a special test to cover thsi type of case.
	@Test
	public void testCreateQueryWithComplexParams() throws StripeException, UnsupportedEncodingException {
        List<String> deepNestedList = new LinkedList<String>();
		deepNestedList.add("A");
		deepNestedList.add("B");

        Map<String, String> deepNestedMap1 = new LinkedHashMap<String, String>();
		deepNestedMap1.put("C", "C-1");
		deepNestedMap1.put("D", "D-1");

        Map<String, String> deepNestedMap2 = new LinkedHashMap<String, String>();
		deepNestedMap2.put("C", "C-2");
		deepNestedMap2.put("D", "D-2");

		List<Object> nested = new LinkedList<Object>();
        nested.add(deepNestedList);
        nested.add(deepNestedMap1);
        nested.add(deepNestedMap2);

		/* Use LinkedHashMap because it preserves iteration order */
		Map<String, Object> params = new LinkedHashMap<String, Object>();
		params.put("nested", nested);

		assertEquals(encode("nested[][]=A&nested[][]=B&nested[][C]=C-1&nested[][D]=D-1&nested[][C]=C-2&nested[][D]=D-2"), srg.createQuery(params));
	}
}

