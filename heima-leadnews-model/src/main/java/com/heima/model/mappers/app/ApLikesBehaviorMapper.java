package com.heima.model.mappers.app;

import com.heima.model.behavior.pojos.ApLikesBehavior;

public interface ApLikesBehaviorMapper {
    /**
     * 选择最后一条喜欢按钮
     * @return
     */
    ApLikesBehavior selectLastLike(String burst, Integer objectId, Integer entryId, Short type);
}
