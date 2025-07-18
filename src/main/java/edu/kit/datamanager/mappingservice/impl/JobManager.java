/*
 * Copyright 2024 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.mappingservice.impl;

import edu.kit.datamanager.mappingservice.domain.JobStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author jejkal
 */
@Service
public class JobManager {

    private final ConcurrentMap<String, CompletableFuture<? extends JobStatus>> mapOfJobs;

    /**
     * Default constructor.
     */
    public JobManager() {
        mapOfJobs = new ConcurrentHashMap<>();
    }

    /**
     * Add a new job.
     *
     * @param jobId The job's unique id.
     * @param theJob The job as completable future.
     */
    public void putJob(String jobId, CompletableFuture<? extends JobStatus> theJob) {
        mapOfJobs.put(jobId, theJob);
    }

    /**
     * Get a job by id from the list of managed jobs.
     *
     * @param jobId The job's id.
     *
     * @return The job status as completable future.
     */
    public CompletableFuture<? extends JobStatus> getJob(String jobId) {
        return mapOfJobs.get(jobId);
    }

    /**
     * Remove the job with the provided id. Keep in mind, that removing the job
     * from the JobManager won't remove job outputs.
     *
     * @param jobId The job's id.
     */
    public void removeJob(String jobId) {
        mapOfJobs.remove(jobId);
    }
}
