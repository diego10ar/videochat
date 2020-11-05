package edu.uclm.esi.videochat.http;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import edu.uclm.esi.videochat.classicdao.UsersDAO;
import edu.uclm.esi.videochat.model.Manager;
import edu.uclm.esi.videochat.model.User;

@RestController
public class UsersController {
	
	@PostMapping(value = "/login")
	public String login(HttpServletRequest request, @RequestBody Map<String, Object> credenciales) throws Exception {
		JSONObject jso = new JSONObject(credenciales);
		String name = jso.getString("name");
		String pwd = jso.getString("pwd");
		String ip = request.getRemoteAddr();
		/*User user = UsersDAO.findByUserNameAndPassword(name, pwd, ip);
		if (user==null)
			throw new Exception("Incorrect login");*/
		User user = new User();
		user.setId(UUID.randomUUID().toString());
		user.setName(name);
		user.setPwd(pwd);
		Manager.get().add(user);
		request.getSession().setAttribute("user", user);
		Manager.get().add(request.getSession());
		return user.getId();
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
		UsersDAO.insert(user);
	}
	
	@PatchMapping("/cambiarPwd")
	public void cambiarPwd(@RequestBody Map<String, String> credenciales) throws Exception {
		JSONObject jso = new JSONObject(credenciales);
		String name = jso.getString("name");
		String pwd = jso.getString("pwd");
		String pwd1 = jso.getString("pwd1");
		String pwd2 = jso.getString("pwd2");
		
		if (UsersDAO.checkPassword(name, pwd)) {
			if (pwd1.equals(pwd2)) {
				UsersDAO.updatePassword(name, pwd1);
			} else throw new Exception("Las paasswords no coinciden");
		} else 
			throw new Exception("Credenciales inválidas");
	}
	
	@GetMapping("/getUsuariosConectados")
	public List<String> getUsuariosConectados() {
		return Manager.get().getUserNames();
	}
}
