package org.example.service.impl;

import org.example.jpa.dao.StockQuoteRepository;
import org.example.jpa.entity.StockQuote;
import org.example.service.StockService;
import org.simmetrics.StringDistance;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StockServiceImpl implements StockService {
    private final StockQuoteRepository stockQuoteRepository;

    public StockServiceImpl(StockQuoteRepository stockQuoteRepository) {
        this.stockQuoteRepository = stockQuoteRepository;
    }

    @Override
    public StockQuote getInfoAboutTicket(String secId) {
        Optional<StockQuote> s = stockQuoteRepository.findBySecId(secId);
        return s.orElse(null);
    }
    public List<StockQuote> fuzzysearchCompany(String userText) {
        List<StockQuote> matchingCompanies = new ArrayList<>();

        List<StockQuote> allCompanies = stockQuoteRepository.findAll(); // Получите все компании из базы данных

        for (StockQuote company : allCompanies) {
            if (isFuzzyMatchLevenshtein(company.getShortName(), userText)) {
                matchingCompanies.add(company);
            }
        }

        return matchingCompanies;
    }


    private boolean isFuzzyMatchLevenshtein(String companyName, String userText) {
        double threshold = 2.0; // Максимальное разрешенное расстояние Левенштейна
        StringDistance levenshtein = (StringDistance) StringMetrics.levenshtein();
        double distance = levenshtein.distance(companyName, userText);
        return distance <= threshold;
    }

    private boolean isFuzzyMatchJaroWinkler(String companyName, String userText) {
        double threshold = 0.85; // Порог схожести для расстояния Жаро-Винклера
        StringDistance jaroWinkler = (StringDistance) StringMetrics.jaroWinkler();
        double similarity = jaroWinkler.distance(companyName, userText);
        return similarity >= threshold;
    }

    private boolean isFuzzyMatchJaccard(String companyName, String userText) {
        double threshold = 0.5; // Порог схожести для коэффициента Жаккара
        StringMetric jaccard = StringMetrics.jaccard();
        double similarity = jaccard.compare(companyName, userText);
        return similarity >= threshold;
    }

    private boolean isFuzzyMatchCosine(String companyName, String userText) {
        double threshold = 0.5; // Порог схожести для косинусного коэффициента
        StringDistance cosine = (StringDistance) StringMetrics.cosineSimilarity();
        double similarity = cosine.distance(companyName, userText);
        return similarity >= threshold;
    }

// Дополнительные методы:
// - isFuzzyMatchJaro()
// - isFuzzyMatchSorensenDice()
// - isFuzzyMatchOverlap()
// - и т.д.
}
