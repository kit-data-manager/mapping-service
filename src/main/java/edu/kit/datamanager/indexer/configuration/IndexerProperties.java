package edu.kit.datamanager.indexer.configuration;


import java.net.URL;
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
     * Folder where the mappings are stored locally.
     * 
     * Example: indexer.recordMapping.mappingFiles: mytemplate,yourtemplate
     * (Assuming your files are in the classpaths root and named mytemplate.hbs
     * and yourtemplate.hbs)
     */
    @Value("${indexer.mappingFolder}}")
    URL mappingFolder;

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
