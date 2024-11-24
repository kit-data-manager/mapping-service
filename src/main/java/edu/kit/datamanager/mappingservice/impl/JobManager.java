package edu.kit.datamanager.mappingservice.impl;

import edu.kit.datamanager.mappingservice.domain.JobStatus;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;

/**
 *
 * @author jejkal
 */
@Service
public class JobManager {

	private final ConcurrentMap<String, CompletableFuture<? extends JobStatus>> mapOfJobs;

	public JobManager() {
		mapOfJobs = new ConcurrentHashMap<>();
	}

	public void putJob(String jobId, CompletableFuture<? extends JobStatus> theJob) {
		mapOfJobs.put(jobId, theJob);
	}

	public CompletableFuture<? extends JobStatus> getJob(String jobId) {
		return mapOfJobs.get(jobId);
	}

	public void removeJob(String jobId) {
		mapOfJobs.remove(jobId);
	}
}
