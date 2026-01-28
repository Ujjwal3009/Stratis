package com.upsc.ai;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UpscAiApplication {

	public static void main(String[] args) {
		// Load .env.local if it exists, otherwise fallback to .env
		Dotenv dotenv = Dotenv.configure()
				.filename(".env.local")
				.ignoreIfMissing()
				.load();

		// Set variables as System properties for Spring to pick them up
		dotenv.entries().forEach(entry -> {
			if (System.getProperty(entry.getKey()) == null) {
				System.setProperty(entry.getKey(), entry.getValue());
			}
		});

		SpringApplication.run(UpscAiApplication.class, args);
	}

}
