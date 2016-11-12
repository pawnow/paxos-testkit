package integration

import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import pl.edu.agh.iosr.cdm.Node
import pl.edu.agh.iosr.utils.ApplicationEndpoints
import pl.edu.agh.iosr.utils.NodesProvider
import spock.lang.Specification

class LeaderElectionTest extends Specification{

    def RestTemplate restTemplate = new RestTemplate()

    def setup() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        NodesProvider.getListOfApplicationNodes()
                .stream()
                .forEach({
            node -> restTemplate.postForEntity(node.getNodeUrl()+ApplicationEndpoints.ONLINE_URL, params, String.class)
        })
    }

    def testFirstNodeShouldBeLeader() {
        given:
        Node first = NodesProvider.get(0);
        Node second = NodesProvider.get(1);
        Node third = NodesProvider.get(2);
        HttpEntity<?> entity = new HttpEntity<>();

        when: 'rest accept url is hit'
        HttpEntity<String> responseFirst = restTemplate.exchange(first.getNodeUrl()+ApplicationEndpoints.LEADER_URL, HttpMethod.GET, entity, String.class);
        HttpEntity<String> responseSecond = restTemplate.exchange(second.getNodeUrl()+ApplicationEndpoints.LEADER_URL, HttpMethod.GET, entity, String.class);
        HttpEntity<String> responseThird = restTemplate.exchange(third.getNodeUrl()+ApplicationEndpoints.LEADER_URL, HttpMethod.GET, entity, String.class);

        then: 'acceptor controller should return empty proposal'
        responseFirst.statusCode == HttpStatus.OK
        responseFirst.body == "true"
        responseSecond.statusCode == HttpStatus.OK
        responseSecond.body == "false"
        responseThird.statusCode == HttpStatus.OK
        responseThird.body == "false"
    }

    def secondNodeShouldBeLeaderWhenFirstIsOffline() {
        given:
        Node first = NodesProvider.get(0);
        Node second = NodesProvider.get(1);
        Node third = NodesProvider.get(2);
        HttpEntity<?> entity = new HttpEntity<>();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();

        when: 'rest accept url is hit'
        HttpEntity<String> responseTurnOffline = restTemplate.postForEntity(first.getNodeUrl()+ApplicationEndpoints.OFFLINE_URL, params, String.class);
        HttpEntity<String> responseIsFirstNodeLeader = restTemplate.exchange(first.getNodeUrl()+ApplicationEndpoints.LEADER_URL, HttpMethod.GET, entity, String.class);
        HttpEntity<String> responseIsSecondNodeLeader = restTemplate.exchange(second.getNodeUrl()+ApplicationEndpoints.LEADER_URL, HttpMethod.GET, entity, String.class);
        HttpEntity<String> responseIsThirdNodeLeader = restTemplate.exchange(third.getNodeUrl()+ApplicationEndpoints.LEADER_URL, HttpMethod.GET, entity, String.class);

        then: 'acceptor controller should return empty proposal'
        responseTurnOffline.statusCode == HttpStatus.OK
        responseIsFirstNodeLeader.statusCode == HttpStatus.OK
        responseIsFirstNodeLeader.body == "false"
        responseIsSecondNodeLeader.statusCode == HttpStatus.OK
        responseIsSecondNodeLeader.body == "true"
        responseIsThirdNodeLeader.statusCode == HttpStatus.OK
        responseIsThirdNodeLeader.body == "false"
    }

    def thirdNodeShouldBeLeaderWhenFirstAndSecondAreOffline() {
        given:
        Node first = NodesProvider.get(0);
        Node second = NodesProvider.get(1);
        Node third = NodesProvider.get(2);
        HttpEntity<?> entity = new HttpEntity<>();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();

        when: 'rest accept url is hit'
        HttpEntity<String> responseTurnOffline = restTemplate.postForEntity(first.getNodeUrl()+ApplicationEndpoints.OFFLINE_URL, params, String.class);
        HttpEntity<String> responseTurnOffline2 = restTemplate.postForEntity(second.getNodeUrl()+ApplicationEndpoints.OFFLINE_URL, params, String.class);
        HttpEntity<String> responseIsFirstNodeLeader = restTemplate.exchange(first.getNodeUrl()+ApplicationEndpoints.LEADER_URL, HttpMethod.GET, entity, String.class);
        HttpEntity<String> responseIsSecondNodeLeader = restTemplate.exchange(second.getNodeUrl()+ApplicationEndpoints.LEADER_URL, HttpMethod.GET, entity, String.class);
        HttpEntity<String> responseIsThirdNodeLeader = restTemplate.exchange(third.getNodeUrl()+ApplicationEndpoints.LEADER_URL, HttpMethod.GET, entity, String.class);

        then: 'acceptor controller should return empty proposal'
        responseTurnOffline.statusCode == HttpStatus.OK
        responseTurnOffline2.statusCode == HttpStatus.OK
        responseIsFirstNodeLeader.statusCode == HttpStatus.OK
        responseIsFirstNodeLeader.body == "false"
        responseIsSecondNodeLeader.statusCode == HttpStatus.OK
        responseIsSecondNodeLeader.body == "false"
        responseIsThirdNodeLeader.statusCode == HttpStatus.OK
        responseIsThirdNodeLeader.body == "true"
    }

}
