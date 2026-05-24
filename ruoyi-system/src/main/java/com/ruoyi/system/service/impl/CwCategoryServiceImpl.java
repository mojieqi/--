package com.ruoyi.system.service.impl;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.CwCategory;
import com.ruoyi.system.mapper.CwCategoryMapper;
import com.ruoyi.system.service.ICwCategoryService;

/**
 * 校园墙分类Service实现
 *
 * @author ruoyi
 * @date 2026-05-24
 */
@Service
public class CwCategoryServiceImpl implements ICwCategoryService {

    @Autowired
    private CwCategoryMapper categoryMapper;

    @Override
    public List<CwCategory> selectCwCategoryList(CwCategory category) {
        if (category == null) {
            category = new CwCategory();
        }
        category.setStatus("0");
        return categoryMapper.selectCwCategoryList(category);
    }

    @Override
    public CwCategory selectCwCategoryByCode(String categoryCode) {
        return categoryMapper.selectCwCategoryByCode(categoryCode);
    }

    @Override
    public CwCategory selectCwCategoryById(Long categoryId) {
        return categoryMapper.selectCwCategoryById(categoryId);
    }
}
