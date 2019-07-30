package br.ce.jhenck.rest.tests.refac;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import br.ce.jhenck.rest.core.BaseTest;
import io.restassured.RestAssured;

public class ContasTest extends BaseTest {
	
	@BeforeClass
	public static void login() {
		Map<String, String> login = new HashMap<String, String>();
		//Entrar com o email e senha cadastrado no site https://srbarriga.herokuapp.com
		login.put("email", "jch@jch.com");
		login.put("senha", "1234");
		
		//login na API
		//Receber token
		String TOKEN = given()
			.body(login)
		.when()
			.post("/signin")
		.then()
			.statusCode(200)
			.extract().path("token");
		
		RestAssured.requestSpecification.header("Authorization", "JWT " + TOKEN);
		
		RestAssured.get("/reset").then().statusCode(200);
	}
	
	@Test
	public void deveIncluirContaComSucesso() {
		given()
			.body("{\"nome\": \"Nova Conta\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(201)
			.body("nome", is("Nova Conta"));
	}
	
	@Test
	public void deveAtualizarContaCriada() {
		Integer CONTA_ID = getIdContaPeloNome("Conta para alterar");
		
		given()
			.body("{\"nome\": \"Conta Alterada\"}")
			.pathParam("id", CONTA_ID)
		.when()
			.put("/contas/{id}")
		.then()
			.statusCode(200)
			.body("nome", is("Conta Alterada"));
	}
	
	@Test
	public void naoDeveIncluirContaDuplicada() {
		given()
			.body("{\"nome\": \"Conta mesmo nome\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(400)
			.body("error", is("Já existe uma conta com esse nome!"));
	}
	
	public Integer getIdContaPeloNome(String nome) {
		return RestAssured.get("/contas?nome=" + nome).then().extract().path("id[0]");
	}
}
