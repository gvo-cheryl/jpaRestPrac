package com.france.main;

import com.france.service.ProductService;
import lombok.RequiredArgsConstructor;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MainController {

    private final ProductService productService;

    @GetMapping(value = "/")
    public String home() {
        return "Server OK.";
    }

    @GetMapping(value="/products")
    public JSONArray callAPI() throws ParseException {
        return productService.saveList();
    }
}
