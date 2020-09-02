package fr.epita.neo4j.datamodels;

import java.util.List;

public class Movie{
	private Long id;
	private String title;
	private String tagline;
	private Long released;
	private List<Person> actors;
	private List<Person> directors;
	private List<Person> producers;
	private List<Person> writers;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTagline() {
		return tagline;
	}
	public void setTagline(String tagline) {
		this.tagline = tagline;
	}
	public List<Person> getActors() {
		return actors;
	}
	public void setActors(List<Person> actors) {
		this.actors = actors;
	}
	public List<Person> getDirectors() {
		return directors;
	}
	public void setDirectors(List<Person> directors) {
		this.directors = directors;
	}
	public List<Person> getProducers() {
		return producers;
	}
	public void setProducers(List<Person> producers) {
		this.producers = producers;
	}
	public List<Person> getWriters() {
		return writers;
	}
	public void setWriters(List<Person> writers) {
		this.writers = writers;
	}
	public Long getReleased() {
		return this.released;
	}
	public void setReleased(Long released) {
		this.released = released;
	}
}
