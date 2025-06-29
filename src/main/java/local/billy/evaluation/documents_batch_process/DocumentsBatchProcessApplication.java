package local.billy.evaluation.documents_batch_process;

import java.util.Base64;
import java.util.Base64.Decoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

import local.billy.evaluation.documents_batch_process.camel_routes.DocumentProcessor;

@SpringBootApplication
public class DocumentsBatchProcessApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentsBatchProcessApplication.class, args);
	}

	@Bean
	public ObjectMapper objectMapper() { return new ObjectMapper(); }
	
	@Bean
	public Decoder decoder() { return Base64.getDecoder();}

	@Bean
	public DocumentProcessor documentProcessor(ObjectMapper objectMapper, Decoder decoder){
		return new DocumentProcessor(objectMapper, decoder);
	}

}
