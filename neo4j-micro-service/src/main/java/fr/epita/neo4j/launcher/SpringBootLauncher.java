package fr.epita.neo4j.launcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages="fr.epita")
public class SpringBootLauncher {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootLauncher.class, args);
	}


}
