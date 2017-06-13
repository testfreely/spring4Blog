package com.walter.controller;

import com.walter.dao.CategoryDao;
import com.walter.model.CategoryVO;
import com.walter.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.SQLException;

/**
 * Created by yhwang131 on 2016-09-13.
 */
@Controller
public class ConfigController extends BaseController {

	@Autowired
	private CategoryDao categoryDao;

	@Autowired
	private ConfigService configService;

	private String result = new String();

	@RequestMapping(value = "/category", method = RequestMethod.GET)
	public String categoryConfigMap(Model model) {
		return "config/categoryConfig";
	}

	@RequestMapping(value = "/config")
	public String configView(Model model) throws SQLException {
		model.addAttribute("categoryVO", new CategoryVO());
		model.addAttribute("categories", configService.getCategoryList());
		return "config/config";
	}

	@RequestMapping(value = "/config/category", method = RequestMethod.POST)
	public String insCategory(@ModelAttribute("categoryVO") @Valid CategoryVO categoryVO,
	                          Model model, Errors errors) throws SQLException {
		categoryVO.setReg_id(super.getUsername());
		configService.insCategoryItem(categoryVO);
		return "redirect:../config";
	}

	@RequestMapping(value = "/config/setCategory{targetAttribute}", produces = "application/json; charset=utf-8")
	@ResponseBody
	public String setCategory(@PathVariable("targetAttribute") String targetAttribute,
	                          @ModelAttribute("categoryVO") @Valid CategoryVO categoryVO, Errors errors) {
		if (errors.hasErrors()) return gson.toJson("{'success': false}");
		categoryVO.setMod_id(super.getUsername());
		return gson.toJson(configService.modCategoryItem(categoryVO, targetAttribute));
	}

	/*

	@RequestMapping(value = "/category", method = RequestMethod.POST)
	@ResponseBody
	public String setCategoryConfig(@ModelAttribute("categoryVO") CategoryVO categoryVO, Model model) throws SQLException {
		try {
			if(categoryVO.getCategory_cd() == 0){
				if(categoryVO.getParent_category_cd() == 0){
					categoryVO.setDepth(1);
					Integer initCd = categoryDao.getNewCategoryCd(1);
					categoryVO.setCategory_cd(initCd!=null?initCd:100);
				} else {
					categoryVO.setDepth(2);
					categoryVO.setCategory_cd(categoryDao.getNewCategoryCd(2));
				}
			}
			categoryVO.setReg_id(super.getLoginUser()!=null?super.getLoginUser().getUsername():"anonymous");
			categoryDao.setCategory(categoryVO);
			result = "success";
		} catch(Exception e) {
			logger.error(e.getMessage());
			result = "failure";
		}
		return gson.toJson(result);
	}

	@RequestMapping(value = "/category", method = RequestMethod.DELETE)
	@ResponseBody
	public String delCategory(@ModelAttribute("categoryVO") CategoryVO categoryVO, Model model) throws SQLException {
		categoryDao.delCategory(categoryVO.getCategory_cd());
		return gson.toJson(categoryVO.getParent_category_cd());
	}

	*/

	@RequestMapping(value = "/category/setActiveOption", method = RequestMethod.POST)
	@ResponseBody
	public String setActiveOption(@ModelAttribute("categoryVO") CategoryVO categoryVO, Model model) throws SQLException {
		try {
			categoryVO.setReg_id(super.getLoginUser()!=null?super.getLoginUser().getUsername():"anonymous");
			categoryDao.setActiveOption(categoryVO);
			result = "success";
		} catch(Exception e) {
			logger.error(e.getMessage());
			result = "failure";
		}
		return gson.toJson(result);
	}

	@RequestMapping("/category/setOrder")
	@ResponseBody
	public String setOrder(@ModelAttribute("categoryVO") CategoryVO categoryVO, Model model) throws SQLException {
		categoryVO.setReg_id(super.getLoginUser()!=null?super.getLoginUser().getUsername():"anonymous");
		categoryDao.setOrder(categoryVO);
		return gson.toJson("success");
	}
}
