package com.emotibot.correctionSolr.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emotibot.correctionSolr.service.CorrectionCommandService;
import com.emotibot.correctionSolr.service.CorrectionSolrService;

@RestController
@RequestMapping("/correction")
public class SolrCorrectionController
{
    @Autowired
    CorrectionSolrService correctionSolrService;
    
    @Autowired
    CorrectionCommandService correctionCommandService;
    
    @RequestMapping(value="/getCorrectionName", method = RequestMethod.GET)
    public String getCorrectionName(@RequestParam(value="text", required=true) String sentence)
    {
        return correctionSolrService.getCorrectionName(sentence);
    }
    
    @RequestMapping(value="/postCorrectionName", method = RequestMethod.POST)
    public String postCorrectionName(@RequestBody String sentence)
    {
        return correctionSolrService.getCorrectionName(sentence);
    }
    
    @RequestMapping(value="/getLikelyName", method = RequestMethod.GET)
    public String getLikelyName(@RequestParam(value="text", required=true) String sentence)
    {
        return correctionSolrService.getLikelyName(sentence);
    }
    
    @RequestMapping(value="/getCorrectionCommand", method = RequestMethod.GET)
    public String getCorrectionCommand(@RequestParam(value="text", required=true) String sentence)
    {
        return correctionCommandService.getCorrectionCommand(sentence);
    }
}
