package com.tcs.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

public class MongoDbUtils {
	MongoClient mongoClient;
	boolean auth = false;
	DB db;
	MongoDatabase mongoDB;
	DBCollection coll;
	final static Logger logger = Logger.getLogger(MongoDbUtils.class);

	public MongoDbUtils(DB db2) {
		this.db = db2;
	}

	public MongoDbUtils(MongoDatabase mongoDB) {
		this.mongoDB = mongoDB;
	}

	public DBCollection getCollection(String collectionName) {
		coll = db.getCollection(collectionName);
		return coll;
	}

	public void addToCollection(DBCollection coll, DBObject value) {
		coll.insert(value);
	}

	/**
	 * 
	 * @param coll
	 * @param path
	 * @return
	 */

	public String findInCollectionPath(DBCollection coll, String path) {
		logger.info("findInCollectionPath");
		String[] path1 = path.split("/");
		path1[0] = path1[0].replace(".", "*");
		System.out.println("path length" + path1.length);
		DBCursor cursor1 = coll.find();
		DBObject updateDocument = null;
		while (cursor1.hasNext()) {
			updateDocument = (DBObject) cursor1.next().get(path1[0]);
			for (int k = 1; k < path1.length; k++) {
				if (updateDocument.get(path1[k]) instanceof DBObject) {
					updateDocument = (DBObject) updateDocument.get(path1[k]);
				} else {
					return (String) updateDocument.get(path1[k]);
				}
			}
		}
		return updateDocument.toString();
	}

	/**
	 * 
	 * @param coll
	 * @param key
	 * @return
	 */
	public String findInCollection(DBCollection coll, String key) {
		System.out.println("in findInCollection");
		String response = null;
		BasicDBObject query = new BasicDBObject();
		BasicDBObject field = new BasicDBObject();
		field.put(key, 1);
		DBCursor cursor = coll.find(query, field);
		BasicDBObject obj = null;
		while (cursor.hasNext()) {
			obj = (BasicDBObject) cursor.next();
			System.out.println("obj is " + obj);
			response = obj.getString("_id");
			System.out.println("response is " + response);
			while (response != null) {
				return response;
			}
		}
		return response;
	}

	/**
	 * 
	 * @param coll
	 * @param key
	 * @return
	 */
	public String getCollectionFromPolicy(DBCollection coll, String key) {
		System.out.println("in getCollectionFromPolicy");
		BasicDBObject fields = new BasicDBObject();
		fields.put("policyName", key);
		DBCursor cursor = coll.find(fields);
		JSONObject output = null;
		while (cursor.hasNext()) {
			DBObject result = cursor.next();
			try {
				output = new JSONObject(JSON.serialize(result));
				System.out.println("fulljson is :: " + output);
				return output.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return output.toString();
	}

	/**
	 * 
	 * @param coll
	 * @param key
	 * @return
	 */
	public HashMap<String, String> getActiveJsonDocument(MongoCollection<Document> coll, String key) {
		BasicDBObject fields = new BasicDBObject();
		fields.put("policyStatus", key);
		FindIterable<Document> fromdumps = coll.find(fields);
		MongoCursor<Document> fromcursor = fromdumps.iterator();
		HashMap<String, String> activepolicyMap = new HashMap<>();
		while (fromcursor.hasNext()) {
			logger.info("validfrom one :: ");
			activepolicyMap.put("policyStatus", fromcursor.next().toJson());
		}
		return activepolicyMap;
	}

	/**
	 * 
	 * @param coll
	 * @param key
	 * @return
	 */
	public boolean removeMyCollection(DBCollection coll, String key) {
		System.out.println("in removeMyCollection");
		boolean response = false;
		BasicDBObject fields = new BasicDBObject();
		fields.put("policyName", key);
		DBCursor cursor = coll.find(fields);
		while (cursor.hasNext()) {
			coll.remove(cursor.next());
			response = true;
		}
		return response;
	}

	/**
	 * 
	 * @param coll
	 * @param path
	 * @param value
	 */
	public void modifyInCollection(DBCollection coll, String path, String value) {
		System.out.println("inside modify");
		String path1 = path.replace(".", "*").replace("/", ".");
		System.out.println(path1);
		BasicDBObject searchQuery = new BasicDBObject().append(path1, findInCollection(coll, path));
		BasicDBObject updateDocument = new BasicDBObject();
		updateDocument.append("$set", new BasicDBObject().append(path1, value));
		coll.update(searchQuery, updateDocument);
	}

	/**
	 * 
	 * @param coll
	 * @param dbObject
	 * @param policyName
	 */
	public void updateCollection(DBCollection coll, DBObject dbObject, String policyName) {
		System.out.println("inside updateCollection");
		BasicDBObject searchQuery = new BasicDBObject().append("policyName", policyName);
		// BasicDBObject updateDocument = new BasicDBObject();
		// updateDocument.put(dbObject.toString());
		coll.update(searchQuery, dbObject);
		System.out.println("Collections has been updated successfully !! ");
	}

	/**
	 * 
	 * @param coll
	 * @param path
	 */
	public void deleteCollection(DBCollection coll, String path) {
		System.out.println("inside deleteCollection");
		String path1 = path.replace(".", "*").replace("/", ".");
		System.out.println(path1);
		BasicDBObject searchQuery = new BasicDBObject().append(path1, removeMyCollection(coll, path));
		BasicDBObject updateDocument = new BasicDBObject();
		updateDocument.append("$unset", new BasicDBObject().append(path1, ""));
		coll.update(searchQuery, updateDocument);
	}

	/**
	 * 
	 * @param coll
	 * @param path
	 */
	public boolean removeCollection(DBCollection coll, String path) {
		System.out.println("inside removeCollection");
		boolean isDeleted = false;
		String path1 = path.replace(".", "*").replace("/", ".");
		System.out.println(path1);
		if (removeMyCollection(coll, path)) {
			System.out.println("removed policy from MongoDB !! ");
			isDeleted = true;
		} else {
			System.out.println("Something went wrong !! document not found !! ");
			isDeleted = false;
		}
		return isDeleted;
	}

	/**
	 * 
	 * @param col
	 * @return
	 */
	public HashMap<String, ArrayList<String>> getSelectiveJsonDocument(MongoCollection<Document> col) {
		logger.info("Fetching a particular document from the collection");

		Date d = new Date();
		System.out.println("date is :: " + d);
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		long time = d.getTime();
		String ourformat = formatter.format(time);
		String ourformatPlus = formatter.format(time + 5000);
		String ourformatMinus = formatter.format(time - 5000);
		System.out.println("ourformat :: " + ourformat + " /n ourformatPlus " + ourformatPlus + " /n ourformatMinus "
				+ ourformatMinus);

		SimpleDateFormat dtformatter = new SimpleDateFormat("dd/MM/yyyy");
		String todaysDate = dtformatter.format(new Date());
		System.out.println("todays date is :: " + todaysDate);

		SimpleDateFormat format1 = new SimpleDateFormat("hh:mm:ss");
		String presentTime = format1.format(time);
		System.out.println("present time is :: " + presentTime);

		BasicDBObject policyValidDate;
		ArrayList<String> validFromDocument = new ArrayList<>();
		HashMap<String, ArrayList<String>> scheduledRecords = new HashMap<>();
		try {
			policyValidDate = BasicDBObject
					.parse("{\"scheduleroptions.policyValidDate\": {$lte: \"" + todaysDate + "\"}}");
			BasicDBObject policyExpiryDate = BasicDBObject
					.parse("{\"scheduleroptions.policyExpiryDate\": {$gt: \"" + todaysDate + "\"}}");

			BasicDBObject policyStartTime = BasicDBObject
					.parse("{\"scheduleroptions.policyStartTime\": {$lte: \"" + presentTime + "\"}}");
			BasicDBObject policyEndTime = BasicDBObject
					.parse("{\"scheduleroptions.policyEndTime\": {$gte: \"" + presentTime + "\"}}");

			BasicDBObject andQuery = new BasicDBObject();
			List<BasicDBObject> obj = new ArrayList<BasicDBObject>();
			obj.add(policyValidDate);
			obj.add(policyExpiryDate);
			obj.add(policyStartTime);
			obj.add(policyEndTime);
			andQuery.put("$and", obj);

			FindIterable<Document> fromdumps = col.find(andQuery);
			MongoCursor<Document> fromcursor = fromdumps.iterator();

			try {
				while (fromcursor.hasNext()) {
					logger.info("validfrom one :: ");
					validFromDocument.add(fromcursor.next().toJson());
				}
				if (!validFromDocument.isEmpty())
					scheduledRecords.put("validFrom", validFromDocument);
			} finally {
				System.out.println();
				fromcursor.close();
			}
			System.out.println("scheduled records size is " + scheduledRecords.size() + " values  " + scheduledRecords);
			return scheduledRecords;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return scheduledRecords;
	}
}
