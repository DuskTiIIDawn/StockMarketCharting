package com.example.StockMarketCharting.jwt;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.StockMarketCharting.entities.UserEntity;
import com.example.StockMarketCharting.services.UserEntityService;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
public class UserEntityController {

	@Autowired
	private PasswordEncoder bcryptEncoder;

	@Autowired
	UserEntityService service;
	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@Autowired
	@Qualifier("javasampleapproachMailSender")
	public MailSender mailSender;

	@RequestMapping(value = "/authenticate", method = RequestMethod.POST, headers = "Accept=application/json")
	public Map<String, String> createAuthenticationToken(@RequestBody JsonNode request) throws Exception {
		Map<String, String> res = new HashMap<>();
		if (request.get("userName") == null || request.get("password") == null) {
			res.put("ERROR", "BAD DATA!");
		}
		String userName = request.get("userName").asText();
		String rawPassword = request.get("password").asText();
		UserEntity userEntity = service.findByUserName(userName);

		if (userEntity == null || bcryptEncoder.matches(rawPassword, userEntity.getPassword()) == false) {
			res.put("ERROR", "Username Password Does Not Match ");
			return res;
		}
		if (userEntity.isConfirmed() == false) {
			res.put("ERROR", "Please verify your email first!");
			return res;
		}
		final UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
		final String token = jwtTokenUtil.generateToken(userDetails);
		UserEntity usr = service.findByUserName(userName);
		if (usr.isAdmin()) {
			res.put("IS_ADMIN", "YES");
		}
		res.put("TOKEN", token);
		return res;
	}

	@RequestMapping(value = "/setuserapi1", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, String> userapi(@RequestBody UserEntity user, BindingResult result) {

		Map<String, String> res = new HashMap<>();

		if (result.hasErrors()) {
			res.put("ERROR", "Bad Data Provided");
			return res;
		}
		if (service.existsByUserName(user.getUserName())) {
			res.put("ERROR", "User name already Exist");
			return res;
		}
		if (service.existsByEmail(user.getEmail())) {
			res.put("ERROR", "Email already Exist");
			return res;
		}

		if (user.getMobileNumber().length() != 10) {
			res.put("ERROR", "Mobile No Must Be Of length 10");
			return res;
		}

		if (service.existsByMobileNo(user.getMobileNumber())) {
			res.put("ERROR", "Mobile No already Exist");
			return res;
		}

		UserEntity usr = new UserEntity(user.getUserName(), bcryptEncoder.encode(user.getPassword()), user.isAdmin(),
				user.getEmail(), user.getMobileNumber(), user.isConfirmed());

		service.saveUser(usr);
		sendEmail(usr.getId(), usr.getUserName(), usr.getEmail());
		res.put("OK", "User Created Succesfully ,Check your Mail To verify and Login!");
		return res;

	}

	@RequestMapping(value = "/changePasswordapi1", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, String> changepasswordrapi(@RequestBody JsonNode request) {

		Map<String, String> res = new HashMap<>();
		if (request.get("userName") == null) {
			res.put("ERROR", "User Name must not be Null ");
			return res;
		}
		UserEntity userRepo = service.findByUserName(request.get("userName").asText());
		if (request.get("oldPassword") == null) {
			res.put("ERROR", "Please Provide Old Password");
			return res;
		}
		if (userRepo == null
				|| bcryptEncoder.matches(request.get("oldPassword").asText(), userRepo.getPassword()) == false) {
			res.put("ERROR", "Incorrect password");
			return res;
		}
		if (request.get("newPassword") == null) {
			res.put("ERROR", "Please Provide New Password");
			return res;
		}
		userRepo.setPassword(bcryptEncoder.encode(request.get("newPassword").asText()));
		service.saveUser(userRepo);
		res.put("OK", "Password Chaged Successfully!");
		return res;

	}

	@RequestMapping(value = "/findByUserNameapi1", method = RequestMethod.POST)
	@ResponseBody
	public UserEntity findbyusernameapi(@RequestBody JsonNode request) {
		if (request.get("userName") == null) {
			return null;
		}
		return service.findByUserName(request.get("userName").asText());
	}

	public void sendEmail(Long uid, String uName, String email) {
		String subject = "Account Confirmation Mail from Stock Chart Marketing";
		String message = "Click The link to confirm --> https://glacial-ridge-65812.herokuapp.com/confirmuser/" + uid
				+ "?token=" + bcryptEncoder.encode(uName + System.getenv().get("CONFIRMATION_SECRET_CODE"));
		mailSender.sendMail("classesfuturetrack@gmail.com", email, subject, message);
	}

	@RequestMapping(value = "/confirmuser/{uid}", method = { RequestMethod.GET, RequestMethod.POST })
	public String welcomepage(@PathVariable Long uid, @RequestParam String token) {
		UserEntity usr = service.findByUserId(uid);
		if (usr == null)
			return "Unknown User";
		if (bcryptEncoder.matches(usr.getUserName() + System.getenv().get("CONFIRMATION_SECRET_CODE"),
				token) == false) {
			return "User Does Not Exist";
		}
		if (usr.isConfirmed() == true) {
			return "User Already Confirmed Go back and Enjoy The app-->" + usr.getUserName();
		}
		usr.setConfirmed(true);
		service.saveUser(usr);
		return "User confirmed ! Thanks You can Login Now--->" + usr.getUserName();
	}

}
