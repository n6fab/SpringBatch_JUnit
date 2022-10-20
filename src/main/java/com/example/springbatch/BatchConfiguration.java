package com.example.springbatch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

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
    @Autowired
    private DataSource dataSource;

//bean classe BatchConfiguration per reader, processor e writer:

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
    public ItemReader<Person> itemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Person>()
                .name("cursorItemReader")
                .dataSource(dataSource)
                .sql("SELECT first_name, last_name FROM people")
                .rowMapper(new BeanPropertyRowMapper<>(Person.class))
                .build();
    }
    @Bean
    public FlatFileItemReader<Person> reader2() { // reader() crea un ItemReader e
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource("spring-batch/output.txt")) // cerca un file chiamato sample-data.csv, analizza ogni riga con informazioni sufficienti per
                .delimited()
                .names(new String[]{"firstName", "lastName"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{ // trasformarla in un Person.
                    setTargetType(Person.class);
                }})
                .build();
    }
    @Bean
    public ProcessorStep1 processor1() { //processor() crea un'istanza del PersonItemProcessor definito in precedenza, che ha lo scopo di convertire i dati in maiuscolo
        return new ProcessorStep1();
    }
    @Bean
    public ProcessorStep2 processor2() {
        return new ProcessorStep2();
    }

    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource) { //  writer(DataSource) crea un ItemWriter,
        return new JdbcBatchItemWriterBuilder<Person>() //questo è indirizzato a una destinazione JDBC e ottiene automaticamente una copia della sorgente dati creata da @EnableBatchProcessing.
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
                .dataSource(dataSource)
                .build();
    }
    @Bean
    public FlatFileItemWriter itemWriter2() {
        return  new FlatFileItemWriterBuilder<Person>()
                .name("itemWriter")
                .resource(new FileSystemResource("spring-batch/output.txt"))
                .lineAggregator(new PassThroughLineAggregator<>())
                .build();
    }
    @Bean
    public FlatFileItemWriter itemWriter3() {
        return  new FlatFileItemWriterBuilder<Person>()
                .name("itemWriter")
                .resource(new FileSystemResource("outputStep3.txt"))
                .lineAggregator(new PassThroughLineAggregator<>())
                .build();
    }
    //configurazione effettiva del job
    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1, Step step2, Step step3) { //metodo che definisce il job
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer()) //un incrementatore, perché i jobs utilizzano un database per mantenere lo stato di esecuzione.
                .listener(listener)
                .flow(step1) //Si elenca ogni passo
                .next(step2)
                .next(step3)
                .end()
                .build();//il lavoro termina
    }

    // I job sono costruiti da steps, dove ogni steps può coinvolgere un reader, un processor e un writer


    /* Nella definizione dei passi, si definisce la quantità di dati da scrivere alla volta.
    In questo caso, vengono scritti fino a dieci record alla volta.
    Successivamente, si configurano il lettore, il processore e lo scrittore, utilizzando i bean iniettati in precedenza. */

    /* Legge da file e scrive in db */
    @Bean
    public Step step1(JdbcBatchItemWriter<Person> writer) { //definisce un singolo step.
        return stepBuilderFactory.get("step1")
                .<Person, Person> chunk(10)
                .reader(reader())
                .processor(processor1())
                .writer(writer)
                .build();
    }/* chunk() ha il prefisso <Person,Person> perché è un metodo generico.
    Rappresenta i tipi di ingresso e di uscita di ogni "pezzo" di elaborazione e si allinea con ItemReader<Person> e ItemWriter<Person>.*/

    /* Legge da db e scrive in file */
    @Bean
    public Step step2(FlatFileItemWriter itemWriter2) {
        //public Step step1(JdbcBatchItemWriter<Person> writer) { //definisce un singolo step
        return stepBuilderFactory.get("step2")
                .<Person, Person> chunk(10)
                .reader(itemReader(dataSource))//reader
                .processor(processor2())
                .writer(itemWriter2())//writer
                .build();
    }
    @Bean
    public Step step3(FlatFileItemWriter itemWriter3) {
        //public Step step1(JdbcBatchItemWriter<Person> writer) { //definisce un singolo step
        return stepBuilderFactory.get("step1")
                .<Person, Person> chunk(10)
                .reader(reader())
                .processor(processor1())
                .writer(itemWriter3)//writer
                .build();
    }
}