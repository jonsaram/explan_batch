/**
 *
 */
package exdev.com.common.vo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ks5011.kim
 *
 */
public class SessionVO {

	private static final long serialVersionUID = 1L;

	private String loginId;
	private String loginNm;
	private String userId; 
	private String userNm;
	private String userType;
	private String grade;
	private String email;
	private String spCstmId;
	private String loginType;
	private String systemRoleId;
	private String systemRoleNm;
	private String buyerId;
	private String buyerNm;
	private String brandId;
	private String brandNm;
	private String storeId;
	private String storeNm;
	private List<Map> brandList;
	private List<Map> authList;
	
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public List<Map> getAuthList() {
		return authList;
	}
	public void setAuthList(List<Map> authList) {
		this.authList = authList;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserNm() {
		return userNm;
	}
	public void setUserNm(String userNm) {
		this.userNm = userNm;
	}
	public String getGrade() {
		return grade;
	}
	public void setGrade(String grade) {
		this.grade = grade;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getSpCstmId() {
		return spCstmId;
	}
	public void setSpCstmId(String spCstmId) {
		this.spCstmId = spCstmId;
	}
	public String getLoginType() {
		return loginType;
	}
	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}
	public String getSystemRoleId() {
		return systemRoleId;
	}
	public void setSystemRoleId(String systemRoleId) {
		this.systemRoleId = systemRoleId;
	}
	public String getSystemRoleNm() {
		return systemRoleNm;
	}
	public void setSystemRoleNm(String systemRoleNm) {
		this.systemRoleNm = systemRoleNm;
	}
	public String getBuyerId() {
		return buyerId;
	}
	public void setBuyerId(String buyerId) {
		this.buyerId = buyerId;
	}
	public String getBuyerNm() {
		return buyerNm;
	}
	public void setBuyerNm(String buyerNm) {
		this.buyerNm = buyerNm;
	}
	public List<Map> getBrandList() {
		return brandList;
	}
	public void setBrandList(List<Map> brandList) {
		this.brandList = brandList;
	}
	public String getLoginId() {
		return loginId;
	}
	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}
	public String getLoginNm() {
		return loginNm;
	}
	public void setLoginNm(String loginNm) {
		this.loginNm = loginNm;
	}
	public String getBrandId() {
		return brandId;
	}
	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}
	public String getBrandNm() {
		return brandNm;
	}
	public void setBrandNm(String brandNm) {
		this.brandNm = brandNm;
	}
	public String getStoreId() {
		return storeId;
	}
	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}
	public String getStoreNm() {
		return storeNm;
	}
	public void setStoreNm(String storeNm) {
		this.storeNm = storeNm;
	}
	
}
