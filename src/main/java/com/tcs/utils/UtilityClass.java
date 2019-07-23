package com.tcs.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class UtilityClass {
	final static Logger logger = Logger.getLogger(UtilityClass.class);

	private static UtilityClass util=null;
	private UtilityClass() {
	}
	
	public static UtilityClass getInstance(){
		if (util == null) {
			util = new UtilityClass();
		}
		return util;
	}
	public static Map<String, String> readPropFile(String fileLocation) {
		logger.debug("Entered readPropFile method");
		Properties prop = new Properties();
		Map<String, String> propsMap = new HashMap<String, String>();
		FileInputStream file;
		try {
			file = new FileInputStream(fileLocation);
			prop.load(file);
			file.close();
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException:" + e.getMessage() + " while reading file with path:" + fileLocation);
		} catch (IOException e) {
			logger.error("IOException:" + e.getMessage() + " while reading file with path:" + fileLocation);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		for (final String name : prop.stringPropertyNames()) {
			propsMap.put(name, prop.getProperty(name).toString());
		}
		return propsMap;
	}
}
