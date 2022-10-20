package com.example.springbatch;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.*;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.test.AssertFile;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBatchTest
@SpringBootTest
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@ContextConfiguration(classes = SpringBatchApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class})
public class BatchTest {
    private static final String TEST_INPUT = "/src/main/resources/sample-data.csv";
    private static final String TEST_OUTPUT = "spring-batch/output.txt";
    private static final String EXPECTED_OUTPUT = "spring-batch/output.txt";

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @After    public void cleanUp() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("sample-data.csv", TEST_INPUT);
        paramsBuilder.addString("output.txt", TEST_OUTPUT);
        return paramsBuilder.toJobParameters();
    }

    @Test
    public void TestJob() throws Exception {
        // given
        FileSystemResource expectedResult = new FileSystemResource(EXPECTED_OUTPUT);
        FileSystemResource actualResult = new FileSystemResource(TEST_OUTPUT);

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(defaultJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        // then
        assertThat(actualJobExitStatus.getExitCode(), is("COMPLETED"));
        assertThat(actualJobInstance.getJobName(), is("importUserJob"));
//       AssertFile.assertFileEquals(expectedResult, actualResult);

    }

    @Test
    public void TestStep1() throws Exception {
        FileSystemResource expectedResult = new FileSystemResource(EXPECTED_OUTPUT);
        FileSystemResource actualResult = new FileSystemResource(TEST_OUTPUT);



        JobExecution jobExecution = jobLauncherTestUtils.launchStep(
                "step1", defaultJobParameters());
        Collection actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        assertThat(actualStepExecutions.size(), is(1));
        assertThat(actualJobExitStatus.getExitCode(), is("COMPLETED"));
//       AssertFile.assertFileEquals(expectedResult, actualResult);
//        AssertFile.assertFileEquals(new FileSystemResource(EXPECTED_OUTPUT),
        //        new FileSystemResource(TEST_OUTPUT));
    }

    @Test
    public void TestStep2() {
        JobExecution jobExecution = jobLauncherTestUtils.launchStep(
                "step2", defaultJobParameters());
        Collection actualStepExecutions = jobExecution.getStepExecutions();
        ExitStatus actualExitStatus = jobExecution.getExitStatus();

        assertThat(actualStepExecutions.size(), is(1));
        assertThat(actualExitStatus.getExitCode(), is("COMPLETED"));
    }
    private StepExecutionListener tested = new NoWorkFoundStepExecutionListener();
    public class NoWorkFoundStepExecutionListener extends StepExecutionListenerSupport {

        public ExitStatus afterStep(StepExecution stepExecution) {
            if (stepExecution.getReadCount() == 0) {
                return ExitStatus.FAILED;
            }
            return null;
        }
    }

    @Test
    public void testAfterStep() {
        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();

        stepExecution.setExitStatus(ExitStatus.COMPLETED);
        stepExecution.setReadCount(0);

        ExitStatus exitStatus = tested.afterStep(stepExecution);
        assertEquals(ExitStatus.FAILED.getExitCode(), exitStatus.getExitCode());
    }
}

