package com.greedy.onoff.jwt;


import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.greedy.onoff.member.dto.MemberDto;
import com.greedy.onoff.member.dto.TokenDto;
import com.greedy.onoff.member.exception.TokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TokenProvider {
	
	private static final String AUTHORITIES_KEY = "auth";
	private static final String BEARER_TYPE = "bearer";
	private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 300; 
	private final Key key;
	
	private final UserDetailsService userDetailsService;
	
	public TokenProvider(@Value("${jwt.secret}") String secretKey, UserDetailsService userDetailsService) {
		
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.userDetailsService = userDetailsService;
	}
	
	
	public TokenDto generateTokenDto(MemberDto member) {
		
		log.info("[TokenProvider] generateTokenDto Start ========================= ");
		
		
        List<String> roles =  Collections.singletonList(member.getMemberRole());
        
        Claims claims = Jwts
				.claims()
				.setSubject(member.getMemberId());
		
    	claims.put(AUTHORITIES_KEY, roles);

						
		long now = (new Date()).getTime();
		
		Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME); 

		String accessToken = Jwts.builder()
				.setClaims(claims)
				.setExpiration(accessTokenExpiresIn)
				.signWith(key, SignatureAlgorithm.HS512) 
				.compact();
		
		return new TokenDto(BEARER_TYPE, member.getMemberName(), accessToken, accessTokenExpiresIn.getTime());
	}

	public boolean validateToken(String jwt) {
		
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt);
			return true; 
		} catch(io.jsonwebtoken.security.SignatureException | MalformedJwtException e) {
			log.info("[TokenProvider] ?????? ??? JWT ???????????????.");
			throw new TokenException("?????? ??? JWT ???????????????"); 
		} catch(ExpiredJwtException e) {
			log.info("[TokenProvider] ?????? ??? JWT ???????????????.");
			throw new TokenException(" ?????? ??? JWT ???????????????.");
		} catch(UnsupportedJwtException e) {
			log.info("[TokenProvider] ???????????? ?????? JWT ???????????????.");
			throw new TokenException("???????????? ?????? JWT ???????????????.");
			
		} catch(IllegalArgumentException e) {
			log.info("[TokenProvider] JWT ????????? ?????? ???????????????.");
			throw new TokenException("JWT ????????? ?????? ???????????????.");
		}
		
	}
	
	
	public Authentication getAuthentication(String jwt) {
		
		Claims claims = parseClaims(jwt);
		
		UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
				
		return new UsernamePasswordAuthenticationToken(userDetails,"", userDetails.getAuthorities());
	}

	private Claims parseClaims(String jwt) {
		
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody();
	}
	

}
