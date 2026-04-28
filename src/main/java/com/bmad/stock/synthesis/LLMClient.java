package com.bmad.stock.synthesis;

import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.Map;

@HttpExchange("/v1/chat/completions")
public interface LLMClient {

    @PostExchange
    Map<String, Object> generateChatCompletion(@RequestBody Map<String, Object> request);
    
    // Standard request structure:
    // {
    //   "model": "gpt-4-turbo",
    //   "messages": [
    //     {"role": "system", "content": "..."},
    //     {"role": "user", "content": "..."}
    //   ]
    // }
}
