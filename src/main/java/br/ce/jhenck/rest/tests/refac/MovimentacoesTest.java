package br.ce.jhenck.rest.tests.refac;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import br.ce.jhenck.rest.core.BaseTest;
import br.ce.jhenck.rest.tests.Movimentacao;
import br.ce.jhenck.rest.utils.DataUtils;
import io.restassured.RestAssured;

public class MovimentacoesTest extends BaseTest {
	
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
	public void deveInserirMovimentacaoComSucesso() throws Exception {
		Movimentacao mov = getMovimentacaoValida();
		
		given()
			.body(mov)
		.when()
			.post("/transacoes")
		.then()
			.statusCode(201)
			.body("conta_id", is(getIdContaPeloNome("Conta para movimentacoes")))
			.body("descricao", is("Descrição da nova movimentação"))
			.body("envolvido", is("Envolvido na mov"))
			.body("tipo", is("REC"))
			.body("data_transacao", startsWith(DataUtils.getDataDiferencaDias(-1, "GMT")))
			.body("data_pagamento", startsWith(DataUtils.getDataDiferencaDias(2, "GMT")))
			.body("valor", is("100.00"))
			.body("status", is(true));
	}
	
	@Test
	public void deveValidarCamposObrigatorios() {
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
	public void naoDeveIncluirContaMovimentacaoDataFutura() throws Exception {
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
	public void naoDeveRemoverContaComMovimentacao() {
		Integer CONTA_ID = getIdContaPeloNome("Conta com movimentacao");
		
		given()
			.pathParam("id", CONTA_ID)
		.when()
			.delete("/contas/{id}")
		.then()
			.statusCode(500)
			.body("constraint", is("transacoes_conta_id_foreign"));
	}
	
	@Test
	public void deveRemoverMovimentacao() {
		Integer MOV_ID = getIdMovPelaDescricao("Movimentacao para exclusao");
		
		given()
			.pathParam("id", MOV_ID)
		.when()
			.delete("/transacoes/{id}")
		.then()
			.statusCode(204);
	}
	
	public Integer getIdContaPeloNome(String nome) {
		return RestAssured.get("/contas?nome=" + nome).then().extract().path("id[0]");
	}
	
	public Integer getIdMovPelaDescricao(String desc) {
		return RestAssured.get("/transacoes?descricao=" + desc).then().extract().path("id[0]");
	}
	
	private Movimentacao getMovimentacaoValida() throws Exception {
		Movimentacao mov = new Movimentacao();
		mov.setConta_id(getIdContaPeloNome("Conta para movimentacoes"));
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
