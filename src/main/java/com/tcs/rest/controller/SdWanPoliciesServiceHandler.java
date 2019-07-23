package com.tcs.rest.controller;

import java.util.ArrayList;

import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.tcs.servicedao.SdWanPolicyConfigurator;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class SdWanPoliciesServiceHandler {
	@Autowired
	SdWanPolicyConfigurator configurator;

	@RequestMapping(method = RequestMethod.GET, value = "/onap/policy/getsdwanPolicies")
	@ResponseBody
	@CrossOrigin(origins = "http://localhost:8081", allowedHeaders = "*")
	public ResponseEntity<Object> getAllSdWanPolicies() {
		ArrayList<JSONObject> policyList = configurator.getAllPoliciesFromDB();
		return ResponseEntity.ok().body(policyList.toString());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/onap/policy/getsdwanPolicy")
	@ResponseBody
	@CrossOrigin(origins = "http://localhost:8081", allowedHeaders = "*")
	public ResponseEntity<Object> getSpecifiedSdWanPolicy(@RequestParam String policyName) {
		return ResponseEntity.ok().body(configurator.getPolicyFromDB(policyName));
	}

	@RequestMapping(method = RequestMethod.POST, value = "/onap/policy/configurepolicy")
	@ResponseBody
	@CrossOrigin(origins = "http://localhost:8081", allowedHeaders = "*")
	public void configureNewPolicyInONAP(@Valid @RequestBody String configData) {
		System.out.println("received request !! calling createScheduledPolicyThroughAPI ");
		configurator.createScheduledPolicyThroughAPI(configData);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/onap/policy/updatepolicy")
	@ResponseBody
	@CrossOrigin(origins = "http://localhost:8081", allowedHeaders = "*")
	public void updateConfiguredPolicyInONAP(@Valid @RequestBody String configData) {
		System.out.println("received request !! calling updateConfiguredPolicyInONAP " +configData);
		configurator.updateScheduledPolicyThroughAPI(configData);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/onap/policy/deletepolicy")
	@ResponseBody
	@CrossOrigin(origins = "http://localhost:8081", allowedHeaders = "*")
	public ResponseEntity<Boolean> deleteConfiguredPolicyInONAP(@RequestParam String policyName) {
		System.out.println("received request !! calling updateConfiguredPolicyInONAP ");
		return ResponseEntity.ok().body(configurator.deleteScheduledPolicyThroughAPI(policyName));
	}
}
