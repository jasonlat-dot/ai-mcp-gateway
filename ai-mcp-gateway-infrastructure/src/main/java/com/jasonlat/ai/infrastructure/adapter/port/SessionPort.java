package com.jasonlat.ai.infrastructure.adapter.port;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jasonlat.ai.domain.session.adapter.port.ISessionPort;
import com.jasonlat.ai.domain.session.model.valobj.SessionSyncEventVO;
import com.jasonlat.ai.domain.session.model.valobj.SessionSyncInfoVO;
import com.jasonlat.ai.domain.session.model.valobj.gateway.McpToolProtocolConfigVO;
import com.jasonlat.ai.infrastructure.gateway.GenericHttpGateway;
import com.jasonlat.ai.types.enums.ResponseCode;
import com.jasonlat.ai.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import retrofit2.Call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jasonlat
 * 2026-06-24  18:59
 */
@Component
@AllArgsConstructor
public class SessionPort implements ISessionPort {

    private final GenericHttpGateway gateway;

    private final ObjectMapper objectMapper;

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
    public Object toolCall(McpToolProtocolConfigVO.HTTPConfig httpConfig, Object params) throws IOException {

        // params 是map 不是就抛异常  --> {"word":"jsaonlat"}  key-value 形式
        if (!(params instanceof Map<?,?> arguments)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER);
        }

        String httpHeadersJson = httpConfig.getHeaders();
        Map<String, Object> headers = objectMapper.readValue(httpHeadersJson, Map.class);

        String httpMethod = httpConfig.getMethod().toLowerCase();

        switch (httpMethod) {
            case "post": {
                RequestBody requestBody = RequestBody.create(
                        MediaType.parse("application/json"),
                        JSON.toJSONString(arguments.values().toArray()[0])
                );
                Call<ResponseBody> call = gateway.post(httpConfig.getUrl(), headers, requestBody);
                try (ResponseBody responseBody = call.execute().body()) {
                    if (responseBody == null) {
                        return null;
                    }
                    return responseBody.string();
                }
            }
            case "get": {
                HashMap<String, Object> objMapRequest = new HashMap<>((Map<String, Object>) arguments.values().toArray()[0]);

                String url = httpConfig.getUrl();
                // 替换路径参数
                // 匹配字符串里形如 {xxx} 的占位符，并且把 xxx 提取到分组 1  http://api/{userId}/info/{orderNo} 匹配到两处：{userId}、{orderNo}
                Matcher matcher = Pattern.compile("\\{([^}]+)}").matcher(url);
                while (matcher.find()) {
                    String name = matcher.group(1);
                    if (objMapRequest.containsKey(name)) {
                        url = url.replace("{" + name + "}", String.valueOf(objMapRequest.get(name)));
                        objMapRequest.remove(name);
                    }
                }

                Call<ResponseBody> call = gateway.get(url, headers, objMapRequest);
                try (ResponseBody responseBody = call.execute().body()) {
                    if (responseBody == null) {
                        return null;
                    }
                    return responseBody.string();
                }
            }
        }
        throw new AppException(ResponseCode.METHOD_NOT_FOUND);
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

}
