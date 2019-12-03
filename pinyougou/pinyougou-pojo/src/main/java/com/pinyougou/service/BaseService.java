package com.pinyougou.service;

import com.github.pagehelper.PageInfo;

import java.io.Serializable;
import java.util.List;

public interface BaseService<T> {
    //根据主键查询
    //T findOne(Object id);
    T findOne(Serializable id);
    
    //查询全部
    List<T> findAll();
    
    //条件查询
    List<T> findByWhere(T t);
    
    //分页查询
    PageInfo<T> findPage(Integer pageNum, Integer pageSize);

    //条件分页查询
    PageInfo<T> findPage(Integer pageNum, Integer pageSize, T t);

    //新增
    void add(T t);

    //根据主键更新
    void update(T t);

    //批量删除
    void deleteByIds(Serializable[] ids);
}
