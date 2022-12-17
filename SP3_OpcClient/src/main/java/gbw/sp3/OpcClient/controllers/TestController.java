package gbw.sp3.OpcClient.controllers;

import gbw.sp3.OpcClient.AsyncEventLoop.EnrichedRunnable;
import gbw.sp3.OpcClient.AsyncEventLoop.iFunction;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static gbw.sp3.OpcClient.AsyncEventLoop.EntryPoint.async;
import static gbw.sp3.OpcClient.AsyncEventLoop.EntryPoint.await;

@RestController
public class TestController {

    private static final String pathRoot = "test/";

    @GetMapping(path=pathRoot+"async")
    public String testMultipleAsync(@RequestParam String[] params)
    {
        System.out.println("Testing multiple async calls");
        List<iFunction<String>> functions = new ArrayList<>(params.length);
        for(int i = 0; i < params.length; i++){
            final int i2 = i;
            functions.add(
                    () -> params[i2] + " - concurrently evaluated B) \n"
            );
        }
        List<EnrichedRunnable<String>> promises = async(functions);
        String response = "";
        for(EnrichedRunnable<String> promise : promises){
            response += await(promise);
        }
        return response;
    }

    @GetMapping(path=pathRoot+"async/{reply}")
    public String testAsync(@PathVariable String reply)
    {
        return await( async(
                () -> reply + " - executed asynchronously B)"
        ));
    }
}
