package org.example.service.impl;

import org.example.jpa.dao.StockQuoteRepository;
import org.example.jpa.entity.StockQuote;
import org.example.service.StockService;
import org.simmetrics.StringDistance;
import org.simmetrics.StringMetric;
import org.simmetrics.metrics.StringMetrics;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class StockServiceImpl implements StockService {
    private final StockQuoteRepository stockQuoteRepository;
    private final EntityManager entityManager;

    public StockServiceImpl(StockQuoteRepository stockQuoteRepository, EntityManager entityManager) {
        this.stockQuoteRepository = stockQuoteRepository;
        this.entityManager = entityManager;
    }

    @Override
    public StockQuote getInfoAboutTicket(String secId) {
        Optional<StockQuote> s = stockQuoteRepository.findBySecId(secId);
        return s.orElse(null);
    }

    public List<StockQuote> searchOnCompanyZAPAS(String searchTerm, int maxResults){
// Рассчитываем порог сходства на основе максимального количества результатов
        float similarityThreshold = calculateSimilarityThreshold(maxResults);

        // Создаем SQL-запрос для выполнения поиска с использованием pg_trgm и заданным порогом
        String sqlQuery = "SELECT * FROM moex WHERE short_name % :searchTerm AND similarity(short_name, :searchTerm) >= :threshold";
        String sqlQuery2 = "SELECT * FROM moex WHERE short_name ILIKE :searchTerm AND similarity(short_name, :searchTerm) >= :threshold";


        // Создаем JPA-запрос
        Query query = entityManager.createNativeQuery(sqlQuery, StockQuote.class);
        query.setParameter("searchTerm", searchTerm);
        query.setParameter("threshold", similarityThreshold);

        // Выполняем запрос и возвращаем результат
        List<StockQuote> results = query.getResultList();
        if (results == null || results.isEmpty()) {
            return null;

        } else {
            return results;
        }
    }
    public List<StockQuote> searchOnCompany(String searchTerm, int maxResults) {
        float similarityThreshold = calculateSimilarityThreshold(maxResults);
        String sqlQuery2 = "SELECT * FROM moex WHERE short_name ILIKE :searchTerm AND similarity(short_name, :searchTerm) >= :threshold";
        String sqlQuery = "SELECT * FROM moex WHERE short_name % :searchTerm AND similarity(short_name, :searchTerm) >= :threshold";

        while (true) {
            Query query = entityManager.createNativeQuery(sqlQuery, StockQuote.class);
            query.setParameter("searchTerm", searchTerm);
            query.setParameter("threshold", similarityThreshold);

            List<StockQuote> results = query.getResultList();
            if (results != null && !results.isEmpty()) {
                return results;
            }

            // Если результатов нет, уменьшаем порог сходства
            similarityThreshold -= 0.1f; // Уменьшаем порог на 0.1 (можете настроить в соответствии с вашими требованиями)

            // Если порог становится слишком низким, выходим из цикла
            if (similarityThreshold < 0.1f) {
                break;
            }
        }

        return null; // Если не найдено ни одной компании при достижении минимального порога
    }


    private float calculateSimilarityThreshold(int maxResults) {
        // Максимальный порог сходства, который будет использоваться, если maxResults = 1
        float maxThreshold = 0.9f;
        // Минимальный порог сходства, который будет использоваться, если maxResults >= 10
        float minThreshold = 0.5f;

        // Линейная интерполяция между maxThreshold и minThreshold на основе maxResults
        float similarityThreshold = maxThreshold - ((maxThreshold - minThreshold) / 9) * (maxResults - 1);

        // Убеждаемся, что порог остается в допустимых границах
        similarityThreshold = Math.max(similarityThreshold, minThreshold);
        similarityThreshold = Math.min(similarityThreshold, maxThreshold);

        return similarityThreshold;
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
