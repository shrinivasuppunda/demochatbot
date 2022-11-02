package com.necsws.chatbot.chatbotdemo.service;


import opennlp.tools.doccat.*;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.namefind.*;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.*;
import opennlp.tools.util.eval.FMeasure;
import opennlp.tools.util.model.ModelUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import static opennlp.tools.ml.TrainerFactory.TrainerType.EVENT_MODEL_SEQUENCE_TRAINER;
import static opennlp.tools.ml.TrainerFactory.TrainerType.SEQUENCE_TRAINER;
@Service
public class ChatBotService {

    private static Map<String, String> questionAnswer = new HashMap<>();
    private static Map<String, Object> questionAnswers = new HashMap<>();
    static {
        questionAnswers.put("greeting", "Hello, how can I help you?");
		questionAnswers.put("Hi", "Hi, how can I help you?");
		questionAnswers.put("Hello", "Hello, how can I help you?");
        questionAnswers.put("team-names", "Stratus, NynemKonto, Emerging Tech & Innovation");
//        questionAnswers.put("stratus-enquiry", "Mamatha, Srinivas, Henrik, Mads, Shilpa");
        questionAnswers.put("nynemkonto-enquiry", "Mamatha, Srinivas, Henrik, Mads, Shilpa, Shaista, Pranati, Nithya, Karthik, Shrinivasa");
        questionAnswers.put("emergingTech-enquiry", "Mamatha, Shaista, Shrusti, Shilpa, Karthik, Shrinivasa");
        questionAnswers.put("team-experience", "5");
        questionAnswers.put("conversation-continue", "What else can I help you with?");
        questionAnswers.put("conversation-complete", "Nice chatting with you. Bbye.");

    }

    static {
		questionAnswer.put("Hi", "Hi, how can I help you?");
		questionAnswer.put("Hello", "Hello, how can I help you?");
        questionAnswer.put("greeting", "Hello, how can I help you?");
        questionAnswer.put("product-inquiry",
                "Product is a Samsung mobile. It is a smart phone with latest features like touch screen, bluetooth etc.");
        questionAnswer.put("price-inquiry", "Price is $3000");
        questionAnswer.put("colours-enquiry", "Sea blue, Black, Grey, White");
        questionAnswer.put("conversation-continue", "What else can I help you with?");
        questionAnswer.put("conversation-complete", "Nice chatting with you. Bbye.");

    }
    public String getMessage(String userInput) throws IOException {
    	 DoccatModel model = trainCategorizerModel();
       String[] sentences = breakSentences(userInput);

       String answer = "";
       boolean conversationComplete = false;

       for (String sentence : sentences) {

           String[] tokens = tokenizeSentence(sentence);

           String[] posTags = detectPOSTags(tokens);

           String[] lemmas = lemmatizeTokens(tokens, posTags);

           String category = detectCategory(model, lemmas);

           answer = answer + " " + questionAnswer.get(category);

           if ("conversation-complete".equals(category)) {
               conversationComplete = true;
           }
       }
//       TokenNameFinderEvaluator evaluator = new TokenNameFinderEvaluator(new NameFinderME(model));
//       evaluator.evaluate(sampleStream);

//       FMeasure result = evaluator.getFMeasure();

//       System.out.println(result.toString());
       System.out.println("##### Chat Bot: " + answer);
		return answer;
    	
    }

    public static void main(String[] args) throws IOException {

        DoccatModel model = trainCategorizerModel();

        Scanner scanner = new Scanner(System.in);
        while (true) {

            System.out.println("##### You:");
            String userInput = scanner.nextLine();

//            String[] sentences = breakSentences(userInput);
//
//            String answer = "";
//            boolean conversationComplete = false;
//
//            for (String sentence : sentences) {
//
//                String[] tokens = tokenizeSentence(sentence);
//
//                String[] posTags = detectPOSTags(tokens);
//
//                String[] lemmas = lemmatizeTokens(tokens, posTags);
//
//                String category = detectCategory(model, lemmas);
//
//                answer = answer + " " + questionAnswer.get(category);
//
//                if ("conversation-complete".equals(category)) {
//                    conversationComplete = true;
//                }
            }
//            TokenNameFinderEvaluator evaluator = new TokenNameFinderEvaluator(new NameFinderME(model));
//            evaluator.evaluate(sampleStream);

//            FMeasure result = evaluator.getFMeasure();

//            System.out.println(result.toString());
//            System.out.println("##### Chat Bot: " + answer);
//            if (conversationComplete) {
//                break;
//            }

//        }

    }

    public static DoccatModel trainCategorizerModel() throws IOException {
        InputStreamFactory inputStreamFactory = new MarkableFileInputStreamFactory(new File("faq-categorizer.txt"));
        ObjectStream<String> lineStream = new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8);
        ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);

        DoccatFactory factory = new DoccatFactory(new FeatureGenerator[]{new BagOfWordsFeatureGenerator()});

        TrainingParameters params = ModelUtil.createDefaultTrainingParameters();
        params.put(TrainingParameters.CUTOFF_PARAM, 0);
        params.put(TrainingParameters.ITERATIONS_PARAM, 10);

        DoccatModel model = DocumentCategorizerME.train("en", sampleStream, TrainingParameters.defaultParams(), factory);
        return model;
    }

    public String detectCategory(DoccatModel model, String[] finalTokens) {

        DocumentCategorizerME myCategorizer = new DocumentCategorizerME(model);

        double[] probabilitiesOfOutcomes = myCategorizer.categorize(finalTokens,questionAnswers);
        String category = myCategorizer.getBestCategory(probabilitiesOfOutcomes);
        System.out.println("Category: " + category);

        return category;

    }

    public String[] breakSentences(String data) throws IOException {
        try (InputStream modelIn = new FileInputStream("en-sent.bin")) {

            SentenceDetectorME myCategorizer = new SentenceDetectorME(new SentenceModel(modelIn));

            String[] sentences = myCategorizer.sentDetect(data);
            System.out.println("Sentence Detection: " + Arrays.stream(sentences).collect(Collectors.joining(" | ")));

            return sentences;
        }
    }

    public String[] tokenizeSentence(String sentence) throws IOException {
        try (InputStream modelIn = new FileInputStream("en-token.bin")) {

            TokenizerME myCategorizer = new TokenizerME(new TokenizerModel(modelIn));

            String[] tokens = myCategorizer.tokenize(sentence);
            System.out.println("Tokenizer : " + Arrays.stream(tokens).collect(Collectors.joining(" | ")));

            return tokens;

        }
    }

    public String[] detectPOSTags(String[] tokens) throws IOException {
        try (InputStream modelIn = new FileInputStream("en-pos-maxent.bin")) {

            POSTaggerME myCategorizer = new POSTaggerME(new POSModel(modelIn));

            String[] posTokens = myCategorizer.tag(tokens);
            System.out.println("POS Tags : " + Arrays.stream(posTokens).collect(Collectors.joining(" | ")));

            return posTokens;

        }

    }

    public String[] lemmatizeTokens(String[] tokens, String[] posTags)
            throws IOException {
        try (InputStream modelIn = new FileInputStream("en-lemmatizer.bin")) {

            LemmatizerME myCategorizer = new LemmatizerME(new LemmatizerModel(modelIn));
            String[] lemmaTokens = myCategorizer.lemmatize(tokens, posTags);
            System.out.println("Lemmatizer : " + Arrays.stream(lemmaTokens).collect(Collectors.joining(" | ")));

            return lemmaTokens;

        }
    }

}