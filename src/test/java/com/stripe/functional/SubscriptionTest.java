package com.stripe.functional;

import com.stripe.BaseStripeFunctionalTest;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SubscriptionTest extends BaseStripeFunctionalTest {
    // Subscription Tests:
    @Test
    public void testUpdateSubscription() throws StripeException {
        Plan plan = Plan.create(getUniquePlanParams());
        Customer customer = Customer.create(defaultCustomerParams);
        Map<String, Object> subscriptionParams = new HashMap<String, Object>();
        subscriptionParams.put("plan", plan.getId());
        Subscription sub = customer.updateSubscription(subscriptionParams);
        assertEquals(sub.getPlan().getId(), plan.getId());
        assertEquals(sub.getCustomer(), customer.getId());
    }

    @Test
    public void testCancelSubscription() throws StripeException {
        Plan plan = Plan.create(getUniquePlanParams());
        Customer customer = createDefaultCustomerWithPlan(plan);
        assertEquals(customer.getSubscriptions().getData().get(0).getStatus(), "active");
        Subscription canceledSubscription = customer.cancelSubscription();
        assertEquals(canceledSubscription.getStatus(), "canceled");
    }

    @Test
    public void testCancelSubscriptionAtPeriodEnd() throws StripeException {
        Plan plan = Plan.create(getUniquePlanParams());
        Customer customer = createDefaultCustomerWithPlan(plan);
        assertEquals(customer.getSubscriptions().getData().get(0).getStatus(), "active");
        Map<String, Object> cancelParams = new HashMap<String, Object>();
        cancelParams.put("at_period_end", true);
        Subscription canceledSubscription = customer
                .cancelSubscription(cancelParams);
        assertEquals(canceledSubscription.getStatus(), "active");
        assertEquals(canceledSubscription.getCancelAtPeriodEnd(), true);
    }

    @Test
    public void testNewStyleSubscriptionAPI() throws StripeException {
        Plan plan = Plan.create(getUniquePlanParams());
        Plan plan2 = Plan.create(getUniquePlanParams());
        Customer customer = Customer.create(defaultCustomerParams);

        // Create
        Map<String, Object> subCreateParams = new HashMap<String, Object>();
        subCreateParams.put("plan", plan.getId());
        Subscription sub = customer.createSubscription(subCreateParams);
        assertEquals(plan.getId(), sub.getPlan().getId());
        customer = Customer.retrieve(customer.getId());
        assertEquals(1, customer.getSubscriptions().getData().size());
        assertEquals(sub.getId(), customer.getSubscriptions().getData().get(0).getId());

        // Retrieve
        Subscription retrievedSub = customer.getSubscriptions().retrieve(sub.getId());
        assertEquals(sub.getId(), retrievedSub.getId());

        // List
        CustomerSubscriptionCollection list = customer.getSubscriptions().all(null);
        assertEquals(1, list.getData().size());
        assertEquals(sub.getId(), list.getData().get(0).getId());

        // Update
        Map<String, Object> subUpdateParams = new HashMap<String, Object>();
        subUpdateParams.put("plan", plan2.getId());
        sub = sub.update(subUpdateParams);
        assertEquals(plan2.getId(), sub.getPlan().getId());

        // Cancel
        sub = sub.cancel(null);
        assertNotNull(sub.getCanceledAt());
    }

    @Test
    public void testTopLevelSubscriptionAPI() throws StripeException {
        Plan plan = Plan.create(getUniquePlanParams());
        Plan plan2 = Plan.create(getUniquePlanParams());
        Customer customer = Customer.create(defaultCustomerParams);

        // Create
        Map<String, Object> subCreateParams = new HashMap<String, Object>();
        subCreateParams.put("plan", plan.getId());
        subCreateParams.put("customer", customer.getId());
        Subscription sub = Subscription.create(subCreateParams);
        assertEquals(plan.getId(), sub.getPlan().getId());
        assertEquals(customer.getId(), sub.getCustomer());

        customer = Customer.retrieve(customer.getId());
        assertEquals(1, customer.getSubscriptions().getData().size());
        assertEquals(sub.getId(), customer.getSubscriptions().getData().get(0).getId());

        // Retrieve
        Subscription retrievedSub = Subscription.retrieve(sub.getId());
        assertEquals(sub.getId(), retrievedSub.getId());

        // List
        Map<String, Object> subAllParams = new HashMap<String, Object>();
        subAllParams.put("plan", plan.getId());
        subAllParams.put("customer", customer.getId());
        SubscriptionCollection list = Subscription.all(subAllParams);
        assertEquals(1, list.getData().size());
        assertEquals(sub.getId(), list.getData().get(0).getId());
        assertEquals(customer.getId(), list.getData().get(0).getCustomer());
        assertEquals(plan.getId(), list.getData().get(0).getPlan().getId());

        // Update
        Map<String, Object> subUpdateParams = new HashMap<String, Object>();
        subUpdateParams.put("plan", plan2.getId());
        sub = sub.update(subUpdateParams);
        assertEquals(plan2.getId(), sub.getPlan().getId());

        // Cancel
        sub = sub.cancel(null);
        assertNotNull(sub.getCanceledAt());
    }


    @Test
    public void testCreateSubscriptionThroughCollection() throws StripeException {
        Plan plan = Plan.create(getUniquePlanParams());
        Customer customer = Customer.create(defaultCustomerParams);

        // Create
        Map<String, Object> subCreateParams = new HashMap<String, Object>();
        subCreateParams.put("plan", plan.getId());

        Subscription sub = customer.getSubscriptions().create(subCreateParams);
        assertEquals(plan.getId(), sub.getPlan().getId());

        // Verify
        customer = Customer.retrieve(customer.getId());
        assertEquals(1, customer.getSubscriptions().getData().size());
        assertEquals(sub.getId(), customer.getSubscriptions().getData().get(0).getId());
    }
}
