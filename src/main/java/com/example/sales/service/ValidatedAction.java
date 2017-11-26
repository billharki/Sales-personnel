package com.example.sales.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.kdgcommons.lang.StringUtil;
import net.sf.kdgcommons.lang.ThreadUtil;

import com.amazonaws.services.cognitoidp.model.AWSCognitoIdentityProviderException;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.TooManyRequestsException;

/**
 * This servlet takes the place of some action that requires a valid user. It
 * simply returns text indicating whether or not the user is authenticated.
 * <p>
 * In a real application, this validation logic (and associated cache) should be
 * pushed into the abstract servlet.
 */
@WebFilter("/app/*")
public class ValidatedAction extends AbstractCognitoServlet implements Filter {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String accessToken = null;
		String refreshToken = null;

		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			reportResult(response, Constants.ResponseMessages.NOT_LOGGED_IN);
			return;
		}

		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(Constants.CookieNames.ACCESS_TOKEN))
				accessToken = cookie.getValue();
			if (cookie.getName().equals(Constants.CookieNames.REFRESH_TOKEN))
				refreshToken = cookie.getValue();
		}

		if (tokenCache.checkToken(accessToken)) {
			reportResult(response, Constants.ResponseMessages.LOGGED_IN);
			return;
		}

		try {
			// GetUserRequest authRequest = new
			// GetUserRequest().withAccessToken(accessToken);
			// GetUserResult authResponse = cognitoClient.getUser(authRequest);

			tokenCache.addToken(accessToken);
			reportResult(response, Constants.ResponseMessages.LOGGED_IN);
		} catch (NotAuthorizedException ex) {
			if (ex.getErrorMessage().equals("Access Token has expired")) {
				attemptRefresh(refreshToken, response);
			} else {
				reportResult(response, Constants.ResponseMessages.NOT_LOGGED_IN);
			}
		} catch (TooManyRequestsException ex) {
			ThreadUtil.sleepQuietly(250);
			doPost(request, response);
		}
	}

	/**
	 * Attempts to create a new access token based on the provided refresh token.
	 */
	private void attemptRefresh(String refreshToken, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			Map<String, String> authParams = new HashMap<String, String>();
			authParams.put("REFRESH_TOKEN", refreshToken);

			AdminInitiateAuthRequest refreshRequest = new AdminInitiateAuthRequest()
					.withAuthFlow(AuthFlowType.REFRESH_TOKEN).withAuthParameters(authParams)
					.withClientId(cognitoClientId()).withUserPoolId(cognitoPoolId());

			AdminInitiateAuthResult refreshResponse = cognitoClient1.adminInitiateAuth(refreshRequest);
			if (StringUtil.isBlank(refreshResponse.getChallengeName())) {
				updateCredentialCookies(response, refreshResponse.getAuthenticationResult());
				reportResult(response, Constants.ResponseMessages.LOGGED_IN);
			} else {
				reportResult(response, Constants.ResponseMessages.NOT_LOGGED_IN);
			}
		} catch (TooManyRequestsException ex) {
			ThreadUtil.sleepQuietly(250);
			attemptRefresh(refreshToken, response);
		} catch (AWSCognitoIdentityProviderException ex) {
			reportResult(response, Constants.ResponseMessages.NOT_LOGGED_IN);
		}
	}

	public ValidatedAction() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String accessToken = null;
		String refreshToken = null;

		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			response.sendRedirect(request.getContextPath() + "/confirm-signup.html");
		}

		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(Constants.CookieNames.ACCESS_TOKEN))
				accessToken = cookie.getValue();
			if (cookie.getName().equals(Constants.CookieNames.REFRESH_TOKEN))
				refreshToken = cookie.getValue();
		}

		if (tokenCache.checkToken(accessToken)) {
			chain.doFilter(req, res);
		}

		try {

			tokenCache.addToken(accessToken);
			chain.doFilter(req, res);
		} catch (NotAuthorizedException ex) {
			if (ex.getErrorMessage().equals("Access Token has expired")) {
				attemptRefresh(refreshToken, response);
			} else {
				response.sendRedirect(request.getContextPath() + "/confirm-signup.html");
			}
		} catch (TooManyRequestsException ex) {
			ThreadUtil.sleepQuietly(250);
			doPost(request, response);
		}

	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

	@Override
	public String getServletInfo() {
		return "Checks authorization based on tokens stored in cookies";
	}

}