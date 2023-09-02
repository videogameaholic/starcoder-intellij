package com.videogameaholic.intellij.starcoder;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

public enum PromptModel {
    STARCODER ("starcoder","StarCoder", "<fim_prefix>","<fim_suffix>","<fim_middle>", "<|endoftext|>"),
    SANTACODER ("santacoder","SantaCoder", "<fim-prefix>","<fim-suffix>","<fim-middle>", "<|endoftext|>"),
    // Whitespace for Code Llama is intentional
    CODELLAMA ("codellama","Code Llama", "<PRE> "," <SUF>"," <MID>", "<EOT>");

    private final String id;
    private final String displayName;
    private final String prefixTag;
    private final String suffixTag;
    private final String middleTag;
    private final String endTag;

    private PromptModel(String uniqueId, String name, String prefix, String suffix, String middle, String end)
    {
        id = uniqueId;
        displayName = name;
        prefixTag = prefix;
        suffixTag = suffix;
        middleTag = middle;
        endTag = end;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String generateFIMPrompt(String metaData, String code, int fillPosition) {
        // First validate text does not already contain tokens that would confuse the AI.
        // Future: May replace with alternate tokens and switch back after response.
        if(code.contains(prefixTag) || code.contains(suffixTag) || code.contains(middleTag) || code.contains(endTag)) return "";

        String prefix = code.substring(0, fillPosition);
        String suffix = code.substring(fillPosition);
        return metaData + prefixTag + prefix + suffixTag + suffix + middleTag;
    }

    @Nullable
    public String[] buildSuggestionList(String generatedText) {
        String[] suggestionList = null;
        generatedText = generatedText.replace(endTag, "");
        if(generatedText.contains(middleTag)) {
            String[] parts = generatedText.split(middleTag);
            if(parts.length > 1) {
                suggestionList = StringUtils.splitPreserveAllTokens(parts[1], "\n");
                if(suggestionList.length == 1 && suggestionList[0].trim().length() == 0) return null;
                if(suggestionList.length > 1) {
                    for (int i = 0; i < suggestionList.length; i++) {
                        StringBuilder sb = new StringBuilder(suggestionList[i]);
                        sb.append("\n");
                        suggestionList[i] = sb.toString();
                    }
                }
            }
        }
        return suggestionList;
    }
}
