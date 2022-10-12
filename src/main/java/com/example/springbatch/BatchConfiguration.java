package com.example.springbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

// batch job

@Configuration
@EnableBatchProcessing //aggiunge molti elementi critici che sono di supporto ai jobs. Si risparmia lavoro.
//Questo esempio utilizza un database basato sulla memoria (fornito da @EnableBatchProcessing), una volta terminato, i dati sono spariti.

public class BatchConfiguration {
  //Inoltre, autocorregge un paio di fattori necessari più avanti.
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
    @Autowired
    public StepBuilderFactory stepBuilderFactory;

//Aggiungiamo i bean alla classe BatchConfiguration per definire un reader, un processor e un writer:

    //definisce l'ingresso, il processore e l'uscita.
    @Bean
    public FlatFileItemReader<Person> reader() { // reader() crea un ItemReader e
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource("sample-data.csv")) // cerca un file chiamato sample-data.csv, analizza ogni riga con informazioni sufficienti per
                .delimited()
                .names(new String[]{"firstName", "lastName"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{ // trasformarla in un Person.
                    setTargetType(Person.class);
                }})
                .build();
    }

    @Bean
    public PersonItemProcessor processor() { //processor() crea un'istanza del PersonItemProcessor definito in precedenza, che ha lo scopo di convertire i dati in maiuscolo
        return new PersonItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource) { //  writer(DataSource) crea un ItemWriter,
        return new JdbcBatchItemWriterBuilder<Person>() //questo è indirizzato a una destinazione JDBC e ottiene automaticamente una copia della sorgente dati creata da @EnableBatchProcessing.
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)") //istruzione SQL necessaria per inserire una singola Persona, guidata dalle proprietà del bean Java.
                .dataSource(dataSource)
                .build();
    }
    //configurazione effettiva del job
    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1) { //metodo che definisce il job
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer()) //un incrementatore, perché i jobs utilizzano un database per mantenere lo stato di esecuzione.
                .listener(listener)
                .flow(step1) //Si elenca ogni passo (anche se questo lavoro ha un solo passo).
                .end()
                .build();//Il lavoro termina e l'API Java produce un lavoro perfettamente configurato.
    }

    // I job sono costruiti da steps, dove ogni steps può coinvolgere un reader, un processor e un writer


    /* Nella definizione dei passi, si definisce la quantità di dati da scrivere alla volta.
    In questo caso, vengono scritti fino a dieci record alla volta.
    Successivamente, si configurano il lettore, il processore e lo scrittore, utilizzando i bean iniettati in precedenza. */
    @Bean
    public Step step1(JdbcBatchItemWriter<Person> writer) { //definisce un singolo step
        return stepBuilderFactory.get("step1")
                .<Person, Person> chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .build();
    }/* chunk() ha il prefisso <Person,Person> perché è un metodo generico.
    Rappresenta i tipi di ingresso e di uscita di ogni "pezzo" di elaborazione e si allinea con ItemReader<Person> e ItemWriter<Person>.*/

}
