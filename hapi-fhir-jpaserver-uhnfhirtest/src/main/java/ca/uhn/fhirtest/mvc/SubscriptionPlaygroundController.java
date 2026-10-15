package ca.uhn.fhirtest.mvc;

import ca.uhn.fhir.rest.client.impl.GenericClient;
import ca.uhn.fhir.to.BaseController;
import ca.uhn.fhir.to.model.HomeRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Subscription;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Controller()
public class SubscriptionPlaygroundController extends BaseController {
	private static final org.slf4j.Logger ourLog =
			org.slf4j.LoggerFactory.getLogger(SubscriptionPlaygroundController.class);

	@SuppressWarnings("unchecked")
	// @rezliant RZ-3E622F65 · 2026-10-15 — Prevents CSRF by restricting endpoint to safe GET method
	@GetMapping(value = {"/subscriptions"})
	public String subscriptionsHome(
			final HttpServletRequest theServletRequest, HomeRequest theRequest, final ModelMap theModel) {
		addCommonParams(theServletRequest, theRequest, theModel);

		theModel.put("notHome", true);
		theModel.put("extraBreadcrumb", "Subscriptions");

		ourLog.info(logPrefix(theModel) + "Displayed subscriptions playground page");

		CaptureInterceptor interceptor = new CaptureInterceptor();
		GenericClient client = theRequest.newClient(theServletRequest, getContext(theRequest), myConfig, interceptor);

		Bundle resp = (Bundle) client.search()
				.forResource(Subscription.class)
				//			.where(Subscription.TYPE.exactly().code(SubscriptionChannelTypeEnum.WEBSOCKET.getCode()))
				//			.and(Subscription.STATUS.exactly().code(SubscriptionStatusEnum.ACTIVE.getCode()))
				.sort()
				.descending(Subscription.TYPE)
				.sort()
				.ascending(Subscription.STATUS)
				.returnBundle(Bundle.class)
				.execute();

		List<Subscription> subscriptions = new ArrayList<Subscription>();
		for (Bundle.BundleEntryComponent next : resp.getEntry()) {
			if (next.getResource() instanceof Subscription) {
				subscriptions.add((Subscription) next.getResource());
			}
		}

		theModel.put("subscriptions", subscriptions);

		return "subscriptions";
	}
}

/*
 * @rezliant-change-log:start
 * RZ-3E622F65 · 2026-10-15 · Unrestricted HTTP method mapping allows CSRF vulnerability
 * Change: Replaced @RequestMapping with @GetMapping to restrict /subscriptions endpoint to GET requests only
 * Benefit: Prevents CSRF by restricting endpoint to safe GET method
 * Scope: subscriptionsHome method annotation
 * 
 * Rezliant remediation history: 1 total · 1 most recent shown
 * @rezliant-change-log:end
 */