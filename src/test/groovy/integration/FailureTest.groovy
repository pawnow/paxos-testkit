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
public class FailureTest extends Specification{

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

    def shouldStillWorkProperlyWithOnlyTwoNodes() {
        Node node = NodesProvider.get(0)
        Node failingNodeFirst = NodesProvider.get(1)
        Node failingNodeSecond = NodesProvider.get(2)
        HttpEntity<?> entity = new HttpEntity<>()
        MultiValueMap<String, String> proposalParamsFirst = new LinkedMultiValueMap<String, String>()
        proposalParamsFirst.add("key", "testkey")
        proposalParamsFirst.add("value", "5")
        MultiValueMap<String, String> retrievalParamsFirst = new LinkedMultiValueMap<String, String>()
        retrievalParamsFirst.add("key", "testkey")
        MultiValueMap<String, String> proposalParamsSecond = new LinkedMultiValueMap<String, String>()
        proposalParamsSecond.add("key", "testkeya")
        proposalParamsSecond.add("value", "7")
        MultiValueMap<String, String> retrievalParamsSecond = new LinkedMultiValueMap<String, String>()
        retrievalParamsSecond.add("key", "testkeya")

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();


        when: 'rest propose url is hit'
        HttpEntity<String> responseTurnOfflineFirst = restTemplate.postForEntity(failingNodeFirst.getNodeUrl()+ApplicationEndpoints.OFFLINE_URL, params, String.class);
        HttpEntity<String> proposalFirst = restTemplate.postForEntity(node.getNodeUrl()+ ApplicationEndpoints.CLIENT_PROPOSE_URL, proposalParamsFirst, String.class);
        HttpEntity<String> resultFirst = restTemplate.postForEntity(node.getNodeUrl()+ApplicationEndpoints.CLIENT_RETRIEVE_URL, retrievalParamsFirst, String.class);
        HttpEntity<String> responseStatusOfflineFirst = restTemplate.exchange(failingNodeFirst.getNodeUrl()+ApplicationEndpoints.STATUS_URL, HttpMethod.GET, entity, String.class);
        HttpEntity<String> responseTurnOnlineFirst = restTemplate.postForEntity(failingNodeFirst.getNodeUrl()+ApplicationEndpoints.ONLINE_URL, params, String.class);

        HttpEntity<String> responseTurnOfflineSecond = restTemplate.postForEntity(failingNodeSecond.getNodeUrl()+ApplicationEndpoints.OFFLINE_URL, params, String.class);
        HttpEntity<String> proposalSecond  = restTemplate.postForEntity(node.getNodeUrl()+ ApplicationEndpoints.CLIENT_PROPOSE_URL, proposalParamsSecond, String.class);
        HttpEntity<String> resultSecond = restTemplate.postForEntity(node.getNodeUrl()+ApplicationEndpoints.CLIENT_RETRIEVE_URL, retrievalParamsSecond, String.class);
        HttpEntity<String> responseStatusOfflineSecond  = restTemplate.exchange(failingNodeSecond.getNodeUrl()+ApplicationEndpoints.STATUS_URL, HttpMethod.GET, entity, String.class);
        HttpEntity<String> responseTurnOnlineSecond  = restTemplate.postForEntity(failingNodeSecond.getNodeUrl()+ApplicationEndpoints.ONLINE_URL, params, String.class);

        then: 'client should recieve proper value'

        responseTurnOfflineFirst.statusCode == HttpStatus.OK
        responseStatusOfflineFirst.statusCode == HttpStatus.OK
        responseStatusOfflineFirst.body == "false"
        responseTurnOnlineFirst.statusCode == HttpStatus.OK
        responseTurnOnlineFirst.statusCode == HttpStatus.OK
        proposalFirst.statusCode == HttpStatus.OK
        resultFirst.statusCode == HttpStatus.OK
        resultFirst.body == "5"

        responseTurnOfflineSecond.statusCode == HttpStatus.OK
        responseStatusOfflineSecond.statusCode == HttpStatus.OK
        responseStatusOfflineSecond.body == "false"
        responseTurnOnlineSecond.statusCode == HttpStatus.OK
        responseTurnOnlineSecond.statusCode == HttpStatus.OK
        proposalSecond.statusCode == HttpStatus.OK
        resultSecond.statusCode == HttpStatus.OK
        resultSecond.body == "7"

    }

}
