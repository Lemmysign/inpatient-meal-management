package com.hospital.meal;

import com.hospital.meal.config.HISProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(HISProperties.class)
public class HospitalMealOrderingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(HospitalMealOrderingSystemApplication.class, args);
	}
}