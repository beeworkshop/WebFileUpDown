package com.example.webfile.webfile.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.any23.encoding.TikaEncodingDetector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileController {

	@Value("${spring.servlet.multipart.location}")
	private String filePath;

	private Map<String, Object> dealResultMap(boolean success, String msg) {
		Map<String, Object> result = new HashMap<>();
		result.put("success", success);
		result.put("message", msg);
		return result;
	}

	private void readCsvByLine(String fileName, String encoding) {

		try {
			String fileFullName = filePath + "/" + fileName;
			File afile = new File(fileFullName);
			if (!afile.exists())
				return;

			FileInputStream fin = new FileInputStream(fileFullName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fin, encoding));

			// 任意编码的文本写入UTF-8编码的文件
			FileOutputStream fout = new FileOutputStream(filePath + "/" + "Converted.txt");
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fout, "UTF-8"));

			String line = "";
			while ((line = br.readLine()) != null) { // 向String line赋值时，默认String是UTF-8编码，因此做了编码UTF-8的隐式转换
				List<String> ls = Arrays.asList(line.split(","));
				for (String str : ls) {
					System.out.print(str.trim() + ", ");

					// 写入新文件
					// bw.write(str.trim() + ", "); //利用上述的隐式UTF-8编码转换

					// 显式字符串编码转换（转换成UTF-8）
					byte[] utf8Bytes = str.getBytes("UTF-8");
					String utf8Str = new String(utf8Bytes, "UTF-8");
					bw.write(utf8Str + ", ");
				}
				System.out.println("");
				bw.newLine();
			}

			bw.close();
			br.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeByLine() {
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
		String fileName = dateFormat.format(now) + ".txt";

		try {
			// 覆盖已存在的文件
			FileOutputStream fout = new FileOutputStream(filePath + "/" + fileName);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fout, "UTF-8"));

			bw.write("写入示例第一行");
			// 回车换行符根据操作系统自动确定
			bw.newLine();
			bw.write("写入示例第二行");
			bw.newLine();
			bw.write("写入示例第三行");
			bw.newLine();
			bw.write("写入示例第四行");
			bw.newLine();

			// 注：可以使用System.getProperty("line.separator")方法得到当前系统的换行符

			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String guessTextFileEncoding(String fileName) {
		String fileFullName = filePath + "/" + fileName;
		File afile = new File(fileFullName);
		if (!afile.exists())
			return null;

		try {
			FileInputStream is = new FileInputStream(fileFullName);
			return new TikaEncodingDetector().guessEncoding(is);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@PostMapping("/upload/part")
	// http://localhost:8090/upload/part
	// key=name value=filename
	// Content-Disposition: form-data; name="file"; filename="D:/file.any"
	public Map<String, Object> uploadPart(MultipartFile file) {
		String fileName = file.getOriginalFilename();
		String fileType = file.getContentType();
		File dest = new File(fileName);
		try {
			// 保存上传的文件
			file.transferTo(dest);
			System.out.println(filePath);
			System.out.println(fileType);

			// 处理上传的文件
			if (fileType.equals("text/csv")) { // 文本文件是：text/plain
				// 推断上传的csv文件的编码方式
				String encoding = guessTextFileEncoding(fileName);
				System.out.println(encoding);

				// 以推断的编码方式读取csv文件
				readCsvByLine(fileName, encoding);
			}

			// 以UTF-8格式逐行写入文本文件
			writeByLine();

		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
			return dealResultMap(false, "上传失败");
		}

		return dealResultMap(true, "上传成功");
	}
}
