package fr.epita.neo4j.services.data;

import static org.neo4j.driver.Values.parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.neo4j.driver.Result;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Value;
import org.neo4j.driver.exceptions.ClientException;
import org.springframework.stereotype.Repository;

import fr.epita.neo4j.connection.DBConnection;
import fr.epita.neo4j.datamodels.Movie;
import fr.epita.neo4j.datamodels.Person;
import fr.epita.neo4j.services.business.Neo4jMovieBusinessException;
import fr.epita.neo4j.services.business.Neo4jPersonBusinessException;


@Repository
public class Neo4jPersonDAO {
	
	@Inject
	Neo4jMovieDAO movieDAO;
	
	/* Get person list
	 * params: page:int, limit:int, orderby:String, order:String, relation:String
	 * return: List<Person>
	 */
	public List<Person> personList(int page, int limit, String orderby, String order, String relation) throws Neo4jPersonBusinessException{
		DBConnection db = new DBConnection();
		List<Person> persons = new ArrayList<>();
		try(Session session = db.getDriver().session()){
			
			Transaction tx = session.beginTransaction();
			
			String query = "MATCH (p:Person) RETURN DISTINCT ID(p) as ID, p ORDER BY p."+orderby+" "+order+"";
			
			if(orderby.equals("id")) {
				query = "MATCH (p:Person) RETURN DISTINCT ID(p) as ID, p ORDER BY ID(p) "+order+" ";
			}
			
			if(!relation.trim().equals("")) {
				query = "MATCH (p:Person)-[r:"+relation+"]->(m:Movie) RETURN DISTINCT ID(p) as ID, p ORDER BY p."+orderby+" "+order+"";
				if(limit > 0) {
					query = "MATCH (p:Person)-[r:"+relation+"]->(m:Movie) RETURN DISTINCT ID(p) as ID, p ORDER BY p."+orderby+" "+order+" SKIP "+((page-1)*limit)+" LIMIT "+limit+"";	
					if(orderby.equals("id")) {
						query = "MATCH (p:Person)-[r:"+relation+"]->(m:Movie) RETURN DISTINCT ID(p) as ID, p ORDER BY ID(p) "+order+" SKIP "+((page-1)*limit)+" LIMIT "+limit+"";
					}
				}
			}
			
			Result rs = tx.run(query);
			
			while (rs.hasNext()) {
	            Record row = rs.next();
	            Value value = row.get("p");
	            Map<String, Object> properties = value.asEntity().asMap();
	            Person person = new Person();
	            person.setId(row.get("ID").asLong());
	            person.setName(String.valueOf(properties.get("name")));
	            if(properties.get("born") != null) {
	            	person.setBorn(Long.valueOf(String.valueOf(properties.get("born"))));
	            }
	            persons.add(person);
	        }
			
			if(persons.size() == 0) {
				throw new Neo4jPersonBusinessException("Person list is empty.");
			}
		}catch(ClientException e) {
			e.printStackTrace();
		}
		return persons;
	}
	
	/* Get actors
	 * params: page:int, limit:int, orderby:String, order:String
	 * return: List<Person>
	 */
	public List<Person> actorList(int page, int limit, String orderby, String order) throws Neo4jPersonBusinessException {
		return personList(page, limit, orderby, order, "ACTED_IN");
	}
	
	/* Get directors
	 * params: page:int, limit:int, orderby:String, order:String
	 * return: List<Person>
	 */
	public List<Person> directorList(int page, int limit, String orderby, String order) throws Neo4jPersonBusinessException {
		return personList(page, limit, orderby, order, "DIRECTED");
	}
	
	/* Get producers
	 * params: page:int, limit:int, orderby:String, order:String
	 * return: List<Person>
	 */
	public List<Person> producerList(int page, int limit, String orderby, String order) throws Neo4jPersonBusinessException {
		return personList(page, limit, orderby, order, "PRODUCED");
	}
	
	/* Get writers
	 * params: page:int, limit:int, orderby:String, order:String
	 * return: List<Person>
	 */
	public List<Person> writerList(int page, int limit, String orderby, String order) throws Neo4jPersonBusinessException {
		return personList(page, limit, orderby, order, "WROTE");
	}
	
	/* Get person list in movies
	 * params: String relation
	 * return: List<Person>
	 */
	private List<Person> moviePersonList(long id, String relation){
		DBConnection db = new DBConnection();
		List<Person> persons = new ArrayList<>();
		try(Session session = db.getDriver().session()){
			Transaction tx = session.beginTransaction();
			Result rs = tx.run(("MATCH (n:Movie)<-[r:"+relation+"]-(p:Person) WHERE ID(n)="+id+" RETURN p, ID(p) as ID"));
			while (rs.hasNext()) {
	            Record row = rs.next();
	            Value value = row.get("p");
	            Map<String, Object> properties = value.asEntity().asMap();
	            Person person = new Person();
	            person.setId(row.get("ID").asLong());
	            person.setName(String.valueOf(properties.get("name")));
	            if(properties.get("born") != null) {
	            	person.setBorn(Long.valueOf(String.valueOf(properties.get("born"))));
	            }
	            persons.add(person);
	        }
		}catch(ClientException e) {
			e.printStackTrace();
		}
		return persons;
	}
	
	/* Get movie's actor
	 * params: long id
	 * return: List<Person>
	 */
	public List<Person> movieActors(long id) {
		return moviePersonList(id, "ACTED_IN");
	}
	
	/* Get movie's directors
	 * params: long id
	 * return: List<Person>
	 */
	public List<Person> movieDirectors(long id) {
		return moviePersonList(id, "DIRECTED");
	}
	
	/* Get movie's producers
	 * params: long id
	 * return: List<Person>
	 */
	public List<Person> movieProducers(long id) {
		return moviePersonList(id, "PRODUCED");
	}
	
	/* Get movie's writers
	 * params: long id
	 * return: List<Person>
	 */
	public List<Person> movieWriters(long id) {
		return moviePersonList(id, "WROTE");
	}
	
	/* Add person
	 * params: Person person
	 * return: Person
	 */
	public Person addPerson(Person person) throws Neo4jPersonBusinessException{
		DBConnection db = new DBConnection();
		Person newPerson = new Person();
		// Check if person with same name is exists
		Transaction tx1 = db.getDriver().session().beginTransaction();
		Result result = tx1.run(("MATCH (n:Person {name: $title, born: $born}) RETURN n"), parameters("name", person.getName(), "born", person.getBorn()));
		if(result.hasNext() && result.next() != null) {
			throw new Neo4jPersonBusinessException("Person is already exists");
		}
		try(Session session = db.getDriver().session()){
			newPerson = session.writeTransaction(tx -> {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put( "name", person.getName() );
				params.put( "born", person.getBorn() );
				String query = "CREATE (n:Person {name: $name, born: $born}) RETURN n, ID(n) as ID";
				Result rs = tx.run(query, params);
				Record record = rs.single();
				try {
					return getPersonById(record.get("ID").asLong());
				} catch (Neo4jPersonBusinessException | Neo4jMovieBusinessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			});
		}catch(ClientException e) {
			e.printStackTrace();
			throw new Neo4jPersonBusinessException("Unable to add person", e);
		}
		return newPerson;
	}
	
	public Person getPersonById(long id) throws Neo4jPersonBusinessException, Neo4jMovieBusinessException {
		Person person = new Person();
		DBConnection db = new DBConnection();
		try(Session session = db.getDriver().session()){
			Transaction tx = session.beginTransaction();
			String query = "MATCH (n:Person) WHERE ID(n)="+id+" RETURN n, ID(n) as ID";
			Result rs = tx.run(query);
			if(rs.hasNext()) {
				Record record = rs.next();
				Value value = record.get("n");
				Map<String, Object> properties = value.asEntity().asMap();
				person.setId(record.get("ID").asLong());
				person.setName(String.valueOf(properties.get("name")));
				if(properties.get("born") != null) {
	            	person.setBorn(Long.valueOf(String.valueOf(properties.get("born"))));
	            }
				person.setActedIn(movieDAO.getActorMovies(record.get("ID").asLong()));
				person.setDirected(movieDAO.getDirectorMovies(record.get("ID").asLong()));
				person.setProduced(movieDAO.getProducerMovies(record.get("ID").asLong()));
				person.setWrote(movieDAO.getWriterMovies(record.get("ID").asLong()));
			}else {
				throw new Neo4jPersonBusinessException("Person not added.");
			}
		}catch(ClientException e) {
			e.printStackTrace();
			throw new Neo4jPersonBusinessException("Unable to retrieve person", e);
		}
		return person;
	}
	
	/* Assign movie to person
	 * params: long mId, long pId, String relation
	 * return: int
	 */
	public int assignMovieToPerson(long pId, long mId, String relation) throws Neo4jPersonBusinessException{
		DBConnection db = new DBConnection();
		int res = 0;
		try(Session session = db.getDriver().session()) {
			Transaction tx1 = db.getDriver().session().beginTransaction();
			String query1 = "MATCH (m:Movie)<-[r:"+relation+"]->(p:Person) WHERE ID(m)="+mId+" AND ID(p)="+pId+" RETURN COUNT(r) AS count";
			Result result = tx1.run(query1);
			if(result.hasNext()) {
				Record rec = result.next();
				if(rec.get("count").asInt() > 0) {
					throw new Neo4jPersonBusinessException("Movie is already assigned.");
				}
			}
			res = session.writeTransaction(tx -> {
				String query = "MATCH (m:Movie),(p:Person) WHERE ID(m)="+mId+" AND ID(p)="+pId+" CREATE (m)<-[r:"+relation+"]-(p) RETURN COUNT(r) AS count";
				Result rs = tx.run(query);
				int count = 0;
				if(rs.hasNext()) {
					Record record = rs.next();
					count = record.get("count").asInt();
				}
				tx.commit();
				tx.close();
				return count;
			});
		}catch(ClientException e) {
			e.printStackTrace();
			throw new Neo4jPersonBusinessException("Unable to assign movie.", e);
		}
		return res;
	}
	
	/* Assign movie to actor
	 * params: long movieId, long personId
	 * return: List<Movie>
	 */
	public List<Movie> assignMovieToActor(long movieId, long personId) throws Neo4jPersonBusinessException, Neo4jMovieBusinessException{
		List<Movie> movies = new ArrayList<>();
		if(assignMovieToPerson(personId, movieId, "ACTED_IN") == 1) {
			movies = movieDAO.getActorMovies(personId);
		}
		return movies;
	}
	
	/* Assign movie to director
	 * params: long movieId, long personId
	 * return: List<Movie>
	 */
	public List<Movie> assignMovieToDirector(long movieId, long personId) throws Neo4jPersonBusinessException, Neo4jMovieBusinessException{
		List<Movie> movies = new ArrayList<>();
		if(assignMovieToPerson(personId, movieId, "DIRECTED") == 1) {
			movies = movieDAO.getDirectorMovies(personId);
		}
		return movies;
	}
	
	/* Assign movie to producer
	 * params: long movieId, long personId
	 * return: List<Movie>
	 */
	public List<Movie> assignMovieToProducer(long movieId, long personId) throws Neo4jPersonBusinessException, Neo4jMovieBusinessException{
		List<Movie> movies = new ArrayList<>();
		if(assignMovieToPerson(personId, movieId, "PRODUCED") == 1) {
			movies = movieDAO.getProducerMovies(personId);
		}
		return movies;
	}
	
	/* Assign movie to witer
	 * params: long movieId, long personId
	 * return: List<Movie>
	 */
	public List<Movie> assignMovieToWriter(long movieId, long personId) throws Neo4jPersonBusinessException, Neo4jMovieBusinessException{
		List<Movie> movies = new ArrayList<>();
		if(assignMovieToPerson(personId, movieId, "WROTE") == 1) {
			movies = movieDAO.getWriterMovies(personId);
		}
		return movies;
	}
}
