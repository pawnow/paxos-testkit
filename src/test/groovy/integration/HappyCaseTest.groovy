package groovy.integration;

import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate;
import pl.edu.agh.iosr.cdm.Node;
import pl.edu.agh.iosr.utils.ApplicationEndpoints;
import pl.edu.agh.iosr.utils.NodesProvider;
import spock.lang.Specification;

/**
 * Created by Szymon on 2016-11-15.
 */
public class HappyCaseTest extends Specification{

    def RestTemplate restTemplate = new RestTemplate()

    def setup() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        NodesProvider.getListOfApplicationNodes()
                .stream()
                .forEach({
            node -> restTemplate.postForEntity(node.getNodeUrl()+ApplicationEndpoints.ONLINE_URL, params, String.class)
                restTemplate.exchange(node.getNodeUrl()+ApplicationEndpoints.CLEANER_URL, HttpMethod.GET, new HttpEntity<>(), String.class)
        })
    }


    def shouldProperlyLearnValue() {
        given:
        Node node = NodesProvider.get(0)
        HttpEntity<?> entity = new HttpEntity<>()
        MultiValueMap<String, String> proposalParams = new LinkedMultiValueMap<String, String>()
        proposalParams.add("key", "testkey")
        proposalParams.add("value", "5")
        MultiValueMap<String, String> retrievalParams = new LinkedMultiValueMap<String, String>()
        retrievalParams.add("key", "testkey")

        when: 'rest propose url is hit'
        HttpEntity<String> proposal = restTemplate.postForEntity(node.getNodeUrl()+ ApplicationEndpoints.CLIENT_PROPOSE_URL, proposalParams, String.class);
        HttpEntity<String> result = restTemplate.postForEntity(node.getNodeUrl()+ApplicationEndpoints.CLIENT_RETRIEVE_URL, retrievalParams, String.class);

        then: 'client should recieve proper value'
        proposal.statusCode == HttpStatus.OK
        result.statusCode == HttpStatus.OK
        result.body == "5"
    }

    def shouldProperlyLearnSeparateValues() {
        given:
        HttpEntity<?> entity = new HttpEntity<>()
        Node first = NodesProvider.get(0)
        Node second = NodesProvider.get(1)
        MultiValueMap<String, String> proposalParamsFirst = new LinkedMultiValueMap<String, String>()
        proposalParamsFirst.add("key", "testkey")
        proposalParamsFirst.add("value", "5")
        MultiValueMap<String, String> retrievalParamsFirst = new LinkedMultiValueMap<String, String>()
        retrievalParamsFirst.add("key", "testkey")
        MultiValueMap<String, String> proposalParamsSecond = new LinkedMultiValueMap<String, String>()
        proposalParamsSecond.add("key", "testkey2")
        proposalParamsSecond.add("value", "7")
        MultiValueMap<String, String> retrievalParamsSecond = new LinkedMultiValueMap<String, String>()
        retrievalParamsSecond.add("key", "testkey2")


        when: 'rest propose for both requests url is hit'
        HttpEntity<String> proposalFirst = restTemplate.postForEntity(first.getNodeUrl()+ ApplicationEndpoints.CLIENT_PROPOSE_URL, proposalParamsFirst, String.class);
        HttpEntity<String> resultFirst = restTemplate.postForEntity(second.getNodeUrl()+ApplicationEndpoints.CLIENT_RETRIEVE_URL, retrievalParamsFirst, String.class);
        HttpEntity<String> proposalSecond = restTemplate.postForEntity(second.getNodeUrl()+ ApplicationEndpoints.CLIENT_PROPOSE_URL, proposalParamsSecond, String.class);
        HttpEntity<String> resultSecond = restTemplate.postForEntity(first.getNodeUrl()+ApplicationEndpoints.CLIENT_RETRIEVE_URL, retrievalParamsSecond, String.class);

        then: 'both clients should recieve proper values'
        proposalFirst.statusCode == HttpStatus.OK
        resultFirst.statusCode == HttpStatus.OK
        resultFirst.body == "5"
        proposalSecond.statusCode == HttpStatus.OK
        proposalSecond.statusCode == HttpStatus.OK
        resultSecond.body == "7"

    }

}