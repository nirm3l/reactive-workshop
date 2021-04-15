package com.example.demo.configuration;

import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class LanguageDetectorConfiguration {

    @Value("classpath:langdetect-183.bin")
    private Resource langDetectModel;

    @Bean
    public LanguageDetector languageDetector() throws IOException {
        LanguageDetectorModel model = new LanguageDetectorModel(langDetectModel.getInputStream());

        return new LanguageDetectorME(model);
    }
}
