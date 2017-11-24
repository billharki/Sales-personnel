package com.example.sales.service;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

import net.sf.kdgcommons.lang.StringUtil;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.example.sales.util.CredentialsCache;

public class AbstractCognitoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

protected BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAJPI3LCGF27DTU5DA", "QCasniQFTtQEiQguyn0JDGA11Scj2mBrgcTX3Gu/");
	
//	protected AWSCredentialsProvider instanceProfileCredentialsProvider = new InstanceProfileCredentialsProvider(false);
//	protected AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
    protected AWSCognitoIdentityProvider cognitoClient1 = AWSCognitoIdentityProviderClientBuilder.standard()
    									.withCredentials(new AWSStaticCredentialsProvider(awsCreds)).withRegion(Regions.AP_SOUTH_1).build();
    
    protected static CredentialsCache tokenCache = new CredentialsCache(10000);

    protected String cognitoPoolId()
    {
        return getServletContext().getInitParameter("cognito_pool_id");
    }

    protected String cognitoClientId()
    {
        return getServletContext().getInitParameter("cognito_client_id");
    }

    protected void updateCredentialCookies(HttpServletResponse response, AuthenticationResultType authResult)
    {
        tokenCache.addToken(authResult.getAccessToken());

        Cookie accessTokenCookie = new Cookie(Constants.CookieNames.ACCESS_TOKEN, authResult.getAccessToken());
        response.addCookie(accessTokenCookie);

        if (!StringUtil.isBlank(authResult.getRefreshToken()))
        {
            Cookie refreshTokenCookie = new Cookie(Constants.CookieNames.REFRESH_TOKEN, authResult.getRefreshToken());
            response.addCookie(refreshTokenCookie);
        }
    }

    protected void reportResult(HttpServletResponse response, String responseMessage)
    throws ServletException, IOException
    {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        try (PrintWriter out = response.getWriter())
        {
            out.print(responseMessage);
        }
    }
	
}
