package com.pinyougou.content.service;

import com.github.pagehelper.PageInfo;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.service.BaseService;

import java.util.List;

public interface ContentService extends BaseService<TbContent> {
    /**
     * 根据条件搜索
     * @param pageNum 页号
     * @param pageSize 页面大小
     * @param content 搜索条件
     * @return 分页信息
     */
    PageInfo<TbContent> search(Integer pageNum, Integer pageSize, TbContent content);

    /**
     * 根据内容分类获取其内容列表
     * @param categoryId 内容分类id
     * @return 内容列表
     */
    List<TbContent> findContentListByCategoryId(Long categoryId);
}
