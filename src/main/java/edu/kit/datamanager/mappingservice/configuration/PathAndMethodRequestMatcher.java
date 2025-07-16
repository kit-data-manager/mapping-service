package edu.kit.datamanager.mappingservice.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class PathAndMethodRequestMatcher implements RequestMatcher {
    private static final Logger logger = LoggerFactory.getLogger(PathAndMethodRequestMatcher.class);

    private final String pathPattern;
    private final String httpMethod;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public PathAndMethodRequestMatcher(String httpMethod, String pathPattern) {
        this.httpMethod = httpMethod;
        this.pathPattern = pathPattern;
    }

    @Override
    public boolean matches(HttpServletRequest request){
    logger.error("DO MATCHING STUFF");
    boolean result =  request.getMethod().equalsIgnoreCase(httpMethod) &&
                pathMatcher.match(pathPattern, request.getRequestURI());
logger.error("RESULT: " + result);
return result;
    }
}
