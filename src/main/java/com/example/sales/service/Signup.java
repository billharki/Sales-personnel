package com.example.sales.service;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.services.cognitoidp.model.*;
import net.sf.kdgcommons.lang.StringUtil;
import net.sf.kdgcommons.lang.ThreadUtil;

public class Signup extends AbstractCognitoServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String emailAddress = request.getParameter(Constants.RequestParameters.EMAIL);
		if (StringUtil.isBlank(emailAddress)) {
			reportResult(response, Constants.ResponseMessages.INVALID_REQUEST);
			return;
		}
		try {
			AdminCreateUserRequest cognitoRequest = new AdminCreateUserRequest().withUserPoolId(cognitoPoolId())
					.withUsername(emailAddress)
					.withUserAttributes(new AttributeType().withName("email").withValue(emailAddress),
							new AttributeType().withName("email_verified").withValue("true"))
					.withDesiredDeliveryMediums(DeliveryMediumType.EMAIL).withForceAliasCreation(Boolean.FALSE);

			cognitoClient1.adminCreateUser(cognitoRequest);
			reportResult(response, Constants.ResponseMessages.USER_CREATED);
		} catch (UsernameExistsException ex) {
			reportResult(response, Constants.ResponseMessages.USER_ALREADY_EXISTS);
		} catch (TooManyRequestsException ex) {
			ThreadUtil.sleepQuietly(250);
			doPost(request, response);
		}
	}

	@Override
	public String getServletInfo() {
		return "Handles the first stage of user signup, creating the user entry";
	}
}
