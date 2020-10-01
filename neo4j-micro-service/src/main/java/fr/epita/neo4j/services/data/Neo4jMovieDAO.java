package fr.epita.neo4j.services.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Value;
import org.neo4j.driver.exceptions.ClientException;
import org.springframework.stereotype.Repository;

import fr.epita.neo4j.connection.DBConnection;
import fr.epita.neo4j.datamodels.Movie;
import fr.epita.neo4j.datamodels.Person;
import fr.epita.neo4j.services.business.Neo4jMovieBusinessException;

import static org.neo4j.driver.Values.parameters;

@Repository
public class Neo4jMovieDAO {
	
	@Inject
	Neo4jPersonDAO personDAO;
	
	private Movie getMovieDetail(Record row, Transaction tx) {
		Value value = row.get("n");
		Movie movie = new Movie();
		Map<String, Object> properties = value.asEntity().asMap();
		
		movie.setId(row.get("ID").asLong());
		movie.setTitle(String.valueOf(properties.get("title")));
        movie.setTagline(String.valueOf(properties.get("tagline")));
        movie.setReleased(Long.valueOf(String.valueOf(properties.get("released"))));
        
        // Select actors of the movie from ACTED_IN relationship
        List<Person> actors = personDAO.movieActors(row.get("ID").asLong());
        movie.setActors(actors);
        
        // Select directors of the movie from DIRECTED relationship
        List<Person> directors = personDAO.movieDirectors(row.get("ID").asLong());
        movie.setDirectors(directors);
        
        // Select producers of the movie from PRODUCED relationship
        List<Person> producers = personDAO.movieProducers(row.get("ID").asLong());
        movie.setProducers(producers);
        
        // Select writers of the movie from WROTE relationship
        List<Person> writers = personDAO.movieWriters(row.get("ID").asLong());
        movie.setWriters(writers);
        return movie;
	}
	
	
	public List<Movie> getMovieList(Result result){
		List<Movie> movies = new ArrayList<Movie>();
		while(result.hasNext()) {
			Record row = result.next();
			Value value = row.get("n");
			Movie movie = new Movie();
			Map<String, Object> properties = value.asEntity().asMap();
			movie.setId(row.get("ID").asLong());
			movie.setTitle(String.valueOf(properties.get("title")));
	        movie.setTagline(String.valueOf(properties.get("tagline")));
	        movie.setReleased(Long.valueOf(String.valueOf(properties.get("released"))));
	        movies.add(movie);
		}
	    return movies;
	}
	
	/* Method to get movies list
	 * params: page:int, limit:int, orderby:String, order:String
	 * return: List<Movie> */
	public List<Movie> listMovies(int page, int limit, String orderby, String order) throws Neo4jMovieBusinessException{
		DBConnection db = new DBConnection();
		List<Movie> res = new ArrayList<>();
		try(Session session = db.getDriver().session()){
			res = session.readTransaction(tx -> {
				Result result = tx.run(("MATCH (n:Movie) RETURN ID(n) as ID, n ORDER BY n."+orderby+" "+order+""));
				if(limit > 0) {
					result = tx.run(("MATCH (n:Movie) RETURN ID(n) as ID, n ORDER BY n."+orderby+" "+order+" SKIP "+((page-1)*limit)+" LIMIT "+limit+""));	
				}
				List<Movie> movies = getMovieList(result);
				tx.close();
				return movies;
			});
			session.close();
			if(res.size() == 0) {
				throw new Neo4jMovieBusinessException("Movies list is empty.");
			}
			db.close();
		}catch(ClientException e) {
			e.printStackTrace();
			throw new Neo4jMovieBusinessException("Unable to retrieve movies list.", e);
		}
		return res;
	}
	
	/* Method to get movie's detail
	 * Parameters: id:long
	 * Returns: Movie */
	public Movie getMovieById(long id) throws Neo4jMovieBusinessException{
		DBConnection db = new DBConnection();
		try( Session session = db.getDriver().session() ){
			Movie res = session.readTransaction(tx -> {
				// Get movie details from movie node id
				Result result = tx.run(("MATCH (n:Movie) WHERE ID(n)="+id+" RETURN n, ID(n) as ID"));
				if(result.hasNext()) {
					Record row = result.next();
					Movie movie =  getMovieDetail(row, tx);
					tx.close();
					return movie;
				}
				return null;
			});
			
			if(res == null) {
				throw new Neo4jMovieBusinessException("No movie found.");
			}
			return res;
		}catch(ClientException e) {
			e.printStackTrace();
			throw new Neo4jMovieBusinessException("Unable to retrieve movie's details.", e);
		}
	}
	
	/* Method to get movies list that matched with movie title
	 * Parameters: str:String
	 * Returns: List<Movie> */
	public List<Movie> getMovieByTitle(String str) throws Neo4jMovieBusinessException {
		DBConnection db = new DBConnection();
		List<Movie> res = new ArrayList<>();
		if(str.trim().length() > 0) {
			try( Session session = db.getDriver().session() ){
				res = session.readTransaction(tx -> {
					
					// Find movies that contains given string in movie title
					Result result = tx.run(("MATCH (n:Movie) WHERE n.title CONTAINS $search RETURN n, ID(n) as ID"), parameters("search", str));
					List<Movie> movies = getMovieList(result);	
					tx.close();
					return movies;
				});
				session.close();
				if(res.size() == 0) {
					throw new Neo4jMovieBusinessException("Movies list is empty.");
				}
				db.close();	
			}catch(ClientException e) {
				e.printStackTrace();
				throw new Neo4jMovieBusinessException("Unable to search movies.", e);
			}
		}else{
			throw new Neo4jMovieBusinessException("Search text is empty.");
		}
		return res;
	}
	
	/* Method to add new movie
	 * Parameters: movie:Movie
	 * Returns: Movie */
	public Movie addNewMovie(Movie movie) throws Neo4jMovieBusinessException{
		DBConnection db = new DBConnection();
		try( Session session = db.getDriver().session() ){
			
			// Check if movie with same title name is exists
			Transaction tx1 = db.getDriver().session().beginTransaction();
			Result result = tx1.run(("MATCH (n:Movie {title: $title}) RETURN n"), parameters("title", movie.getTitle()));
			if(result.hasNext() && result.next() != null) {
				throw new Neo4jMovieBusinessException("Movie is already exists");
			}
			
			Movie res = session.writeTransaction(tx -> {
				Map<String, Object> movieParams = new HashMap<String, Object>();
				movieParams.put( "title", movie.getTitle() );
				movieParams.put( "tagline", movie.getTagline() );
				movieParams.put( "released", movie.getReleased() );
				
				// Create new node of movie
				String createMovieQuery = "CREATE (n:Movie {title: $title, tagline: $tagline, released: $released }) RETURN n, ID(n) as ID";
				Result createMovieResult = tx.run(createMovieQuery, movieParams);
				Record record = createMovieResult.single();	
				Movie addedMovie = getMovieDetail(record, tx);
				tx.commit();
				tx.close();
				return addedMovie;
			});
			session.close();
			if(res == null) {
				throw new Neo4jMovieBusinessException("Cannot find movie.");
			}
			db.close();
			return res;
		}
	}
	
	/* Method to delete movie
	 * Parameters: id:Long
	 * Returns: int */
	public Movie deleteMovie(long id) throws Neo4jMovieBusinessException{
		DBConnection db = new DBConnection();
		Movie movie = new Movie();
		try( Session session = db.getDriver().session()){
			Movie deletedMovie = new Movie();
			
			// Check if movie with same title name is exists
			Transaction tx1 = db.getDriver().session().beginTransaction();
			Result result = tx1.run(("MATCH (n:Movie) WHERE id(n)="+id+" RETURN n, id(n) as ID"));
			if(!result.hasNext()) {
				throw new Neo4jMovieBusinessException("Movie does not exists");
			}else {
				Record rec = result.next();
				deletedMovie = getMovieDetail(rec, tx1);
			}
						
			int res = session.writeTransaction(tx -> {
				
				// Delete movie by movie node id
				String deleteMovieQuery = "MATCH (m:Movie) WHERE id(m)="+id+" DETACH DELETE m RETURN COUNT(m) as deletedNodes";
				Result deleteMovieResult = tx.run(deleteMovieQuery);
				Record record = deleteMovieResult.single();
				tx.commit();
				tx.close();
				return record.get("deletedNodes").asInt();
			});
			session.close();
			db.close();
			if(res > 0) {
				movie = deletedMovie;
			}
		}catch(ClientException e) {
			e.printStackTrace();
			throw new Neo4jMovieBusinessException("Unable to delete movie.", e);
		}
		return movie;
	}
	
	/* Method to update movie
	 * Parameters: id:Long
	 * Returns: Movie */
	public Movie updateMovie(long id, Movie movie) throws Neo4jMovieBusinessException{
		DBConnection db = new DBConnection();
		try( Session session = db.getDriver().session() ){
			
			// Check if movie with same title name is exists
			Transaction tx1 = db.getDriver().session().beginTransaction();
			Result result = tx1.run(("MATCH (n:Movie) WHERE id(n)="+id+" RETURN n"));
			if(!result.hasNext()) {
				throw new Neo4jMovieBusinessException("Movie does not exists");
			}
						
			Movie res = session.writeTransaction(tx -> {
				Map<String, Object> movieParams = new HashMap<String, Object>();
				movieParams.put( "title", movie.getTitle() );
				movieParams.put( "tagline", movie.getTagline() );
				movieParams.put( "released", movie.getReleased() );
				
				// Update movie by node id
				String updateMovieQuery = "MATCH (n:Movie) WHERE id(n)="+id+" SET n.title='"+movie.getTitle()+"', n.tagline='"+movie.getTagline()+"', "
						+ "n.released="+movie.getReleased()+" RETURN n, ID(n) as ID";
				Result createMovieResult = tx.run(updateMovieQuery, movieParams);
				Record record = createMovieResult.single();
				Movie updatedMovie = getMovieDetail(record, tx);
				tx.commit();
				tx.close();
				return updatedMovie;
			});
			session.close();
			if(res == null) {
				throw new Neo4jMovieBusinessException("Cannot find movie.");
			}
			db.close();
			return res;
		}catch(ClientException e) {
			e.printStackTrace();
			throw new Neo4jMovieBusinessException("Unable to update movie's details.", e);
		}
	}	
	
	/* Get person movies
	 * params: Long id, String relation
	 * return: List<Movie>
	 */
	private List<Movie> personMovies(long id, String relation) throws Neo4jMovieBusinessException{
		DBConnection db = new DBConnection();
		
		try(Session session = db.getDriver().session()){
			Transaction tx = session.beginTransaction();
			String query = "MATCH (p:Person)-[r:"+relation+"]->(n:Movie) WHERE ID(p)="+id+" RETURN DISTINCT n, ID(n) as ID";
			Result rs = tx.run(query);
			return getMovieList(rs);
		}catch(ClientException e) {
			e.printStackTrace();
			throw new Neo4jMovieBusinessException("Unable to retrieve movies.", e);
		}
	}
	
	/* Get person acted movies
	 * params: long id
	 * return: List<Movie>
	 */
	public List<Movie> getActorMovies(long id) throws Neo4jMovieBusinessException{
		return personMovies(id, "ACTED_IN");
	}
	
	/* Get person directed movies
	 * params: long id
	 * return: List<Movie>
	 */
	public List<Movie> getDirectorMovies(long id) throws Neo4jMovieBusinessException{
		return personMovies(id, "DIRECTED");
	}
	
	/* Get person produced movies
	 * params: long id
	 * return: List<Movie>
	 */
	public List<Movie> getProducerMovies(long id) throws Neo4jMovieBusinessException{
		return personMovies(id, "PRODUCED");
	}
	
	/* Get person wrote movies
	 * params: long id
	 * return: List<Movie>
	 */
	public List<Movie> getWriterMovies(long id) throws Neo4jMovieBusinessException{
		return personMovies(id, "WROTE");
	}
	
	/* Assign person to movie
	 * params: long mId, long pId, String relation
	 * return: int
	 */
	public List<Person> assignPersonToMovie(long mId, List<Person> persons, String relation) throws Neo4jMovieBusinessException{
		DBConnection db = new DBConnection();
		List<Person> result = new ArrayList<>();
		try(Session session = db.getDriver().session()) {
			if(persons.size() > 0) {
				for(Person person : persons) {
					System.out.println(person.getId());
					Person res = session.writeTransaction(tx -> {
						String query = "MATCH (m:Movie), (p:Person) WHERE ID(m)="+mId+" AND ID(p)="+person.getId()+" MERGE (m)<-[r:"+relation+"]-(p) RETURN p, ID(p) as ID";
						Result rs = tx.run(query);
						Person p = new Person();
						if(rs.hasNext()) {
							Record record = rs.next();
							Value value = record.get("p");
							Map<String, Object> properties = value.asEntity().asMap();
							p.setId(record.get("ID").asLong());
							p.setName(String.valueOf(properties.get("name")));
							if(properties.get("born") != null) {
				            	p.setBorn(Long.valueOf(String.valueOf(properties.get("born"))));
				            }
						}
						tx.commit();
						tx.close();
						return p;
					});
				    result.add(res);
				}
			}
		}catch(ClientException e) {
			e.printStackTrace();
			throw new Neo4jMovieBusinessException("Unable to assign actor.", e);
		}
		return result;
	}
	
	/* Assign actor to movie
	 * params: long movieId, Person List
	 * return: List<Person>
	 */
	public List<Person> assignActorsToMovie(long movieId, List<Person> persons) throws Neo4jMovieBusinessException{
		
		return assignPersonToMovie(movieId, persons, "ACTED_IN");
	}
	
	/* Assign director to movie
	 * params: long movieId, long personId
	 * return: List<Person>
	 */
	public List<Person> assignDirectorsToMovie(long movieId, List<Person> persons) throws Neo4jMovieBusinessException{
		return assignPersonToMovie(movieId, persons, "DIRECTED");
	}
	
	/* Assign producer to movie
	 * params: long movieId, long personId
	 * return: List<Person>
	 */
	public List<Person> assignProducersToMovie(long movieId, List<Person> persons) throws Neo4jMovieBusinessException{
		return assignPersonToMovie(movieId, persons, "PRODUCED");
	}
	
	/* Assign director to movie
	 * params: long movieId, long personId
	 * return: List<Person>
	 */
	public List<Person> assignWritersToMovie(long movieId, List<Person> persons) throws Neo4jMovieBusinessException{
		return assignPersonToMovie(movieId, persons, "WROTE");
	}
	
	/* Assign person to movie
	 * params: long mId, long pId, String relation
	 * return: int
	 */
	public List<Person> removePersonFromMovie(long mId, List<Person> persons, String relation) throws Neo4jMovieBusinessException{
		DBConnection db = new DBConnection();
		List<Person> result = new ArrayList<>();
		try(Session session = db.getDriver().session()) {
			for(Person person : persons) {
				System.out.println(person.getId());
				Person res = session.writeTransaction(tx -> {
					String query = "MATCH (m:Movie), (p:Person), (m)<-[r:"+relation+"]-(p) WHERE ID(m)="+mId+" AND ID(p)="+person.getId()+" DELETE r RETURN p, ID(p) as ID";
					Result rs = tx.run(query);
					Person p = new Person();
					if(rs.hasNext()) {
						Record record = rs.next();
						Value value = record.get("p");
						Map<String, Object> properties = value.asEntity().asMap();
						p.setId(record.get("ID").asLong());
						p.setName(String.valueOf(properties.get("name")));
						if(properties.get("born") != null) {
			            	p.setBorn(Long.valueOf(String.valueOf(properties.get("born"))));
			            }
					}
					tx.commit();
					tx.close();
					return p;
				});
			    result.add(res);
			}
		}catch(ClientException e) {
			e.printStackTrace();
			throw new Neo4jMovieBusinessException("Unable to unassign actor.", e);
		}
		return result;
	}
	
	/* Unassign actors from movie
	 * params: long movieId, Person List
	 * return: List<Person>
	 */
	public List<Person> removeActorsFromMovie(long movieId, List<Person> persons) throws Neo4jMovieBusinessException{
		
		return removePersonFromMovie(movieId, persons, "ACTED_IN");
	}
	
	/* Unassign director from movie
	 * params: long movieId, long personId
	 * return: List<Person>
	 */
	public List<Person> removeDirectorsFromMovie(long movieId, List<Person> persons) throws Neo4jMovieBusinessException{
		return removePersonFromMovie(movieId, persons, "DIRECTED");
	}
	
	/* Unassign producer from movie
	 * params: long movieId, long personId
	 * return: List<Person>
	 */
	public List<Person> removeProducersFromMovie(long movieId, List<Person> persons) throws Neo4jMovieBusinessException{
		return removePersonFromMovie(movieId, persons, "PRODUCED");
	}
	
	/* Unassign director from movie
	 * params: long movieId, long personId
	 * return: List<Person>
	 */
	public List<Person> removeWritersFromMovie(long movieId, List<Person> persons) throws Neo4jMovieBusinessException{
		return removePersonFromMovie(movieId, persons, "WROTE");
	}
}
