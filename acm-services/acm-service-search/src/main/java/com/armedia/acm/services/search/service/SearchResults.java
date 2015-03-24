package com.armedia.acm.services.search.service;

import java.util.ArrayList;
import java.util.List;

import com.armedia.acm.services.search.model.SearchConstants;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by armdev on 2/13/15.
 */
public class SearchResults
{
    public int getNumFound(String jsonResults)
    {
        JSONObject jsonResponseHeader = new JSONObject(jsonResults);
        JSONObject jsonResponse = jsonResponseHeader.getJSONObject(SearchConstants.PROPERTY_RESPONSE);
        int numFound = jsonResponse.getInt(SearchConstants.PROPERTY_NUMBER_FOUND);

        return numFound;
    }
    
    public JSONArray getDocuments(String jsonResults)
    {
    	JSONArray retval = null;
    	
    	JSONObject jsonResponseHeader = new JSONObject(jsonResults);
    	
    	if (jsonResponseHeader != null && jsonResponseHeader.has(SearchConstants.PROPERTY_RESPONSE))
    	{
    		JSONObject jsonResponse = jsonResponseHeader.getJSONObject(SearchConstants.PROPERTY_RESPONSE);
    		
    		if (jsonResponse != null && jsonResponse.has(SearchConstants.PROPERTY_DOCS))
    		{
    			retval = jsonResponse.getJSONArray(SearchConstants.PROPERTY_DOCS);
    		}
    	}
    	
    	return retval;
    }
    
    public List<String> getListForField(JSONArray jsonArray, String field)
    {
    	List<String> retval = new ArrayList<String>();
    	
    	if (jsonArray != null && field != null)
    	{
    		for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject object = jsonArray.getJSONObject(i);
				
				if (object.has(field))
				{
					retval.add(object.getString(field));
				}
			}
    	}
    	
    	return retval;
    }
}
