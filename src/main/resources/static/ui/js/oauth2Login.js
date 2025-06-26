	let kakaoInitKey;let kakaoRedirectUri;let kakaoClientId;
	let naverClientKey;let naverLoginCallback;let naverSetDomain
	let kakaoAccessToken;let googleClientId;
	ajaxRequest("/config.do", function(data) {
		kakaoInitKey = data.kakaoInitKey;
		kakaoRedirectUri = data.kakaoRedirectUri;
		kakaoClientId = data.kakaoClientId;
	    naverClientKey = data.naverClientKey;
	    naverLoginCallback = data.naverLoginCallback;
	    naverSetDomain = data.naverSetDomain;
	    kakaoAccessToken = data.kakaoAccessToken;
	    googleClientId = data.googleClientId;
	});	
