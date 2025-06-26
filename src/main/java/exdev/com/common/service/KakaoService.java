package exdev.com.common.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Service
public class KakaoService {

    @Value("${kakao.apiKey}")
    private String kakaoApiKey;  // 카카오 비즈니스 API 인증 키

    private final String API_URL = "https://kapi.kakao.com/v1/messages";

    // 알림톡/친구톡을 발송하는 메소드
    public void sendKakaoMessage( List<String> recipients, String messageContent) {
    	
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "KakaoAK " + kakaoApiKey);  // 카카오 비즈니스 API 키 설정

        String receiverPhonesJson = String.join("\",\"", recipients); 
        // 메시지 내용 준비
        String payload = "{"
                + "\"receiver_phones\": [\"" + receiverPhonesJson + "\"],"
                + "\"template_object\": {"
                + "\"object_type\": \"text\","
                + "\"text\": \"" + messageContent + "\","
                + "\"link\": {"
                + "\"web_url\": \"https://your.website\","
                + "\"mobile_web_url\": \"https://your.website\""
                + "}"
                + "}"
                + "}";

        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        // POST 요청으로 카카오 API 호출
        ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Message sent successfully: " + response.getBody());
        } else {
            System.out.println("Failed to send message: " + response.getBody());
        }
    }
}
