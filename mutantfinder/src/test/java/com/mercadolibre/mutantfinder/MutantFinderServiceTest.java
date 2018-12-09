package com.mercadolibre.mutantfinder;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.mercadolibre.mutantfinder.dao.AdnRepository;
import com.mercadolibre.mutantfinder.dao.StatsRepository;
import com.mercadolibre.mutantfinder.entity.Adn;

import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class MutantFinderServiceTest {
	
	@Autowired
	private StatsRepository repositoryStats;
	@Autowired
	private AdnRepository repositoryAdn;
	
	@Autowired
	private WebTestClient webClient;

	@After
	public void limpieza() throws Exception {
		repositoryAdn.deleteAll();
		repositoryStats.deleteAll();
	}
	
	@Test
	public void estadisticasSinNAda() {
		this.webClient.get().uri("/stats").exchange().expectBody().json("{\"ratio\":\"0\"}");
	}

	@Test
	public void sendingMutantDNA() {
		String[] cadena = { "ATGCGA", "CCGTGC", "TTATGT", "AGAAGG", "CCTCTA", "TCACTG" };
		Adn adnToSend = new Adn(cadena);

		this.webClient.post().uri("/mutant").contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaType.APPLICATION_JSON_UTF8).body(Mono.just(adnToSend), Adn.class).exchange().expectStatus()
				.isOk();
	}

	@Test
	public void sendingHumantDNA() {
		String[] cadena = { "ATGCCA", "CCGTGC", "TTATGT", "AGAAGG", "CCTCTA", "TCACTG" };
		Adn adnToSend = new Adn(cadena);

		this.webClient.post().uri("/mutant").contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaType.APPLICATION_JSON_UTF8).body(Mono.just(adnToSend), Adn.class).exchange().expectStatus()
				.isForbidden();
	}

	@Test
	public void sendingWrongDNA() {
		String[] cadena = { "ATXCCA", "CCGTGC", "TTMTGT", "AGAAGG", "CCTCTA", "TCACTG" };
		Adn adnToSend = new Adn(cadena);

		this.webClient.post().uri("/mutant").contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaType.APPLICATION_JSON_UTF8).body(Mono.just(adnToSend), Adn.class).exchange().expectStatus()
				.isForbidden();
	}

	@Test
	public void ratioCalculator() {
		// HUMANO 1
		String[] cadena = { "ATGCCA", "CCGTGC", "TTATGT", "AGAAGG", "CCTCTA", "TCACTG" };
		Adn adnToSend = new Adn(cadena);

		this.webClient.post().uri("/mutant").contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaType.APPLICATION_JSON_UTF8).body(Mono.just(adnToSend), Adn.class).exchange().expectStatus()
				.isForbidden();
		// HUMANO 2
		String[] cadena2 = { "ATGCCA", "CCGTGC", "TTATGT", "AGAAGG", "CCTCTA", "TCACTG" };
		adnToSend = new Adn(cadena2);

		this.webClient.post().uri("/mutant").contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaType.APPLICATION_JSON_UTF8).body(Mono.just(adnToSend), Adn.class).exchange().expectStatus()
				.isForbidden();
		// MUTANTE 1
		String[] cadena3 = { "ATGCGA", "CCGTGC", "TTATGT", "AGAAGG", "CCTCTA", "TCACTG" };
		adnToSend = new Adn(cadena3);

		this.webClient.post().uri("/mutant").contentType(MediaType.APPLICATION_JSON_UTF8)
				.accept(MediaType.APPLICATION_JSON_UTF8).body(Mono.just(adnToSend), Adn.class).exchange().expectStatus()
				.isOk();
		//RATIO
		this.webClient.get().uri("/stats").exchange().expectBody().json("{\"ratio\":\"0.5\"}");
	}

}
