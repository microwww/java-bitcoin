package com.github.microwww.bitcoin.rpc.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GlobalController {

    @RequestMapping(value = {"/", "/index"}, produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "<html><body style='text-align:center;margin:0;padding:50px 0;'><H1>java bitcoin BLOCK-CHAIN</H1> <p>©2021</p></body></html>";
    }
}
