package com.rfidreader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RfidReaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(RfidReaderApplication.class, args);
		RFIDReader.checkPort();
	}

}
