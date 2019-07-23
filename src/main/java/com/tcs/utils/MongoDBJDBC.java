package com.tcs.utils;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

public class MongoDBJDBC {
	public static void main(String args[]) {
		try {
			System.out.println("in try ");
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("testMongo");
			System.out.println("Connect to database successfully");

			DBCollection coll = db.getCollection("mycolnew");
			JSONObject finaljson = new JSONObject();
			JSONObject tokenObject = new JSONObject();
			tokenObject.append("alarm_d", 1).append("alarm_state", "severe").append("time", "123456");
			finaljson.put("notification", tokenObject.toString());
			BasicDBObject doc = new BasicDBObject((BasicDBObject) JSON.parse(finaljson.toString()));
			coll.insert(doc);

			getCollection(coll);

		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	private static void getCollection(DBCollection coll) {
		DBCursor cursor = coll.find();
		ArrayList<org.json.simple.JSONObject> jsonList = new ArrayList<org.json.simple.JSONObject>();
		while (cursor.hasNext()) {
			BasicDBObject obj = (BasicDBObject) cursor.next();
			String response = (String) obj.get("notification");
			JSONParser parser = new JSONParser();
			Object obj1;
			try {
				obj1 = parser.parse(response);
				org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) obj1;
				jsonList.add(jsonObject);
				for (Iterator iterator = jsonObject.keySet().iterator(); iterator.hasNext();) {
					String key = (String) iterator.next();
					System.out.println("key is " + key);
					System.out.println(jsonObject.get("alarm_d"));
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		System.out.println("jsonobject in List is  " + jsonList);
	}
}
