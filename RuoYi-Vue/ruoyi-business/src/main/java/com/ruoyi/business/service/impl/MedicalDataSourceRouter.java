package com.ruoyi.business.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.ruoyi.business.config.MedicalDataSourceProperties;
import com.ruoyi.business.domain.medical.MedicalQueryRequest;
import com.ruoyi.business.service.MedicalDataSource;
import com.ruoyi.business.service.MedicalQueryException;

@Primary
@Service
public class MedicalDataSourceRouter implements MedicalDataSource
{
    private final MedicalDataSourceProperties properties;
    private final List<MedicalDataSource> dataSources;

    public MedicalDataSourceRouter(MedicalDataSourceProperties properties, List<MedicalDataSource> dataSources)
    {
        this.properties = properties;
        this.dataSources = dataSources.stream()
                .filter(source -> !(source instanceof MedicalDataSourceRouter))
                .sorted(Comparator.comparing(MedicalDataSource::sourceCode))
                .toList();
    }

    @Override
    public Map<String, Object> query(MedicalQueryRequest request)
    {
        MedicalDataSource source = selectSource(request == null ? null : request.getQueryType());
        if (source == null)
        {
            throw new MedicalQueryException("5001", "no healthy medical data source");
        }
        return source.query(request);
    }

    @Override
    public boolean health()
    {
        return dataSources.stream().anyMatch(MedicalDataSource::health);
    }

    @Override
    public String sourceCode()
    {
        return "router";
    }

    @Override
    public String sourceCode(MedicalQueryRequest request)
    {
        MedicalDataSource source = selectSource(request == null ? null : request.getQueryType());
        return source == null ? sourceCode() : source.sourceCode(request);
    }

    MedicalDataSource selectSource(String queryType)
    {
        String sourceCode = properties.getRoutes().getOrDefault(queryType, properties.getDefaultSource());
        MedicalDataSource selected = findHealthy(sourceCode);
        if (selected != null)
        {
            return selected;
        }
        if (!properties.isFallbackEnabled())
        {
            return null;
        }
        return findHealthy(properties.getDefaultSource());
    }

    private MedicalDataSource findHealthy(String sourceCode)
    {
        if (sourceCode == null || sourceCode.isEmpty())
        {
            return null;
        }
        return dataSources.stream()
                .filter(source -> sourceCode.equals(source.sourceCode()))
                .filter(MedicalDataSource::health)
                .findFirst()
                .orElse(null);
    }
}

