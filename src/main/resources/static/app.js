const transcript = document.querySelector("#transcript");
const faceSelect = document.querySelector("#faceSelect");
const confidence = document.querySelector("#confidence");
const confidenceValue = document.querySelector("#confidenceValue");
const sendButton = document.querySelector("#sendButton");
const typedButton = document.querySelector("#typedButton");
const micButton = document.querySelector("#micButton");
const resetButton = document.querySelector("#resetButton");
const speakButton = document.querySelector("#speakButton");
const voiceOutput = document.querySelector("#voiceOutput");
const pipeline = document.querySelector("#pipeline");
const assistantResponse = document.querySelector("#assistantResponse");
const promptPreview = document.querySelector("#promptPreview");
const contextPreview = document.querySelector("#contextPreview");
const apiStatus = document.querySelector("#apiStatus");
const lightStatus = document.querySelector("#lightStatus");
const deviceState = document.querySelector("#deviceState");
const deviceTile = document.querySelector("#deviceTile");
const voiceStatus = document.querySelector("#voiceStatus");
const liveTranscript = document.querySelector("#liveTranscript");
const traceSteps = {
    listen: document.querySelector("#traceListen"),
    post: document.querySelector("#tracePost"),
    backend: document.querySelector("#traceBackend"),
    speak: document.querySelector("#traceSpeak")
};

const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
let lastResponse = "";
let listening = false;

function percent(value) {
    return `${Math.round(Number(value) * 100)}%`;
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}

function setLights(status) {
    lightStatus.textContent = status;
    deviceState.textContent = status;
    deviceTile.classList.toggle("device-on", status === "ON");
}

function setTrace(activeKey) {
    Object.entries(traceSteps).forEach(([key, element]) => {
        element.classList.toggle("active", key === activeKey);
        element.classList.toggle("complete", key !== activeKey && element.dataset.completed === "true");
    });
}

function markTraceComplete(key) {
    traceSteps[key].dataset.completed = "true";
    traceSteps[key].classList.add("complete");
}

function resetTrace() {
    Object.values(traceSteps).forEach((element) => {
        element.dataset.completed = "false";
        element.classList.remove("active", "complete");
    });
    setTrace("listen");
}

function setListeningState(isListening, status, detail) {
    listening = isListening;
    document.body.classList.toggle("is-listening", isListening);
    micButton.classList.toggle("listening", isListening);
    voiceStatus.textContent = status;
    if (detail) {
        liveTranscript.textContent = detail;
    }
}

function renderPipeline(steps) {
    pipeline.innerHTML = steps.map((step) => {
        const stateClass = `state-${String(step.state).toLowerCase()}`;
        const stateLabel = String(step.state).replaceAll("_", " ").replaceAll("-", " ");
        return `
            <article class="pipeline-card">
                <span class="state-pill ${stateClass}">${escapeHtml(stateLabel)}</span>
                <strong>${escapeHtml(step.label)}</strong>
                <p>${escapeHtml(step.detail)}</p>
            </article>
        `;
    }).join("");
}

async function refreshAppliances() {
    const response = await fetch("/api/appliances");
    const appliances = await response.json();
    const lights = appliances.find((item) => item.id === "living-room-lights");
    setLights(lights?.status ?? "OFF");
}

function speak(text) {
    lastResponse = text;
    if (!("speechSynthesis" in window) || !text) {
        return;
    }
    window.speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.rate = 0.96;
    utterance.pitch = 1.02;
    utterance.volume = 1;
    window.speechSynthesis.speak(utterance);
}

function listenForSpeech() {
    return new Promise((resolve, reject) => {
        if (!SpeechRecognition) {
            reject(new Error("Live speech recognition is not supported in this browser. Use Chrome or Edge on localhost."));
            return;
        }

        const recognition = new SpeechRecognition();
        recognition.lang = "en-US";
        recognition.interimResults = true;
        recognition.continuous = false;
        recognition.maxAlternatives = 1;

        let finalTranscript = "";
        let interimTranscript = "";

        recognition.onstart = () => {
            setListeningState(true, "Listening", "Iris is listening...");
            setTrace("listen");
        };

        recognition.onresult = (event) => {
            finalTranscript = "";
            interimTranscript = "";
            for (let index = event.resultIndex; index < event.results.length; index += 1) {
                const phrase = event.results[index][0].transcript.trim();
                if (event.results[index].isFinal) {
                    finalTranscript += `${phrase} `;
                } else {
                    interimTranscript += `${phrase} `;
                }
            }
            const heard = `${finalTranscript}${interimTranscript}`.trim();
            if (heard) {
                transcript.value = heard;
                liveTranscript.textContent = heard;
            }
        };

        recognition.onerror = (event) => {
            setListeningState(false, "Speech error", event.error || "Speech recognition failed");
            reject(new Error(event.error || "Speech recognition failed"));
        };

        recognition.onend = () => {
            const heard = (finalTranscript || transcript.value).trim();
            setListeningState(false, heard ? "Captured" : "No speech captured", heard || "Try again or run the typed transcript.");
            if (heard) {
                markTraceComplete("listen");
                resolve(heard);
            } else {
                reject(new Error("No speech was captured."));
            }
        };

        recognition.start();
    });
}

async function sendTranscriptToBackend(source = "typed") {
    sendButton.disabled = true;
    typedButton.disabled = true;
    micButton.disabled = true;
    sendButton.textContent = source === "voice" ? "Sending voice..." : "Running...";
    try {
        setTrace("post");
        markTraceComplete("post");
        const response = await fetch("/api/interactions", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                transcript: transcript.value,
                faceId: faceSelect.value,
                confidence: Number(confidence.value)
            })
        });
        setTrace("backend");
        const result = await response.json();
        markTraceComplete("backend");
        assistantResponse.textContent = result.assistantResponse;
        lastResponse = result.assistantResponse;
        promptPreview.textContent = result.prompt;
        contextPreview.textContent = JSON.stringify(result.context, null, 2);
        renderPipeline(result.pipeline);
        setLights(result.appliance?.status ?? lightStatus.textContent);
        setTrace("speak");
        if (voiceOutput.checked) {
            speak(result.assistantResponse);
        }
        markTraceComplete("speak");
        setListeningState(false, "Answer ready", result.transcript || transcript.value);
    } catch (error) {
        assistantResponse.textContent = "Iris could not reach the backend. Start the Spring Boot app and try again.";
        promptPreview.textContent = String(error);
        setListeningState(false, "Backend error", String(error.message || error));
    } finally {
        sendButton.disabled = false;
        typedButton.disabled = false;
        micButton.disabled = false;
        sendButton.textContent = "Run live voice pipeline";
    }
}

async function runLiveVoicePipeline() {
    resetTrace();
    try {
        await listenForSpeech();
        await sendTranscriptToBackend("voice");
    } catch (error) {
        if (!SpeechRecognition) {
            assistantResponse.textContent = "Live speech recognition needs Chrome or Edge. You can still run the typed transcript.";
        }
        setListeningState(false, "Ready", error.message || "Try speaking again.");
        sendButton.disabled = false;
        typedButton.disabled = false;
        micButton.disabled = false;
        sendButton.textContent = "Run live voice pipeline";
    }
}

async function resetDemo() {
    await fetch("/api/demo/reset", {method: "POST"});
    await refreshAppliances();
    assistantResponse.textContent = "Demo reset. Living room lights are OFF.";
    promptPreview.textContent = "Waiting for interaction...";
    contextPreview.textContent = "{}";
    pipeline.innerHTML = "";
    resetTrace();
    setListeningState(false, "Ready", "Press the pipeline button and speak.");
}

confidence.addEventListener("input", () => {
    confidenceValue.textContent = percent(confidence.value);
});

document.querySelectorAll(".scenario").forEach((button) => {
    button.addEventListener("click", () => {
        transcript.value = button.dataset.text;
        faceSelect.value = button.dataset.face;
        confidence.value = button.dataset.confidence;
        confidenceValue.textContent = percent(confidence.value);
        liveTranscript.textContent = button.dataset.text;
        resetTrace();
        markTraceComplete("listen");
        sendTranscriptToBackend("typed");
    });
});

sendButton.addEventListener("click", runLiveVoicePipeline);
micButton.addEventListener("click", runLiveVoicePipeline);
typedButton.addEventListener("click", () => {
    resetTrace();
    markTraceComplete("listen");
    sendTranscriptToBackend("typed");
});
speakButton.addEventListener("click", () => speak(lastResponse || assistantResponse.textContent));
resetButton.addEventListener("click", resetDemo);

async function boot() {
    try {
        const health = await fetch("/api/health").then((response) => response.json());
        apiStatus.textContent = health.status;
        await refreshAppliances();
        if (!SpeechRecognition) {
            voiceStatus.textContent = "Typed fallback";
            liveTranscript.textContent = "Live speech works in Chrome or Edge. Typed demo remains available.";
        }
    } catch {
        apiStatus.textContent = "Offline";
    }
}

resetTrace();
boot();
