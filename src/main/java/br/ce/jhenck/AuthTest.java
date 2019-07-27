package br.ce.jhenck;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.restassured.http.ContentType;
import io.restassured.path.xml.XmlPath;
import io.restassured.path.xml.XmlPath.CompatibilityMode;

public class AuthTest {
	
	@Test
	public void deveAcessarSWAPI() {
		given()
			.log().all()
		.when()
			.get("https://swapi.co/api/people/1")
		.then()
			.log().all()
			.statusCode(200)
			.body("name", is("Luke Skywalker"));
	}

	@Test
	public void deveObterClima() {
		given()
			.log().all()
			.queryParam("q", "Barueri,BR")
			//Alterar o valor de appid para a chave apresentada em https://home.openweathermap.org/api_keys 
			.queryParam("appid", "valor")
			.queryParam("units", "metric")
		.when()
			.get("http://api.openweathermap.org/data/2.5/weather")
		.then()
			.log().all()
			.statusCode(200)
			.body("name", is("Barueri"))
			.body("coord.lon", is(-46.88f))
			.body("main.temp", greaterThan(10f));
	}

	@Test
	public void naoDeveAcessarSemSenha() {
		given()
			.log().all()
		.when()
			.get("https://restapi.wcaquino.me/basicauth")
		.then()
			.log().all()
			.statusCode(401);
	}
	
	@Test
	public void deveFazerAutenticacaoBasica1() {
		given()
			.log().all()
		.when()
			.get("https://admin:senha@restapi.wcaquino.me/basicauth")
		.then()
			.log().all()
			.statusCode(200)
			.body("status", is("logado"));
	}
	
	@Test
	public void deveFazerAutenticacaoBasica2() {
		given()
			.log().all()
			.auth().basic("admin", "senha")
		.when()
			.get("https://restapi.wcaquino.me/basicauth")
		.then()
			.log().all()
			.statusCode(200)
			.body("status", is("logado"));
	}
	
	@Test
	public void deveFazerAutenticacaoBasicaChallenge() {
		given()
			.log().all()
			.auth().preemptive().basic("admin", "senha")
		.when()
			.get("https://restapi.wcaquino.me/basicauth2")
		.then()
			.log().all()
			.statusCode(200)
			.body("status", is("logado"));
	}
	
	@Test
	public void deveFazerAutenticacaoComTokenJWT() {
		Map<String, String> login = new HashMap<String, String>();
		//Entrar com o email e senha cadastrado no site https://srbarriga.herokuapp.com
		login.put("email", "user@user.com");
		login.put("senha", "1234");
		System.out.println(login);
		
		//login na api
		//Receber token
		String token = given()
			.log().all()
			.body(login)
			.contentType(ContentType.JSON)
		.when()
			.post("http://barrigarest.wcaquino.me/signin")
		.then()
			.log().all()
			.statusCode(200)
			.extract().path("token");
				
		//Obter as contas
		given()
			.log().all()
			.header("Authorization", "JWT " + token)
		.when()
			.get("http://barrigarest.wcaquino.me/contas")
		.then()
			.log().all()
			.statusCode(200)
			.body("nome", hasItem("Conta para saldo"));
	}
	
	@Test
	public void deveAcessarAplicacoesWeb() {
		
		//login
		String cookie = given()
			.log().all()
			.formParam("email", "user@user.com")
			.formParam("senha", "1234")
			.contentType(ContentType.URLENC.withCharset("UTF-8"))
		.when()
			.post("https://srbarriga.herokuapp.com/logar")
		.then()
			.log().all()
			.statusCode(200)
			.extract().header("set-cookie");
		
		cookie = cookie.split("=")[1].split(";")[0];
		
		//Obter conta
		
		String body = given()
			.log().all()
			.cookie("connect.sid", cookie)
		.when()
			.get("https://srbarriga.herokuapp.com/contas")
		.then()
			.log().all()
			.statusCode(200)
			.body("html.body.table.tbody.tr[3].td[0]", is("Conta com movimentacao"))
			.extract().body().asString();
		
		System.out.println("-------------------------------");
		XmlPath xmlPath = new XmlPath(CompatibilityMode.HTML, body);
		System.out.println(xmlPath.getString("html.body.table.tbody.tr[3].td[0]"));
	}
}