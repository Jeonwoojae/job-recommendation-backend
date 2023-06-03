package kr.ac.kumoh.whale.authservice.global.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.kumoh.whale.authservice.global.batch.json.health_center.HealthCenterInfo;
import kr.ac.kumoh.whale.authservice.global.batch.json.risk_assessment_certified_workplace.RiskAssessmentCertifiedWorkplace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ApiReader {
    private String serviceKey = "vdGJEuz%2B3h6ABcRGeQGy8ZFlf9w4QYyvY4pCW3PD4YrofQFod1ylz6kX870R98FprfaItjyPu1Y1V%2BCNjGkn6Q%3D%3D";

    @Bean
    public ItemReader<RiskAssessmentCertifiedWorkplace> riskAssessmentCertifiedWorkplaceItemReader() throws URISyntaxException, JsonProcessingException {
        List<RiskAssessmentCertifiedWorkplace> riskAssessmentCertifiedWorkplaces = getFileItemReaderFromRiskApi(RiskAssessmentCertifiedWorkplace.class);
        return new IteratorItemReader<>(riskAssessmentCertifiedWorkplaces);
    }

    private List<RiskAssessmentCertifiedWorkplace> getFileItemReaderFromRiskApi(Class<RiskAssessmentCertifiedWorkplace> riskAssessmentCertifiedWorkplaceClass) throws URISyntaxException, JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        int currentPage = 0;
        int currentCount = 1000;
        int totalCount = 1001;

        List<RiskAssessmentCertifiedWorkplace> result = new ArrayList<>();

        while (currentCount!=0){
            currentPage++;
            log.info("totalCount : {}, currentPage : {}, currentCount : {}",totalCount, currentPage, currentCount);
            String uri = "https://api.odcloud.kr/api/15002452/v1/uddi:4bc92095-bfa2-464f-8723-b65c6a336565?"
                    + "page=" + currentPage + "&perPage=" + 1000 + "&serviceKey=" + serviceKey;
            URI uri1 = new URI(uri);

            //log.info("Fetching data from an external API by using the url: {}", uri);

            ResponseEntity<String> response = restTemplate.exchange(uri1, HttpMethod.GET,
                    new HttpEntity<>(headers), String.class);

            // Json parsing
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> responseObject = objectMapper.readValue(response.getBody(),
                    new TypeReference<Map<String, Object>>() {});

            ArrayList<Map<String,String>> dataProperty = (ArrayList<Map<String, String>>) responseObject.get("data");
            List<RiskAssessmentCertifiedWorkplace> riskAssessmentCertifiedWorkplaces = dataProperty.stream().map((Map<String, String> t) -> new RiskAssessmentCertifiedWorkplace(t)).collect(Collectors.toList());

            currentCount = Integer.parseInt(responseObject.get("currentCount").toString());
            currentPage = Integer.parseInt(responseObject.get("page").toString());
            totalCount = Integer.parseInt(responseObject.get("totalCount").toString());
            result.addAll(riskAssessmentCertifiedWorkplaces);
        }

        return result;
    }

    @Bean
    public ItemReader<HealthCenterInfo>  apiItemReader() throws JsonProcessingException, URISyntaxException {
        List<HealthCenterInfo> healthCenterInfos = getFileItemReaderFromApi(HealthCenterInfo.class);
        return new IteratorItemReader<>(healthCenterInfos);
    }

    private <T> List<T> getFileItemReaderFromApi(Class<T> targetType) throws JsonProcessingException, URISyntaxException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        String uri = "http://api.odcloud.kr/api/15002433/v1/uddi:a4faea37-767a-4a67-afd7-d2599e936453?"
                +"page="+ 1+ "&perPage="+1000+"&serviceKey="+"vdGJEuz%2B3h6ABcRGeQGy8ZFlf9w4QYyvY4pCW3PD4YrofQFod1ylz6kX870R98FprfaItjyPu1Y1V%2BCNjGkn6Q%3D%3D";
        URI uri1 = new URI(uri);

        log.info("Fetching data from an external API by using the url: {}", uri);

        ResponseEntity<String> response = restTemplate.exchange(uri1, HttpMethod.GET,
                new HttpEntity<>(headers), String.class);

        // Json parsing
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseObject = objectMapper.readValue(response.getBody(),
                new TypeReference<Map<String, Object>>() {});

        ArrayList<Map<String,String>> dataProperty = (ArrayList<Map<String, String>>) responseObject.get("data");
        List<HealthCenterInfo> healthCenterInfos = dataProperty.stream().map((Map<String, String> t) -> new HealthCenterInfo(t)).collect(Collectors.toList());


        int numOfRows = Integer.parseInt(responseObject.get("currentCount").toString());
        int pageNo = Integer.parseInt(responseObject.get("page").toString());
        int totalCount = Integer.parseInt(responseObject.get("totalCount").toString());

        return (List<T>) healthCenterInfos;
    }
}