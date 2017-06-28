package com.walter.controller;

import com.walter.config.CustomStringUtils;
import com.walter.config.authentication.SignInUserDetailsService;
import com.walter.model.MemberVO;
import com.walter.service.SocialService;
import com.walter.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.web.ConnectController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.servlet.view.RedirectView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by yhwang131 on 2017-05-17.
 */
@Controller
@RequestMapping("/connect")
public class SocialController extends ConnectController {
	private String TARGET_URL = new String();

	@Autowired
	private SocialService service;

	@Autowired
	private SignInUserDetailsService signInUserDetailsService;

	@Inject
	public SocialController(ConnectionFactoryLocator connectionFactoryLocator, ConnectionRepository connectionRepository) {
		super(connectionFactoryLocator, connectionRepository);
	}

	@RequestMapping(value="/{providerId}", method=RequestMethod.POST)
	public RedirectView connect(@PathVariable String providerId, NativeWebRequest request) {
		HttpServletRequest httpServletRequest = (HttpServletRequest)request.getNativeRequest();
		TARGET_URL = httpServletRequest.getHeader("REFERER");
		return super.connect(providerId, request);
	}

	@RequestMapping(value="/{providerId}", method= RequestMethod.GET, params="code")
	public RedirectView oauth2Callback(@PathVariable String providerId, NativeWebRequest request) {
		RedirectView redirectView = super.oauth2Callback(providerId, request);

		MemberVO memberVO = service.bindingSocialUserInfo(providerId);
		if (memberVO != null) signInUserDetailsService.onAuthenticationWithSocial(memberVO);
		else {
			HttpServletRequest httpServletRequest = (HttpServletRequest)request.getNativeRequest();
			httpServletRequest.getSession().setAttribute("msg",
					CustomStringUtils.executeAlertMessage(Message.ERROR_SOCIAL_API.getText()));
		}
		redirectView.setUrl(TARGET_URL);
		return redirectView;
	}
}