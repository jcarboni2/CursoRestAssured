package br.ce.jhenck.rest.suite;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import br.ce.jhenck.rest.core.BaseTest;
import br.ce.jhenck.rest.tests.refac.AuthTest;
import br.ce.jhenck.rest.tests.refac.ContasTest;
import br.ce.jhenck.rest.tests.refac.MovimentacoesTest;
import br.ce.jhenck.rest.tests.refac.SaldoTest;
import io.restassured.RestAssured;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	ContasTest.class,
	MovimentacoesTest.class,
	SaldoTest.class,
	AuthTest.class
})

public class Suite extends BaseTest{
	
	@BeforeClass
	public static void login() {
		Map<String, String> login = new HashMap<String, String>();
		//Entrar com o email e senha cadastrado no site https://barrigarest.wcaquino.me
		login.put("email", "user@user.com");
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
}