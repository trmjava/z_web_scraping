package com.rogs.webscraping.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GitResultDTO {
	
	String gitRepository;
	
	List<ReportDTO> body; 
	
    LocalDateTime expirationTime;
}
