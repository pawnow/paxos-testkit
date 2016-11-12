package integration

import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import pl.edu.agh.iosr.utils.ApplicationEndpoints
import pl.edu.agh.iosr.utils.NodesProvider
import spock.lang.Specification
import pl.edu.agh.iosr.cdm.Node;

class OfflineOnlineTest extends Specification{

    def RestTemplate restTemplate = new RestTemplate()

    def setup() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        NodesProvider.getListOfApplicationNodes()
                .stream()
                .forEach({
            node -> restTemplate.postForEntity(node.getNodeUrl()+ApplicationEndpoints.ONLINE_URL, params, String.class)
        })
    }

    def testShouldReturnOnlineStatusOfFirstNode() {
        given:
        Node node = NodesProvider.get(0);
        HttpEntity<?> entity = new HttpEntity<>();

        when: 'rest accept url is hit'
        HttpEntity<String> response = restTemplate.exchange(node.getNodeUrl()+ApplicationEndpoints.STATUS_URL, HttpMethod.GET, entity, String.class);

        then: 'acceptor controller should return empty proposal'
        response.statusCode == HttpStatus.OK
        response.body == "true"
    }

    def testShouldTurnFirstNodeOfflineAndThenOnline() {
        given:
        Node node = NodesProvider.get(0);
        HttpEntity<?> entity = new HttpEntity<>();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();

        when: 'rest accept url is hit'
        HttpEntity<String> responseTurnOffline = restTemplate.postForEntity(node.getNodeUrl()+ApplicationEndpoints.OFFLINE_URL, params, String.class);
        HttpEntity<String> responseStatusOffline = restTemplate.exchange(node.getNodeUrl()+ApplicationEndpoints.STATUS_URL, HttpMethod.GET, entity, String.class);
        HttpEntity<String> responseTurnOnline = restTemplate.postForEntity(node.getNodeUrl()+ApplicationEndpoints.ONLINE_URL, params, String.class);
        HttpEntity<String> responseStatusOnline = restTemplate.exchange(node.getNodeUrl()+ApplicationEndpoints.STATUS_URL, HttpMethod.GET, entity, String.class);

        then: 'acceptor controller should return empty proposal'
        responseTurnOffline.statusCode == HttpStatus.OK
        responseStatusOffline.statusCode == HttpStatus.OK
        responseStatusOffline.body == "false"
        responseTurnOnline.statusCode == HttpStatus.OK
        responseStatusOnline.statusCode == HttpStatus.OK
        responseStatusOnline.body == "true"
    }

    def testSwitchesShouldInfluenceOnlyOneInstance() {
        given:
        Node first = NodesProvider.get(0);
        Node second = NodesProvider.get(1);
        Node third = NodesProvider.get(2);
        HttpEntity<?> entity = new HttpEntity<>();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();

        when: 'rest accept url is hit'
        HttpEntity<String> responseTurnOfflineFirst = restTemplate.postForEntity(second.getNodeUrl()+ApplicationEndpoints.OFFLINE_URL, params, String.class);
        HttpEntity<String> responseStatusOnlineFirst = restTemplate.exchange(first.getNodeUrl()+ApplicationEndpoints.STATUS_URL, HttpMethod.GET, entity, String.class);
        HttpEntity<String> responseStatusOfflineSecond = restTemplate.exchange(second.getNodeUrl()+ApplicationEndpoints.STATUS_URL, HttpMethod.GET, entity, String.class);
        HttpEntity<String> responseStatusOnlineThird = restTemplate.exchange(third.getNodeUrl()+ApplicationEndpoints.STATUS_URL, HttpMethod.GET, entity, String.class);
        HttpEntity<String> responseTurnOnlineFirst = restTemplate.postForEntity(second.getNodeUrl()+ApplicationEndpoints.ONLINE_URL, params, String.class);
        HttpEntity<String> responseStatusOnlineFirst2 = restTemplate.exchange(first.getNodeUrl()+ApplicationEndpoints.STATUS_URL, HttpMethod.GET, entity, String.class);
        HttpEntity<String> responseStatusOnlineSecond2 = restTemplate.exchange(first.getNodeUrl()+ApplicationEndpoints.STATUS_URL, HttpMethod.GET, entity, String.class);
        HttpEntity<String> responseStatusOnlineThird2 = restTemplate.exchange(third.getNodeUrl()+ApplicationEndpoints.STATUS_URL, HttpMethod.GET, entity, String.class);

        then: 'acceptor controller should return empty proposal'
        responseTurnOfflineFirst.statusCode == HttpStatus.OK
        responseStatusOnlineFirst.statusCode == HttpStatus.OK
        responseStatusOnlineFirst.body == "true"
        responseStatusOfflineSecond.statusCode == HttpStatus.OK
        responseStatusOfflineSecond.body == "false"
        responseStatusOnlineThird.statusCode == HttpStatus.OK
        responseStatusOnlineThird.body == "true"
        responseTurnOnlineFirst.statusCode == HttpStatus.OK
        responseStatusOnlineFirst2.statusCode == HttpStatus.OK
        responseStatusOnlineFirst2.body == "true"
        responseStatusOnlineSecond2.statusCode == HttpStatus.OK
        responseStatusOnlineSecond2.body == "true"
        responseStatusOnlineThird2.statusCode == HttpStatus.OK
        responseStatusOnlineThird2.body == "true"
    }

}
