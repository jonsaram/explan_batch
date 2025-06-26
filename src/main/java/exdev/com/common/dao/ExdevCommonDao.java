package exdev.com.common.dao;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

import exdev.com.ExdevCommonAPI;
import exdev.com.common.ExdevConstants;

@Repository("ExdevCommonDao")
public class ExdevCommonDao {
	
	@Autowired(required=false)
	@Qualifier("sqlSession")
	private SqlSessionTemplate sqlMainSession;
	
    @Autowired
    public Environment env;
    
    public String makeTableName(String tableName) {
		// table Name을 Parameter로 받았을 때 접두어 처리 
		List<String> commonTableList 	= ExdevConstants.commonTableList;
		List<String> posDataTableList 	= ExdevConstants.posDataTableList;

		String dbHdr 		= (String)env.getProperty("dbHdr");
		String posHdr		= (String)env.getProperty("posHdr");
		String comnHdr 		= (String)env.getProperty("comnHdr");
		if(dbHdr 	== null ) 	dbHdr 	= "";
		if(posHdr 	== null ) 	posHdr 	= "";
		if(comnHdr	== null ) 	comnHdr	= "";

		String newTableName = tableName;
		if 		(commonTableList .contains(tableName)) newTableName = dbHdr + comnHdr + tableName;
		else if	(posDataTableList.contains(tableName)) newTableName = dbHdr + posHdr  + tableName;
		
		return newTableName;
    }
    public String getOwner() {
    	return (String)env.getProperty("spring.datasource.username");
    }
    public Object setDbUserHeader(Object obj) {
    	if (obj instanceof Map) {
    		Map map 	= (Map)obj;
    		
    		String dbHdr 		= (String)env.getProperty("dbHdr");
    		String posHdr		= (String)env.getProperty("posHdr");
    		String comnHdr 		= (String)env.getProperty("comnHdr");
    		if(dbHdr 	== null ) 	dbHdr 	= "";
    		if(posHdr 	== null ) 	posHdr 	= "";
    		if(comnHdr	== null ) 	comnHdr	= "";
    		map.put("dbHdr"		, dbHdr		);
    		map.put("posHdr"	, posHdr	);
    		map.put("comnHdr"	, comnHdr 	);
	
	    	Map parm	= (Map)map.get("parm");
			if(parm != null) {
	    		String tableName 	= (String)parm.get("tableName");
	    		String newTableName	= makeTableName(tableName);
	    		parm.put("tableName", newTableName);
	    		map.put("parm", parm);
	    	}
			
	    	String tableName 	= (String)map.get("tableName");
	    	if(ExdevCommonAPI.isValid(tableName)) {
	    		String newTableName	= makeTableName(tableName);
	    		map.put("tableName", newTableName);
	    	}
			return map;
    	} else {
    		return obj;
    	}
    }
	
	@SuppressWarnings("rawtypes")
	public List<Map> getList(String queryId, Object obj) throws Exception {
		System.out.println("=================================================");
		System.out.println("Request Query Id: " + queryId);
		System.out.println("=================================================");

		// DB관련 접두어 Setting 
		obj = setDbUserHeader(obj);
		return sqlMainSession.selectList(queryId, obj);
	}
	public Object getObject(String queryId, Object obj) throws Exception {
		System.out.println("=================================================");
		System.out.println("Request Query Id: " + queryId);
		System.out.println("=================================================");

		// DB관련 접두어 Setting 
		obj = setDbUserHeader(obj);
		return sqlMainSession.selectOne(queryId, obj);
	}
	public int update(String queryId, Object obj) throws Exception {
		System.out.println("=================================================");
		System.out.println("Request Query Id : " + queryId);
		System.out.println("=================================================");

		// DB관련 접두어 Setting 
		obj = setDbUserHeader(obj);
		return sqlMainSession.update(queryId, obj);
	}
	public int insert(String queryId, Object obj) throws Exception {
		System.out.println("=================================================");
		System.out.println("Request Query Id : " + queryId);
		System.out.println("=================================================");

		// DB관련 접두어 Setting 
		obj = setDbUserHeader(obj);
		return sqlMainSession.insert(queryId, obj);
	}
    public int delete(String queryId, Object obj) throws Exception {
		System.out.println("=================================================");
		System.out.println("Request Query Id : " + queryId);
		System.out.println("=================================================");

		// DB관련 접두어 Setting 
		obj = setDbUserHeader(obj);
        return sqlMainSession.delete(queryId, obj);
    }
}