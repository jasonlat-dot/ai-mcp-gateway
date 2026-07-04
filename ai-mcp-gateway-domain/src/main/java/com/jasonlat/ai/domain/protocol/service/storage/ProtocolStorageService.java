package com.jasonlat.ai.domain.protocol.service.storage;

import com.jasonlat.ai.domain.protocol.adapter.repository.IProtocolRepository;
import com.jasonlat.ai.domain.protocol.model.entity.StorageCommandEntity;
import com.jasonlat.ai.domain.protocol.service.IProtocolStorageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author jasonlat
 * 2026-07-03  18:58
 */
@Service
public class ProtocolStorageService implements IProtocolStorageService {
    @Resource
    private IProtocolRepository repository;

    @Override
    public List<Long> storage(StorageCommandEntity commandEntity) {
        return repository.saveHttpProtocolAndMapping(commandEntity.getHttpProtocolVOS());
    }
}
