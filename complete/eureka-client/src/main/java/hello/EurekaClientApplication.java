package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
public class EurekaClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaClientApplication.class, args);
    }
}

@RestController
class ServiceInstanceRestController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @RequestMapping("/service-instances/{applicationName}")
    public List<ServiceInstance> serviceInstancesByApplicationName(
            @PathVariable String applicationName) {
        return this.discoveryClient.getInstances(applicationName);
    }

    @RequestMapping("/services")
    public List<String> services() {
    	System.out.println(System.getProperty("server.port") + ":/services");
        return this.discoveryClient.getServices();
    }

	@LoadBalanced
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ABootifulClient aBootifulClient;
    
    @RequestMapping("/servicesFeign")
    public List<String> servicesFeign() {
    	System.out.println(System.getProperty("server.port") + ":/servicesFeign");
    	return this.aBootifulClient.getServices();
    }

    @RequestMapping("/servicesLB")
    public List<String> servicesLB() {
    	System.out.println(System.getProperty("server.port") + ":/servicesLB");
        // use the "smart" Eureka-aware RestTemplate
        ResponseEntity<List<String>> response =
                this.restTemplate.exchange(
                        "http://a-bootiful-client/services",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<String>>() {
                        },
                        (Object) "mstine");

        return response.getBody();
    }
}

@FeignClient("a-bootiful-client")
interface ABootifulClient {

    @RequestMapping(method = RequestMethod.GET, value = "services")
    List<String> getServices();
}
