package edu.uclm.esi.videochat.http;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import edu.uclm.esi.videochat.model.Manager;
import edu.uclm.esi.videochat.model.User;
import edu.uclm.esi.videochat.springdao.UserRepository;


@RestController
public class UsersController {
	
	@Autowired
	private UserRepository userRepo;
	
	@PostMapping(value = "/login")
	public User login(HttpServletRequest request, @RequestBody Map<String, Object> credenciales) throws Exception {
		JSONObject jso = new JSONObject(credenciales);
		String name = jso.getString("name");
		String pwd = jso.getString("pwd");
		String ip = request.getRemoteAddr();
		User user = userRepo.findByNameAndPwd(name, pwd);
		if (user==null)
			throw new Exception("Incorrect login");
		Manager.get().add(user);
		request.getSession().setAttribute("user", user);
		Manager.get().add(request.getSession());
		return user;
	}
	
	@PutMapping("/register")
	public void register(@RequestBody Map<String, Object> credenciales) throws Exception {
		JSONObject jso = new JSONObject(credenciales);
		String name = jso.getString("name");
		String email = jso.getString("email");
		String pwd1 = jso.getString("pwd1");
		String pwd2 = jso.getString("pwd2");
		if (!pwd1.equals(pwd2))
			throw new Exception("Error: las contraseñas no coinciden");
		User user = new User();
		user.setEmail(email);
		user.setName(name);
		user.setPwd(pwd1);
		String picture = jso.optString("picture");
		user.setPicture(picture);
		userRepo.save(user);
	}
	
	@PatchMapping("/cambiarPwd")
	public void cambiarPwd(@RequestBody Map<String, String> credenciales) throws Exception {
		JSONObject jso = new JSONObject(credenciales);
		String name = jso.getString("name");
		String pwd = jso.getString("pwd");
		String pwd1 = jso.getString("pwd1");
		String pwd2 = jso.getString("pwd2");
		if (userRepo.checkPassword(name, pwd) > 0) { 
			if (pwd1.equals(pwd2)) {
				User user = userRepo.findByNameAndPwd(name, pwd);
				user.setPwd(pwd2);
				userRepo.save(user);

			} else throw new Exception("Las paasswords no coinciden");
		} else 
			throw new Exception("Credenciales inválidas");
	}
	
	@GetMapping(value = "/getUsuariosConectados", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<User> getUsuariosConectados() {
		return Manager.get().getUsuariosConectados();
	}
}
