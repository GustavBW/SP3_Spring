package gbw.sp3.OpcClient.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrorController {

    @GetMapping(path="/error")
    public String error404 (){
        return "<h1> How did this happen? </h1>";
    }

}
