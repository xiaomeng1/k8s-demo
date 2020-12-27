package com.kubernetes;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by mengxiangli on 2020-12-20.
 */
@RestController
public class FirstController {

    @GetMapping("/hello")
    public String helloKubernetes() {
        return "hello k8s";
    }
}
