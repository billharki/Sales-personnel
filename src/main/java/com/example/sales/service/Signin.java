package com.example.sales.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.amazonaws.services.cognitoidp.model.*;
import net.sf.kdgcommons.lang.StringUtil;
import net.sf.kdgcommons.lang.ThreadUtil;


/**
 *  This servlet handles normal user sign-in, based on username and password.
 */
public class Signin extends AbstractCognitoServlet
{
    private static final long serialVersionUID = 1L;


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        String emailAddress = request.getParameter(Constants.RequestParameters.EMAIL);
        String password = request.getParameter(Constants.RequestParameters.PASSWORD);
        if (StringUtil.isBlank(emailAddress) || StringUtil.isBlank(password))
        {
            reportResult(response, Constants.ResponseMessages.INVALID_REQUEST);
            return;
        }


        try
        {
            Map<String,String> authParams = new HashMap<String,String>();
            authParams.put("USERNAME", emailAddress);
            authParams.put("PASSWORD", password);

            AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                    .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .withAuthParameters(authParams)
                    .withClientId(cognitoClientId())
                    .withUserPoolId(cognitoPoolId());

            AdminInitiateAuthResult authResponse = cognitoClient1.adminInitiateAuth(authRequest);
            if (StringUtil.isBlank(authResponse.getChallengeName()))
            {
                updateCredentialCookies(response, authResponse.getAuthenticationResult());
                reportResult(response, Constants.ResponseMessages.LOGGED_IN);
                HttpSession session = request.getSession();
                session.setAttribute("user", emailAddress);
                return;
            }
            else if (ChallengeNameType.NEW_PASSWORD_REQUIRED.name().equals(authResponse.getChallengeName()))
            {
                reportResult(response, Constants.ResponseMessages.FORCE_PASSWORD_CHANGE);
            }
            else
            {
                throw new RuntimeException("unexpected challenge on signin: " + authResponse.getChallengeName());
            }
        }
        catch (UserNotFoundException ex)
        {
            reportResult(response, Constants.ResponseMessages.NO_SUCH_USER);
        }
        catch (NotAuthorizedException ex)
        {
            reportResult(response, Constants.ResponseMessages.NO_SUCH_USER);
        }
        catch (TooManyRequestsException ex)
        {
            ThreadUtil.sleepQuietly(250);
            doPost(request, response);
        }
    }


    @Override
    public String getServletInfo()
    {
        return "Handles user signin";
    }

}