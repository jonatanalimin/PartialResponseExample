package org.example;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.cx.v3.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class DetectIntentStreamBased {
    private static final Object getFinalResponse = new Object();
    private static boolean endSession = false;

    public static void detectIntent(
            String projectId,
            String locationId,
            String agentId,
            String sessionId,
            String environmentId,
            String languageCode,
            String pathKey
    ) throws IOException {
        ResponseObserver<StreamingDetectIntentResponse> responseObserver =
                new ResponseObserver<>() {
                    @Override
                    public void onStart(StreamController streamController) {

                    }

                    @Override
                    public void onResponse(StreamingDetectIntentResponse streamingDetectIntentResponse) {
                        if (streamingDetectIntentResponse.hasDetectIntentResponse()) {
                            if(streamingDetectIntentResponse.getDetectIntentResponse().getQueryResult().getResponseMessagesList().size() > 0) {
                                for (ResponseMessage agentResponse : streamingDetectIntentResponse.getDetectIntentResponse()
                                        .getQueryResult().getResponseMessagesList()) {
                                    if (agentResponse.hasText()) {
                                        System.out.printf("Agent Response (%s): %s%n",
                                                streamingDetectIntentResponse.getDetectIntentResponse().getResponseType(),
                                                agentResponse.getText().getText(0));
                                    }
                                }
                            }

                            if (streamingDetectIntentResponse.getDetectIntentResponse().getQueryResult().getCurrentPage()
                                    .getDisplayName().equals("End Session")){
                                endSession = true;
                            }
                        } else {
                            System.out.println(streamingDetectIntentResponse);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println(throwable.toString());
                        endSession = true;
                        synchronized (getFinalResponse) {
                            getFinalResponse.notifyAll();
                        }
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("bidistream onComplete called!\n");
                        synchronized (getFinalResponse) {
                            getFinalResponse.notifyAll();
                        }
                    }
                };


        SessionsSettings.Builder sessionSettingBuilder = SessionsSettings.newBuilder();

        sessionSettingBuilder.setCredentialsProvider(FixedCredentialsProvider
                .create(GoogleCredentials
                        .fromStream(new FileInputStream(pathKey))));

        if (locationId.equals("global")){
            sessionSettingBuilder.setEndpoint("dialogflow.googleapis.com:443");
        } else{
            sessionSettingBuilder.setEndpoint(locationId + "-dialogflow.googleapis.com:443");
        }
        SessionsSettings sessionsSettings = sessionSettingBuilder.build();
        SessionName session = SessionName.ofProjectLocationAgentEnvironmentSessionName(
                projectId, locationId, agentId, environmentId, sessionId);
        //TODO If you not use environment, use the below line instead
//        SessionName session = SessionName.ofProjectLocationAgentSessionName(
//                projectId, locationId, agentId, sessionId);

        String userTextInput;
        Scanner scanner = new Scanner(System.in);
        try (SessionsClient sessionsClient = SessionsClient.create(sessionsSettings)) {
            do{
                System.out.print("Input Text: ");
                userTextInput = scanner.nextLine();
                QueryInput queryInput = QueryInput.newBuilder()
                        .setText(TextInput.newBuilder().setText(userTextInput).build())
                        .setLanguageCode(languageCode)
                        .build();

                ClientStream<StreamingDetectIntentRequest> bidiStream =
                        sessionsClient.streamingDetectIntentCallable().splitCall(responseObserver);
                bidiStream.send(StreamingDetectIntentRequest.newBuilder()
                        .setSession(session.toString())
                        .setQueryInput(queryInput)
                        .setEnablePartialResponse(true)
                        .build());

                bidiStream.closeSend();

                synchronized (getFinalResponse){
                    try {
                        getFinalResponse.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } while (!endSession);
        }
    }
}
