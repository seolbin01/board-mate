package com.benny.board_mate.rulemaster.client;

import com.benny.board_mate.common.exception.BusinessException;
import com.benny.board_mate.common.exception.ErrorCode;
import com.benny.board_mate.rulemaster.dto.GameDetailResponse;
import com.benny.board_mate.rulemaster.dto.GameSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BggApiClient {

    private static final String BGG_API_BASE_URL = "https://boardgamegeek.com/xmlapi2";
    private final WebClient bggWebClient;

    @Cacheable(value = "rulemaster-bgg-search", key = "#query")
    public List<GameSearchResponse> searchGames(String query) {
        try {
            log.info("BGG API 검색 요청: query={}", query);

            String xmlResponse = bggWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("query", query)
                            .queryParam("type", "boardgame")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseSearchResponse(xmlResponse);
        } catch (Exception e) {
            log.error("BGG API 검색 실패: query={}", query, e);
            throw new BusinessException(ErrorCode.RULEMASTER_BGG_API_ERROR);
        }
    }

    @Cacheable(value = "rulemaster-bgg-game", key = "#bggId")
    public GameDetailResponse getGameDetail(Long bggId) {
        try {
            log.info("BGG API 상세 정보 요청: bggId={}", bggId);

            String xmlResponse = bggWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/thing")
                            .queryParam("id", bggId)
                            .queryParam("stats", "1")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseGameDetail(xmlResponse);
        } catch (Exception e) {
            log.error("BGG API 상세 정보 조회 실패: bggId={}", bggId, e);
            throw new BusinessException(ErrorCode.RULEMASTER_BGG_API_ERROR);
        }
    }

    private List<GameSearchResponse> parseSearchResponse(String xmlResponse) throws Exception {
        List<GameSearchResponse> results = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));

        NodeList items = doc.getElementsByTagName("item");
        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);

            Long bggId = Long.parseLong(item.getAttribute("id"));

            Element nameElement = (Element) item.getElementsByTagName("name").item(0);
            String name = nameElement != null ? nameElement.getAttribute("value") : null;

            Element yearElement = (Element) item.getElementsByTagName("yearpublished").item(0);
            Integer year = null;
            if (yearElement != null) {
                String yearValue = yearElement.getAttribute("value");
                year = yearValue != null && !yearValue.isEmpty() ? Integer.parseInt(yearValue) : null;
            }

            results.add(new GameSearchResponse(bggId, name, year, null));
        }

        return results;
    }

    private GameDetailResponse parseGameDetail(String xmlResponse) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));

        Element item = (Element) doc.getElementsByTagName("item").item(0);
        if (item == null) {
            throw new BusinessException(ErrorCode.GAME_NOT_FOUND);
        }

        Long bggId = Long.parseLong(item.getAttribute("id"));

        // Names
        String name = null;
        String nameKorean = null;
        NodeList names = item.getElementsByTagName("name");
        for (int i = 0; i < names.getLength(); i++) {
            Element nameElement = (Element) names.item(i);
            String type = nameElement.getAttribute("type");
            String value = nameElement.getAttribute("value");

            if ("primary".equals(type)) {
                name = decodeHtmlEntities(value);
            } else if ("alternate".equals(type) && value != null && containsKorean(value)) {
                nameKorean = decodeHtmlEntities(value);
            }
        }

        // Description
        String description = getTextContent(item, "description");
        if (description != null) {
            description = decodeHtmlEntities(description);
        }

        // Year
        Integer yearPublished = getIntegerAttribute(item, "yearpublished", "value");

        // Players
        Integer minPlayers = getIntegerAttribute(item, "minplayers", "value");
        Integer maxPlayers = getIntegerAttribute(item, "maxplayers", "value");

        // Playing time
        Integer playingTime = getIntegerAttribute(item, "playingtime", "value");
        Integer minPlayTime = getIntegerAttribute(item, "minplaytime", "value");
        Integer maxPlayTime = getIntegerAttribute(item, "maxplaytime", "value");

        // Images
        String imageUrl = getTextContent(item, "image");
        String thumbnailUrl = getTextContent(item, "thumbnail");

        // Mechanics and Categories
        List<String> mechanics = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        NodeList links = item.getElementsByTagName("link");
        for (int i = 0; i < links.getLength(); i++) {
            Element link = (Element) links.item(i);
            String type = link.getAttribute("type");
            String value = link.getAttribute("value");

            if ("boardgamemechanic".equals(type)) {
                mechanics.add(decodeHtmlEntities(value));
            } else if ("boardgamecategory".equals(type)) {
                categories.add(decodeHtmlEntities(value));
            }
        }

        // Statistics
        Double averageRating = null;
        Double weight = null;
        NodeList statistics = item.getElementsByTagName("statistics");
        if (statistics.getLength() > 0) {
            Element stats = (Element) statistics.item(0);
            Element ratings = (Element) stats.getElementsByTagName("ratings").item(0);
            if (ratings != null) {
                averageRating = getDoubleAttribute(ratings, "average", "value");
                weight = getDoubleAttribute(ratings, "averageweight", "value");
            }
        }

        return new GameDetailResponse(
            bggId,
            name,
            nameKorean,
            yearPublished,
            description,
            minPlayers,
            maxPlayers,
            playingTime,
            minPlayTime,
            maxPlayTime,
            mechanics,
            categories,
            imageUrl,
            thumbnailUrl,
            averageRating,
            weight
        );
    }

    private String getTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    private Integer getIntegerAttribute(Element parent, String tagName, String attributeName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            Element element = (Element) nodes.item(0);
            String value = element.getAttribute(attributeName);
            if (value != null && !value.isEmpty()) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private Double getDoubleAttribute(Element parent, String tagName, String attributeName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            Element element = (Element) nodes.item(0);
            String value = element.getAttribute(attributeName);
            if (value != null && !value.isEmpty()) {
                try {
                    return Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private boolean containsKorean(String text) {
        if (text == null) return false;
        return text.chars().anyMatch(ch ->
            (ch >= 0xAC00 && ch <= 0xD7A3) || // 한글 음절
            (ch >= 0x1100 && ch <= 0x11FF) || // 한글 자모
            (ch >= 0x3130 && ch <= 0x318F)    // 한글 호환 자모
        );
    }

    private String decodeHtmlEntities(String text) {
        if (text == null) return null;

        return text
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&apos;", "'")
            .replace("&mdash;", "—")
            .replace("&ndash;", "–")
            .replace("&hellip;", "…")
            .replace("&rsquo;", "'")
            .replace("&lsquo;", "'")
            .replace("&rdquo;", "\"")
            .replace("&ldquo;", "\"");
    }
}
