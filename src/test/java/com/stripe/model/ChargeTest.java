package com.stripe.model;


import com.google.common.collect.ImmutableMap;
import com.stripe.Stripe;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.net.RequestOptions;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.*;
import com.stripe.BaseStripeTest;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class ChargeTest extends BaseStripeTest {
    static Map<String, Object> defaultChargeParams = new HashMap<String, Object>();
    static Map<String, Object> defaultCardParams = new HashMap<String, Object>();
    static RequestOptions supportedRequestOptions;

    static String getYear() {
        Date date = new Date(); //Get current date
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR) + 1 +"";
    }

    @BeforeClass
    public static void setUp() {
        Stripe.apiKey = "tGN0bIwXnHdwOa85VABjPdSn8nWY7G7I"; // stripe public
        // test key

        supportedRequestOptions = RequestOptions.builder().setStripeVersion("2015-02-16").build();

        defaultCardParams.put("number", "4242424242424242");
        defaultCardParams.put("exp_month", 12);
        defaultCardParams.put("exp_year", getYear());
        defaultCardParams.put("cvc", "123");
        defaultCardParams.put("name", "J Bindings Cardholder");
        defaultCardParams.put("address_line1", "140 2nd Street");
        defaultCardParams.put("address_line2", "4th Floor");
        defaultCardParams.put("address_city", "San Francisco");
        defaultCardParams.put("address_zip", "94105");
        defaultCardParams.put("address_state", "CA");
        defaultCardParams.put("address_country", "USA");

        defaultChargeParams.put("amount", 100);
        defaultChargeParams.put("currency", "usd");
        defaultChargeParams.put("card", defaultCardParams);
    }

    @Test
    public void testChargeCreate() throws StripeException {
        Charge createdCharge = Charge.create(defaultChargeParams);
        assertFalse(createdCharge.getRefunded());
    }

    @Test
    public void testChargeCreateWithStatementDescriptor() throws StripeException {
        Map<String, Object> chargeWithStatementDescriptorParams = new HashMap<String, Object>();
        chargeWithStatementDescriptorParams.putAll(defaultChargeParams);
        chargeWithStatementDescriptorParams.put("description", "hahaha1234");
        chargeWithStatementDescriptorParams.put("statement_descriptor", "Stripe");

        Charge createdCharge = Charge.create(chargeWithStatementDescriptorParams);
        assertEquals("Stripe", createdCharge.getStatementDescriptor());
    }

    @Test
    public void testChargeCreateWithShippingDetails() throws StripeException {
        ShippingDetails shippingDetails = new ShippingDetails();
        shippingDetails.setName("name");
        shippingDetails.setPhone("123-456-7890");
        Address address = new Address()
                .setCity("Washington")
                .setCountry("USA")
                .setLine1("1600 Pennsylvania Ave.")
                .setLine2("line 2 address")
                .setPostalCode("20500")
                .setState("D.C.");
        shippingDetails.setAddress(address);

        Map<String, Object> params = ImmutableMap.<String, Object>builder()
                .putAll(defaultChargeParams)
                .put("shipping", ImmutableMap.builder().put("address", ImmutableMap.builder().put("line1", address.getLine1()).put("line2", address.getLine2()).put("city", address.getCity()).put("country", address.getCountry()).put("postal_code", address.getPostalCode()).put("state", address.getState()).build()).put("name", shippingDetails.getName()).put("phone", shippingDetails.getPhone()).build())
                .build();
        Charge createdCharge = Charge.create(params);
        assertEquals(createdCharge.getShipping(), shippingDetails);
    }

    @Test
    public void testChargeRetrieve() throws StripeException {
        Charge createdCharge = Charge.create(defaultChargeParams);
        Charge retrievedCharge = Charge.retrieve(createdCharge.getId());
        assertEquals(createdCharge.getCreated(), retrievedCharge.getCreated());
        assertEquals(createdCharge.getId(), retrievedCharge.getId());
        assertNotNull(retrievedCharge.getSource());
        assertEquals("card", retrievedCharge.getSource().getObject());
        Card card = (Card) retrievedCharge.getSource();
        assertEquals(defaultCardParams.get("address_city"), card.getAddressCity());
    }

    @Test
    public void testChargeRetrieveNullId() throws StripeException {
        try {
            Charge.retrieve(null);
            assertTrue(false);
        }
        catch (InvalidRequestException e) {
            String requestId = e.getRequestId();
            assertFalse(requestId == null);
            assertFalse(requestId.equals(""));
            assertTrue(e.toString().contains(requestId));
            assertTrue(404 == e.getStatusCode());
        }
    }

    @Test
    public void testChargeCapture() throws StripeException {
        Map<String, Object> options = new HashMap<String, Object>(defaultChargeParams);
        options.put("capture", false);

        Charge created = Charge.create(options);
        assertFalse(created.getCaptured());

        Charge captured = created.capture();
        assertTrue(captured.getCaptured());
    }

    @Test(expected = CardException.class)
    public void testInvalidCard() throws StripeException {
        Map<String, Object> invalidChargeParams = new HashMap<String, Object>();
        invalidChargeParams.putAll(defaultChargeParams);
        Map<String, Object> invalidCardParams = new HashMap<String, Object>();
        invalidCardParams.put("number", "4242424242424241");
        invalidCardParams.put("exp_month", 12);
        invalidCardParams.put("exp_year", getYear());
        invalidChargeParams.put("card", invalidCardParams);
        Charge.create(invalidChargeParams);
    }

    @Test
    public void testDeclinedCard() throws StripeException {
        Map<String, Object> declinedChargeParams = new HashMap<String, Object>();
        declinedChargeParams.putAll(defaultChargeParams);
        Map<String, Object> declinedCardParams = new HashMap<String, Object>();
        declinedCardParams.put("number", "4000000000000002");
        declinedCardParams.put("exp_month", 12);
        declinedCardParams.put("exp_year", getYear());
        declinedChargeParams.put("card", declinedCardParams);

        try {
            Charge.create(declinedChargeParams);
        }
        catch (CardException e) {
            assertEquals("card_declined", e.getCode());
            assertNotNull(e.getCharge());
        }
    }

    @Test
    public void testInvalidAddressZipTest() throws StripeException {
        Map<String, Object> invalidChargeParams = new HashMap<String, Object>();
        invalidChargeParams.putAll(defaultChargeParams);
        Map<String, Object> invalidCardParams = new HashMap<String, Object>();
        // See https://stripe.com/docs/testing
        invalidCardParams.put("number", "4000000000000036");
        invalidCardParams.put("address_zip", "94024");
        invalidCardParams.put("address_line1", "42 Foo Street");
        invalidCardParams.put("exp_month", 12);
        invalidCardParams.put("exp_year", getYear());
        invalidChargeParams.put("card", invalidCardParams);
        Charge charge = Charge.create(invalidChargeParams, supportedRequestOptions);
        assertEquals(charge.getPaid(), true);
        assertEquals(charge.getCard().getAddressZipCheck(), "fail");
        assertEquals(charge.getCard().getAddressLine1Check(), "pass");
    }

    @Test
    public void testInvalidAddressLine1Test() throws StripeException {
        Map<String, Object> invalidChargeParams = new HashMap<String, Object>();
        invalidChargeParams.putAll(defaultChargeParams);
        Map<String, Object> invalidCardParams = new HashMap<String, Object>();
        // See https://stripe.com/docs/testing
        invalidCardParams.put("number", "4000000000000028");
        invalidCardParams.put("address_zip", "94024");
        invalidCardParams.put("address_line1", "42 Foo Street");
        invalidCardParams.put("exp_month", 12);
        invalidCardParams.put("exp_year", getYear());
        invalidChargeParams.put("card", invalidCardParams);
        Charge charge = Charge.create(invalidChargeParams, supportedRequestOptions);
        assertEquals(charge.getPaid(), true);
        assertEquals(charge.getCard().getAddressZipCheck(), "pass");
        assertEquals(charge.getCard().getAddressLine1Check(), "fail");
    }


}