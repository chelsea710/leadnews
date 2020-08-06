package com.heima.model.mappers.app;

import com.heima.model.behavior.pojos.ApUnlikesBehavior;

public interface ApUnlikesBehaviorMapper {
    /**
     * 选择最后一条不喜欢数据
     * @return
     */
    ApUnlikesBehavior selectLastUnLike(Integer entryId, Integer articleId);
}
