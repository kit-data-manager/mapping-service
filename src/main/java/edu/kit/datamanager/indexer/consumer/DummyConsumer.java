package edu.kit.datamanager.indexer.consumer;

import org.springframework.stereotype.Component;

@Component
public class DummyConsumer implements IConsumerEngine {

    @Override
    public boolean consume() {
        return true;
    }
    
}