package com.armedia.acm.plugins.ecm.web;

import com.armedia.acm.pluginmanager.model.AcmPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


import java.util.Map;
import java.util.Properties;

/**
 * Created by jwu on 8/28/14.
 */
@RequestMapping("/plugin/document")
public class DocumentUiController
{
    private Logger log = LoggerFactory.getLogger(getClass());
    private Properties ecmFileServiceProperties;

//    @RequestMapping(method = RequestMethod.GET)
//    public ModelAndView openComplaints(Authentication auth) {
//        ModelAndView mv = new ModelAndView();
//        mv.setViewName("doclist");
//        return mv;
//    }

    @RequestMapping(value = "/{fileId}", method = RequestMethod.GET)
    public ModelAndView openComplaint(Authentication auth, @PathVariable(value = "fileId") Long fileId
    ) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("document");
        mv.addObject("objId", fileId);
        String participantTypes = getEcmFileServiceProperties().getProperty("ecm.participantTypes");
        if(participantTypes != null){
            mv.addObject("participantTypes", participantTypes);
        }
        return mv;
    }

    public Properties getEcmFileServiceProperties() {
        return ecmFileServiceProperties;
    }

    public void setEcmFileServiceProperties(Properties ecmFileServiceProperties) {
        this.ecmFileServiceProperties = ecmFileServiceProperties;
    }
}
