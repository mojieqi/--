package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.CwCategory;

/**
 * 校园墙分类Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public interface CwCategoryMapper {

    /**
     * 查询所有分类
     */
    public List<CwCategory> selectCwCategoryList(CwCategory category);

    /**
     * 根据分类代码查询
     */
    public CwCategory selectCwCategoryByCode(String categoryCode);

    /**
     * 根据ID查询
     */
    public CwCategory selectCwCategoryById(Long categoryId);

    /**
     * 新增分类
     */
    public int insertCwCategory(CwCategory category);

    /**
     * 修改分类
     */
    public int updateCwCategory(CwCategory category);

    /**
     * 删除分类
     */
    public int deleteCwCategoryById(Long categoryId);
}
