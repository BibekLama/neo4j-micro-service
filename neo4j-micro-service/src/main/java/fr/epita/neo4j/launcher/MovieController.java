package fr.epita.neo4j.launcher;

import java.util.List;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.epita.neo4j.datamodels.Movie;
import fr.epita.neo4j.datamodels.Person;
import fr.epita.neo4j.services.business.Neo4jMovieBusinessException;
import fr.epita.neo4j.services.business.Neo4jPersonBusinessException;
import fr.epita.neo4j.services.data.Neo4jMovieDAO;

@RestController
public class MovieController {
	
	@Inject
	Neo4jMovieDAO dao;
	
	/* Method: GET
	 * URL: http://localhost:19080/movies?page=1&limit=5&orderBy=title&order=ASC
	 * return: List<Movie> */
	@CrossOrigin(origins = "*")
	@GetMapping(value = "/movies")
	public List<Movie> listMoviesPaginationSort(
			@RequestParam(value = "page",required = false, defaultValue="0") int page, 
			@RequestParam(value = "limit",required = false, defaultValue="0") int limit,
			@RequestParam(value = "orderBy",required = false, defaultValue="released") String orderby, 
			@RequestParam(value = "order",required = false, defaultValue="DESC") String order) throws Neo4jMovieBusinessException {
		
		return dao.listMovies(page, limit, orderby, order);
		
	}
	
	/* Method: GET
	 * URL: http://localhost:19080/movies/{id}
	 * return: Movie */
	@CrossOrigin(origins = "*")
	@GetMapping(value = "/movies/{id}")
	public Movie getMovieById(@PathVariable Long id) throws Neo4jMovieBusinessException {
		return dao.getMovieById(id);
	}
	
	/* Method: GET
	 * URL: http://localhost:19080/movies/search?title=Matrix
	 * return: List<Movie> */
	@CrossOrigin(origins = "*")
	@GetMapping(value = "/movies/search")
	public List<Movie> searchMovieByTitle(
			@RequestParam(value = "title",required = true) String title) throws Neo4jMovieBusinessException{
		
		return dao.getMovieByTitle(title);
		
	}
	
	/* Method: POST
	 * URL: http://localhost:19080/movies/new
	 * body: movie:Movie
	 * return: Movie */
	@CrossOrigin(origins = "*")
	@PostMapping(value = "/movies")
	public Movie addMovie(@RequestBody Movie movie) throws Neo4jMovieBusinessException {
		return dao.addNewMovie(movie);
	}
	
	/* Method: DELETE
	 * URL: http://localhost:19080/movies/{id}
	 * return: Movie */
	@CrossOrigin(origins = "*")
	@DeleteMapping(value = "/movies/{id}")
	public Movie deleteMovie(@PathVariable Long id) throws Neo4jMovieBusinessException {
		return dao.deleteMovie(id);
	}
	
	/* Method: PATCH
	 * URL: http://localhost:19080/movies/{id}
	 * return: Movie */
	@CrossOrigin(origins = "*")
	@PatchMapping(value = "/movies/{id}")
	public Movie updateMovie(@PathVariable Long id, @RequestBody Movie movie) throws Neo4jMovieBusinessException {
		return dao.updateMovie(id, movie);
	}
	
	@CrossOrigin(origins = "*")
	@GetMapping(value="/movies/{movieId}/actors/{personId}")
	public List<Person> assignActorToMovie(@PathVariable long personId, @PathVariable long movieId) throws Neo4jPersonBusinessException, Neo4jMovieBusinessException{
		return dao.assignActorToMovie(movieId, personId);
	}
	
	@CrossOrigin(origins = "*")
	@GetMapping(value="/movies/{movieId}/directors/{personId}")
	public List<Person> assignDirectorToMovie(@PathVariable long personId, @PathVariable long movieId) throws Neo4jPersonBusinessException, Neo4jMovieBusinessException{
		return dao.assignDirectorToMovie(movieId, personId);
	}
	
	@CrossOrigin(origins = "*")
	@GetMapping(value="/movies/{movieId}/producers/{personId}")
	public List<Person> assignProducerToMovie(@PathVariable long personId, @PathVariable long movieId) throws Neo4jPersonBusinessException, Neo4jMovieBusinessException{
		return dao.assignProducerToMovie(movieId, personId);
	}
	
	@CrossOrigin(origins = "*")
	@GetMapping(value="/movies/{movieId}/writers/{personId}")
	public List<Person> assignWriterToMovie(@PathVariable long personId, @PathVariable long movieId) throws Neo4jPersonBusinessException, Neo4jMovieBusinessException{
		return dao.assignWriterToMovie(movieId, personId);
	}
}
