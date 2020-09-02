package fr.epita.neo4j.services.business;

public class Neo4jPersonBusinessException extends Neo4jBusinessException{

	private static final long serialVersionUID = 1L;

	public Neo4jPersonBusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public Neo4jPersonBusinessException(String message) {
		super(message);
	}
}
