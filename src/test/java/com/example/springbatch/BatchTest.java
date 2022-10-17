package com.example.springbatch;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.*;
import org.springframework.batch.test.AssertFile;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBatchTest
@SpringBootTest
@RunWith(SpringRunner.class)
public class BatchTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;
    //@Autowired
    //private SimpleJdbcTemplate simpleJdbcTemplate;

    /*  @Autowired
      public void setDataSource(DataSource dataSource) {
          this.simpleJdbcTemplate = new SimpleJdbcTemplate(dataSource);
      }*/
    @After
    public void cleanUp() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    @Test
    public void testJob() throws Exception {
        //testing a job
        JobExecution jobExecutionJob = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, jobExecutionJob.getStatus());
        //Testing a individual step
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("step1");
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        JobExecution jobExecution2 = jobLauncherTestUtils.launchStep("step2");
        assertEquals(BatchStatus.COMPLETED, jobExecution2.getStatus());
    }
}
     /*   simpleJdbcTemplate.update("delete from CUSTOMER");
        for (int i = 1; i <= 10; i++) {
            simpleJdbcTemplate.update("insert into CUSTOMER values (?, 0, ?, 100000)",
                    i, "customer" + i);
        }

        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

    }*/


/*
@SpringBatchTest
@SpringBootTest
@RunWith(SpringRunner.class)
public class BatchTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

     @After
     public void cleanUp() {
         jobRepositoryTestUtils.removeJobExecutions();
     }

    @Test
    public void testJob() throws Exception {
        //testing a job
        JobExecution jobExecutionJob = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, jobExecutionJob.getStatus());
        //Testing a individual step
        JobExecution jobExecution = jobLauncherTestUtils.launchStep("step1");
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        JobExecution jobExecution2 = jobLauncherTestUtils.launchStep("step2");
        assertEquals(BatchStatus.COMPLETED, jobExecution2.getStatus());
    }
} */

