package com.naver.OnATrip.controller;

import com.naver.OnATrip.entity.Member;
import com.naver.OnATrip.entity.VerifyCode;
import com.naver.OnATrip.repository.MemberRepository;
import com.naver.OnATrip.service.EmailService;
import com.naver.OnATrip.service.MemberService;
import com.naver.OnATrip.service.VerifyCodeService;
import com.naver.OnATrip.web.dto.member.MemberDTO;
import com.naver.OnATrip.web.dto.member.MemberDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final VerifyCodeService verifyCodeService;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    @GetMapping("/login")
    public String login() {

        return "member/login"
                ;
    }

    // 주석 처리한 이유 => 시큐리티가 로그인프로세스 대신 해줌.. 매핑 없어도 잘 됨.
//    @PostMapping("/login")
//    public String loginProc(@ModelAttribute MemberDTO memberDTO, HttpSession session, Model model) {
//        MemberDTO loginResult = memberService.login(memberDTO);
//        if (loginResult != null) {
//            // login 성공
//            System.out.println("로그인 성공" + loginResult.getEmail());
//            session.setAttribute("email", loginResult.getEmail());
//            return "redirect:/main";
//        } else {
//            // login 실패
//            System.out.println("로그인 실패: 이메일이나 비밀번호가 잘못되었습니다.");
//            model.addAttribute("loginErrorMsg","아이디 또는 비밀번호를 확인해주세요");
//            return "member/login";
//        }
//    }
    //비밀번호 찾기
    @GetMapping("/findPassword")
    public String findPassword() {

        return "member/findPassword"
                ;
    }

    //이메일로 링크 발송 후 재설정
    @GetMapping(value = "/findPassword/{code}")
    public String changePasswordForm(@PathVariable("code") String code, Model model) {
        model.addAttribute("code", code);
        System.out.println("받아온 code는 무엇인가 = "+ code);
        return "member/changePassword";
    }
    @PostMapping(value = "/findPassword/{code}")
    public String changePassword(
            @RequestParam("newPassword") String newPassword,
            @RequestParam("newPasswordCheck") String newPasswordCheck,
            @PathVariable("code") String code) {

        VerifyCode verifyCode = verifyCodeService.findByCodeConfirm(code);
        System.out.println("멤버컨트롤러 verifyCode = " + verifyCode);

        if (verifyCode == null) {
            System.out.println("만료되었거나 잘못된 링크");
            return "redirect:/findPassword/" + code;
        }

        // 패스워드 확인이 일치하는지 검증
        if (!newPassword.equals(newPasswordCheck)) {
            System.out.println("비밀번호 확인이 일치하지 않습니다.");
            return "redirect:/findPassword/" + code;
        }

        // 패스워드 변경 로직
        MemberDTO.PasswordDto passwordDto = new MemberDTO.PasswordDto();
        passwordDto.setNewPassword(newPassword);

        memberService.updatePassword(verifyCode.getEmail(), passwordDto);

        if (true) {
            verifyCodeService.deleteByEmail(verifyCode.getEmail());
            verifyCodeService.deleteCode(code);
            System.out.println("비밀번호 변경 성공");
            return "member/login";
        } else {
            System.out.println("비밀번호 변경 실패");
            return "redirect:/findPassword/" + code;
        }
    }
    //회원가입
    @GetMapping("/join")
    public String join(Model model) {
        logger.info("--------------------------------------------MemberController, join");
        model.addAttribute("memberDTO", new MemberDTO());

        return "member/join";
    }

    @PostMapping("/join")
    public String joinProcess(@Valid MemberDTO memberDTO, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("MemberDTO", memberDTO);
            return "member/join";
        }

        if (!memberDTO.getPassword().equals(memberDTO.getPasswdCheck())) {
            bindingResult.rejectValue("PasswdCheck", "passwdIncorrect", "비밀번호가 일치하지 않습니다");
            return "member/join";
        }

        try {
            int result = memberService.join(memberDTO);
        } catch (
                DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("signUpFailed", "이미 등록된 사용자입니다.");
            return "Member/join";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("signUpFailed", e.getMessage());
            return "Member/join";
        }

        System.out.println("@@@@@ MemberController");

        return "redirect:login";
    }

    //회원가입시 이메일 중복 확인
    @PostMapping("/checkEmail")
    @ResponseBody
    public boolean checkEmail(@RequestParam("email") String email) {

        return memberService.checkEmail(email);
    }

    //마이페이지 비밀번호 변경
    @PostMapping("/checkCurrentPassword")
    public ResponseEntity<String> checkCurrentPassword(Principal principal, @RequestParam("currentPassword") String currentPassword) {
        String email = principal.getName();
        boolean isValid = memberService.validatePassword(email, currentPassword);
        if (isValid) {
            return ResponseEntity.status(HttpStatus.OK).body("비밀번호가 일치합니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("비밀번호가 일치하지 않습니다.");
        }
    }

    @PostMapping("/newPassword")
    public ResponseEntity<String> changePassword(Principal principal, @RequestBody MemberDTO.PasswordDto passwordDto) {
        try {
            String email = principal.getName(); // 로그인된 사용자의 이메일을 가져옴
            memberService.updatePassword(email, passwordDto);
            return ResponseEntity.status(HttpStatus.OK).body("비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("비밀번호 변경 중 오류가 발생했습니다.");
        }
    }

    //회원 탈퇴
    @ResponseBody
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestParam("password") String password, Principal principal) {
        try {
            String email = principal.getName();
            System.out.println("Withdraw email: " + email + ", password: " + password);

            boolean result = memberService.withdraw(email, password);

            if (result) {
                return ResponseEntity.ok().body(Collections.singletonMap("success", true));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("success", false));
            }
        } catch (Exception e) {
            e.printStackTrace(); // 예외 로그를 남깁니다.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", "서버 오류가 발생했습니다."));
        }
    }



}

