package exdev.com.common.controller;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class KakaoOAuthController {

    @Value("${kakao.init.key}")private String kakaoInitKey;
    @Value("${kakao.redirectUri}")private String kakaoRedirectUri;
    @Value("${kakao.clientId}")private String kakaoClientId;
    @Value("${naver.client.key}")private String naverClientKey;

    @GetMapping("/oauth")
    public ResponseEntity<?>  kakaoRedirect(
    	    @RequestParam("code") String code,
    	    @RequestParam(value = "state", required = false) String state,
    	    HttpServletRequest requset, 
    	    HttpServletResponse response, HttpSession session
    		) {
    	
        String loginType = null;
        String returnUrl = null;
        if (state != null) {
            try {
                String decodedState = new String(Base64.getDecoder().decode(state));
                ObjectMapper mapper = new ObjectMapper();
                JsonNode stateJson = mapper.readTree(decodedState);
                loginType = stateJson.get("loginType").asText();
                returnUrl = stateJson.get("returnUrl").asText();
            } catch (Exception e) {
                System.out.println("state 파싱 실패: " + e.getMessage());
            }
        }

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);//"308c1a25132ab27c95b73a4d0e3f514c");
        body.add("redirect_uri",kakaoRedirectUri);// "http://localhost:8083/oauth");
        body.add("code", code);//카카오에 Access Token 요청을 위한 인가 코드

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        //토큰요청
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(tokenUrl, request, String.class);

        try {
            // access_token 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseEntity.getBody());
            String accessToken = jsonNode.get("access_token").asText();

            // 사용자 정보 Rest API
            String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(accessToken);
            userInfoHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);
            ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.POST,
                    userInfoRequest,
                    String.class
            );

            JsonNode userInfo = objectMapper.readTree(userInfoResponse.getBody());
            String kakaoId 	= userInfo.get("id").asText();
            String nickname = userInfo.get("properties").get("nickname").asText();
            String email 	= userInfo.get("kakao_account").get("email").asText();

            Map<String, Object> userInfoMap = new HashMap<>();
            userInfoMap.put("id", kakaoId);
            userInfoMap.put("userName", nickname);
            userInfoMap.put("mailType", "KAKAO");
            userInfoMap.put("eMail", email);
            userInfoMap.put("loginType", loginType);

           // setSession.do
            RestTemplate restTemplate2 = new RestTemplate();
            HttpHeaders headers2 = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request2 = new HttpEntity<>(userInfoMap, headers2);
            ResponseEntity<Map> response2 = restTemplate2.postForEntity(
            		getSetSessionUrl(requset), request2, Map.class
            );

            if ("S".equals(response2.getBody().get("state"))) {
    			
    			String html = "<script>" + "fetch('/setSession.do', {" +
    			"  method: 'POST'," + "  headers: {'Content-Type': 'application/json'}," +
    			"  body: JSON.stringify(" + new
    			ObjectMapper().writeValueAsString(userInfoMap) + ")," +
    			"  credentials: 'include'" + "}).then(res => res.json()).then(data => {" +
    			"  window.location.replace('/start.html');" + "});" +
    			"</script>";
    			
    			return ResponseEntity.ok() .contentType(MediaType.TEXT_HTML) .body(html);
    				
            } else {
                //세션 설정 실패
            	if("E".equals(response2.getBody().get("state"))) {
            		
            		String REASON =(String) response2.getBody().get("REASON"); 
            		try {
            			response.sendRedirect("/loginMain.html?REASON=" + URLEncoder.encode(REASON, "UTF-8"));
            		} catch (IOException e1) {
            			// TODO Auto-generated catch block
            			e1.printStackTrace();
            		}            	
            	}
            }

        } catch (Exception e) {
				e.printStackTrace();
        }
		return responseEntity;        
    }

	private String getSetSessionUrl(HttpServletRequest requset) throws MalformedURLException {
        String scheme = requset.getScheme();// http
        String serverName = requset.getServerName();// localhost
        int serverPort = requset.getServerPort();// 8083
        String baseUrl = scheme + "://" + serverName + ":" + serverPort;
        String fullUrl = baseUrl + "/setSession.do";
		return fullUrl;
	}
}
