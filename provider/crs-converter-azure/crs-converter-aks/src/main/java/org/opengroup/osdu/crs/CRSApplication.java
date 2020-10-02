package org.opengroup.osdu.crs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CRSApplication extends CRSApplicationBase {

	public static void main(String[] args) {
		SpringApplication.run(CRSApplication.class, args);
	}

}
