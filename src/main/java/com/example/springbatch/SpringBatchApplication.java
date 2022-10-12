package com.example.springbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication

public class SpringBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchApplication.class, args);
	}
	// Il metodo main() utilizza il metodo SpringApplication.run() di Spring Boot per lanciare un'applicazione.



}

/*	@SpringBootApplication è un'annotazione di comodo che aggiunge quanto segue:

/* @Configuration: etichetta la classe come fonte di definizioni di bean per il contesto dell'applicazione.
/* @EnableAutoConfiguration: Indica a Spring Boot d'iniziare ad aggiungere i bean in base alle impostazioni del classpath,
ad altri bean e a varie impostazioni di proprietà. Per esempio, se spring-webmvc è nel classpath,
questa annotazione segnala l'applicazione come applicazione web e attiva comportamenti chiave, come l'impostazione di un DispatcherServlet.
/* @ComponentScan: Indica a Spring di cercare altri componenti, configurazioni e servizi nel pacchetto com/example, consentendogli di trovare i controllori.

Non c'è XML o file web.xml, è al 100% puramente Java e non è stato necessario configurare alcun impianto o infrastruttura.
 */
