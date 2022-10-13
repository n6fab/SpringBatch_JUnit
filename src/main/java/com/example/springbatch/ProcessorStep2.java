package com.example.springbatch;

        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.springframework.batch.item.ItemProcessor;

public class ProcessorStep2 implements ItemProcessor<Person, Person> {

    /* PersonItemProcessor implementa l'interfaccia ItemProcessor di Spring Batch
    /* Questo facilita il collegamento del codice a un job batch.

    /* In base all'interfaccia, si riceve un oggetto Person in ingresso e lo si trasforma in un Person maiuscolo.*/
    private static final Logger log = LoggerFactory.getLogger(ProcessorStep2.class); //???

    @Override
    public Person process(final Person person) throws Exception {
        final String firstName = person.getFirstName().toLowerCase();
        final String lastName = person.getLastName().toLowerCase();

        final Person transformedPerson = new Person(firstName, lastName);

        log.info("Converting (" + person + ") into (" + transformedPerson + ")");
        return transformedPerson;
    }
}
