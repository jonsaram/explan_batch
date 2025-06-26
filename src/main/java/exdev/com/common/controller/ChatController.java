package exdev.com.common.controller;

import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/askerSendMessage")// asker → 서버
    @SendTo("/topic/publicAnswerer")    // 서버 → answerer
    public Map<String, Object> sendMessage(@Payload Map<String, Object> message) {
        return message;
    }

    @MessageMapping("/answererSendMessage")// answerer → 서버
    @SendTo("/topic/publicAsker")          // 서버 → asker
    public Map<String, Object> sendMessage1(@Payload Map<String, Object> message) {
        return message;
    }
}
