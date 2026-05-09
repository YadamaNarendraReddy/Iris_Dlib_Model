package com.iris.assistant.service;

import com.iris.assistant.api.InteractionRequest;
import com.iris.assistant.domain.Appliance;
import com.iris.assistant.domain.FaceRecognitionResult;
import com.iris.assistant.domain.IntentResult;
import com.iris.assistant.domain.IntentType;
import com.iris.assistant.domain.InteractionResult;
import com.iris.assistant.domain.PipelineStep;
import com.iris.assistant.domain.UserProfile;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class IrisOrchestrator {

    private final FaceRecognitionService faceRecognitionService;
    private final CommandInterpreter commandInterpreter;
    private final ApplianceService applianceService;
    private final UserDirectory userDirectory;
    private final PromptComposer promptComposer;
    private final ResponseGenerator responseGenerator;

    public IrisOrchestrator(
            FaceRecognitionService faceRecognitionService,
            CommandInterpreter commandInterpreter,
            ApplianceService applianceService,
            UserDirectory userDirectory,
            PromptComposer promptComposer,
            ResponseGenerator responseGenerator
    ) {
        this.faceRecognitionService = faceRecognitionService;
        this.commandInterpreter = commandInterpreter;
        this.applianceService = applianceService;
        this.userDirectory = userDirectory;
        this.promptComposer = promptComposer;
        this.responseGenerator = responseGenerator;
    }

    public InteractionResult handle(InteractionRequest request) {
        String transcript = cleanTranscript(request.transcript());
        FaceRecognitionResult face = faceRecognitionService.recognize(request.faceId(), request.confidence());
        IntentResult intent = commandInterpreter.interpret(transcript);
        Optional<UserProfile> user = userDirectory.findByFaceId(face.faceId()).filter(ignored -> face.recognized());
        Optional<Appliance> beforeAppliance = Optional.ofNullable(intent.applianceId()).flatMap(applianceService::findById);
        boolean authenticated = user
                .filter(profile -> intent.applianceId() == null || profile.canControl(intent.applianceId()))
                .isPresent();

        Appliance finalAppliance = beforeAppliance.orElse(null);
        if (isDeviceCommand(intent) && authenticated) {
            finalAppliance = applianceService.updateStatus(intent.applianceId(), intent.targetStatus()).orElse(finalAppliance);
        }

        String prompt = promptComposer.compose(transcript, face, intent, authenticated, finalAppliance);
        String response = responseGenerator.generate(face, intent, authenticated);
        List<PipelineStep> pipeline = buildPipeline(transcript, face, intent, authenticated, finalAppliance, prompt, response);
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("speechToText", transcript);
        context.put("faceDetected", face.displayName());
        context.put("confidence", face.confidence());
        context.put("userAuth", authenticated);
        context.put("lightStatus", finalAppliance == null ? "N/A" : finalAppliance.status().name());

        return new InteractionResult(
                UUID.randomUUID().toString(),
                Instant.now(),
                transcript,
                face,
                intent,
                authenticated,
                finalAppliance,
                prompt,
                response,
                response,
                pipeline,
                context
        );
    }

    private static String cleanTranscript(String transcript) {
        if (transcript == null || transcript.isBlank()) {
            return "Hey Iris, how are you?";
        }
        return transcript.trim();
    }

    private static boolean isDeviceCommand(IntentResult intent) {
        return intent.type() == IntentType.TURN_LIGHT_ON || intent.type() == IntentType.TURN_LIGHT_OFF;
    }

    private List<PipelineStep> buildPipeline(
            String transcript,
            FaceRecognitionResult face,
            IntentResult intent,
            boolean authenticated,
            Appliance appliance,
            String prompt,
            String response
    ) {
        List<PipelineStep> steps = new ArrayList<>();
        steps.add(new PipelineStep("speech", "Speech to text", "complete", transcript));
        steps.add(new PipelineStep("face", "Facial recognition", face.recognized() ? "matched" : "unverified",
                "%s (%.0f%% confidence)".formatted(face.displayName(), face.confidence() * 100)));
        steps.add(new PipelineStep("intent", "Intent detection", intent.type().name(), intent.summary()));
        steps.add(new PipelineStep("auth", "Authorization", authenticated ? "approved" : "blocked",
                authenticated ? "User may control this appliance" : "Command requires a recognized authorized user"));
        steps.add(new PipelineStep("prompt", "Prompt composer", "ready",
                "Prompt includes username, face match, authorization, intent, appliance status, and transcript"));
        steps.add(new PipelineStep("response", "Response generator", "spoken", response));
        steps.add(new PipelineStep("device", "Device state", appliance == null ? "not-applicable" : appliance.status().name(),
                appliance == null ? "No appliance changed" : "%s is %s".formatted(appliance.displayName(), appliance.status())));
        return steps;
    }
}
