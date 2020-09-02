package fr.epita.neo4j.datamodels;

import java.util.List;

public class Person {
	private long id;
	private String name;
	private long born;
	private List<Movie> actedIn;
	private List<Movie> directed;
	private List<Movie> produced;
	private List<Movie> wrote;

	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getBorn() {
		return born;
	}
	public void setBorn(long born) {
		this.born = born;
	}
	public List<Movie> getActedIn() {
		return actedIn;
	}
	public void setActedIn(List<Movie> actedIn) {
		this.actedIn = actedIn;
	}
	public List<Movie> getDirected() {
		return directed;
	}
	public void setDirected(List<Movie> directed) {
		this.directed = directed;
	}
	public List<Movie> getProduced() {
		return produced;
	}
	public void setProduced(List<Movie> produced) {
		this.produced = produced;
	}
	public List<Movie> getWrote() {
		return wrote;
	}
	public void setWrote(List<Movie> wrote) {
		this.wrote = wrote;
	}
	
}
