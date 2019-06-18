package org.ashipilo.s3.controllers;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.ashipilo.s3.s3.S3Wrapper;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/aws/s3")
public class UploadController {

	private final S3Wrapper s3Wrapper;

	@Autowired
	public UploadController(S3Wrapper s3Wrapper) {
		this.s3Wrapper = s3Wrapper;
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public List<PutObjectResult> upload(@RequestParam("file") MultipartFile[] multipartFiles) {
		return s3Wrapper.upload(multipartFiles);
	}

	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public ResponseEntity<byte[]> download(@RequestParam String key) throws IOException {
		return s3Wrapper.download(key);
	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public ResponseEntity<String> delete(@RequestParam String key) throws Exception {
		return s3Wrapper.delete(key);
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public List<S3ObjectSummary> list(@RequestParam String bucket) throws IOException {
		return s3Wrapper.list(bucket);
	}

	@RequestMapping(value = "/list_buckets", method = RequestMethod.GET)
	public List<Bucket> list_buckets() throws IOException {
		return s3Wrapper.list_buckets();
	}

	@RequestMapping(value = "/create_bucket", method = RequestMethod.POST)
	public Bucket create_bucket(@RequestParam String bucket) throws Exception {
		return s3Wrapper.create_bucket(key);
	}
}
