package cloudsufi.nextgen.tms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TmsApplication.class, args);
	}

}
