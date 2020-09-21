package edu.kit.datamanager.indexer.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author Andreas Pfeil
 */
@Component
@Data
@Validated
@EqualsAndHashCode(callSuper = false)
public class IndexerProperties {
    /**
     * A list of filenames, stored in the classpath (i.e. java/main/resources)
     * which will be used to map the records into elastic-compatible json.
     * 
     * Example: indexer.recordMapping.mappingFiles: mytemplate,yourtemplate
     * (Assuming your files are in the classpaths root and named mytemplate.hbs
     * and yourtemplate.hbs)
     */
    @Value("#{'${indexer.recordMapping.mappingFiles}'.split(',')}")
    List<String> schemaMappings;

    /**
     * The base URL of the elasticsearch service, including port.
     */
    @Value("${indexer.elastic.baseUrl}")
    String elasticUrl;

    /**
     * The elastic index ("database") where the records will be stored into.
     */
    @Value("${indexer.elastic.index}")
    String elasticIndex;

    /**
     * The subfolder where the json files, which are ingested into elasticsearch
     * are stored locally. This might be useful as a backup.
     */
    @Value("${indexer.elastic.folder}")
    String elasticFilesStorage;
}
