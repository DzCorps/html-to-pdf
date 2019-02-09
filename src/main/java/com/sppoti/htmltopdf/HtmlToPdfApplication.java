package com.sppoti.htmltopdf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@Slf4j
@SpringBootApplication
public class HtmlToPdfApplication {
	
	private final HtmlToPdfConverterService htmlToPdfConverterService;
	
	@Autowired public HtmlToPdfApplication(HtmlToPdfConverterService htmlToPdfConverterService) {
		this.htmlToPdfConverterService = htmlToPdfConverterService;
	}
	
	public static void main(String[] args) {
		SpringApplication.run(HtmlToPdfApplication.class, args);
	}
	
	@PostConstruct
	public void setup() {
		log.info("Convert and save html with css [sppoti1]");
		htmlToPdfConverterService.convertAndSaveHtmlFile("sppoti.html", "sppoti1.css");
		log.info("---> DONE");

		log.info("Convert and save html with css [sppoti2]");
		htmlToPdfConverterService.convertAndSaveHtmlFile("sppoti.html", "sppoti2.css");
		log.info("---> DONE");

		System.exit(0);
	}
}

