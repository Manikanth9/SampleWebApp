package com.tcs.servicedao;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.tcs.constants.Constants;
import com.tcs.utils.MongoDbUtils;
import com.tcs.utils.UtilityClass;

@Component
public class SdWanPolicyConfigurator {
	final static Logger logger = Logger.getLogger(SdWanPolicyConfigurator.class);
	public static String policyName;
	private static MongoDConnectionInitializer dbInitIns = null;
	private static DB db;

	// all variables
	private String onappolicyIp = null;
	// private String dbUserName = null;
	private String onappolicyPort = null;

	public SdWanPolicyConfigurator() {
		init();
	}

	private void init() {
		Map<String, String> restpropsMap = UtilityClass.readPropFile(Constants.REST_PROP);
		this.onappolicyIp = restpropsMap.get(Constants.POLICY_HOSTNAME);
		this.onappolicyPort = restpropsMap.get(Constants.POLICY_PORT);
	}

	/**
	 * 
	 * @param uri
	 * @param type
	 * @return
	 */
	public static HttpURLConnection getUrlConnObject(String uri, String type) {
		logger.debug("Entered getUrlConnObject method");
		URL obj;
		HttpURLConnection con = null;
		try {
			obj = new URL(uri);
			con = (HttpURLConnection) obj.openConnection();
			if (!type.equals("PATCH")) {
				con.setRequestMethod(type);
			} else {
				con.setRequestProperty("X-HTTP-Method-Override", "PATCH");
				con.setRequestMethod("PUT");
			}
			con.setRequestProperty("Authorization", "Basic " + "dGVzdHBkcDphbHBoYTEyMw==");
			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			con.setRequestProperty("accept", "application/json");
			con.setRequestProperty("Environment", "TEST");
			con.setRequestProperty("ClientAuth", "cHl0aG9uOnRlc3Q=");
			con.setDoInput(true);
			con.setDoOutput(true);
			return con;
		} catch (MalformedURLException e) {
			logger.error("MalformedURLException: " + e.getMessage());
			return null;
		} catch (IOException e) {
			logger.error("IOException: " + e.getMessage());
			return null;
		} finally {
			if (con != null) {
				logger.info("Disconnecting connection");
				con.disconnect();
			}
		}
	}

	/**
	 * 
	 * @param configData
	 *            creating the scheduled policy in ONAP
	 */
	public void createScheduledPolicyThroughAPI(@Valid String configData) {
		System.out.println("calling createScheduledPolicyThroughAPI !!! ");
		String formedUrl = "http" + "://" + onappolicyIp + ":" + onappolicyPort + "/pdp/api/createPolicy";
		JSONObject payloadObj = new JSONObject(configData);
		//int statuscode = createOrUpdatePolicy(formedUrl, payloadObj);
		int statuscode = 200;
		if (statuscode == 200) {
			try {
				System.out.println("Now persisting data into MongoDB !! " +configData);
				dbInitIns = MongoDConnectionInitializer.getInstance();
				db = dbInitIns.getConnection();

				MongoDbUtils mdu = new MongoDbUtils(db);// find("PolicyObject", "BusyHourPolicyFriday","");
				DBCollection coll = mdu.getCollection("SDWAN_POLICIES");

				mdu.addToCollection(coll, (DBObject) JSON.parse(configData.toString()));
				System.out.println("Data persisted into Mongo DB !!! ");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param policyName
	 *            creating the scheduled policy in ONAP
	 */
	public boolean deleteScheduledPolicyThroughAPI(String policyName) {
		System.out.println("calling deleteScheduledPolicyThroughAPI !!! ");
		String formedUrl = "http" + "://" + onappolicyIp + ":" + onappolicyPort + "/pdp/api/deletePolicy";
		//int statuscode = createDeletePayLoad(formedUrl, policyName);
		int statuscode = 200;
		if (statuscode == 200) {
			System.out.println("Deleting policy from MongoDB !! ");
			dbInitIns = MongoDConnectionInitializer.getInstance();
			db = dbInitIns.getConnection();
			MongoDbUtils mdu = new MongoDbUtils(db);
			DBCollection coll = mdu.getCollection("SDWAN_POLICIES");
			return mdu.removeCollection(coll, policyName);
		}
		return false;
	}

	/**
	 * 
	 * @param formedUrl
	 * @param policyName
	 * @return
	 */
	public static int createDeletePayLoad(String formedUrl, String policyName) {
		System.out.println("in createDeletePayLoad method !! ");
		JSONObject deletepayload = new JSONObject();
		System.out.println("formed Url " + formedUrl);
		String requestType = "DELETE";
		DataOutputStream dataOutputStream = null;
		HttpURLConnection conn = null;
		int code = 0;
		try {
			// deletepayload.put("deleteCondition", "Current Version");
			deletepayload.put("deleteCondition", "ALL");
			deletepayload.put("pdpGroup", "default");
			deletepayload.put("policyName", "SON." + policyName);
			deletepayload.put("policyType", "Base");
			deletepayload.put("policyComponent", "PDP");
			conn = getUrlConnObject(formedUrl, "DELETE");
			if (!(requestType.equals("GET"))) {
				dataOutputStream = new DataOutputStream(conn.getOutputStream());
				dataOutputStream.writeBytes(deletepayload.toString());
				dataOutputStream.flush();
				dataOutputStream.close();
			}
			code = conn.getResponseCode();
			String message = conn.getResponseMessage();
			System.out.println("Response    (Code):" + code);
			System.out.println("Response (Message):" + message);

			// read the response
			DataInputStream input = new DataInputStream(conn.getInputStream());
			int c;
			StringBuilder resultBuf = new StringBuilder();
			while ((c = input.read()) != -1) {
				resultBuf.append((char) c);
			}
			input.close();
			conn.disconnect();

			System.out.println("delete response is " + resultBuf.toString());

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return code;
	}

	/**
	 * 
	 * @param url
	 * @param payloadObj
	 */
	public int createOrUpdatePolicy(String url, JSONObject payloadObj) {
		System.out.println("formed Url " + url);
		String requestType = "PUT";
		DataOutputStream dataOutputStream = null;
		HttpURLConnection conn = null;
		int code = 0;
		try {
			// JSONObject payloadJson = createPayloadObjectOrig();

			JSONObject payloadJson = createPayloadObject(payloadObj);
			System.out.println("payload is " + payloadJson.toString());
			conn = getUrlConnObject(url, "PUT");
			if (!(requestType.equals("DELETE") || requestType.equals("GET"))) {
				dataOutputStream = new DataOutputStream(conn.getOutputStream());
				dataOutputStream.writeBytes(payloadJson.toString());
				dataOutputStream.flush();
				dataOutputStream.close();
			}

			code = conn.getResponseCode();
			String message = conn.getResponseMessage();
			System.out.println("Response    (Code):" + code);
			System.out.println("Response (Message):" + message);

			// read the response
			DataInputStream input = new DataInputStream(conn.getInputStream());
			int c;
			StringBuilder resultBuf = new StringBuilder();
			while ((c = input.read()) != -1) {
				resultBuf.append((char) c);
			}
			input.close();
			conn.disconnect();

			System.out.println("result response is " + resultBuf.toString());

			if (code == 200 && message.equalsIgnoreCase("OK")) {
				System.out.println("pushing policy " + policyName + " to pdp group :: ");
				String pushPolicyUrl = "http" + "://" + onappolicyIp + ":" + onappolicyPort + "/pdp/api/pushPolicy";
				System.out.println("formed Url " + pushPolicyUrl);
				JSONObject pushpayloadJson = new JSONObject();
				pushpayloadJson.put("pdpGroup", "default");
				pushpayloadJson.put("policyName", "SON." + policyName);
				pushpayloadJson.put("policyType", "Base");

				System.out.println("payload is " + pushpayloadJson.toString());
				conn = getUrlConnObject(pushPolicyUrl, "PUT");
				if (!(requestType.equals("DELETE") || requestType.equals("GET"))) {
					dataOutputStream = new DataOutputStream(conn.getOutputStream());
					dataOutputStream.writeBytes(pushpayloadJson.toString());
					dataOutputStream.flush();
					dataOutputStream.close();
				}
				code = conn.getResponseCode();
				message = conn.getResponseMessage();
				System.out.println("Response    (Code) after pushing policy:" + code);
				System.out.println("Response (Message)after pushing policy :" + message);

				// read the response
				input = new DataInputStream(conn.getInputStream());
				StringBuilder resBuf4PushPolicy = new StringBuilder();
				while ((c = input.read()) != -1) {
					resBuf4PushPolicy.append((char) c);
				}
				input.close();
				conn.disconnect();
				System.out.println("result buffer after pushing policy :: " + resBuf4PushPolicy.toString());
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return code;
	}

	/**
	 * 
	 * @param policyPayload
	 *            updating the scheduled policy in ONAP
	 */
	public void updateScheduledPolicyThroughAPI(String policyPayload) {
		System.out.println("calling updateScheduledPolicyThroughAPI !!! ");
		String formedUrl = "http" + "://" + onappolicyIp + ":" + onappolicyPort + "/pdp/api/updatePolicy";
		JSONObject payloadObj = new JSONObject(policyPayload);
		//int statuscode = createOrUpdatePolicy(formedUrl, payloadObj);
		int statuscode = 200;
		if (statuscode == 200) {
			try {
				policyName = payloadObj.getString("policyName");
				System.out.println("Updating the Policy in MongoDB !!  for policy" + policyName);
				dbInitIns = MongoDConnectionInitializer.getInstance();
				db = dbInitIns.getConnection();
				MongoDbUtils mdu = new MongoDbUtils(db);
				DBCollection coll = mdu.getCollection("SDWAN_POLICIES");
				// mdu.modifyInCollection(coll,"BusyHourPolicyFrid", policyPayload.toString());
				mdu.updateCollection(coll, (DBObject) JSON.parse(policyPayload.toString()), policyName);
				System.out.println("Data updated into Mongo DB !!! ");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param policyName
	 *            retrieving the scheduled policy in ONAP
	 */
	public String getPolicyFromDB(String policyName) {
		System.out.println("calling getPolicyFromDB !!! ");
		String policyObject = null;
		try {
			System.out.println("Fetching policy from MongoDB !! ");
			dbInitIns = MongoDConnectionInitializer.getInstance();
			db = dbInitIns.getConnection();
			MongoDbUtils mdu = new MongoDbUtils(db);
			DBCollection coll = mdu.getCollection("SDWAN_POLICIES");
			policyObject = mdu.getCollectionFromPolicy(coll, policyName);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return policyObject;
	}

	/**
	 * 
	 * @return retrieving list of scheduled policy in ONAP
	 */
	public ArrayList<JSONObject> getAllPoliciesFromDB() {
		System.out.println("calling getAllPoliciesFromDB !!! ");
		ArrayList<JSONObject> allpoliciesList = null;
		try {
			dbInitIns = MongoDConnectionInitializer.getInstance();
			db = dbInitIns.getConnection();
			MongoDbUtils mdu = new MongoDbUtils(db);
			DBCollection coll = mdu.getCollection("SDWAN_POLICIES");
			DBCursor cursor = coll.find();
			allpoliciesList = new ArrayList<>();
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				JSONObject output = new JSONObject(JSON.serialize(obj));
				allpoliciesList.add(output);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("returning the list of polcies to UI :: " + allpoliciesList.size());
		return allpoliciesList;
	}

	/**
	 * 
	 * @param policyPayload
	 * @return
	 */
	public JSONObject createPayloadObject(JSONObject policyPayload) {
		JSONObject payloadJson = new JSONObject();
		try {
			policyName = policyPayload.getString("policyName");
			System.out.println("policy name from json object is :: " + policyName);

			JSONObject matching = new JSONObject();
			JSONObject intmatching = new JSONObject();
			intmatching.put("key", "value");
			matching.put("MATCHING", intmatching);
			payloadJson.put("configBodyType", "OTHER");
			payloadJson.put("configName", "testConfig");
			payloadJson.put("ecompName", "DCAE");
			payloadJson.put("policyClass", "Config");
			payloadJson.put("policyConfigType", "Base");
			payloadJson.put("policyDescription", "SDWAN Service policy creation");
			payloadJson.put("policyName", "SON." + policyName);
			payloadJson.put("configBody", policyPayload.toString());
			payloadJson.put("attributes", matching);
			System.out.println("final json is ::" + payloadJson);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return payloadJson;
	}
}
