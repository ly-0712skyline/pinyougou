package com.pinyougou.es.dao;

import com.pinyougou.pojo.TbItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemElasticSearchRepository extends ElasticsearchRepository<TbItem, Long> {
}
