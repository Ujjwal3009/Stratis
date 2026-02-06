package com.upsc.ai.service;

import com.upsc.ai.entity.SystemConfig;
import com.upsc.ai.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigService {

    @Autowired
    private SystemConfigRepository repository;

    @Cacheable(value = "system_configs", key = "#key")
    public String getConfig(String key, String defaultValue) {
        return repository.findById(key)
                .map(SystemConfig::getConfigValue)
                .orElse(defaultValue);
    }

    @CacheEvict(value = "system_configs", key = "#config.configKey")
    public SystemConfig setConfig(SystemConfig config) {
        return repository.save(config);
    }

    public List<SystemConfig> getAllConfigs() {
        return repository.findAll();
    }
}
