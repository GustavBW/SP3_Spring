package gbw.sp3.OpcClient;

import gbw.sp3.OpcClient.AsyncEventLoop.EntryPoint;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class OpcClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpcClientApplication.class, args);
		EntryPoint.initialize(8);
	}

	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/client").allowedOrigins("*");
			}
		};
	}
}
