package exdev.com.common.controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import exdev.com.ExdevCommonAPI;
import exdev.com.common.ExdevConstants;
import exdev.com.common.dao.ExdevCommonDao;
import exdev.com.common.vo.SessionVO;
import java.util.stream.Collectors;
/**
 * @author 위성열
 */
@Controller("ExdevSessionController")
public class ExdevSessionController {
	
	@Autowired
	private ExdevCommonDao commonDao;
 
	@RequestMapping("setSession.do")
	public @ResponseBody Map setSession(@RequestBody Map map, HttpSession session) throws Exception {
		
		SessionVO sessionVO = new SessionVO();
		
		Map userInfo 			= null;
		Map userLockInfo 		= null; 
		List<Map> brandList 	= null;
		List<Map> authList 		= null;
		
		String mailType 		= (String)map.get("mailType");
		String loginType		= (String)map.get("loginType");
		
		if( mailType == null) {
			
			// 로그인 LOCK 체크
			userLockInfo = (Map)commonDao.getObject("system.getUserLoginLock"	, map);
			if( userLockInfo !=null && "Y".equals(userLockInfo.get("IS_LOCK"))){
				userLockInfo.put("state","E");
				return userLockInfo;
			}
			
			userInfo = (Map)commonDao.getObject("system.getLoginUserInfo"		, map);
			
		}else {
			
			userInfo = (Map)commonDao.getObject("system.getUserInfoByEmailLogin", map);
			
			// 메일 로그인 실패
			if( userInfo == null ) {
				map.put("state", "E");
				return map;
			}
			
			map.put("userId", userInfo.get("LOGIN_ID"));
			
			if("KAKAO".equals(mailType)) {
				//userInfo.put("KAKAO_UUID", map.get("uuid"));
				userInfo.put("KAKAO_UUID", map.get("id"));
				commonDao.update("system.updateKakaoUUID", userInfo);
			}
		}
		
		if( userInfo != null) {

			String loginId 		= (String)userInfo.get("LOGIN_ID"	);
			String loginNm 		= (String)userInfo.get("LOGIN_NM"	);
			String buyerId 		= (String)userInfo.get("BUYER_ID"	);
			String buyerNm 		= (String)userInfo.get("BUYER_NM"	);
			String brandId 		= (String)userInfo.get("BRAND_ID"	);
			String brandNm 		= (String)userInfo.get("BRAND_NM"	);
			String storeId 		= (String)userInfo.get("STORE_ID"	);
			String storeNm 		= (String)userInfo.get("STORE_NM"	);
			
			sessionVO.setUserId(loginId);
			
			// 권한 읽기
			Map authObj = (Map)commonDao.getObject("getBaseAuth", map);
			
			if(authObj == null) {
				userInfo = new HashMap();
				userInfo.put("REASON", "AUTH");
				userInfo.put("state", "E");
				if( mailType == null) { commonDao.update("system.addlockCnt", userInfo);}
				return userInfo;
			}
			
			String userType = (String)authObj.get("USER_TYPE");
			
			if( !userType.equals( loginType )){
				map.put("REASON", "LOGIN_TYPE");
				map.put("state", "E");
				if( mailType == null) { commonDao.update("system.addlockCnt", map);}
				return map;
			}
			
			userInfo.put("USER_TYPE", userType	);
			
			String buyerBrandQueryId = "";
			if(userType.equals("HEADER") || userType.equals("ADMIN")) {
				buyerBrandQueryId = "system.getBuyerBrands";
			} else if(userType.equals("STORE")) {
				buyerBrandQueryId = "system.getBuyerBrandForStore";
			}
			
			brandList = (List<Map>) commonDao.getList(buyerBrandQueryId, userInfo);
			
			authList = (List<Map>) commonDao.getList("common.getAuthListForSession", sessionVO);
			
			sessionVO.setLoginId	(loginId	);
			sessionVO.setLoginNm	(loginNm	);
			sessionVO.setBuyerId	(buyerId	);
			sessionVO.setBuyerNm	(buyerNm	);
			sessionVO.setBrandId	(brandId	);
			sessionVO.setBrandNm	(brandNm	);
			sessionVO.setStoreId	(storeId	);
			sessionVO.setStoreNm	(storeNm	);
			sessionVO.setBrandList	(brandList	);
			sessionVO.setUserType	(userType	);
			sessionVO.setAuthList	(authList	);
			sessionVO.setUserNm		(loginNm	);
			
			session.setAttribute(ExdevConstants.SESSION_ID, sessionVO);

			map.put("FAILED_ATTEMPTS",-1);
			map.put("IS_LOCK",'N');
			map.put("USER_ID", loginId);
			commonDao.update("system.releaseLoginLock", map);
			
			commonDao.update("sample.allowParallel", map);
			
			userInfo.put("state", "S");
			
		}else
		{

			if( userLockInfo == null ) {
				userLockInfo = map;
			}

			userLockInfo.put("REASON","NOT_MATCH");
			userLockInfo.put("state", "E");
			return userLockInfo;
			
		}
		
		return userInfo;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping("getSession.do")
	public @ResponseBody Map getSession(@RequestBody Map map, HttpSession session) throws Exception {
		
		SessionVO sessionVO = (SessionVO)session.getAttribute(ExdevConstants.SESSION_ID);
		
		if( sessionVO == null ) {
			return Map.of("state","E","msg","NO_SESSION");
		}
		
		HashMap userInfo = new HashMap();

		userInfo.put("LOGIN_ID"			,sessionVO.getLoginId	());
		userInfo.put("LOGIN_NM"			,sessionVO.getLoginNm	());
		userInfo.put("USER_ID"			,sessionVO.getUserId	());
		userInfo.put("USER_NM"			,sessionVO.getUserNm	());
		userInfo.put("BUYER_ID"			,sessionVO.getBuyerId	());
		userInfo.put("BUYER_NM"			,sessionVO.getBuyerNm	());
		userInfo.put("BRAND_ID"			,sessionVO.getBrandId	());
		userInfo.put("BRAND_NM"			,sessionVO.getBrandNm	());
		userInfo.put("BRAND_LIST"		,sessionVO.getBrandList	());
		userInfo.put("STORE_ID"			,sessionVO.getStoreId	());
		userInfo.put("STORE_NM"			,sessionVO.getStoreNm	());
		userInfo.put("USER_TYPE"		,sessionVO.getUserType	());
		userInfo.put("AUTH_LIST"		,sessionVO.getAuthList	());
		
		return userInfo;
	}
 
	
	
	@SuppressWarnings("rawtypes")
	@RequestMapping("logout.do")
	public @ResponseBody Map logout(HttpSession session) {

	    session.invalidate();
	    
	    Map<String, String> result = new HashMap<>();
	    result.put("status", "S");
	    return result;
	}	
}
