package fr.epita.neo4j.launcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages="fr.epita")
public class SpringBootLauncher extends SpringBootServletInitializer{

	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SpringBootLauncher.class);
    }
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootLauncher.class, args);
	}


}
