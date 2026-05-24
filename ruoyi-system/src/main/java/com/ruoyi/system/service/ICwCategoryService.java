package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.CwCategory;

/**
 * 校园墙分类Service接口
 *
 * @author ruoyi
 * @date 2026-05-24
 */
public interface ICwCategoryService {

    /**
     * 查询所有启用的分类
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
}
