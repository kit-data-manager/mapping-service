package edu.kit.datamanager.mappingservice.plugins;

public class MappingPluginException extends Exception{
    private MappingPluginStates state;

    public MappingPluginException(String message){
        super(message);
    }

    public MappingPluginException(Throwable cause){
        super(cause);
    }

    public MappingPluginException(String message, Throwable cause){
        super(message, cause);
    }

    public MappingPluginException(MappingPluginStates state, String message){
        super(message);
        this.state = state;
    }

    public MappingPluginException(MappingPluginStates state, String message, Throwable cause){
        super(message, cause);
        this.state = state;
    }
}

