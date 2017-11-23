package com.example.sales.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class CredentialsCache implements Serializable {

	 private static final long serialVersionUID = 1L;

	    private static final long DEFAULT_TIMEOUT = 15 * 60 * 1000L;

	    private Map<String,Date> cache;


	    /**
	     *  Creates a new cache, holding up to <code>maxEntries</code> entries.
	     */
	    public CredentialsCache(final int maxEntries)
	    {
	    	
	    	cache = Collections.synchronizedMap(new LinkedHashMap<String, Date>() {

				private static final long serialVersionUID = 1L;
				
				@Override
				protected boolean removeEldestEntry(java.util.Map.Entry<String, Date> eldest) {
					// TODO Auto-generated method stub
					return super.removeEldestEntry(eldest);
				}
	    		
	    	});
	    	
	    }


	    /**
	     *  Adds an access token to the cache, with default (1 hour) timeout.
	     */
	    public void addToken(String accessToken)
	    {
	        addToken(accessToken, DEFAULT_TIMEOUT);
	    }


	    /**
	     *  Adds an access token to the cache with specified timeout (in millis).
	     *  This should be called when an uncached token has been validated (which
	     *  would happen when the app restarts).
	     */
	    public void addToken(String accessToken, long timeoutMillis)
	    {
	        cache.put(accessToken, new Date(System.currentTimeMillis() + timeoutMillis));
	    }

	    /**
	     *  Checks the cache for the given access token, returning true if the token
	     *  exists and has not yet timed out.
	     */
	    public boolean checkToken(String accessToken)
	    {
	        Date expirationDate = cache.get(accessToken);
	        if (expirationDate == null)
	        {
	            return false;
	        }
	        else if (System.currentTimeMillis() > expirationDate.getTime())
	        {
	            cache.remove(accessToken);
	            return false;
	        }
	        else
	        {
	            return true;
	        }
	    }
	
}
