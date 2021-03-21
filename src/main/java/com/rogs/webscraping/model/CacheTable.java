package com.rogs.webscraping.model;

import java.time.LocalDateTime;
import java.util.List;

import com.rogs.webscraping.dto.ReportDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CacheTable {

	private LocalDateTime expirationTime;
	
	private List<ReportDTO> content;
	
}
