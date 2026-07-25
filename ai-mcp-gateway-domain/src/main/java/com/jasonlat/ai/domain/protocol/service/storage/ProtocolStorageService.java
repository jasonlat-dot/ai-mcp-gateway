package com.jasonlat.ai.domain.protocol.service.storage;

import com.jasonlat.ai.domain.protocol.adapter.repository.IProtocolRepository;
import com.jasonlat.ai.domain.protocol.model.entity.StorageCommandEntity;
import com.jasonlat.ai.domain.protocol.model.valobj.dubbo.DubboProtocolVO;
import com.jasonlat.ai.domain.protocol.model.valobj.http.HTTPProtocolVO;
import com.jasonlat.ai.domain.protocol.service.IProtocolStorageService;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jasonlat
 * 2026-07-03  18:58
 */
@Slf4j
@Service
public class ProtocolStorageService implements IProtocolStorageService {
    @Resource
    private IProtocolRepository repository;

    @Override
    public List<Long> storage(StorageCommandEntity commandEntity) {
        List<Long> result = new ArrayList<>();
        List<HTTPProtocolVO> httpList = commandEntity.getHttpProtocolVOS();
        List<DubboProtocolVO> dubboList = commandEntity.getDubboProtocolVOS();
        if (httpList != null && !httpList.isEmpty()) {
            result.addAll(repository.saveHttpProtocolAndMapping(httpList));
        }
        if (dubboList != null && !dubboList.isEmpty()) {
            result.addAll(repository.saveDubboProtocolAndMapping(dubboList));
        }
        return result;
    }

    @Override
    public boolean updateGatewayProtocol(StorageCommandEntity commandEntity) {
        List<HTTPProtocolVO> httpProtocolVOS = commandEntity.getHttpProtocolVOS();
        List<DubboProtocolVO> dubboProtocolVOS = commandEntity.getDubboProtocolVOS();
        if ((httpProtocolVOS == null || httpProtocolVOS.isEmpty())
                && (dubboProtocolVOS == null || dubboProtocolVOS.isEmpty())) {
            log.error("更新协议配置失败: 协议列表为空");
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }

        if (httpProtocolVOS != null) {
            for (HTTPProtocolVO httpProtocolVO : httpProtocolVOS) {
                if (httpProtocolVO.getProtocolId() == null) {
                    log.error("更新协议配置失败: protocolId为空");
                    throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
                }
                if (!repository.updateHttpProtocolAndMapping(httpProtocolVO)) {
                    log.error("更新协议配置失败: protocolId={}", httpProtocolVO.getProtocolId());
                    throw new AppException(ResponseCode.DB_UPDATE_FAIL);
                }
            }
        }

        if (dubboProtocolVOS != null) {
            for (DubboProtocolVO vo : dubboProtocolVOS) {
                if (vo.getProtocolId() == null) {
                    log.error("更新 Dubbo 协议配置失败: protocolId为空");
                    throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
                }
                if (!repository.updateDubboProtocolAndMapping(vo)) {
                    log.error("更新 Dubbo 协议配置失败: protocolId={}", vo.getProtocolId());
                    throw new AppException(ResponseCode.DB_UPDATE_FAIL);
                }
            }
        }
        return true;
    }

    @Override
    public void deleteGatewayProtocol(Long protocolId) {
        repository.deleteGatewayProtocol(protocolId);
    }

}
