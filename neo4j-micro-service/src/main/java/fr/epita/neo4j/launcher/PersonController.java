package fr.epita.neo4j.launcher;

import java.util.List;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.epita.neo4j.datamodels.Movie;
import fr.epita.neo4j.datamodels.Person;
import fr.epita.neo4j.services.business.Neo4jMovieBusinessException;
import fr.epita.neo4j.services.business.Neo4jPersonBusinessException;
import fr.epita.neo4j.services.data.Neo4jPersonDAO;

@RestController
public class PersonController {
	
	@Inject
	Neo4jPersonDAO dao;
	
	@CrossOrigin(origins = "*")
	@GetMapping(value="/persons")
	public List<Person> personList(
			@RequestParam(value = "page",required = false, defaultValue="0") int page, 
			@RequestParam(value = "limit",required = false, defaultValue="0") int limit,
			@RequestParam(value = "orderBy",required = false, defaultValue="id") String orderby, 
			@RequestParam(value = "order",required = false, defaultValue="DESC") String order) throws Neo4jPersonBusinessException{
		return dao.personList(page, limit, orderby, order, "");
	}
	
	@CrossOrigin(origins = "*")
	@GetMapping(value="/actors")
	public List<Person> actorList(
			@RequestParam(value = "page",required = false, defaultValue="0") int page, 
			@RequestParam(value = "limit",required = false, defaultValue="0") int limit,
			@RequestParam(value = "orderBy",required = false, defaultValue="id") String orderby, 
			@RequestParam(value = "order",required = false, defaultValue="DESC") String order) throws Neo4jPersonBusinessException{
		return dao.actorList(page, limit, orderby, order);
	}
	
	@CrossOrigin(origins = "*")
	@GetMapping(value="/directors")
	public List<Person> directorList(
			@RequestParam(value = "page",required = false, defaultValue="0") int page, 
			@RequestParam(value = "limit",required = false, defaultValue="0") int limit,
			@RequestParam(value = "orderBy",required = false, defaultValue="id") String orderby, 
			@RequestParam(value = "order",required = false, defaultValue="DESC") String order) throws Neo4jPersonBusinessException{
		return dao.directorList(page, limit, orderby, order);
	}
	
	@CrossOrigin(origins = "*")
	@GetMapping(value="/producers")
	public List<Person> producerList(
			@RequestParam(value = "page",required = false, defaultValue="0") int page, 
			@RequestParam(value = "limit",required = false, defaultValue="0") int limit,
			@RequestParam(value = "orderBy",required = false, defaultValue="id") String orderby, 
			@RequestParam(value = "order",required = false, defaultValue="DESC") String order) throws Neo4jPersonBusinessException{
		return dao.producerList(page, limit, orderby, order);
	}
	
	@CrossOrigin(origins = "*")
	@GetMapping(value="/writers")
	public List<Person> writerList(
			@RequestParam(value = "page",required = false, defaultValue="0") int page, 
			@RequestParam(value = "limit",required = false, defaultValue="0") int limit,
			@RequestParam(value = "orderBy",required = false, defaultValue="id") String orderby, 
			@RequestParam(value = "order",required = false, defaultValue="DESC") String order) throws Neo4jPersonBusinessException{
		return dao.writerList(page, limit, orderby, order);
	}
	
	@PostMapping(value="/persons")
	public Person addPerson(@RequestBody Person person) throws Neo4jPersonBusinessException {
		return dao.addPerson(person);
	}
	
	@CrossOrigin(origins = "*")
	@GetMapping(value="/persons/{id}")
	public Person getPerson(@PathVariable long id) throws Neo4jPersonBusinessException, Neo4jMovieBusinessException{
		return dao.getPersonById(id);
	}
	
	@CrossOrigin(origins = "*")
	@GetMapping(value="/actors/{personId}/movies/{movieId}")
	public List<Movie> assignMovieToActor(@PathVariable long personId, @PathVariable long movieId) throws Neo4jPersonBusinessException, Neo4jMovieBusinessException{
		return dao.assignMovieToActor(movieId, personId);
	}
	
	@CrossOrigin(origins = "*")
	@GetMapping(value="/directors/{personId}/movies/{movieId}")
	public List<Movie> assignMovieToDirector(@PathVariable long personId, @PathVariable long movieId) throws Neo4jPersonBusinessException, Neo4jMovieBusinessException{
		return dao.assignMovieToDirector(movieId, personId);
	}
	
	@CrossOrigin(origins = "*")
	@GetMapping(value="/producers/{personId}/movies/{movieId}")
	public List<Movie> assignMovieToProducer(@PathVariable long personId, @PathVariable long movieId) throws Neo4jPersonBusinessException, Neo4jMovieBusinessException{
		return dao.assignMovieToProducer(movieId, personId);
	}
	
	@CrossOrigin(origins = "*")
	@GetMapping(value="/writers/{personId}/movies/{movieId}")
	public List<Movie> assignMovieToWriter(@PathVariable long personId, @PathVariable long movieId) throws Neo4jPersonBusinessException, Neo4jMovieBusinessException{
		return dao.assignMovieToWriter(movieId, personId);
	}

}
