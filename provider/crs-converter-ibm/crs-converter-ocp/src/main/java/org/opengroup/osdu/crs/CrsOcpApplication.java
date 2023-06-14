/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.crs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@ComponentScan({"org.opengroup.osdu"})
@SpringBootApplication
@PropertySource("classpath:swagger.properties")
public class CrsOcpApplication extends CRSApplicationBase {

	public static void main(String[] args) {
		SpringApplication.run(CrsOcpApplication.class, args);
	}
	
	//@Bean
//	public JaxRsDpsLog jaxRsDpsLog() {
//		return new JaxRsDpsLog(new DefaultLogger(), new DpsHeaders());
//	}
}
