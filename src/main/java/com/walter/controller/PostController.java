package com.walter.controller;

import com.google.api.services.drive.model.File;
import com.walter.dao.ApiDao;
import com.walter.dao.PostDao;
import com.walter.model.PostVO;
import com.walter.service.GoogleDriveService;
import com.walter.service.PostService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.util.HashMap;

/**
 * Created by yhwang131 on 2016-10-11.
 */
@Controller
@RequestMapping("/post")
public class PostController extends BaseController {

	@Autowired
	private PostService service;

	@Resource(name = "googleDriveServiceImage")
	private GoogleDriveService googleDriveImageService;

	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public String registerPostForm(Model model) {
		model = service.setInputForm(model);
		model.addAttribute("postVO", new PostVO(true, true));
		return "post/postForm";
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public String registerPost(@ModelAttribute("postVO") @Valid PostVO postVO, Errors errors) {
		if(errors.hasErrors()) {
			return "post/postForm";
		}
		postVO.setReg_id(super.getLoginUser()!=null?super.getLoginUser().getUsername():"anonymous");
		service.setPost(postVO);
		return "redirect:" + postVO.getPost_cd();
	}

	@RequestMapping(value = "/{post_cd}")
	public String postView(@PathVariable int post_cd, Model model) {
		model.addAttribute("post", service.getPost(post_cd));
		return "post/postView";
	}

	@RequestMapping(value = "/fileUpload", method = RequestMethod.GET)
	public String fileUploadForm() {
		return "post/fileUpload";
	}

	@RequestMapping(value = "/imageUpload", method = RequestMethod.POST)
	public String imgUpload(@RequestParam("upload")MultipartFile file, Model model, HttpServletRequest httpServletRequest) throws IOException {
		File resultFile = googleDriveImageService.createFile(file);
		model.addAttribute("CKEditorFuncNum", httpServletRequest.getParameter("CKEditorFuncNum"));
		model.addAttribute("fileURL", "\\/post\\/images\\/" + resultFile.getId());
		return "post/imgUpload";
	}

	@RequestMapping(value = "/dragAndDropUpload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String dragAndDropUpload(@RequestParam("upload")MultipartFile file) throws IOException {
		HashMap<String, Object> resultMap = new HashMap<>();
		try {
			File resultFile = googleDriveImageService.createFile(file);
			resultMap.put("uploaded", 1);
			resultMap.put("fileName", resultFile.getName());
			resultMap.put("url", "/post/images/" + resultFile.getId());
		} catch(IOException ioe) {
			ioe.printStackTrace();
			resultMap.put("uploaded", 0);
			resultMap.put("error", new HashMap<String, Object>().put("message", ioe.getMessage()));
		}
		return gson.toJson(resultMap);
	}

	@RequestMapping(value = "/images/{file_id}", method = RequestMethod.GET)
	public void imgView(@PathVariable String file_id, HttpServletResponse httpServletResponse) throws IOException {
		HashMap<String, Object> hashMap = googleDriveImageService.openFile(file_id);
		httpServletResponse.setContentType(hashMap.get("mimeType").toString());
		httpServletResponse.getOutputStream().write(IOUtils.toByteArray((InputStream)hashMap.get("data")));
	}
}