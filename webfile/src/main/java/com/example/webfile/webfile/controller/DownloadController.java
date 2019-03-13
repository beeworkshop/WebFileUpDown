package com.example.webfile.webfile.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class DownloadController {

	private String filename = "D:\\work\\projects\\MyJavaPrj\\webfile\\src\\main\\resources\\files\\" + "Converted.txt";

	@RequestMapping(value = "/download1", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> downloadFile(Long id) throws IOException {

		System.out.println("这是第1个下载文件方法");

		FileSystemResource file = new FileSystemResource(filename);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getFilename()));
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");

		return ResponseEntity.ok().headers(headers).contentLength(file.contentLength())
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.body(new InputStreamResource(file.getInputStream()));
	}

	@RequestMapping(value = "/download2", method = RequestMethod.GET)
	public void getDownload(Long id, HttpServletRequest request, HttpServletResponse response) {

		System.out.println("这是第2个下载文件方法");

		// Get your file stream from wherever.
		File downloadFile = new File(filename);

		ServletContext context = request.getServletContext();

		// get MIME type of the file
		String mimeType = context.getMimeType(filename);
		if (mimeType == null) {
			// set to binary type if MIME mapping not found
			mimeType = "application/octet-stream";
			System.out.println("context getMimeType is null");
		}
		System.out.println("MIME type: " + mimeType);

		// set content attributes for the response
		response.setContentType(mimeType);
		response.setContentLength((int) downloadFile.length());

		// set headers for the response
		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
		response.setHeader(headerKey, headerValue);

		// Copy the stream to the response's output stream.
		try {
			InputStream myStream = new FileInputStream(filename);
			IOUtils.copy(myStream, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping("/download3")
	public void downloadFile(HttpServletRequest request, HttpServletResponse response) {

		System.out.println("这是第3个下载文件方法");

		if (filename != null) {
			// 设置文件路径
			File file = new File(filename);
			if (file.exists()) {
				response.setContentType("application/octet-stream");
				response.setHeader("content-type", "application/octet-stream");
				response.setHeader("Content-Disposition", "attachment;fileName=" + "Converted.txt");// 设置文件名
				byte[] buffer = new byte[1024];
				FileInputStream fis = null;
				BufferedInputStream bis = null;
				try {
					fis = new FileInputStream(file);
					bis = new BufferedInputStream(fis);
					OutputStream os = response.getOutputStream();
					int i = bis.read(buffer);
					while (i != -1) {
						os.write(buffer, 0, i);
						i = bis.read(buffer);
					}
					System.out.println("success");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (bis != null) {
						try {
							bis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (fis != null) {
						try {
							fis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
