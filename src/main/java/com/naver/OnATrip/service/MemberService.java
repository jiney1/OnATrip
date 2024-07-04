package com.naver.OnATrip.service;

import com.naver.OnATrip.entity.Member;
import com.naver.OnATrip.repository.MemberRepository;
import com.naver.OnATrip.web.dto.member.MemberDTO;
import com.naver.OnATrip.web.dto.member.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public int join(MemberDTO memberDTO) {

        try {
            Member member = memberDTO.toEntity();  // MemberDTO를 Member 객체로 변환
            String rawPass = member.getPassword();
            String encPass = passwordEncoder.encode(rawPass);
            member.setPassword(encPass);
            member.setRole("USER");
            memberRepository.save(member);
            return 100;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("memberService " + e.getMessage());
        }
        return -100;
    }

    public boolean checkEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String email){
        Member member = memberRepository.findByEmail(email);
        if(member != null){
            return new MemberDetails(member);
        }
        return null;
    }
}


