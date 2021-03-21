package com.rogs.webscraping.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Branch {

	private String URL;
	
	private BranchType type;
	
}
