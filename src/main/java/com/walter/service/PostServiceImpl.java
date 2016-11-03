package com.walter.service;

import com.walter.config.CustomStringUtils;
import com.walter.dao.ApiDao;
import com.walter.dao.PostDao;
import com.walter.model.*;
import com.walter.repository.CommentRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * Created by yhwang131 on 2016-10-27.
 */
@Service
public class PostServiceImpl implements PostService {

	@Autowired
	private PostDao dao;

	@Autowired
	private ApiDao apiDao;

	@Autowired
	private CommentRepository repository;

	private static String DATE_FORMAT = "yyyy-MM-dd HH:mm";

	@Override
	public Model setInputForm(Model model) {
		List<CategoryVO> categoryList = new ArrayList<>();
		apiDao.getCategoryList(new CategoryVO(1,0)).stream()
				.forEach(category -> {
					categoryList.add(category);
					categoryList.addAll(apiDao.getCategoryList(new CategoryVO(2,category.getCategory_cd())));
				});

		model.addAttribute("categoryList", categoryList);
		model.addAttribute("countryList", apiDao.getCodeList(new CodeVO("NAT")));
		return model;
	}

	@Override
	public int setPost(PostVO postVO) {
		return dao.setPost(postVO);
	}

	@Override
	public PostVO getPost(int post_cd) {
		PostVO postVO = dao.getPost(post_cd);
		postVO.setDf_reg_dt(CustomStringUtils.dateToString(postVO.getReg_dt(), DATE_FORMAT));
		if(postVO.getMod_dt() != null) {
			postVO.setDf_mod_dt(CustomStringUtils.dateToString(postVO.getMod_dt(), DATE_FORMAT));
		}
		return postVO;
	}

	@Override
	public HashMap<String, Object> getPostList(PostSearchVO postSearchVO) throws IndexOutOfBoundsException {
		PagingVO pagingVO = new PagingVO(postSearchVO.getCurrPageNo(), postSearchVO.getRowsPerPage());
		int offset = (pagingVO.getCurrPageNo() - 1) * pagingVO.getRowsPerPage();
		postSearchVO.setOffset(offset);
		List<PostVO> postVOList = dao.getPostList(postSearchVO);
		List<PostVO> resultList = new ArrayList<>();

		if(postVOList.size() > 0) {
			int limit = offset + pagingVO.getRowsPerPage();
			if(limit > postVOList.size()) limit = postVOList.size();
			resultList = postVOList.subList(offset, limit);
			resultList.stream().forEach(p -> p.setDf_reg_dt(CustomStringUtils.dateToString(p.getReg_dt(), DATE_FORMAT)));
		}
		pagingVO.setNumberOfRows(postVOList.size());
		pagingVO.Paging();

		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("postList", resultList);
		hashMap.put("paging", pagingVO);
		return hashMap;
	}

	@Override
	public void setComment(CommentVO commentVO) {
		repository.insert(commentVO);
	}

	@Override
	public CommentVO setReply(String _id, CommentVO commentVO) {
		/*commentVO = mongoOps.findAndModify(query(where("_id").is(new ObjectId(_id))),
				new Update().push("replys", commentVO),
				new FindAndModifyOptions().returnNew(true),
				CommentVO.class);*/
		/*System.out.println("_id : " + _id);
		CommentVO comment = repository.findOne(_id);
		comment.addReplys(commentVO);
		repository.save(comment);*/
		return commentVO;
	}

	@Override
	public CommentVO getCommentById(String _id) {
		return repository.findOne(_id);
	}

	@Override
	public List<CommentVO> getComments(int parentPostCd) {
		return repository.findByPostCd(parentPostCd);
	}
}
