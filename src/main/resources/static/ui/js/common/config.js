// Local Server
var _CONTEXT_ROOT	= ""

var _DOMAIN_URL	= location.href.split("//")[0] + "//" + location.href.split("//")[1].split("/")[0];
var _WEB_ROOT_URL	= _DOMAIN_URL + _CONTEXT_ROOT;

//var _ROOT_PAGEID = "project_projectList";
//var _ROOT_PAGEID = "project_saveProject";
var _ROOT_PAGEID = "main";

// 구글달력
var _DISCOVERY_DOCS = ["https://www.googleapis.com/discovery/v1/apis/calendar/v3/rest"];
var _SCOPES      = "https://www.googleapis.com/auth/calendar.events";
var _SCOPES_READ = "https://www.googleapis.com/auth/calendar.readonly";
var _CLIENT_ID = "962547459367-qpb0heuh1dhuqh074jdntl5qdia0ht0g.apps.googleusercontent.com";
var _API_KEY = "AIzaSyAv7fvLZMg0GuRYPn4v_JLiG0o2jUROLx8";
var _GAPI_SCRIPT = "https://apis.google.com/js/api.js";
var _GIS_SCRIPT = "https://accounts.google.com/gsi/client";



// dalert, dwrite 작동 여부 설정
var debugflag = true;