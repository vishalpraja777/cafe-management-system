package com.inn.cafe.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtUtil;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constants.CafeConstants;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.service.UserService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.utils.EmailUtils;
import com.inn.cafe.wrapper.UserWrapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserDao userDao;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	CustomerUserDetailsService customerUserDetailsService;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	JwtUtil jwtUtil;

	@Autowired
	JwtFilter jwtFilter;

	@Autowired
	EmailUtils emailUtils;

	@Override
	public ResponseEntity<String> signUp(Map<String, String> requestMap) {
		log.info("Inside signup {}", requestMap);
		try {
			if (validateSignUpMap(requestMap)) {
				User user = userDao.findByEmailId(requestMap.get("email"));
				if (Objects.isNull(user)) {
					userDao.save(getUserFromMap(requestMap));
					return CafeUtils.getResponseEntity("Successfully Registered", HttpStatus.OK);
				} else {
					return CafeUtils.getResponseEntity("Email already exists", HttpStatus.BAD_REQUEST);
				}
			} else {
				return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private boolean validateSignUpMap(Map<String, String> requestMap) {
		if (requestMap.containsKey("name") && requestMap.containsKey("contactNumber")
				&& requestMap.containsKey("email") && requestMap.containsKey("password")) {
			return true;
		}
		return false;
	}

	private User getUserFromMap(Map<String, String> requestMap) {
		User user = new User();
		user.setName(requestMap.get("name"));
		user.setEmail(requestMap.get("email"));
		user.setContactNumber(requestMap.get("contactNumber"));
		user.setPassword(passwordEncoder.encode(requestMap.get("password")));
		// user.setStatus(requestMap.get("status"));
		// user.setRole(requestMap.get("role"));
		user.setStatus("false");
		user.setRole("user");
		return user;

	}

	@Override
	public ResponseEntity<String> login(Map<String, String> requestMap) {
		log.info("Inside Login");
		try{
			UserDetails userDetails = customerUserDetailsService.loadUserByUsername(requestMap.get("email"));
			// System.out.println("userDetails");
			// System.out.println(userDetails);

			// UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, requestMap.get("password"),userDetails.getAuthorities());
			// authenticationManager.authenticate(token);
			// System.out.println(requestMap);
			Authentication auth = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password"))
			);
			// boolean result = token.isAuthenticated();
			if(auth.isAuthenticated()){
				if(customerUserDetailsService.getUserDetails().getStatus().equalsIgnoreCase("true")){
					return new ResponseEntity<String>("{\"token\":\"" 
					+ jwtUtil.generateToken(customerUserDetailsService.getUserDetails().getEmail(), 
					customerUserDetailsService.getUserDetails().getRole()) + "\"}", 
					HttpStatus.OK);
				}
				else{
					return new ResponseEntity<String>("{\"message\":\"" + "Wait for admin approval." + "\"}", 
					HttpStatus.BAD_REQUEST);
				}
			}
		} catch(Exception e){
			System.out.println("Error:");
			System.out.println(e);
			log.error("{}", e);
		}
		return new ResponseEntity<String>("{\"message\":\"" + "Bad Credentials." + "\"}", 
					HttpStatus.BAD_REQUEST);
	}

	@Override
	public ResponseEntity<List<UserWrapper>> getAllUser() {
		try{
			if(jwtUtil.isAdmin()){
				return new ResponseEntity<List<UserWrapper>>(userDao.getAllUser(), HttpStatus.OK);
			} else{
				return new ResponseEntity<List<UserWrapper>>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
			}
		} catch(Exception ex){
			ex.printStackTrace();
		}
		return new ResponseEntity<List<UserWrapper>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> update(Map<String, String> requestMap) {
		try{
			if(jwtUtil.isAdmin()){
				Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));
				if(!optional.isEmpty()){
					userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
					sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(), userDao.getAllAdmin());
					return CafeUtils.getResponseEntity( "User Status Updated Successfully", HttpStatus.OK);
				} else{
					return CafeUtils.getResponseEntity( "User ID does not exist", HttpStatus.OK);
				}
			} else{
		return CafeUtils.getResponseEntity( CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
			}
		} catch(Exception ex){
			ex.printStackTrace();
		}
		return CafeUtils.getResponseEntity( CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
		allAdmin.remove(jwtFilter.getCurrentUser());

		if(status != null && status.equalsIgnoreCase("true")){
			emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Approved", "User:- " + user + "\nis approved by \nADMIN:- " + jwtFilter.getCurrentUser(), allAdmin);
		} else {
			emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(), "Account Disabled", "User:- " + user + "\nis disabled by \nADMIN:- " + jwtFilter.getCurrentUser(), allAdmin);
		}
	}

	@Override
	public ResponseEntity<String> checkToken() {
		return CafeUtils.getResponseEntity( "true", HttpStatus.OK);

	}

	@Override
	public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
		try{
			User userObj = userDao.findByEmail(jwtFilter.getCurrentUser());
			if(!userObj.equals(null)){
				// if(userObj.getPassword().equals(requestMap.get("oldPassword"))){
				if(passwordEncoder.matches(requestMap.get("oldPassword"), userObj.getPassword())){
					userObj.setPassword(passwordEncoder.encode(requestMap.get("newPassword")));
					userDao.save(userObj);
					return CafeUtils.getResponseEntity( "Password Updated Successfully", HttpStatus.OK);

				}
				return CafeUtils.getResponseEntity( "Incorrect old Password", HttpStatus.BAD_REQUEST);
			}
			return CafeUtils.getResponseEntity( CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch(Exception ex){
			ex.printStackTrace();
		}
		return CafeUtils.getResponseEntity( CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
		try{
			User user = userDao.findByEmail(requestMap.get("email"));
			if(!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail())){
				emailUtils.forgotMail(user.getEmail(), "Credentials by Cafe Management System", user.getPassword());
			}
				return CafeUtils.getResponseEntity( "Check your mail for credentials", HttpStatus.OK);
		} catch(Exception ex){
			ex.printStackTrace();
		}
		return CafeUtils.getResponseEntity( CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
