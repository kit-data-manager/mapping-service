package edu.kit.datamanager.mappingservice.plugins;

public class MappingPluginException extends Exception{
    private MappingPluginState state;

    public MappingPluginException(String message){
        super(message);
    }

    public MappingPluginException(Throwable cause){
        super(cause);
    }

    public MappingPluginException(String message, Throwable cause){
        super(message, cause);
    }

    public MappingPluginException(MappingPluginState state, String message){
        super(message);
        this.state = state;
    }

    public MappingPluginException(MappingPluginState state){
        super(state.name());
        this.state = state;
    }

    public MappingPluginException(MappingPluginState state, String message, Throwable cause){
        super(message, cause);
        this.state = state;
    }
}

