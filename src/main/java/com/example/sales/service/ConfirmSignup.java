package com.example.sales.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.services.cognitoidentity.model.GetOpenIdTokenForDeveloperIdentityRequest;
import com.amazonaws.services.cognitoidp.model.*;
import net.sf.kdgcommons.lang.StringUtil;
import net.sf.kdgcommons.lang.ThreadUtil;

public class ConfirmSignup extends AbstractCognitoServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String emailAddress = request.getParameter(Constants.RequestParameters.EMAIL);
		String tempPassword = request.getParameter(Constants.RequestParameters.TEMPORARY_PASSWORD);
		String finalPassword = request.getParameter(Constants.RequestParameters.PASSWORD);
		if (StringUtil.isBlank(emailAddress) || StringUtil.isBlank(tempPassword) || StringUtil.isBlank(finalPassword)) {
			reportResult(response, Constants.ResponseMessages.INVALID_REQUEST);
			return;
		}


		try {
			// must attempt signin with temporary password in order to establish session for
			// password change
			// (even though it's documented as not required)

			Map<String, String> initialParams = new HashMap<String, String>();
			initialParams.put("USERNAME", emailAddress);
			initialParams.put("PASSWORD", tempPassword);
			
			AdminInitiateAuthRequest initialRequest = new AdminInitiateAuthRequest()
					.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
					.withAuthParameters(initialParams)
					.withClientId(cognitoClientId())
					.withUserPoolId(cognitoPoolId());

			AdminInitiateAuthResult initialResponse = cognitoClient1.adminInitiateAuth(initialRequest);
			if (!ChallengeNameType.NEW_PASSWORD_REQUIRED.name().equals(initialResponse.getChallengeName())) {
				throw new RuntimeException("unexpected challenge: " + initialResponse.getChallengeName());
			}

			Map<String, String> challengeResponses = new HashMap<String, String>();
			challengeResponses.put("USERNAME", emailAddress);
			challengeResponses.put("PASSWORD", tempPassword);
			challengeResponses.put("NEW_PASSWORD", finalPassword);

			AdminRespondToAuthChallengeRequest finalRequest = new AdminRespondToAuthChallengeRequest()
					.withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
					.withChallengeResponses(challengeResponses).withClientId(cognitoClientId())
					.withUserPoolId(cognitoPoolId()).withSession(initialResponse.getSession());

			AdminRespondToAuthChallengeResult challengeResponse = cognitoClient1
					.adminRespondToAuthChallenge(finalRequest);
			if (StringUtil.isBlank(challengeResponse.getChallengeName())) {
				updateCredentialCookies(response, challengeResponse.getAuthenticationResult());
				reportResult(response, Constants.ResponseMessages.LOGGED_IN);
			} else {
				throw new RuntimeException("unexpected challenge: " + challengeResponse.getChallengeName());
			}
		} catch (InvalidPasswordException ex) {
			reportResult(response, Constants.ResponseMessages.INVALID_PASSWORD);
		} catch (UserNotFoundException ex) {
			reportResult(response, Constants.ResponseMessages.NO_SUCH_USER);
		} catch (NotAuthorizedException ex) {
			reportResult(response, Constants.ResponseMessages.NO_SUCH_USER);
		} catch (TooManyRequestsException ex) {
			ThreadUtil.sleepQuietly(250);
			doPost(request, response);
		}
	}

	@Override
	public String getServletInfo() {
		return "Replacing temporary password with final!";
	}
}
