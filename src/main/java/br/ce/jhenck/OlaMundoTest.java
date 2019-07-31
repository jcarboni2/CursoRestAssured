package br.ce.jhenck;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.request;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

public class OlaMundoTest {

	@Test
	public void testOlaMundo() {
		Response response = request(Method.GET, "http://restapi.wcaquino.me/ola");
		Assert.assertTrue(response.getBody().asString().equals("Ola Mundo!"));
		Assert.assertTrue(response.statusCode() == 200);
		Assert.assertTrue("O status code deveria ser 200", response.statusCode() == 200);
		Assert.assertEquals(200, response.statusCode());

		ValidatableResponse validacao = response.then();
		validacao.statusCode(200);
	}

	@Test
	public void devoConhecerOutrasFormasRestAssured() {
		Response response = request(Method.GET, "http://restapi.wcaquino.me/ola");
		ValidatableResponse validacao = response.then();
		validacao.statusCode(200);

		get("http://restapi.wcaquino.me/ola").then().statusCode(200);

		// Modo Fluente
		given().when().get("http://restapi.wcaquino.me/ola").then().statusCode(200);
	}

	@Test
	public void devoConhecerMatchersHumcrest() {
		Assert.assertThat("Maria", Matchers.is("Maria"));
		Assert.assertThat(128d, Matchers.is(128d));
		Assert.assertThat(128, Matchers.isA(Integer.class));
		Assert.assertThat(128d, Matchers.isA(Double.class));
		Assert.assertThat(128d, Matchers.greaterThan(120d));
		Assert.assertThat(128d, Matchers.lessThan(130d));

		List<Integer> impares = Arrays.asList(1, 3, 5, 7, 9);
		assertThat(impares, hasSize(5));
		assertThat(impares, contains(1, 3, 5, 7, 9));
		assertThat(impares, containsInAnyOrder(1, 9, 7, 3, 5));
		assertThat(impares, hasItem(1));
		assertThat(impares, hasItems(1, 7));

		Assert.assertThat("Maria", Matchers.is(not("João")));
		Assert.assertThat("Maria", not("João"));
		Assert.assertThat("Maria", anyOf(is("Maria"), is("Joaquina")));
		Assert.assertThat("Joaquina", allOf(startsWith("Joa"), endsWith("ina"), containsString("qui")));
	}

	@Test
	public void devoValidarBody() {
		// Modo Fluente
		given().when().get("http://restapi.wcaquino.me/ola").then().statusCode(200).body(is("Ola Mundo!"))
				.body(containsString("Mundo")).body(is(not(nullValue())));
	}

}
