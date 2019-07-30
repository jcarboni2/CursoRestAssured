package br.ce.jhenck.rest.tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import br.ce.jhenck.rest.core.BaseTest;

public class BarrigaRestTest extends BaseTest{
	
	private String TOKEN;
	
	@Before
	public void login() {
		Map<String, String> login = new HashMap<String, String>();
		//Entrar com o email e senha cadastrado no site https://srbarriga.herokuapp.com
		login.put("email", "jch@jch.com");
		login.put("senha", "1234");
		
		//login na API
		//Receber token
		TOKEN = given()
			.body(login)
		.when()
			.post("/signin")
		.then()
			.statusCode(200)
			.extract().path("token");
	}
	
	@Test
	public void naoDeveAcessarAPISemToken() {
		given()
		.when()
			.get("/contas")
		.then()
			.statusCode(401);
	}
	
	@Test
	public void deveIncluirContaComSucesso() {
		given()
			.header("Authorization", "JWT " + TOKEN)
			.body("{\"nome\": \"Nova Conta\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(201)
			.body("nome", is("Nova Conta"));
	}
	
	@Test
	public void deveAtualizarContaCriada() {
		given()
			.header("Authorization", "JWT " + TOKEN)
			.body("{\"nome\": \"Nova Conta Alterada\"}")
		.when()
			.put("/contas/26234")
		.then()
			.statusCode(200)
			.body("nome", is("Nova Conta Alterada"));
	}
	
	@Test
	public void naoDeveIncluirContaDuplicada() {
		given()
			.header("Authorization", "JWT " + TOKEN)
			.body("{\"nome\": \"Nova Conta Alterada\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(400)
			.body("error", is("Já existe uma conta com esse nome!"));
	}
	
	@Test
	public void deveInserirMovimentacoesComSucesso() {
		Movimentacoes mov = new Movimentacoes();
		mov.setConta_id(26234);
		mov.setDescricao("Descrição da nova movimentação");
		mov.setEnvolvido("Envolvido na mov");
		mov.setTipo("REC");
		mov.setData_transacao("01/02/2019");
		mov.setData_pagamento("03/02/2019");
		mov.setValor(100f);
		mov.setStatus(true);
		
		given()
			.header("Authorization", "JWT " + TOKEN)
			.body(mov)
		.when()
			.post("/transacoes")
		.then()
			.statusCode(201);
	}
	
	@Test
	public void deveValidarCamposObrigatorios() {
		given()
			.header("Authorization", "JWT " + TOKEN)
			.body("{}")
		.when()
			.post("/transacoes")
		.then()
			.statusCode(400)
			.body("$", hasSize(8))
			.body("msg", hasItems(
					"Data da Movimentação é obrigatório",
					"Data do pagamento é obrigatório",
					"Descrição é obrigatório",
					"Interessado é obrigatório",
					"Valor é obrigatório",
					"Valor deve ser um número",
					"Conta é obrigatório",
					"Situação é obrigatório"
					));
	}
	
}
