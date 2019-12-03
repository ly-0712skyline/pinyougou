package cn.itcast.es.dao;

import com.pinyougou.pojo.TbItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemDao extends ElasticsearchRepository<TbItem, Long> {
}
