package com.tcs.servicedao;

import java.util.Map;

import org.apache.log4j.Logger;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.tcs.constants.DBConstants;
import com.tcs.utils.UtilityClass;

public class MongoDConnectionInitializer {
	final static Logger logger = Logger.getLogger(MongoDConnectionInitializer.class);

	private static MongoDConnectionInitializer dbInitIns = null;
	private String dbName = null;
	// private String dbUserName = null;
	private String dbPort = null;
	private String dbIp = null;
	// private String dbPassword = null;
	DB db;
	MongoDatabase mongoDB;

	public static MongoDConnectionInitializer getInstance() {
		if (dbInitIns == null) {
			dbInitIns = new MongoDConnectionInitializer();
		}
		return dbInitIns;
	}

	private MongoDConnectionInitializer() {

	}

	public DB getConnection() {
		Map<String, String> dbPropsMap = UtilityClass.readPropFile(DBConstants.DB_PROPS_LOC);
		this.dbIp = dbPropsMap.get(DBConstants.DB_SERVER_IP);
		this.dbPort = dbPropsMap.get(DBConstants.DB_SERVER_PORT);
		this.dbName = dbPropsMap.get(DBConstants.DB_DATABASE_NAME);
		// this.dbUserName = dbPropsMap.get(DBConstants.DB_USER_NAME);
		// this.dbPassword = dbPropsMap.get(DBConstants.DB_PASSWORD);

		logger.debug("inside getConnection method " + dbIp + " " + dbPort);
		boolean auth = false;
		try {
			// MongoClient mongoClient = new MongoClient(dbIp, 27017);
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			db = mongoClient.getDB(dbName);
			logger.debug("Connected to database successfully");
			// auth = db.authenticate(dbUserName, dbPassword.toCharArray());
			logger.debug("Authentication: " + auth);
		} catch (NumberFormatException e) {
			logger.error("Numberformat exception" + e.getMessage());
		}
		return db;
	}

	public MongoDatabase getMongoConnection() {
		Map<String, String> dbPropsMap = UtilityClass.readPropFile(DBConstants.DB_PROPS_LOC);
		this.dbIp = dbPropsMap.get(DBConstants.DB_SERVER_IP);
		this.dbPort = dbPropsMap.get(DBConstants.DB_SERVER_PORT);
		this.dbName = dbPropsMap.get(DBConstants.DB_DATABASE_NAME);

		// Mongodb connection string.
		String client_url = "mongodb://" + dbIp + ":" + dbPort + "/" + dbName;
		MongoClientURI uri = new MongoClientURI(client_url);

		// Connecting to the mongodb server using the given client uri.
		MongoClient mongo_client;
		try {
			mongo_client = new MongoClient(uri);
			mongoDB = mongo_client.getDatabase(dbName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mongoDB;
	}
}
