package fr.epita.neo4j.services.business;

public class Neo4jBusinessException extends Exception{

	private static final long serialVersionUID = 1L;
	
	public Neo4jBusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public Neo4jBusinessException(String message) {
		super(message);
	}
}
