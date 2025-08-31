package br.com.paranabanco.dataprev.infra.config;

import br.com.paranabanco.dataprev.cnab.AppCnabProperties;
import br.com.paranabanco.dataprev.cnab.CnabLineMapper;
import br.com.paranabanco.dataprev.cnab.CnabRecord;
import br.com.paranabanco.dataprev.cnab.PositionalFormatter;
import br.com.paranabanco.dataprev.infra.config.io.OutputPathResolver;
import br.com.paranabanco.dataprev.utils.CnabItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;


@Configuration
public class BatchConfig  {

    private final AppCnabProperties props;


    public BatchConfig(AppCnabProperties props) {
        this.props = props;
    }


    @Bean
    public Job cnabJob(JobRepository jobRepository, Step cnabStep) {
        return new JobBuilder("cnabJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(cnabStep)
                .build();
    }

    @Bean
    public Step cnabStep(JobRepository jobRepository,
                         @Qualifier("cnabReader") FlatFileItemReader<CnabRecord> reader,
                         @Qualifier("cnabProcessor") ItemProcessor<CnabRecord, String> processor,
                         @Qualifier("cnabWriter") CnabItemWriter writer) {
        return new StepBuilder("cnabStep", jobRepository)
                .<CnabRecord, String>chunk(500, new ResourcelessTransactionManager())
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    @Qualifier("cnabLineMapper")
    public CnabLineMapper cnabLineMapper() {
        return new CnabLineMapper();
    }

    @Bean(name = "cnabReader")
    public FlatFileItemReader<CnabRecord> cnabReader(@Qualifier("cnabLineMapper") CnabLineMapper cnabLineMapper) {
        FlatFileItemReader<CnabRecord> r = new FlatFileItemReader<>();
        var res = props.getInputFile();
        System.out.println(">> app.cnab.file = " + (res != null ? res.toString() : "null"));
        r.setResource(res);
        r.setEncoding(charsetOf(props.getCharset()).name());
        r.setLinesToSkip(0);
        r.setStrict(true);                // mantém estrito
        r.setLineMapper(cnabLineMapper);  // auto 240/480
        return r;
    }

    @Bean
    @Qualifier("positionalFormatter")
    public PositionalFormatter positionalFormatter(@Qualifier("cnabLineMapper") CnabLineMapper cnabLineMapper) {
        return new PositionalFormatter(cnabLineMapper, charsetOf(props.getCharset()));
    }

    @Bean
    @Qualifier("cnabProcessor")
    public ItemProcessor<CnabRecord, String> processor(@Qualifier("positionalFormatter") PositionalFormatter formatter) {
        return formatter::format;
    }

    @Bean
    @Qualifier("cnabWriter")
    public CnabItemWriter writer(@Qualifier("positionalFormatter") PositionalFormatter formatter,
                                 OutputPathResolver resolver) {
        Path outDir = (props.getOutputDir() == null || props.getOutputDir().toString().isBlank())
                ? null
                : Paths.get(props.getOutputDir().toString());

        String outName = (props.getOutputName() == null || props.getOutputName().isBlank())
                ? null
                : props.getOutputName();

        return new CnabItemWriter(outDir, outName, formatter, resolver);
    }

    private static Charset charsetOf(Charset cs) {
        return (cs != null) ? cs : java.nio.charset.StandardCharsets.US_ASCII;
    }

}