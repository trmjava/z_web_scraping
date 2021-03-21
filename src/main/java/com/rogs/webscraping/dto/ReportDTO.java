package com.rogs.webscraping.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportDTO {
	
	String extension;
	
	private Long totalNumberOfLines;
	
	private Long totalNumberOfBytes; 

}
