package org.opengroup.osdu.crs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"org.opengroup.osdu"})
@SpringBootApplication
public class CRSApplication extends CRSApplicationBase {

	public static void main(String[] args) {
		SpringApplication.run(CRSApplication.class, args);
	}

}
