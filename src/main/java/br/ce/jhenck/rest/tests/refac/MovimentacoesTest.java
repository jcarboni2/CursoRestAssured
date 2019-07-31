package br.ce.jhenck.rest.tests.refac;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import org.junit.Test;

import br.ce.jhenck.rest.core.BaseTest;
import br.ce.jhenck.rest.tests.Movimentacao;
import br.ce.jhenck.rest.utils.BarrigaUtils;
import br.ce.jhenck.rest.utils.DataUtils;

public class MovimentacoesTest extends BaseTest {
	
	@Test
	public void deveInserirMovimentacaoComSucesso() throws Exception {
		Movimentacao mov = getMovimentacaoValida();
		
		given()
			.body(mov)
		.when()
			.post("/transacoes")
		.then()
			.statusCode(201)
			.body("conta_id", is(BarrigaUtils.getIdContaPeloNome("Conta para movimentacoes")))
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
		Integer CONTA_ID = BarrigaUtils.getIdContaPeloNome("Conta com movimentacao");
		
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
		Integer MOV_ID = BarrigaUtils.getIdMovPelaDescricao("Movimentacao para exclusao");
		
		given()
			.pathParam("id", MOV_ID)
		.when()
			.delete("/transacoes/{id}")
		.then()
			.statusCode(204);
	}
	
	private Movimentacao getMovimentacaoValida() throws Exception {
		Movimentacao mov = new Movimentacao();
		mov.setConta_id(BarrigaUtils.getIdContaPeloNome("Conta para movimentacoes"));
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