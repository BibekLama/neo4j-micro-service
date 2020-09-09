package fr.epita.neo4j.connection;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class DBConnection {
	final String neo4jURL = "bolt://192.168.0.34:10687";
	final String neo4jUser = "neo4j";
	final String neo4jPass = "epita";
	Driver driver;
	
	public DBConnection() {
		driver = GraphDatabase.driver(neo4jURL,
				AuthTokens.basic(neo4jUser, neo4jPass));
	}
	
	public Driver getDriver() {
		return driver;
	}


	public void close() {
		driver.close();
	}
}
