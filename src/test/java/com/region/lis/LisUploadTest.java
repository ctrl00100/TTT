package com.region.lis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.region.lis.dto.OrderUploadDTO;
import com.region.lis.entity.TestOrder;
import com.region.lis.mapper.TestOrderMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LisUploadTest {

    @Autowired
    private TestRestTemplate restTemplate;
//    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private TestOrderMapper testOrderMapper;

    private static String token;

    /**
     * 前置：登录拿 Token
     */
    @BeforeAll
    static void login(@Autowired TestRestTemplate restTemplate) {
//        // 关闭自动重定向和认证
//        restTemplate.getRestTemplate().setRequestFactory(
//                new org.springframework.http.client.SimpleClientHttpRequestFactory()
//        );
        String url = "/auth/login?orgCode=TOWN01&password=123456";
        ResponseEntity<Map> resp = restTemplate.postForEntity(url, null, Map.class);
        Map data = (Map) resp.getBody().get("data");
        token = (String) data.get("token");
        System.out.println("🔑 Token: " + token);
    }

    /**
     * 构造请求头
     */
    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("Authorization", token);
        return h;
    }

    /**
     * 用例1：正常上传 → 200
     */
    @Test
    @Order(1)
    @DisplayName("正常上传：返回200")
    void testUploadSuccess() {
        OrderUploadDTO dto = new OrderUploadDTO();
        dto.setOrderNo("TEST_ORD_001");
        dto.setPatientName("测试患者");
        dto.setIdCard("510123199001011234");
        dto.setPhone("13800138000");
        dto.setTownshipId("TOWN01");
        dto.setItemCode("XZ001");
        dto.setQuantity(1);

        ResponseEntity<Map> resp = restTemplate.exchange(
            "/lis/upload", HttpMethod.POST, new HttpEntity<>(dto, headers()), Map.class
        );

        assertEquals(200, resp.getBody().get("code"));
        assertEquals("上传成功", resp.getBody().get("data"));

        // 查库，确认 status=1
        TestOrder order = testOrderMapper.selectOne(
            new LambdaQueryWrapper<TestOrder>().eq(TestOrder::getOrderNo, "TEST_ORD_001")
        );
        assertNotNull(order);
        assertEquals(1, order.getStatus());
    }

    /**
     * 用例2：重复上传 → 1002
     */
    @Test
    @Order(2)
    @DisplayName("重复上传：返回1002")
    void testUploadDuplicate() {
        OrderUploadDTO dto = new OrderUploadDTO();
        dto.setOrderNo("TEST_ORD_001");  // 已存在
        dto.setTownshipId("TOWN01");
        dto.setItemCode("XZ001");

        ResponseEntity<Map> resp = restTemplate.exchange(
            "/lis/upload", HttpMethod.POST, new HttpEntity<>(dto, headers()), Map.class
        );

        assertEquals(1002, resp.getBody().get("code"));
        assertTrue(resp.getBody().get("message").toString().contains("重复上传"));
    }

    /**
     * 用例3：未对码 → 1001
     */
    @Test
    @Order(3)
    @DisplayName("未对码：返回1001")
    void testUploadNotMapped() {
        OrderUploadDTO dto = new OrderUploadDTO();
        dto.setOrderNo("TEST_ORD_002");
        dto.setTownshipId("TOWN01");
        dto.setItemCode("XZ999");  // 未对码

        ResponseEntity<Map> resp = restTemplate.exchange(
            "/lis/upload", HttpMethod.POST, new HttpEntity<>(dto, headers()), Map.class
        );

        assertEquals(1001, resp.getBody().get("code"));
        assertTrue(resp.getBody().get("message").toString().contains("未对码"));
    }

    /**
     * 用例4：参数缺失 → 400
     */
    @Test
    @Order(4)
    @DisplayName("参数缺失：返回400")
    void testUploadMissingParam() {
        OrderUploadDTO dto = new OrderUploadDTO();
        dto.setOrderNo(null);  // 医嘱号为空
        dto.setTownshipId("TOWN01");

        ResponseEntity<Map> resp = restTemplate.exchange(
            "/lis/upload", HttpMethod.POST, new HttpEntity<>(dto, headers()), Map.class
        );

        assertEquals(400, resp.getBody().get("code"));
    }

    /**
     * 用例5：不带 Token → 401
     */
    @Test
    @Order(5)
    @DisplayName("不带Token：返回401")
    void testUploadNoToken() {
        OrderUploadDTO dto = new OrderUploadDTO();
        dto.setOrderNo("TEST_ORD_003");
        dto.setTownshipId("TOWN01");
        dto.setItemCode("XZ001");

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        // 不设 Authorization

        ResponseEntity<Map> resp = restTemplate.exchange(
            "/lis/upload", HttpMethod.POST, new HttpEntity<>(dto, h), Map.class
        );

        assertEquals(401, resp.getBody().get("code"));
    }
}