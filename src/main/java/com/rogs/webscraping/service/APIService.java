package com.rogs.webscraping.service;

import java.io.BufferedInputStream;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rogs.webscraping.dto.GitResultDTO;
import com.rogs.webscraping.dto.ReportDTO;
import com.rogs.webscraping.model.Branch;
import com.rogs.webscraping.model.BranchType;
import com.rogs.webscraping.model.CacheTable;

@Service
public class APIService {

	private static HashMap<String, CacheTable> cacheData = new HashMap<String, CacheTable>();

	public GitResultDTO getGitFilesreport(String URL) throws Exception {

		if (APIService.hasCache(URL)) {
			return GitResultDTO.builder().gitRepository(URL).body(APIService.getCacheData(URL))
					.expirationTime(APIService.getCacheExpirationTime(URL)).build();
		}

		HashMap<String, ReportDTO> resultList = new HashMap<String, ReportDTO>();
		LinkedList<Branch> branches = new LinkedList<Branch>();

		branches.add(Branch.builder().URL(URL).type(BranchType.TREE).build());

		while (branches.size() > 0) {
			Branch br = branches.removeFirst();
			if (br.getType() == BranchType.TREE) {
				branches.addAll(this.getTREEBranches(br.getURL()));
			} else {
				ReportDTO data = this.getBLOBFileInfo(br.getURL());

				if (!resultList.containsKey(data.getExtension()))
					resultList.put(data.getExtension(), data);
				else {
					resultList.get(data.getExtension()).setTotalNumberOfBytes(
							resultList.get(data.getExtension()).getTotalNumberOfBytes() + data.getTotalNumberOfBytes());
					resultList.get(data.getExtension()).setTotalNumberOfLines(
							resultList.get(data.getExtension()).getTotalNumberOfLines() + data.getTotalNumberOfLines());
				}

			}
		}

		ArrayList<ReportDTO> result = new ArrayList<ReportDTO>(resultList.values());
		APIService.saveCache(URL, result);

		return GitResultDTO.builder().gitRepository(URL).body(result)
				.expirationTime(APIService.getCacheExpirationTime(URL)).build();
	}

	private static LocalDateTime getCacheExpirationTime(String URL) {

		return APIService.cacheData.get(URL).getExpirationTime();
	}

	private static void saveCache(String URL, ArrayList<ReportDTO> result) {
		synchronized (APIService.cacheData) {
			APIService.cacheData.put(URL,
					CacheTable.builder().content(result).expirationTime(LocalDateTime.now().plusHours(4L)).build());
		}
	}

	private static List<ReportDTO> getCacheData(String URL) {
		return APIService.cacheData.get(URL).getContent();
	}

	private static boolean hasCache(String url) {
		if (!APIService.cacheData.containsKey(url)) {
			return false;
		}
		if (APIService.cacheData.get(url).getExpirationTime().compareTo(LocalDateTime.now()) > 0) {
			return true;
		} else {
			synchronized (APIService.cacheData) {
				APIService.cacheData.remove(url);
			}
			return false;
		}
	}

	private String getURLcontent(String url) throws Exception {
		byte[] buffer = new byte[4096];
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		BufferedInputStream inputStream = new BufferedInputStream((new URL(url)).openStream());

		for (int length; (length = inputStream.read(buffer)) != -1;) {
			result.write(buffer, 0, length);
		}

		return result.toString("UTF-8");

	}

	private List<Branch> getTREEBranches(String url) throws Exception {
		String content = this.getURLcontent(url);
		ArrayList<Branch> branches = new ArrayList<Branch>();
		String path = "";

		if (url.substring(0, 8).toLowerCase().equals("https://")) {
			path = url.substring(18);
		} else {
			path = url.substring(10);
		}

		if (!path.contains("/tree/")) {
			path += "/tree/";
		}
		Pattern p = Pattern.compile(path + "?");
		Matcher m = p.matcher(content);

		while (m.find()) {

			int start = m.end(), end = 0;
			end = start;
			while (content.charAt(end) != '"' && end < content.length()) {
				end++;
			}
			String aux = content.substring(start, end);
			if (!aux.contains("urlEncodedRefName") && aux.contains("/")) {
				branches.add(Branch.builder().type(BranchType.TREE).URL("https://github.com" + path + aux).build());
			}
		}

		path = "";

		if (url.substring(0, 8).toLowerCase().equals("https://")) {
			path = url.substring(18);
		} else {
			path = url.substring(10);
		}

		if (path.contains("/tree/")) {
			path = path.replace("/tree/", "/blob/");
		} else {
			path += "/blob/";
		}

		p = Pattern.compile(path + "?");
		m = p.matcher(content);
		HashSet<String> h = new HashSet<String>();

		while (m.find()) {

			int start = m.end(), end = 0;
			end = start;
			while (content.charAt(end) != '"' && end < content.length()) {
				end++;
			}
			String aux = content.substring(start, end);
			if (aux.contains("/")) {
				h.add("https://github.com" + path + aux);
			}
		}

		h.forEach(s -> branches.add(Branch.builder().type(BranchType.BLOB).URL(s).build()));

		return branches;
	}

	private ReportDTO getBLOBFileInfo(String url) throws Exception {
		String content = this.getURLcontent(url);

		Pattern p = Pattern.compile("text-mono f6 flex-auto pr-3 flex-order-2 flex-md-order-1 mt-2 mt-md-0");
		Matcher m = p.matcher(content);
		m.find();

		int start = m.end(), end = 0;
		Long lines = 0L, bytes = 0L;
		end = start;
		while (!content.substring(end, end + 6).contains("</div>") && (end + 6) < content.length()) {
			end++;
		}
		String aux = content.substring(start + 4, end);
		String aux1 = "", aux2 = "";
		String[] sa;

		if (aux.contains("lines")) {
			sa = aux.split("lines");
			lines = Long.parseLong(sa[0].trim());
		} else {
			lines = 0L;
		}
		sa = aux.split("</span>");

		aux1 = sa[sa.length - 1].trim().split(" ")[0];
		aux2 = sa[sa.length - 1].trim().split(" ")[1].trim().toUpperCase();

		switch (aux2) {
		case "BYTES":
			bytes = Long.parseLong(aux1);
			break;
		case "KB":
			bytes = (long) (Float.parseFloat(aux1) * 1024);
			break;
		case "MB":
			bytes = (long) (Float.parseFloat(aux1) * 1024 * 1024);
			break;
		case "GB":
			bytes = (long) (Float.parseFloat(aux1) * 1024 * 1024 * 1024);
			break;
		}

		String fileExt = url.split("/")[url.split("/").length - 1];
		fileExt = fileExt.split("\\.")[fileExt.split("\\.").length - 1];

		return ReportDTO.builder().totalNumberOfLines(lines).totalNumberOfBytes(bytes).extension(fileExt).build();
	}

	@Async
	public void processGitFilesReportByWebHook(String URLGITRepository, String webHookURL) throws Exception {

		ObjectMapper gson = new ObjectMapper();
		String jsonInputString = gson.writeValueAsString(this.getGitFilesreport(URLGITRepository));

		URL url = new URL(webHookURL);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json; utf-8");
		con.setRequestProperty("Accept", "application/json");
		con.setDoOutput(true);

		try (OutputStream os = con.getOutputStream()) {
			byte[] input = jsonInputString.getBytes("utf-8");
			os.write(input, 0, input.length);
			os.flush();
			os.close();
		}
		con.getResponseCode();
		con.disconnect();
	}
}
