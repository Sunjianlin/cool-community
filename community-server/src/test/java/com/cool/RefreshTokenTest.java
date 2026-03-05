package com.cool;


import com.cool.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RefreshTokenTest {
    @Autowired
    JwtUtil jwtUtil;
    @Test
    void test1(){
        String token="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJJZCI6MSwidG9rZW5UeXBlIjoicmVmcmVzaCIsImp0aSI6ImFjNGFjMjU0LWE3MTQtNGY5Zi1iMTBlLWRiZjNkN2NjYWFhNCIsImlhdCI6MTc3MjcxNjA0NywiZXhwIjoxNzczMzIwODQ3fQ.V13j1R19D10BmL15gnJ2uylBvzu902tOPwdqdRJGSlA";
        Claims refreshTokenClaims=jwtUtil.parseToken(token);
        String tokenType = jwtUtil.getTokenType(token);
        System.out.println(tokenType);
        System.out.println(refreshTokenClaims);

    }
}
