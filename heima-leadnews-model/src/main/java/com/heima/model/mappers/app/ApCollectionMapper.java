package com.heima.model.mappers.app;

import com.heima.model.article.pojos.ApCollection;

public interface ApCollectionMapper {
    /**
     * 选择一个终端的收藏数据
     * @return
     */
    ApCollection selectForEntryId(String burst, Integer objectId, Integer entryId, Short type);
}
