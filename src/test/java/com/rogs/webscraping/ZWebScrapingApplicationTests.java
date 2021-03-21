package com.rogs.webscraping;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.rogs.webscraping.controller.APIController;
import com.rogs.webscraping.dto.GitResultDTO;
import com.rogs.webscraping.dto.ReportDTO;
import com.rogs.webscraping.service.APIService;

@SpringBootTest
@AutoConfigureMockMvc
class ZWebScrapingApplicationTests {

	static String WEB_SCRAPING_API = "/countgitsmallrepo?URLGITRepository=https://github.com/aws-samples/aws-cognito-java-desktop-app";

	private MockMvc mvc;

	private APIService service;
	private APIController controller;
	
	public ZWebScrapingApplicationTests() {
		service = Mockito.mock(APIService.class);
		controller = new APIController(service);
	}

	@BeforeEach
	void setUp() {
		mvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	@DisplayName("Controller Test")
	void processTest() throws Exception {

		ArrayList<ReportDTO> resultFake = new ArrayList<ReportDTO>();
		resultFake.add(ReportDTO.builder().extension("Java").totalNumberOfBytes(10L).totalNumberOfLines(1L).build());
		resultFake.add(ReportDTO.builder().extension("Txt").totalNumberOfBytes(30L).totalNumberOfLines(3L).build());

		when(service.getGitFilesreport("https://github.com/aws-samples/aws-cognito-java-desktop-app"))
				.thenReturn(GitResultDTO.builder().gitRepository("Mock-Service").body(resultFake)
						.expirationTime(LocalDateTime.now()).build());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(WEB_SCRAPING_API)
				.accept(MediaType.APPLICATION_JSON);

		mvc.perform(request).andDo(print()).andExpect(status().isOk());
	}

}
