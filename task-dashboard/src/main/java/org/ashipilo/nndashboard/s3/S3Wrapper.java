package org.ashipilo.nndashboard.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class S3Wrapper {

	private final AmazonS3 amazonS3Client;

	@Value("${cloud.aws.s3.bucket}")
	private String defaultBucket;

	@Autowired
	public S3Wrapper(AmazonS3 amazonS3Client) {
		this.amazonS3Client = amazonS3Client;
	}

	public List<S3ObjectSummary> list(String bucket) {

		if (bucket.isEmpty()) {
			bucket = defaultBucket;
		}

		ObjectListing objectListing = amazonS3Client.listObjects(new ListObjectsRequest().withBucketName(bucket));

		return objectListing.getObjectSummaries();
	}

	public List<Bucket> list_buckets() {
		return amazonS3Client.listBuckets();
	}
}
