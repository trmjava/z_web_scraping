package com.rogs.webscraping.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rogs.webscraping.dto.GitResultDTO;
import com.rogs.webscraping.service.APIService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class APIController {

		
	@Autowired
	private final APIService service;
	
	/* Develop an API that returns the total number of lines and the total number of bytes of all the files of a given public Github repository, grouped by file extension */
		
	@RequestMapping(value = "/countgitsmallrepo", method = RequestMethod.GET)
	public ResponseEntity<GitResultDTO> getNumberOfLinesAndNumberOfBytesGroupByFileExtension(@RequestParam String URLGITRepository) throws Exception{
				
		return ResponseEntity.ok(service.getGitFilesreport(URLGITRepository));
		
	}
	
	@RequestMapping(value = "/countgitlargerepo", method = RequestMethod.GET)
	public ResponseEntity<String> getNumberOfLinesAndNumberOfBytesGroupByFileExtensionLarge(@RequestParam String URLGITRepository, @RequestParam String WebHookURL) throws Exception{
		
		service.processGitFilesReportByWebHook(URLGITRepository, WebHookURL);
		
		return ResponseEntity.ok("Process started at: " + LocalDateTime.now().toString());
	}
	
	@RequestMapping(value = "/postconfirmation", method = RequestMethod.POST)
	public void processPost(@RequestParam String token,  @RequestBody String body) {
	
		System.out.println("JSON Boby for token: " + token + " is => " +  body);
	}
}
