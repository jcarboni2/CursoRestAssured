package br.ce.jhenck;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.Test;

public class FileTest {
	
	@Test
	public void deveObrigarEnviarArquivo() {
		given()
			.log().all()
		.when()
			.post("https://restapi.wcaquino.me/upload")
		.then()
			.log().all()
			.statusCode(404) //Deveria ser 400
			.body("error", is("Arquivo não enviado"));
	}

	@Test
	public void deveFazerUploadArquivo() {
		given()
			.log().all()
			.multiPart("arquivo", new File("src/main/resources/users.pdf"))
		.when()
			.post("https://restapi.wcaquino.me/upload")
		.then()
			.log().all()
			.statusCode(200)
			.body("name", is("users.pdf"));
	}
	
	@Test
	public void naoDeveFazerUploadArquivoGrande() {
		given()
			.log().all()
			.multiPart("arquivo", new File("src/main/resources/ArquivoMaiorUmMega.txt"))
		.when()
			.post("https://restapi.wcaquino.me/upload")
		.then()
			.log().all()
			.time(lessThan(10000L)) //Determine o tempo ideal de resposta de sua request
			.statusCode(413);
	}
	
	@Test
	public void deveBaixarArquivo() throws IOException {
		byte[] image = given()
			.log().all()
		.when()
			.get("https://restapi.wcaquino.me/download")
		.then()
			.statusCode(200)
			.extract().asByteArray();
		File imagem = new File("src/main/resources/file.jpg");
		OutputStream out = new FileOutputStream(imagem);
		out.write(image);
		out.close();
		Assert.assertThat(imagem.length(), lessThan(100000L));
	}
}
