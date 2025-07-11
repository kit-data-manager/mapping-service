package edu.kit.datamanager.mappingservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.File;

/**
 *
 * @author jejkal
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class JobStatus {

    public enum STATUS {
        SUBMITTED,
        RUNNING,
        SUCCEEDED,
        FAILED,
        DELETED
    }
    private String jobId;
    private STATUS status;
    private String error;
    private String outputFileURI;
    @JsonIgnore
    private File jobOutput;

    JobStatus(String jobId, STATUS status, String error, String outputFileURI, File jobOutput) {
        this.jobId = jobId;
        this.status = status;
        this.error = error;
        this.outputFileURI = outputFileURI;
        this.jobOutput = jobOutput;
    }

    public static JobStatus status(String jobId, STATUS status) {
        return new JobStatus(jobId, status, null, null, null);
    }

    public static JobStatus error(String jobId, STATUS status, String error) {
        return new JobStatus(jobId, status, error, null, null);
    }

    public static JobStatus result(String jobId, STATUS status, String outputFileUrl) {
        return new JobStatus(jobId, status, null, outputFileUrl, null);
    }

    public static JobStatus complete(String jobId, STATUS status, File jobOutput) {
        return new JobStatus(jobId, status, null, null, jobOutput);
    }

}
