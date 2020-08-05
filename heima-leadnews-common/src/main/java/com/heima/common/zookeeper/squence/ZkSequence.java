package com.heima.common.zookeeper.squence;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class ZkSequence {

    RetryPolicy retryPolicy = new ExponentialBackoffRetry(500,3);

    DistributedAtomicLong distributedAtomicLong;

    public ZkSequence(CuratorFramework curatorFramework,String counterPath){
        this.distributedAtomicLong = new DistributedAtomicLong(curatorFramework,counterPath,this.retryPolicy);
    }



    public Long sequence()throws Exception{
        AtomicValue<Long> increment = distributedAtomicLong.increment();
        if(increment.succeeded()){
            return increment.postValue();
        }
        return null;
    }

}
