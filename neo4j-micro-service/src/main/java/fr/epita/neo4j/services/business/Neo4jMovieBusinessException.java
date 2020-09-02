package fr.epita.neo4j.services.business;

public class Neo4jMovieBusinessException extends Neo4jBusinessException{

	private static final long serialVersionUID = 1L;

	public Neo4jMovieBusinessException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public Neo4jMovieBusinessException(String message) {
		super(message);
	}
}
