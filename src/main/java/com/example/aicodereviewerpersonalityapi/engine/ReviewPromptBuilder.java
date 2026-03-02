package com.example.aicodereviewerpersonalityapi.engine;

import com.example.aicodereviewerpersonalityapi.config.ReviewerProperties;
import com.example.aicodereviewerpersonalityapi.model.Personality;
import com.example.aicodereviewerpersonalityapi.model.ReviewRequest;
import org.springframework.stereotype.Component;

@Component
public class ReviewPromptBuilder {

  private final ReviewerProperties properties;

  public ReviewPromptBuilder(ReviewerProperties properties) {
    this.properties = properties;
  }

  public String buildUserPrompt(ReviewRequest request) {
    return """
        Review this code snippet.

        Requirements:
        - Return only JSON with fields: verdict, comment
        - verdict must be one of: APPROVE, COMMENT, REQUEST_CHANGES
        - comment must include:
          1) short summary line
          2) bullet list of findings (if none, include "No major issues found")
          3) suggested next step
        - Multi-line comment is allowed.
        - Detect at least: TODO/FIXME, empty catch blocks, Thread.sleep, hardcoded password/token patterns, SQL without WHERE, huge function heuristic, no tests mention.
        - If security-like pattern exists, verdict should usually be REQUEST_CHANGES when strictSecurity is true.
        - If warnings count is >= warningThreshold, verdict should usually be COMMENT.
        - Keep tone strictly in this personality style: %s
        - sarcasmEnabled for passive-aggressive style: %s
        - strictSecurity: %s
        - warningThreshold: %d

        language: %s
        personality: %s
        codeSnippet:
        %s
        """
        .formatted(
            styleHint(request.getPersonality()),
            properties.isEnableSarcasm(),
            properties.isStrictSecurity(),
            properties.getWarningThreshold(),
            request.getLanguage(),
            request.getPersonality(),
            request.getCodeSnippet());
  }

  public String systemPrompt() {
    return """
        You are a code review assistant.
        Respond with valid JSON only, no markdown, no code fences, no extra keys.
        JSON schema:
        {
          "verdict": "APPROVE|COMMENT|REQUEST_CHANGES",
          "comment": "string"
        }
        Keep language concise and actionable.
        """;
  }

  private String styleHint(Personality personality) {
    return switch (personality) {
      case STRICT -> "direct and formal";
      case PASSIVE_AGGRESSIVE -> "snarky but non-abusive and non-insulting";
      case OVER_ENTHUSIASTIC -> "overly positive with at most 1-2 emojis";
      case FAANG_INTERVIEWER -> "focused on complexity, edge cases, and tests";
      case STARTUP_CTO -> "pragmatic, shipping-focused, but safety-aware";
      case VIBE_CODER -> "super casual, vibe-driven reaction, less technical breakdown, more gut-feeling commentary, no structured analysis";
    };
  }
}
