package org.sample.aws.chatbot.starwars.lex;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.sample.aws.chatbot.starwars.common.StarWarsIntent;
import org.sample.aws.chatbot.starwars.common.StarWarsResponse;
import org.sample.aws.lex.request.LexRequest;
import org.sample.aws.lex.response.DialogAction;
import org.sample.aws.lex.response.LexResponse;
import org.sample.aws.lex.response.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class StarWarsLexBot implements RequestHandler<LexRequest, LexResponse> {

    private static final Logger log = LoggerFactory.getLogger(StarWarsLexBot.class);

    @Override
    public LexResponse handleRequest(LexRequest request, Context context) {
        log.info("onIntent requestId={} intent={}", context.getAwsRequestId(), request.getCurrentIntent().getName());

        String intent = request.getCurrentIntent().getName();
        String character = request.getCurrentIntent().getSlots().get("character");
        if (StarWarsIntent.MOVIE_INTENT.equals(intent)) {
            return getIntroResponse("Star Wars is cool");
        } else if (StarWarsIntent.PLANET_INTENT.equals(intent)) {
            return getPlanetResponse(character);
        } else if (StarWarsIntent.LIGHTSABER_INTENT.equals(intent)) {
            return getLightsaberResponse(character);
        } else if (StarWarsIntent.QUOTES_INTENT.equals(intent)) {
            return getQuotesResponse(character);
        } else if (StarWarsIntent.FORCE_SENSITIVE_INTENT.equals(intent)) {
            return getForceSensitiveResponse(character);
        } else if (StarWarsIntent.FORCE_SIDE_INTENT.equals(intent)) {
            return getForceSideResponse(character);
        } else if (StarWarsIntent.QUESTION_INTENT.equals(intent)) {

            String actualCharacter = request.getInputTranscript();
            String expectedCharacter = request.getSessionAttributes().get("character");
            String question = request.getSessionAttributes().get("question");

            System.out.println("expected/character: " + expectedCharacter);
            System.out.println("actual: " + actualCharacter);
            System.out.println("question: " + question);

            if (null != expectedCharacter && null != actualCharacter && expectedCharacter.equals(actualCharacter)) {
                return getQuoteResponse(actualCharacter, expectedCharacter);
            } else {
                return getQuoteQuestion(request.getSessionAttributes());
            }
        } else if ("AMAZON.HelpIntent".equals(intent)) {
            return getHelpResponse();
        } else {
            throw new RuntimeException("Invalid Intent: " + request.getCurrentIntent().getName());
        }
    }

    private LexResponse getIntroResponse(String intro) {
        StarWarsResponse response = StarWarsResponse.getWelcomeResponse();
        return getLexResponse(response.getSpeechText(), response.getTitle());
    }

    private LexResponse getPlanetResponse(String slotValue) {
        StarWarsResponse response = StarWarsResponse.getPlanetResponse(slotValue);
        return getLexResponse(response.getSpeechText(), response.getTitle());
    }

    private LexResponse getLightsaberResponse(String slotValue) {
        StarWarsResponse response = StarWarsResponse.getLightsaberResponse(slotValue);
        return getLexResponse(response.getSpeechText(), response.getTitle());
    }

    private LexResponse getQuotesResponse(String slotValue) {
        StarWarsResponse response = StarWarsResponse.getQuotesResponse(slotValue);
        return getLexResponse(response.getSpeechText(), response.getTitle());
    }

    private LexResponse getForceSensitiveResponse(String slotValue) {
        StarWarsResponse response = StarWarsResponse.getForceSensitiveResponse(slotValue);
        return getLexResponse(response.getSpeechText(), response.getTitle());
    }

    private LexResponse getForceSideResponse(String slotValue) {
        StarWarsResponse response = StarWarsResponse.getForceSideResponse(slotValue);
        return getLexResponse(response.getSpeechText(), response.getTitle());
    }

    private LexResponse getQuoteQuestion(Map<String, String> sessionAttributes) {
        System.out.println("getQuoteQuestion");

        LexResponse lexResponse = new LexResponse();

        String answered = sessionAttributes.get("answered");
//        String character = sessionAttributes.get("character");
        String question = sessionAttributes.get("question");

        if ((null != answered && answered.equals("yes") ||
                (null == question))) {
            System.out.println("Getting a new question");
            StarWarsResponse response = StarWarsResponse.getQuoteQuestion();
            String character = response.getSessionAttributes().get("character");
            question = response.getSessionAttributes().get("question");

            lexResponse.addAttribute("character", character);
            lexResponse.addAttribute("question", question);
            lexResponse.addAttribute("answered", "no");
        }

        DialogAction dialogAction = new DialogAction();
        dialogAction.setType(DialogAction.ELICIT_SLOT_TYPE);
        Message message = new Message();
        message.setContentType(Message.CONTENT_TYPE_PLAIN_TEXT);
        message.setContent(question);
        dialogAction.setMessage(message);
        dialogAction.setIntentName(StarWarsIntent.QUESTION_INTENT);
        dialogAction.setSlotToElicit("AnswerSlot");
        dialogAction.addSlots("AnswerSlot", "");

        lexResponse.setDialogAction(dialogAction);

        return lexResponse;
    }

    private LexResponse getQuoteResponse(String actual, String expected) {
        StarWarsResponse response = StarWarsResponse.getQuoteResponse(actual, expected);
        LexResponse lexResponse = getLexResponse(response.getSpeechText(), response.getTitle());
        lexResponse.addAttribute("answered", "yes");
        lexResponse.clearAttributes();
        return lexResponse;
    }

    private LexResponse getHelpResponse() {
        StarWarsResponse response = StarWarsResponse.getWelcomeResponse();
        return getLexResponse(response.getSpeechText(), response.getTitle());
    }

    private LexResponse getLexResponse(String speechText, String title) {
        Message message = new Message(Message.CONTENT_TYPE_PLAIN_TEXT, speechText);
        DialogAction dialogAction = new DialogAction(DialogAction.CLOSE_TYPE, DialogAction.FULFILLMENT_STATE_FULFILLED, message);

        return new LexResponse(dialogAction);
    }
}