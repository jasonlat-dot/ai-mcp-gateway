package com.jasonlat.ai.domain.protocol.service;

import com.jasonlat.ai.domain.protocol.model.entity.StorageCommandEntity;

import java.util.List;

public interface IProtocolStorageService {

    List<Long> storage(StorageCommandEntity commandEntity);

}
