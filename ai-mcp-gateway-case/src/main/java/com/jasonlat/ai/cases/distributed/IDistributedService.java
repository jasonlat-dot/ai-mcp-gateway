package com.jasonlat.ai.cases.distributed;


import com.jasonlat.ai.domain.session.model.valobj.SessionSyncEventVO;

import java.util.function.Consumer;

/**
 * 分布式服务接口
 */
public interface IDistributedService {

    void handleSessionSyncEvent(SessionSyncEventVO event);

    void subscribeSessionSyncEvent(Consumer<SessionSyncEventVO> consumer);

}
