package br.ce.jhenck.rest.tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import br.ce.jhenck.rest.core.BaseTest;
import br.ce.jhenck.rest.utils.DataUtils;
import io.restassured.RestAssured;
import io.restassured.specification.FilterableRequestSpecification;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BarrigaRestTest extends BaseTest{
	
	private static String CONTA_NAME = "Conta " + System.nanoTime();
	private static Integer CONTA_ID;
	private static Integer MOV_ID;
	
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
	}
	
	@Test
	public void t02_deveIncluirContaComSucesso() {
		CONTA_ID = given()
			.body("{\"nome\": \"" + CONTA_NAME + "\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(201)
			.body("nome", is(CONTA_NAME))
			.extract().path("id");
	}
	
	@Test
	public void t03_deveAtualizarContaCriada() {
		CONTA_NAME = given()
			.body("{\"nome\": \"" + CONTA_NAME + " Alterada\"}")
			.pathParam("id", CONTA_ID)
		.when()
			.put("/contas/{id}")
		.then()
			.statusCode(200)
			.body("nome", is(CONTA_NAME + " Alterada"))
			.extract().path("nome");
	}
	
	@Test
	public void t04_naoDeveIncluirContaDuplicada() {
		given()
			.body("{\"nome\": \"" + CONTA_NAME + "\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(400)
			.body("error", is("Já existe uma conta com esse nome!"));
	}
	
	@Test
	public void t05_deveInserirMovimentacaoComSucesso() throws Exception {
		Movimentacao mov = getMovimentacaoValida();
		
		MOV_ID = given()
			.body(mov)
		.when()
			.post("/transacoes")
		.then()
			.statusCode(201)
			.body("conta_id", is(CONTA_ID))
			.body("descricao", is("Descrição da nova movimentação"))
			.body("envolvido", is("Envolvido na mov"))
			.body("tipo", is("REC"))
			.body("data_transacao", startsWith(DataUtils.getDataDiferencaDias(-1, "GMT")))
			.body("data_pagamento", startsWith(DataUtils.getDataDiferencaDias(2, "GMT")))
			.body("valor", is("100.00"))
			.body("status", is(true))
			.extract().path("id");
	}
	
	@Test
	public void t06_deveValidarCamposObrigatorios() {
		given()
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
	
	@Test
	public void t07_naoDeveIncluirContaMovimentacaoDataFutura() throws Exception {
		Movimentacao mov = getMovimentacaoValida();
		mov.setData_transacao(DataUtils.getDataDiferencaDias(2, "BR"));

		given()
			.body(mov)
		.when()
			.post("/transacoes")
		.then()
			.statusCode(400)
			.body("$", hasSize(1))
			.body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"));
	}
	
	@Test
	public void t08_naoDeveRemoverContaComMovimentacao() {
		given()
			.pathParam("id", CONTA_ID)
		.when()
			.delete("/contas/{id}")
		.then()
			.statusCode(500)
			.body("constraint", is("transacoes_conta_id_foreign"));
	}
	
	@Test
	public void t09_deveCalcularSaldoContas() {
		given()
		.when()
			.get("/saldo")
		.then()
			.statusCode(200)
			.body("find{it.conta_id == " + CONTA_ID + "}.saldo", is("100.00"));
	}
	
	@Test
	public void t10_deveRemoverMovimentacao() {
		given()
			.pathParam("id", MOV_ID)
		.when()
			.delete("/transacoes/{id}")
		.then()
			.statusCode(204);
	}
	
	@Test
	public void t11_naoDeveAcessarAPISemToken() {
		FilterableRequestSpecification req = (FilterableRequestSpecification) RestAssured.requestSpecification;
		req.removeHeader("Authorization");
		
		given()
		.when()
			.get("/contas")
		.then()
			.statusCode(401);
	}
	
	private Movimentacao getMovimentacaoValida() throws Exception {
		Movimentacao mov = new Movimentacao();
		mov.setConta_id(CONTA_ID);
		mov.setDescricao("Descrição da nova movimentação");
		mov.setEnvolvido("Envolvido na mov");
		mov.setTipo("REC");
		mov.setData_transacao(DataUtils.getDataDiferencaDias(-1, "BR"));
		mov.setData_pagamento(DataUtils.getDataDiferencaDias(2, "BR"));
		mov.setValor(100f);
		mov.setStatus(true);
		return mov;
	}
}
