package ru.practicum.service;

import stats.messages.analyzer.AnalyzerMessages;

import java.util.List;

public interface RecommendationService {
    List<AnalyzerMessages.RecommendedEventProto> getRecommendations(AnalyzerMessages.UserPredictionsRequestProto request);

    List<AnalyzerMessages.RecommendedEventProto> getSimilarEvents(AnalyzerMessages.SimilarEventsRequestProto request);

    List<AnalyzerMessages.RecommendedEventProto> getInteractionsCount(AnalyzerMessages.InteractionsCountRequestProto request);
}