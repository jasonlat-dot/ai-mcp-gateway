package com.jasonlat.ai.infrastructure.adapter.port;

import com.jasonlat.ai.domain.session.adapter.port.ISessionPort;
import com.jasonlat.ai.domain.session.model.valobj.SessionForwardRequestVO;
import com.jasonlat.ai.domain.session.model.valobj.SessionForwardResponseVO;
import com.jasonlat.ai.domain.session.model.valobj.SessionSyncEventVO;
import com.jasonlat.ai.domain.session.model.valobj.SessionSyncInfoVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;
import com.jasonlat.ai.infrastructure.adapter.port.tool.DubboInvoker;
import com.jasonlat.ai.infrastructure.adapter.port.tool.HttpInvoker;
import com.jasonlat.ai.infrastructure.gateway.GenericHttpGateway;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


/**
 * @author jasonlat
 * 2026-06-24  18:59
 */
@Component
@AllArgsConstructor
public class SessionPort implements ISessionPort {

    private final GenericHttpGateway gateway;

    private final HttpInvoker httpInvoker;

    private final DubboInvoker dubboInvoker;

    // Redis Topic
    private static final String SESSION_SYNC_TOPIC = "ai:mcp:gateway:session:sync";
    /**
     * Redis Map 名称：活跃会话元数据存储
     * <p>
     * key = sessionId，value = SessionSyncInfoVO
     * 用于持久化会话元数据，支持服务重启后全量恢复
     */
    private static final String SESSION_SYNC_MAP = "ai:mcp:gateway:session:active";

    @Resource
    private RedissonClient redissonClient;

    @Override
    public Object toolCall(McpToolProtocolConfigVO protocolConfig, Object params) throws Exception {
        if (!(params instanceof Map<?, ?>)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }

        String protocolType = protocolConfig.getProtocolType();
        if (protocolType == null) {
            // 兼容老数据:没填就当 HTTP
            protocolType = "HTTP";
        }

        // =========================================================================
        // 注:HTTP 调用的具体实现已委托给 HttpInvoker,Dubbo 委托给 DubboInvoker(后续)。
        // 这里 SessionPort 只做"按 protocolType 分发"的工厂职责,
        // 不再持有 HTTP/Dubbo 的调用细节,避免一处逻辑被两套代码各自维护。
        // =========================================================================
        return switch (protocolType.toUpperCase()) {
            case "HTTP"  -> httpInvoker.invoke(protocolConfig.getHttpConfig(), params);
            case "DUBBO" -> dubboInvoker.invoke(protocolConfig, params);
            default -> throw new AppException("unknown protocol type: " + protocolType);
        };

    }

    /**
     * 保存会话同步信息到 Redis Map
     * <p>
     * 写入元数据后，立即发布 CREATE 事件到 Redis Topic，
     * 通知其他应用实例同步该会话到本地 activeSessions。
     *
     * @param sessionSyncInfoVO 会话同步元数据
     */
    @Override
    public void saveSessionSyncInfo(SessionSyncInfoVO sessionSyncInfoVO) {
        redissonClient.<String, SessionSyncInfoVO>getMap(SESSION_SYNC_MAP)
                .put(sessionSyncInfoVO.getSessionId(), sessionSyncInfoVO);
        // 写入成功后发布 CREATE 事件，触发其他实例增量同步
        publishSessionSyncEvent(SessionSyncEventVO.builder()
                .eventType(SessionSyncEventVO.EventType.CREATE)
                .sessionSyncInfo(sessionSyncInfoVO)
                .build());
    }

    /**
     * 从 Redis Map 删除会话同步信息
     * <p>
     * 删除元数据后，如果记录存在则发布 REMOVE 事件到 Redis Topic，
     * 通知其他应用实例清理本地 activeSessions 中的对应会话。
     *
     * @param sessionId 待删除的会话ID
     */
    @Override
    public void removeSessionSyncInfo(String sessionId) {
        SessionSyncInfoVO removed = redissonClient.<String, SessionSyncInfoVO>getMap(SESSION_SYNC_MAP).remove(sessionId);
        if (removed != null) {
            // 记录存在时才发布事件，避免无效广播
            publishSessionSyncEvent(SessionSyncEventVO.builder()
                    .eventType(SessionSyncEventVO.EventType.REMOVE)
                    .sessionSyncInfo(removed)
                    .build());
        }
    }

    /**
     * 从 Redis Map 全量加载当前所有有效会话
     * <p>
     * 服务启动时调用，读取 Redis Map 中的全部会话元数据，
     * 供 SessionManagementService 逐个 rebuildLocalSession 恢复到本地。
     *
     * @return 所有存储在 Redis 中的会话元数据列表
     */
    @Override
    public List<SessionSyncInfoVO> loadActiveSessions() {
        RMap<String, SessionSyncInfoVO> sessionMap = redissonClient.getMap(SESSION_SYNC_MAP);
        return new ArrayList<>(sessionMap.readAllValues());
    }

    /**
     * 发布会话同步事件到 Redis Topic
     * <p>
     * 通过 Redisson RTopic 的 publish 方法广播事件，
     * 所有订阅了该 Topic 的应用实例都会收到消息。
     *
     * @param event 会话同步事件（CREATE / REMOVE）
     */
    @Override
    public void publishSessionSyncEvent(SessionSyncEventVO event) {
        redissonClient.getTopic(SESSION_SYNC_TOPIC).publish(event);
    }

    @Override
    public void subscribeSessionSyncEvent(Consumer<SessionSyncEventVO> consumer) {
        RTopic topic = redissonClient.getTopic(SESSION_SYNC_TOPIC);
        topic.addListener(SessionSyncEventVO.class, (channel, msg) -> consumer.accept(msg));
    }

    @Override
    public SessionSyncInfoVO getSession(String sessionId) {
        RMap<String, SessionSyncInfoVO> sessionMap = redissonClient.getMap(SESSION_SYNC_MAP);
        return sessionMap.get(sessionId);
    }

    @Override
    public SessionForwardResponseVO forwardToHolder(SessionForwardRequestVO request) {
        if (request == null) {
            return SessionForwardResponseVO.fail(400, "转发请求 VO 不能为空");
        }
        if (StringUtils.isBlank(request.getHolderInstanceId())) {
            return SessionForwardResponseVO.fail(400, "holderInstanceId 不能为空");
        }
        String url = buildHolderUrl(
                request.getHolderInstanceId(),
                request.getContextPath(),
                request.getGatewayId(),
                request.getSessionId(),
                request.getApiKey()
        );
        try {
            Map<String, Object> headers = buildForwardHeaders(request.getFromInstanceId());
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"),
                    request.getMessageBody() == null ? "" : request.getMessageBody()
            );
            Call<ResponseBody> call = gateway.post(url, headers, requestBody);
            Response<ResponseBody> response = call.execute();
            int statusCode = response.code();
            try (ResponseBody responseBody = response.body()) {
                if (responseBody == null) return null;
                if (response.isSuccessful()) {
                    return SessionForwardResponseVO.ok(statusCode, responseBody.toString());
                } else {
                    return SessionForwardResponseVO.fail(statusCode, responseBody.toString());
                }
            }

        } catch (IOException e) {
            return SessionForwardResponseVO.fail(503, "转发 IO 异常: " + e.getMessage());
        } catch (Exception e) {
            return SessionForwardResponseVO.fail(500, "转发异常: " + e.getMessage());
        }
    }
    private String buildHolderUrl(String holderInstanceId, String contextPath,
                                  String gatewayId, String sessionId, String apiKey) {
        StringBuilder url = new StringBuilder()
                .append("http://")
                .append(holderInstanceId)
                .append(contextPath)
                .append("/")
                .append(gatewayId)
                .append("/mcp/internal/forward")
                .append("?sessionId=").append(sessionId);
        if (StringUtils.isNotBlank(apiKey)) {
            url.append("&api_key=").append(apiKey);
        }
        return url.toString();
    }
    private Map<String, Object> buildForwardHeaders(String fromInstanceId) {
        Map<String, Object> headers = new HashMap<>(4);
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        headers.put("X-Internal-Forward", "true");
        if (StringUtils.isNotBlank(fromInstanceId)) {
            headers.put("X-Forwarded-Instance", fromInstanceId);
        }
        return headers;
    }

}
