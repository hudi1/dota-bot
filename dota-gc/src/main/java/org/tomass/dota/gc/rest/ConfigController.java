package org.tomass.dota.gc.rest;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tomass.dota.gc.config.AppConfig;

@RestController
public class ConfigController extends BaseCommonController {

    private final Logger log = LoggerFactory.getLogger(ConfigController.class);

    @Autowired
    private AppConfig appConfig;

    @RequestMapping(value = "/config/app", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> konfigurujApp(@RequestParam String funkce,
            @RequestParam(required = false) boolean bezKonverze, @RequestParam(required = false) String args,
            @RequestParam(required = false) Integer batch, @RequestParam(required = false) String oddelovace,
            HttpServletRequest request) {

        log.debug(">>configApp '{}' '{}' '{}' '{}'", funkce, args, batch, oddelovace);
        String result = konfigurujBatch(funkce, bezKonverze, args, batch, appConfig, oddelovace);
        log.debug("<<configApp '{}'", result);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}