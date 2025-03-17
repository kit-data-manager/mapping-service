package edu.kit.datamanager.mappingservice.rest.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.MessageDigest;
import java.util.HashSet;

@Service
public class PreHandleInterceptor implements HandlerInterceptor {
    private final HashSet<String> uniqueUsers = new HashSet<>();
    private final Counter counter;

    @Autowired
    PreHandleInterceptor(MeterRegistry meterRegistry) {
        Gauge.builder("mapping.unique-users", uniqueUsers::size).register(meterRegistry);
        counter = Counter.builder("mapping.requests-served").register(meterRegistry);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler) throws Exception {
        String ip = request.getRemoteAddr();
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(ip.getBytes());
        uniqueUsers.add(new String(messageDigest.digest()));

        counter.increment();

        return true;
    }
}
