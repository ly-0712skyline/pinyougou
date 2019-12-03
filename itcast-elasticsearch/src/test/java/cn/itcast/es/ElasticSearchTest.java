package cn.itcast.es;

import cn.itcast.es.dao.ItemDao;
import com.pinyougou.pojo.TbItem;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-es.xml")
public class ElasticSearchTest {

    //创建索引、映射、复杂的查询
    @Autowired
    private ElasticsearchTemplate esTemplate;

    //简单的CURD使用
    @Autowired
    private ItemDao itemDao;

    //创建索引和映射
    @Test
    public void createIndexAndMapping(){
        //创建索引
        esTemplate.createIndex(TbItem.class);
        //设置映射
        esTemplate.putMapping(TbItem.class);
    }

    //新增或更新
    @Test
    public void save(){
        TbItem item = new TbItem();
        item.setId(100003344497L);
        item.setTitle("222 一加 OnePlus 7 Pro 2K+90Hz 流体屏 骁龙855旗舰 4800万超广角三摄 8GB+256GB 曜岩灰 全面屏拍照游戏手机");
        item.setPrice(4999.0);
        item.setGoodsId(123L);
        item.setBrand("一加");
        item.setCategory("手机");
        item.setSeller("一加");
        item.setImage("https://item.jd.com/100003344497.html");
        item.setUpdateTime(new Date());

        //设置嵌套域
        Map<String, String> specMap = new HashMap<String, String>();
        specMap.put("颜色", "曜岩灰");
        specMap.put("内存大小", "64G");
        item.setSpecMap(specMap);

        itemDao.save(item);
    }

    //查询全部
    @Test
    public void findAll(){
        Iterable<TbItem> itemList = itemDao.findAll();
        for (TbItem tbItem : itemList) {
            System.out.println(tbItem);
        }
    }

    //根据id删除
    @Test
    public void deleteById(){
        itemDao.deleteById(100003344497L);
    }

    //通配符
    @Test
    public void wildcardQuery(){

        //在spring data es中所有的对象几乎都有一个自身的构造对象

        //查询构造对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //设置查询条件对象
        queryBuilder.withQuery(QueryBuilders.wildcardQuery("title", "拍*"));

        //构造查询对象
        NativeSearchQuery query = queryBuilder.build();

        //搜索
        AggregatedPage<TbItem> aggregatedPage = esTemplate.queryForPage(query, TbItem.class);

        //分页信息
        System.out.println("总记录数为：" + aggregatedPage.getTotalElements());
        System.out.println("总页数为：" + aggregatedPage.getTotalPages());

        for (TbItem item : aggregatedPage.getContent()) {
            System.out.println(item);
        }
    }

    //模糊查询 会对搜索的关键字进行分词之后查询；并且查询的结果是以分出的词条的结果 or 的关系
    @Test
    public void matchQuery(){

        //在spring data es中所有的对象几乎都有一个自身的构造对象

        //查询构造对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //设置查询条件对象
        queryBuilder.withQuery(QueryBuilders.matchQuery("title", "一加手机"));

        //构造查询对象
        NativeSearchQuery query = queryBuilder.build();

        //搜索
        AggregatedPage<TbItem> aggregatedPage = esTemplate.queryForPage(query, TbItem.class);

        //分页信息
        System.out.println("总记录数为：" + aggregatedPage.getTotalElements());
        System.out.println("总页数为：" + aggregatedPage.getTotalPages());

        for (TbItem item : aggregatedPage.getContent()) {
            System.out.println(item);
        }
    }


    //复制域；相当于查询复制域对应的 来源域
    @Test
    public void copyFieldQuery(){

        //在spring data es中所有的对象几乎都有一个自身的构造对象

        //查询构造对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //设置查询条件对象
        queryBuilder.withQuery(QueryBuilders.wildcardQuery("keywords", "拍*"));

        //构造查询对象
        NativeSearchQuery query = queryBuilder.build();

        //搜索
        AggregatedPage<TbItem> aggregatedPage = esTemplate.queryForPage(query, TbItem.class);

        //分页信息
        System.out.println("总记录数为：" + aggregatedPage.getTotalElements());
        System.out.println("总页数为：" + aggregatedPage.getTotalPages());

        for (TbItem item : aggregatedPage.getContent()) {
            System.out.println(item);
        }
    }


    //嵌套域
    @Test
    public void nestedQuery(){

        //在spring data es中所有的对象几乎都有一个自身的构造对象

        //查询构造对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //设置查询条件对象
        queryBuilder.withQuery(QueryBuilders.matchQuery("title", "手机"));

        //添加过滤查询

        //组合查询，可以设置多个条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        /**
         *  参数 1 ：路径；嵌套对象；实体类中属性名称
         *  参数 2 ：查询条件对象
         *  参数 3 ： ScoreMode  定义里嵌套对象计算的分数与当前查询分数的处理方式， max
         表示取最大。查询到的结果中有多个文档，每个文档都有得分，max 表示选择最大的分值作
         为本次查询的_score 分值
         */
        NestedQueryBuilder nestedQueryBuilder1 =
                new NestedQueryBuilder("specMap", QueryBuilders.wildcardQuery("specMap.颜色.keyword", "曜岩*"), ScoreMode.Max);
        boolQueryBuilder.must(nestedQueryBuilder1);

        NestedQueryBuilder nestedQueryBuilder2 =
                new NestedQueryBuilder("specMap", QueryBuilders.wildcardQuery("specMap.内存大小.keyword", "64G"), ScoreMode.Max);
        boolQueryBuilder.must(nestedQueryBuilder2);

        //设置组合查询为过滤查询条件
        queryBuilder.withFilter(boolQueryBuilder);

        //构造查询对象
        NativeSearchQuery query = queryBuilder.build();

        //搜索
        AggregatedPage<TbItem> aggregatedPage = esTemplate.queryForPage(query, TbItem.class);

        //分页信息
        System.out.println("总记录数为：" + aggregatedPage.getTotalElements());
        System.out.println("总页数为：" + aggregatedPage.getTotalPages());

        for (TbItem item : aggregatedPage.getContent()) {
            System.out.println(item);
        }
    }


}


















