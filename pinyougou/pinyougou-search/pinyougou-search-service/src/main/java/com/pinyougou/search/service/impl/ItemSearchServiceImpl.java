package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.dao.ItemDao;
import com.pinyougou.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Autowired
    private ItemDao itemDao;

    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {
        Map<String, Object> resultMap = new HashMap<>();

        //查询构造对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //如果没有搜索关键字的时候则查询全部
        queryBuilder.withQuery(QueryBuilders.matchAllQuery());

        //是否要高亮
        boolean highlight = false;

        if (searchMap != null) {
            //搜索关键字
            String keywords = searchMap.get("keywords")+"";
            if (StringUtils.isNotBlank(keywords)) {
                //根据搜索关键字查询；参数1：用户输入的关键字，参数2：要在es的哪些域中查询
                //默认情况下，搜索的时候会根据搜索关键字分词之后 或者 关系查询结果，可以修改为 并列
                queryBuilder.withQuery(
                        QueryBuilders.multiMatchQuery(keywords, "title", "category", "seller", "brand")
                                .operator(Operator.AND));


                //设置高亮域
                highlight = true;
                HighlightBuilder.Field highlightField = new HighlightBuilder.Field("title")
                        //设置高亮起始标签
                        .preTags("<span style='color:red'>")
                        //设置高亮结束标签
                        .postTags("</span>");

                //设置高亮域
                queryBuilder.withHighlightFields(highlightField);
            }

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            //分类
            String category = searchMap.get("category")+"";
            if (StringUtils.isNotBlank(category)) {
                boolQueryBuilder.must(QueryBuilders.termQuery("category", category));
            }

            //品牌
            String brand = searchMap.get("brand")+"";
            if (StringUtils.isNotBlank(brand)) {
                boolQueryBuilder.must(QueryBuilders.termQuery("brand", brand));
            }

            //规格
            if (searchMap.get("spec") != null) {
                Map<String, String> map = (Map<String, String>) searchMap.get("spec");
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    //嵌套域的域名称
                    String key = "specMap." + entry.getKey() + ".keyword";
                    boolQueryBuilder.must(QueryBuilders.nestedQuery("specMap",
                            QueryBuilders.matchQuery(key,entry.getValue()), ScoreMode.Max));
                }
            }

            //价格
            String price = searchMap.get("price")+"";
            if (StringUtils.isNotBlank(price)) {
                String[] prices = price.split("-");

                //价格的下限
                boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gte(prices[0]));

                //价格的上限
                if(!"*".equals(prices[1])) {
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(prices[1]));
                }

            }


            queryBuilder.withFilter(boolQueryBuilder);
        }

        //设置分页
        int pageNo = 1;
        int pageSize = 10;
        String pageNoStr = searchMap.get("pageNo")+"";
        if (StringUtils.isNotBlank(pageNoStr)) {
            pageNo = Integer.parseInt(pageNoStr);
        }
        String pageSizeStr = searchMap.get("pageSize")+"";
        if (StringUtils.isNotBlank(pageSizeStr)) {
            pageSize = Integer.parseInt(pageSizeStr);
        }

        //创建分页信息对象
        PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize);

        queryBuilder.withPageable(pageRequest);

        //处理排序
        String sortField = searchMap.get("sortField")+"";
        String sortOrder = searchMap.get("sortOrder")+"";
        if (StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sortOrder)) {
            queryBuilder.withSort(SortBuilders.fieldSort(sortField).order("DESC".equals(sortOrder)? SortOrder.DESC:SortOrder.ASC));
        }

        //创建查询条件对象
        NativeSearchQuery query = queryBuilder.build();
        //搜索
        AggregatedPage<TbItem> aggregatedPage = null;
        if (highlight) {
            //高亮查询
            aggregatedPage = esTemplate.queryForPage(query, TbItem.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                    List<T> itemList = new ArrayList<>();

                    //获取查询的数据
                    SearchHits hits = searchResponse.getHits();
                    TbItem tbItem = null;
                    for (SearchHit hit : hits) {
                        //es保存的商品tbItem的json格式字符串
                         tbItem = JSON.parseObject(hit.getSourceAsString(), TbItem.class);

                         //处理高亮标题
                        HighlightField highlightField = hit.getHighlightFields().get("title");
                        if (highlightField != null) {
                            Text[] fragments = highlightField.getFragments();
                            if (fragments != null && fragments.length > 0) {
                                //高亮的标题部分
                                StringBuilder sb = new StringBuilder();
                                for (Text fragment : fragments) {
                                    sb.append(fragment.toString());
                                }
                                //设置高亮标题
                                tbItem.setTitle(sb.toString());
                            }
                        }

                        itemList.add((T)tbItem);
                    }

                    //参数1：商品列表，分页信息对象，符合本次查询条件的总记录数
                    return new AggregatedPageImpl<T>(itemList, pageable, searchResponse.getHits().getTotalHits());
                }
            });
        } else {
            aggregatedPage = esTemplate.queryForPage(query, TbItem.class);
        }

        //商品列表
        resultMap.put("itemList", aggregatedPage.getContent());
        //总记录数
        resultMap.put("total", aggregatedPage.getTotalElements());
        //总页数
        resultMap.put("totalPages", aggregatedPage.getTotalPages());

        return resultMap;
    }

    @Override
    public void importItemList(List<TbItem> itemList) {
        //将每个sku 的spec字符串转换为一个map
        if(itemList != null && itemList.size() > 0 ) {
            for (TbItem tbItem : itemList) {
                Map map = JSON.parseObject(tbItem.getSpec(), Map.class);
                tbItem.setSpecMap(map);
            }
            itemDao.saveAll(itemList);
        }
    }

    @Override
    public void deleteByGoodsIds(Long[] goodsIds) {
        itemDao.deleteByGoodsIdIn(goodsIds);
    }
}
