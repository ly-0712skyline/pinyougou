package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.ContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.service.impl.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Service
public class ContentServiceImpl extends BaseServiceImpl<TbContent> implements ContentService {

    //内容列表在redis中的键名
    private static final String REDIS_CONTENT = "CONTNET_LIST";

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void add(TbContent tbContent) {
        super.add(tbContent);
        //更新缓存
        updateContentListInRedisByCategoryId(tbContent.getCategoryId());
    }

    /**
     * 更新redis中分类对应的内容列表（删除）
     * @param categoryId 内容分类id
     */
    private void updateContentListInRedisByCategoryId(Long categoryId) {
        redisTemplate.boundHashOps(REDIS_CONTENT).delete(categoryId);
    }

    @Override
    public void update(TbContent tbContent) {
        //查询老内容
        //需要更新内容分类（当前修改的内容对应的内容分类）对应的内容列表；
        // 如果原来的内容分类与修改之后的内容分类不相同，也需要更新原来内容分类对应的缓存数据。
        TbContent oldContent = findOne(tbContent.getId());
        if (!oldContent.getCategoryId().equals(tbContent.getCategoryId())) {
            updateContentListInRedisByCategoryId(oldContent.getCategoryId());
        }
        super.update(tbContent);

        //更新缓存
        updateContentListInRedisByCategoryId(tbContent.getCategoryId());
    }

    @Override
    public void deleteByIds(Serializable[] ids) {
        /**
         * 根据内容id数组查询其对应的内容列表；遍历每个内容列表，然后根据每个内容中的内容分类删除redis缓存。
         */
        Example example = new Example(TbContent.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));
        List<TbContent> contentList = contentMapper.selectByExample(example);
        for (TbContent tbContent : contentList) {
            updateContentListInRedisByCategoryId(tbContent.getCategoryId());
        }

        super.deleteByIds(ids);
    }

    @Override
    public PageInfo<TbContent> search(Integer pageNum, Integer pageSize, TbContent content) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbContent.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //模糊查询
        /**if (StringUtils.isNotBlank(content.getProperty())) {
            criteria.andLike("property", "%" + content.getProperty() + "%");
        }*/

        List<TbContent> list = contentMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public List<TbContent> findContentListByCategoryId(Long categoryId) {
        List<TbContent> contentList = null;

        try {
            //查询redis中是否存在分类对应的内容列表
            contentList = (List<TbContent>) redisTemplate.boundHashOps(REDIS_CONTENT).get(categoryId);
            if (contentList != null && contentList.size() > 0) {
                return contentList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * -- 查询有效，内容分类为 轮播广告 的内容并根据排序字段降序排序
         * select * from tb_content where status='1' and category_id=? order by sort_roder desc
         */
        Example example = new Example(TbContent.class);

        example.createCriteria()
                .andEqualTo("status", "1")
                .andEqualTo("categoryId", categoryId);

        //设置排序
        example.orderBy("sortOrder").desc();

        contentList = contentMapper.selectByExample(example);

        try {
            //将内容列表存入到redis
            redisTemplate.boundHashOps(REDIS_CONTENT).put(categoryId, contentList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contentList;
    }

}
