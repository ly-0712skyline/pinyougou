package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Goods;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Transactional
@Service
public class GoodsServiceImpl extends BaseServiceImpl<TbGoods> implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsDescMapper goodsDescMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private SellerMapper sellerMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private ItemCatMapper itemCatMapper;

    @Override
    public PageInfo<TbGoods> search(Integer pageNum, Integer pageSize, TbGoods goods) {
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        //创建查询对象
        Example example = new Example(TbGoods.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //只查询没有删除的商品
        criteria.andNotEqualTo("isDelete", "1");

        //商家id
        if (StringUtils.isNotBlank(goods.getSellerId())) {
            criteria.andEqualTo("sellerId", goods.getSellerId());
        }

        //商品状态
        if (StringUtils.isNotBlank(goods.getAuditStatus())) {
            criteria.andEqualTo("auditStatus", goods.getAuditStatus());
        }


        //商品名称模糊查询
        if (StringUtils.isNotBlank(goods.getGoodsName())) {
            criteria.andLike("goodsName", "%" + goods.getGoodsName() + "%");
        }

        List<TbGoods> list = goodsMapper.selectByExample(example);
        return new PageInfo<>(list);
    }

    @Override
    public void addGoods(Goods goods) {
        //保存商品基本信息
        add(goods.getGoods());

        //int i = 1/0;

        //保存商品描述信息
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());

        goodsDescMapper.insertSelective(goods.getGoodsDesc());

        //保存商品sku列表
        saveItemList(goods);
    }

    @Override
    public Goods findGoodsById(Long id) {
        return findGoodsByIdAndItemStatus(id, null);
    }

    @Override
    public void updateGoods(Goods goods) {
        //更新基本信息
        update(goods.getGoods());

        //更新描述信息
        goodsDescMapper.updateByPrimaryKeySelective(goods.getGoodsDesc());

        //根据商品spu id删除其对应的sku
        //delete from tb_item where goods_id=?
        TbItem param = new TbItem();
        param.setGoodsId(goods.getGoods().getId());
        itemMapper.delete(param);
        //新增商品sku列表
        saveItemList(goods);
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        //根据商品spu id数组批量修改商品spu的审核状态
        //update tb_goods set audit_status = ? where id in (?, ?,? )
        //要更新的数据
        TbGoods tbGoods = new TbGoods();
        tbGoods.setAuditStatus(status);
        //更新条件
        Example example = new Example(TbGoods.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));
        goodsMapper.updateByExampleSelective(tbGoods, example);

        //- 如果审核不通过则需要将商品spu的状态修改为审核不通过3，将对应的sku状态修改为待启用0
        TbItem tbItem = new TbItem();
        tbItem.setStatus("0");

        //- 如果审核通过则需要将商品spu的状态修改为审核通过2，将对应的sku状态修改为已启用1
        if ("2".equals(status)) {
            //审核通过，需要启用sku
            tbItem.setStatus("1");
        }
        //update tb_item set status=? where goods_id in(?,?,?)
        Example itemExample = new Example(TbItem.class);
        itemExample.createCriteria()
                .andIn("goodsId", Arrays.asList(ids));

        itemMapper.updateByExampleSelective(tbItem, itemExample);
    }

    @Override
    public void deleteGoodsByIds(Long[] ids) {
        //批量更新商品的删除状态为已删除1
        //update tb_goods set is_delete='1' where id in(?,..?)
        TbGoods tbGoods = new TbGoods();
        tbGoods.setIsDelete("1");
        //更新条件
        Example example = new Example(TbGoods.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));
        goodsMapper.updateByExampleSelective(tbGoods, example);
    }

    @Override
    public List<TbItem> findGoodsByIdsAndStatus(Long[] ids, String itemStatus) {
        Example example = new Example(TbItem.class);
        example.createCriteria()
                .andEqualTo("status", itemStatus)
                .andIn("goodsId", Arrays.asList(ids));
        return itemMapper.selectByExample(example);
    }

    @Override
    public Goods findGoodsByIdAndItemStatus(Long goodsId, String itemStatus) {
        Goods goods = new Goods();

        //1、根据商品spu id查询基本信息
        goods.setGoods(findOne(goodsId));

        //2、根据商品spu id查询描述信息
        goods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(goodsId));

        //2、根据商品spu id查询sku列表
        // select * from tb_item where goods_id=? and status='1' order by is_default desc
        Example itemExample = new Example(TbItem.class);
        Example.Criteria criteria = itemExample.createCriteria();

        criteria.andEqualTo("goodsId", goodsId);

        if (StringUtils.isNotBlank(itemStatus)) {
            criteria.andEqualTo("status", itemStatus);
        }
        itemExample.orderBy("isDefault").desc();

        List<TbItem> itemList = itemMapper.selectByExample(itemExample);
        goods.setItemList(itemList);

        return goods;
    }

    /**
     * 保存商品sku
     *
     * @param goods 商品信息（基本、描述、sku列表）
     */
    private void saveItemList(Goods goods) {
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            //启用规格
            if (goods.getItemList() != null && goods.getItemList().size() > 0) {
                for (TbItem tbItem : goods.getItemList()) {

                    //标题：spu商品名称+当前这个sku的所有规格选项值拼接而成
                    String title = goods.getGoods().getGoodsName();
                    //将规格转换为对象，然后将其属性值拼接到标题中
                    Map<String, String> specMap = JSON.parseObject(tbItem.getSpec(), Map.class);
                    for (Map.Entry<String, String> entry : specMap.entrySet()) {
                        title += " " + entry.getValue();
                    }
                    tbItem.setTitle(title);

                    setItemValue(tbItem, goods);

                    //保存商品sku
                    itemMapper.insertSelective(tbItem);
                }
            }

        } else {
            //不启用规格；默认基于spu生成一条sku商品
            TbItem tbItem = new TbItem();
            //价格
            tbItem.setPrice(goods.getGoods().getPrice());
            //库存
            tbItem.setNum(9999);
            //是否启用；不启用
            tbItem.setStatus("0");
            //是否默认
            tbItem.setIsDefault("1");
            //规格
            tbItem.setSpec("{}");

            tbItem.setTitle(goods.getGoods().getGoodsName());

            setItemValue(tbItem, goods);

            itemMapper.insertSelective(tbItem);
        }
    }

    /**
     * 设置sku商品信息
     *
     * @param tbItem sku
     * @param goods  vo商品信息
     */
    private void setItemValue(TbItem tbItem, Goods goods) {
        //图片，获取spu的第1张图片
        if (StringUtils.isNotBlank(goods.getGoodsDesc().getItemImages())) {
            List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
            if (imageList != null && imageList.size() > 0) {
                tbItem.setImage(imageList.get(0).get("url").toString());
            }
        }

        tbItem.setGoodsId(goods.getGoods().getId());
        //未审核
        tbItem.setStatus("0");
        //商家
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        tbItem.setSellerId(seller.getSellerId());
        tbItem.setSeller(seller.getName());
        //品牌
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        tbItem.setBrand(brand.getName());

        //分类;使用商品spu中的第3级商品分类
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        tbItem.setCategoryid(itemCat.getId());
        tbItem.setCategory(itemCat.getName());

        tbItem.setCreateTime(new Date());
        tbItem.setUpdateTime(tbItem.getCreateTime());

    }

}
