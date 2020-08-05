package com.heima.common.zookeeper;


import com.google.common.collect.Maps;
import com.heima.common.zookeeper.squence.ZkSequence;
import com.heima.common.zookeeper.squence.ZkSequenceEnum;
import lombok.Data;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Map;

@Data
public class ZookeeperClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperClient.class);

    private String host;

    private String sequencePath;

    // 重试休眠时间
    private final int SLEEP_TIME_MS = 1000;
    // 最大重试1000次
    private final int MAX_RETRIES = 1000;
    //会话超时时间
    private final int SESSION_TIMEOUT = 30 * 1000;
    //连接超时时间
    private final int CONNECTION_TIMEOUT = 3 * 1000;

    //创建连接实例
    private CuratorFramework client = null;
    // 序列化集合
    private Map<String, ZkSequence> zkSequenceMap = Maps.newConcurrentMap();

    public ZookeeperClient(String host,String sequencePath){
        this.host = host;
        this.sequencePath = sequencePath;
    }

    @PostConstruct
    public void init()throws Exception{
        client = CuratorFrameworkFactory.builder()
                .connectString(this.getHost())
                .connectionTimeoutMs(CONNECTION_TIMEOUT)
                .sessionTimeoutMs(SESSION_TIMEOUT)
                .retryPolicy(new ExponentialBackoffRetry(SLEEP_TIME_MS,MAX_RETRIES)).build();
        this.client.start();
        this.initSequence();
    }

    public void initSequence(){
        ZkSequenceEnum[] values = ZkSequenceEnum.values();
        for (ZkSequenceEnum value : values) {
            String name = value.name();
            String path = this.sequencePath+name;
            ZkSequence zkSequence = new ZkSequence(this.client,path);
            zkSequenceMap.put(name,zkSequence);
        }
    }

    /**
     * 生成Sequence
     * @param zkSequenceEnum
     * @return
     */
    public Long sequence(ZkSequenceEnum zkSequenceEnum){
        try {
            ZkSequence zkSequence = zkSequenceMap.get(zkSequenceEnum.name());
            if(null != zkSequence) {
                return zkSequence.sequence();
            }
        }catch (Exception e){
            LOGGER.error("获取[{}]Sequence错误:{}",zkSequenceEnum,e);
        }
        return null;
    }



}
